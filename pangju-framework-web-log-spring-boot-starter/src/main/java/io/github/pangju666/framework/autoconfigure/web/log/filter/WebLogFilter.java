package io.github.pangju666.framework.autoconfigure.web.log.filter;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.github.pangju666.commons.lang.utils.DateFormatUtils;
import io.github.pangju666.commons.lang.utils.JsonUtils;
import io.github.pangju666.framework.autoconfigure.web.log.annotation.WebLogIgnore;
import io.github.pangju666.framework.autoconfigure.web.log.model.WebLog;
import io.github.pangju666.framework.autoconfigure.web.log.properties.WebLogProperties;
import io.github.pangju666.framework.autoconfigure.web.log.sender.WebLogSender;
import io.github.pangju666.framework.web.filter.BaseRequestFilter;
import io.github.pangju666.framework.web.model.Result;
import io.github.pangju666.framework.web.utils.RequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class WebLogFilter extends BaseRequestFilter {
	private final WebLogSender sender;
	private final WebLogProperties properties;
	private final RequestMappingHandlerMapping requestMappingHandlerMapping;

	public WebLogFilter(WebLogProperties properties,
						WebLogSender sender,
						Set<String> excludePathPatterns,
						RequestMappingHandlerMapping requestMappingHandlerMapping) {
		super(excludePathPatterns);
		this.properties = properties;
		this.sender = sender;
		this.requestMappingHandlerMapping = requestMappingHandlerMapping;
	}

	@Override
	protected void handle(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		Date requestDate = new Date();
		try {
			HandlerExecutionChain handlerMappingHandler = requestMappingHandlerMapping.getHandler(request);
			if (Objects.nonNull(handlerMappingHandler) &&
				(handlerMappingHandler.getHandler() instanceof HandlerMethod handlerMethod)) {
				Class<?> targetClass = handlerMethod.getBeanType();
				Method targetMethod = handlerMethod.getMethod();
				if (Objects.nonNull(targetMethod.getAnnotation(WebLogIgnore.class)) ||
					Objects.nonNull(targetClass.getAnnotation(WebLogIgnore.class))) {
					filterChain.doFilter(request, response);
					return;
				}
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
			webLog.setIp(RequestUtils.getIpAddress(requestWrapper));
			webLog.setMethod(requestWrapper.getMethod());
			webLog.setDate(DateFormatUtils.formatDatetime(requestDate));
			webLog.setUrl(requestWrapper.getRequestURI());
			webLog.setCostMillis(stopWatch.lastTaskInfo().getTimeMillis());

			WebLog.Request requestLog = new WebLog.Request();
			requestLog.setCharacterEncoding(requestWrapper.getCharacterEncoding());
			requestLog.setContentLength(requestWrapper.getContentLength());
			requestLog.setContentType(requestWrapper.getContentType());
			if (properties.getRequest().isHeaders()) {
				requestLog.setHeaders(RequestUtils.getHeaderMap(requestWrapper));
			}
			if (properties.getRequest().isQueryParams()) {
				requestLog.setQueryParams(RequestUtils.getParameterMap(requestWrapper));
			}
			if (properties.getRequest().isMultipart() &&
				StringUtils.startsWithIgnoreCase(requestWrapper.getContentType(), MediaType.MULTIPART_FORM_DATA_VALUE)) {
				requestLog.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
				try {
					requestLog.setFormData(RequestUtils.getMultipartMap(requestWrapper));
					requestWrapper.getParts().stream()
						.map(Part::getName)
						.forEach(fieldName -> requestLog.getQueryParams().remove(fieldName));
				} catch (IllegalStateException ignored) {
				}
			} else if (properties.getRequest().isBody()) {
				requestLog.setBody(RequestUtils.getRequestBodyMap(requestWrapper));
			}
			webLog.setRequest(requestLog);

			WebLog.Response responseLog = new WebLog.Response();
			responseLog.setStatus(responseWrapper.getStatus());
			if (properties.getResponse().isHeaders()) {
				Map<String, Object> headers = responseWrapper.getHeaderNames()
					.stream()
					.distinct()
					.collect(Collectors.toMap(headerName -> headerName, responseWrapper::getHeaders));
				responseLog.setHeaders(headers);
			}
			responseLog.setContentType(responseWrapper.getContentType());
			responseLog.setCharacterEncoding(responseWrapper.getCharacterEncoding());
			if (properties.getResponse().isBody()) {
				if (properties.getResponse().isBodyData()) {
					if (!StringUtils.equalsAnyIgnoreCase(responseWrapper.getContentType(),
						MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE)) {
						responseLog.setBody(Base64.encodeBase64String(responseWrapper.getContentAsByteArray()));
					} else {
						String responseBodyStr = new String(responseWrapper.getContentAsByteArray());
						if (StringUtils.isNotBlank(responseBodyStr)) {
							responseLog.setBody(JsonUtils.fromString(responseBodyStr, new TypeToken<Object>() {
							}));
						}
					}
				} else {
					if (StringUtils.equalsAnyIgnoreCase(responseWrapper.getContentType(),
						MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE)) {
						String responseBodyStr = new String(responseWrapper.getContentAsByteArray());
						if (StringUtils.isNotBlank(responseBodyStr)) {
							JsonObject responseBody = JsonUtils.parseString(responseBodyStr).getAsJsonObject();
							if (responseBody.has("code") && responseBody.has("message")) {
								Result<?> result = new Result<>(
									responseBody.getAsJsonPrimitive("message").getAsString(),
									responseBody.getAsJsonPrimitive("code").getAsInt(),
									null
								);
								responseLog.setBody(result);
							}
						}
					}
				}
			}
			webLog.setResponse(responseLog);

			sender.send(webLog);
		}
	}
}
