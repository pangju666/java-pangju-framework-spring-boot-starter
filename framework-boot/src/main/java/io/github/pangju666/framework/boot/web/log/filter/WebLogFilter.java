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

package io.github.pangju666.framework.boot.web.log.filter;

import io.github.pangju666.commons.lang.concurrent.SystemClock;
import io.github.pangju666.commons.lang.utils.DateFormatUtils;
import io.github.pangju666.framework.boot.web.log.configuration.WebLogConfiguration;
import io.github.pangju666.framework.boot.web.log.handler.WebLogHandler;
import io.github.pangju666.framework.boot.web.log.model.WebLog;
import io.github.pangju666.framework.boot.web.log.sender.WebLogSender;
import io.github.pangju666.framework.boot.web.log.utils.WebLogUtils;
import io.github.pangju666.framework.web.servlet.BaseHttpRequestFilter;
import io.github.pangju666.framework.web.servlet.utils.HttpRequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

/**
 * Web 日志过滤器。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>拦截每次 HTTP 请求与响应，采集并封装为 {@link WebLog}。</li>
 *   <li>通过 {@link WebLogSender} 将日志发送到外部存储或处理管道。</li>
 *   <li>支持通过 {@link WebLogHandler} 扩展日志处理。</li>
 * </ul>
 *
 * <p><b>使用约束</b></p>
 * <ul>
 *   <li>为保证可读取请求/响应体，内部自动包裹为 {@link ContentCachingRequestWrapper} 与 {@link ContentCachingResponseWrapper}。</li>
 *   <li>响应体被读取后，必须调用 {@link ContentCachingResponseWrapper#copyBodyToResponse()} 以写回到真实响应。</li>
 * </ul>
 *
 * <p><b>执行流程</b></p>
 * <ul>
 *   <li>记录请求到达时间与起始时间戳。</li>
 *   <li>包裹请求与响应对象为内容缓存包装器。</li>
 *   <li>将 {@link WebLog} 放入请求作用域，继续执行过滤链。</li>
 *   <li>出栈后构建请求/响应日志、计算耗时并发送。</li>
 *   <li>清理作用域属性并将响应体写回。</li>
 * </ul>
 *
 * <p><b>注意事项</b></p>
 * <ul>
 *   <li>日志发送失败时仅记录错误，不影响请求处理流程。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class WebLogFilter extends BaseHttpRequestFilter {
	private static final Logger logger = LoggerFactory.getLogger(WebLogFilter.class);

	/**
	 * Web 日志发送器
	 * <p>
	 * 用于将采集到的日志发送到目标存储系统。
	 * 支持多种实现方式，例如 Kafka、MongoDB 或自定义实现。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final WebLogSender sender;
	/**
	 * Web 日志配置
	 * <p>
	 * 存储日志功能的各项配置参数，例如是否记录请求体、响应头、响应体等。
	 * 配置项可以在 {@code application.yml} 中定义。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final WebLogConfiguration configuration;

	/**
	 * 构造方法
	 *
	 * @param configuration       日志配置对象
	 * @param sender              日志发送器
	 * @param excludePathPatterns 需要排除的路径匹配规则
	 * @since 1.0.0
	 */
	public WebLogFilter(WebLogConfiguration configuration, WebLogSender sender, Set<String> excludePathPatterns) {
		super(excludePathPatterns);
		this.configuration = configuration;
		this.sender = sender;
	}

	/**
	 * 过滤器处理方法。
	 *
	 * <p><b>行为</b></p>
	 * <ul>
	 *   <li>包裹请求与响应以支持内容读取。</li>
	 *   <li>执行过滤链，随后构建并发送 {@link WebLog}。</li>
	 *   <li>最后将缓存的响应体写回真实响应。</li>
	 * </ul>
	 *
	 * <p><b>参数</b></p>
	 * <ul>
	 *   <li>{@code request} 当前 HTTP 请求。</li>
	 *   <li>{@code response} 当前 HTTP 响应。</li>
	 *   <li>{@code filterChain} 过滤器链。</li>
	 * </ul>
	 *
	 * @throws ServletException 过滤链或请求体解析过程中发生的 servlet 异常
	 * @throws IOException I/O 异常（例如读取/写回响应体）
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		Date requestDate = new Date();
		long start = SystemClock.now();

		ContentCachingRequestWrapper requestWrapper;
		ContentCachingResponseWrapper responseWrapper;
		if (request instanceof ContentCachingRequestWrapper) {
			requestWrapper = (ContentCachingRequestWrapper) request;
		} else {
			requestWrapper = new ContentCachingRequestWrapper(request);
		}
		if (request instanceof ContentCachingResponseWrapper) {
			responseWrapper = (ContentCachingResponseWrapper) response;
		} else {
			responseWrapper = new ContentCachingResponseWrapper(response);
		}

		WebLog webLog = new WebLog();
		RequestContextHolder.currentRequestAttributes().setAttribute("webLog", webLog, RequestAttributes.SCOPE_REQUEST);
		filterChain.doFilter(requestWrapper, responseWrapper);
		long end = SystemClock.now();
		RequestContextHolder.currentRequestAttributes().removeAttribute("webLog", RequestAttributes.SCOPE_REQUEST);

		webLog.setIp(HttpRequestUtils.getIpAddress(request));
		webLog.setMethod(request.getMethod());
		webLog.setUrl(request.getRequestURI());
		webLog.setRequest(WebLogUtils.getRequestLog(request, configuration));
		webLog.setResponse(WebLogUtils.getResponseLog(response, configuration));
		webLog.setCostMillis(end - start);
		webLog.setDate(DateFormatUtils.formatDatetime(requestDate));

		try {
			sender.send(webLog);
		} catch (Exception e) {
			logger.error("网络日志发送失败", e);
		}

		responseWrapper.copyBodyToResponse();
	}
}