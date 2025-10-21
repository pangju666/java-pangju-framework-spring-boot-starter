/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.framework.autoconfigure.web.log.filter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.pangju666.commons.lang.utils.DateFormatUtils;
import io.github.pangju666.commons.lang.utils.JsonUtils;
import io.github.pangju666.framework.autoconfigure.web.log.WebLogProperties;
import io.github.pangju666.framework.autoconfigure.web.log.annotation.WebLogIgnore;
import io.github.pangju666.framework.autoconfigure.web.log.annotation.WebLogOperation;
import io.github.pangju666.framework.autoconfigure.web.log.handler.WebLogHandler;
import io.github.pangju666.framework.autoconfigure.web.log.model.WebLog;
import io.github.pangju666.framework.autoconfigure.web.log.sender.WebLogSender;
import io.github.pangju666.framework.web.exception.base.BaseHttpException;
import io.github.pangju666.framework.web.filter.BaseHttpOncePerRequestFilter;
import io.github.pangju666.framework.web.utils.ServletRequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class WebLogFilter extends BaseHttpOncePerRequestFilter {
	private static final Logger logger = LoggerFactory.getLogger(WebLogFilter.class);

	private final WebLogSender sender;
	private final WebLogProperties properties;
	private final RequestMappingHandlerMapping requestMappingHandlerMapping;
	private final List<WebLogHandler> webLogHandlers;

	public WebLogFilter(WebLogProperties properties,
						WebLogSender sender,
						Set<String> excludePathPatterns,
						List<WebLogHandler> webLogHandlers,
						RequestMappingHandlerMapping requestMappingHandlerMapping) {
		super(excludePathPatterns);
		this.properties = properties;
		this.sender = sender;
		this.requestMappingHandlerMapping = requestMappingHandlerMapping;
		this.webLogHandlers = webLogHandlers;
	}

	@Override
	protected void handle(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		Date requestDate = new Date();

		WebLogOperation operation = null;
		Class<?> targetClass = null;
		Method targetMethod = null;
		try {
			HandlerExecutionChain handlerMappingHandler = requestMappingHandlerMapping.getHandler(request);
			if (Objects.nonNull(handlerMappingHandler) && (handlerMappingHandler.getHandler() instanceof HandlerMethod handlerMethod)) {
				targetClass = handlerMethod.getBeanType();
				targetMethod = handlerMethod.getMethod();
				if (Objects.nonNull(targetMethod.getAnnotation(WebLogIgnore.class)) || Objects.nonNull(targetClass.getAnnotation(WebLogIgnore.class))) {
					filterChain.doFilter(request, response);
					return;
				}
				operation = targetMethod.getAnnotation(WebLogOperation.class);
			}
		} catch (Exception e) {
			filterChain.doFilter(request, response);
			return;
		}

		if (request instanceof ContentCachingRequestWrapper requestWrapper &&
			response instanceof ContentCachingResponseWrapper responseWrapper) {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			filterChain.doFilter(request, response);
			stopWatch.stop();

			WebLog webLog = new WebLog();
			if (Objects.nonNull(operation)) {
				webLog.setOperation(operation.value());
			}
			webLog.setIp(ServletRequestUtils.getIpAddress(requestWrapper));
			webLog.setMethod(requestWrapper.getMethod());
			webLog.setDate(DateFormatUtils.formatDatetime(requestDate));
			webLog.setUrl(requestWrapper.getRequestURI());
			webLog.setCostMillis(stopWatch.lastTaskInfo().getTimeMillis());

			WebLog.Request requestLog = new WebLog.Request();
			requestLog.setCharacterEncoding(requestWrapper.getCharacterEncoding());
			requestLog.setContentLength(requestWrapper.getContentLength());
			requestLog.setContentType(requestWrapper.getContentType());

			if (properties.getRequest().isHeaders()) {
				requestLog.setHeaders(ServletRequestUtils.getHttpHeaders(requestWrapper));
			}
			if (properties.getRequest().isQueryParams()) {
				requestLog.setQueryParams(ServletRequestUtils.getRequestParameters(requestWrapper));
			}
			if (properties.getRequest().isMultipart() &&
				StringUtils.startsWithIgnoreCase(requestWrapper.getContentType(), MediaType.MULTIPART_FORM_DATA_VALUE)) {
				requestLog.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
				try {
					requestLog.setFormData(ServletRequestUtils.getRequestParts(requestWrapper));
					requestWrapper.getParts().stream()
						.map(Part::getName)
						.forEach(fieldName -> requestLog.getQueryParams().remove(fieldName));
				} catch (IllegalStateException ignored) {
				}
			} else if (properties.getRequest().isBody()) {
				requestLog.setBody(ServletRequestUtils.getJsonRequestBody(requestWrapper, Object.class));
			}
			webLog.setRequest(requestLog);

			WebLog.Response responseLog = new WebLog.Response();
			responseLog.setStatus(responseWrapper.getStatus());
			if (properties.getResponse().isHeaders()) {
				HttpHeaders headers = new HttpHeaders();
				for (String headerName : responseWrapper.getHeaderNames()) {
					headers.add(headerName, responseWrapper.getHeader(headerName));
				}
				responseLog.setHeaders(headers);
			}
			responseLog.setContentType(responseWrapper.getContentType());
			responseLog.setCharacterEncoding(responseWrapper.getCharacterEncoding());

			if (MediaType.APPLICATION_JSON_VALUE.equals(responseWrapper.getContentType()) ||
				MediaType.APPLICATION_JSON_UTF8_VALUE.equals(responseWrapper.getContentType())) {
				if (response.getStatus() != HttpStatus.FOUND.value() && properties.getResponse().isBody()) {
					String responseBodyStr = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
					if (StringUtils.isNotBlank(responseBodyStr)) {
						try {
							JsonElement responseBodyJson = JsonUtils.parseString(responseBodyStr);
							if (responseBodyJson.isJsonObject()) {
								JsonObject responseBodyJsonObject = responseBodyJson.getAsJsonObject();
								if (responseBodyJsonObject.has("code") &&
									responseBodyJsonObject.has("message") &&
									!properties.getResponse().isResultData()) {
									responseBodyJsonObject.remove("data");
								}
							}
							responseLog.setBody(JsonUtils.fromJson(responseBodyJson, Object.class));
						} catch (JsonSyntaxException e) {
							logger.error("响应结果解析失败", e);
						}
					}
				}
			}
			webLog.setResponse(responseLog);

			try {
				for (WebLogHandler webLogHandler : webLogHandlers) {
					webLogHandler.handle(webLog, requestWrapper, responseWrapper, targetClass, targetMethod);
				}
			} catch (Exception e) {
				if (e instanceof BaseHttpException baseHttpException) {
					baseHttpException.log(logger, Level.ERROR);
				} else {
					logger.error("自定义网络日志收集处理器错误", e);
				}
			}
			sender.send(webLog);
		}
	}
}