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

import com.google.gson.JsonSyntaxException;
import io.github.pangju666.commons.lang.concurrent.SystemClock;
import io.github.pangju666.commons.lang.utils.ArrayUtils;
import io.github.pangju666.commons.lang.utils.DateFormatUtils;
import io.github.pangju666.commons.lang.utils.JsonUtils;
import io.github.pangju666.framework.boot.web.log.configuration.WebLogConfiguration;
import io.github.pangju666.framework.boot.web.log.interceptor.WebLogInterceptor;
import io.github.pangju666.framework.boot.web.log.model.WebLog;
import io.github.pangju666.framework.boot.web.log.sender.WebLogSender;
import io.github.pangju666.framework.boot.web.log.type.MediaTypeBodyHandler;
import io.github.pangju666.framework.boot.web.log.utils.WebLogResponseWrapper;
import io.github.pangju666.framework.web.model.Result;
import io.github.pangju666.framework.web.servlet.BaseHttpRequestFilter;
import io.github.pangju666.framework.web.servlet.utils.HttpRequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Web 日志过滤器。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>在满足条件时拦截 HTTP 请求/响应并采集为 {@link WebLog}。</li>
 *   <li>通过 {@link WebLogSender} 将日志发送到外部存储或处理管道。</li>
 * </ul>
 *
 * <p><b>执行流程</b></p>
 * <ul>
 *   <li>记录请求到达时间与起始时间戳。</li>
 *   <li>当请求 {@code Content-Type} 为空或非法时，仅透传过滤链，不进行采集。</li>
 *   <li>当可解析且满足采集条件时：按需包裹请求为 {@link ContentCachingRequestWrapper}，响应包裹为 {@link io.github.pangju666.framework.boot.web.log.utils.WebLogResponseWrapper} 并携带 {@link WebLog}。</li>
 *   <li>过滤链出栈后构建请求/响应日志、计算耗时并发送。</li>
 *   <li>最后写回响应体到真实响应。</li>
 * </ul>
 *
 * <p><b>协作关系</b></p>
 * <ul>
 *   <li>响应侧包裹为 {@link io.github.pangju666.framework.boot.web.log.utils.WebLogResponseWrapper}，用于携带 {@link WebLog} 并复用响应体缓存。</li>
 *   <li>后置拦截器可从 {@code WebLogResponseWrapper} 读取缓存的响应体与 {@link WebLog}，但不负责写回响应体。</li>
 *   <li>本过滤器在链路末尾调用 {@code copyBodyToResponse()} 写回响应体，确保客户端接收完整输出。</li>
 * </ul>
 *
 * <p><b>配置影响</b></p>
 * <ul>
 *   <li>请求侧：是否记录头、查询参数、请求体、文件上传信息以及可接受的内容类型。</li>
 *   <li>响应侧：是否记录头、响应体、结果数据以及可接受的内容类型。</li>
 *   <li>仅在配置允许且内容类型匹配时读取/解析请求与响应体，以降低内存与解析开销。</li>
 * </ul>
 *
 * <p><b>媒体类型处理器选择</b></p>
 * <ul>
 *   <li>按 {@code MediaType} 从已注册的 {@link MediaTypeBodyHandler} 列表中选择首个支持的处理器进行解析。</li>
 *   <li>处理器需通过构造器注入，顺序决定命中优先级；命中后即停止尝试。</li>
 *   <li>仅在配置允许且内容类型匹配时触发解析；JSON 响应仅记录符合 {@link Result} 结构的数据。</li>
 * </ul>
 *
 * <p><b>注意事项</b></p>
 * <ul>
 *   <li>日志发送失败时仅记录错误，不影响请求处理流程。</li>
 *   <li>包装器实例按请求创建，线程安全由请求上下文保证。</li>
 * </ul>
 *
 * @author pangju666
 * @see WebLogInterceptor
 * @see WebLogResponseWrapper
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
	 * 媒体类型处理器列表
	 * <p>
	 * 按注册顺序进行媒体类型匹配，选择首个支持的处理器进行解析；
	 * 处理器由构造器注入，可按需扩展支持的内容类型。
	 * </p>
	 */
	private final List<MediaTypeBodyHandler> bodyHandlers;

	/**
	 * 构造方法
	 *
	 * @param configuration       日志配置对象
	 * @param sender              日志发送器
	 * @param bodyHandlers        已注册的媒体类型处理器列表（与内置处理器合并）
	 * @param excludePathPatterns 需要排除的路径匹配规则
	 * @since 1.0.0
	 */
	public WebLogFilter(WebLogConfiguration configuration, WebLogSender sender, List<MediaTypeBodyHandler> bodyHandlers,
						Set<String> excludePathPatterns) {
		super(excludePathPatterns);
		this.configuration = configuration;
		this.sender = sender;
		this.bodyHandlers = bodyHandlers;
	}

	/**
	 * 过滤器处理方法。
	 *
	 * <p><b>行为</b></p>
	 * <ul>
	 *   <li>当请求 {@code Content-Type} 无法解析时，直接透传不过滤与采集。</li>
	 *   <li>在需要读取请求体时包裹为 {@link ContentCachingRequestWrapper}；进入采集分支时响应包裹为 {@link io.github.pangju666.framework.boot.web.log.utils.WebLogResponseWrapper}。</li>
	 *   <li>执行过滤链，随后构建并发送 {@link WebLog}。</li>
	 *   <li>最后将缓存的响应体写回真实响应（调用 {@code copyBodyToResponse()}）。</li>
	 * </ul>
	 *
	 * <p><b>参数</b></p>
	 * <ul>
	 *   <li>{@code request} 当前 HTTP 请求。</li>
	 *   <li>{@code response} 当前 HTTP 响应（将被统一包裹为 {@code WebLogResponseWrapper}）。</li>
	 *   <li>{@code filterChain} 过滤器链。</li>
	 * </ul>
	 *
	 * <p><b>约束与约定</b></p>
	 * <ul>
	 *   <li>仅在配置允许且内容类型匹配时读取/解析请求体与响应体，以降低内存与解析开销。</li>
	 *   <li>后置组件若读取响应体缓存，不负责写回；本方法末尾统一写回响应体。</li>
	 *   <li>日志发送异常被捕获并记录，不影响业务处理。</li>
	 * </ul>
	 *
	 * @throws ServletException 过滤链或请求体解析过程中发生的 servlet 异常
	 * @throws IOException      I/O 异常（例如读取/写回响应体）
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		// 记录请求起始时间
		long start = SystemClock.now();

		if (StringUtils.isBlank(request.getContentType())) {
			filterChain.doFilter(request, response);
			return;
		}

		MediaType requestContentType;
		try {
			requestContentType = MediaType.parseMediaType(request.getContentType());
		} catch (InvalidMimeTypeException ignored) {
			filterChain.doFilter(request, response);
			return;
		}

		ContentCachingRequestWrapper contentCachingRequestWrapper = null;
		// 判断是否需要缓存请求体
		if (configuration.getRequest().isBody() && isAcceptableMediaType(
			configuration.getRequest().getAcceptableMediaTypes(), requestContentType)) {
			if (request instanceof ContentCachingRequestWrapper) {
				contentCachingRequestWrapper = (ContentCachingRequestWrapper) request;
			} else {
				contentCachingRequestWrapper = new ContentCachingRequestWrapper(request);
			}
			request = contentCachingRequestWrapper;
		}

		WebLog webLog = new WebLog();
		WebLogResponseWrapper webLogResponseWrapper = new WebLogResponseWrapper(response, webLog);

		// 执行过滤链
		filterChain.doFilter(request, webLogResponseWrapper);
		// 记录请求结束时间
		long end = SystemClock.now();

		webLog.setIp(HttpRequestUtils.getIpAddress(request));
		webLog.setMethod(request.getMethod());
		webLog.setUrl(request.getRequestURI());
		webLog.setRequest(getRequestLog(request, configuration));
		webLog.setResponse(getResponseLog(webLogResponseWrapper, configuration));
		webLog.setCostMillis(end - start);
		webLog.setDate(DateFormatUtils.formatDatetime(start));

		// 记录文件上传信息
		if (configuration.getRequest().isMultipart() && MediaType.MULTIPART_FORM_DATA.equalsTypeAndSubtype(requestContentType)) {
			webLog.getRequest().setFileParts(getFileParts(request));
		} else if (Objects.nonNull(contentCachingRequestWrapper)) { // 记录请求体
			byte[] requestBodyBytes = contentCachingRequestWrapper.getContentAsByteArray();
			if (ArrayUtils.isNotEmpty(requestBodyBytes)) {
				writeRequestBody(requestBodyBytes, webLog.getRequest(), requestContentType);
			}
		}

		// 记录响应重定向信息
		if (webLogResponseWrapper.getStatus() >= 300 && webLogResponseWrapper.getStatus() < 400) {
			webLog.getResponse().setLocation(webLogResponseWrapper.getHeader(HttpHeaders.LOCATION));
		} else { // 记录响应体信息
			byte[] responseBodyBytes = webLogResponseWrapper.getContentAsByteArray();
			if (ArrayUtils.isNotEmpty(responseBodyBytes)) {
				String responseContentType = webLogResponseWrapper.getContentType();
				if (StringUtils.isNotBlank(responseContentType)) {
					writeResponseBody(responseBodyBytes, webLog.getResponse(), responseContentType, configuration);
				}
			}
		}

		try {
			sender.send(webLog);
		} catch (Exception e) {
			logger.error("网络日志发送失败", e);
		}

		webLogResponseWrapper.copyBodyToResponse();
	}

	/**
	 * 构建请求日志。
	 *
	 * <p>记录字符集、内容长度、内容类型，并依据配置选择性记录请求头与查询参数。</p>
	 *
	 * @param request       当前请求对象
	 * @param configuration 日志采集配置
	 * @return 请求日志对象，包含基础信息及可选的头与查询参数
	 * @since 1.0.0
	 */
	protected WebLog.Request getRequestLog(HttpServletRequest request, WebLogConfiguration configuration) {
		WebLog.Request requestLog = new WebLog.Request();
		requestLog.setCharacterEncoding(request.getCharacterEncoding());
		requestLog.setContentLength(request.getContentLength());
		requestLog.setContentType(request.getContentType());
		// 记录请求头信息
		if (configuration.getRequest().isHeaders()) {
			requestLog.setHeaders(HttpRequestUtils.getHeaders(request));
		}
		// 记录查询参数
		if (configuration.getRequest().isQueryParams()) {
			requestLog.setQueryParams(HttpRequestUtils.getParameters(request));
		}
		return requestLog;
	}

	/**
	 * 写入请求体信息到请求日志。
	 *
	 * <p><b>行为</b></p>
	 * <ul>
	 *   <li>根据 {@link MediaType} 选择首个支持的 {@link MediaTypeBodyHandler} 进行解析与封装。</li>
	 *   <li>处理器由构造器注入，按照注册顺序进行匹配；命中后即停止尝试。</li>
	 * </ul>
	 *
	 * @param responseBodyBytes 请求体字节数组（来源于 {@link ContentCachingRequestWrapper}）
	 * @param requestLog       请求日志对象（写入解析后的请求体）
	 * @param contentType      请求内容类型（已解析为 {@link MediaType}）
	 * @since 1.0.0
	 */
	protected void writeRequestBody(byte[] responseBodyBytes, WebLog.Request requestLog, MediaType contentType) {
		for (MediaTypeBodyHandler bodyHandler : this.bodyHandlers) {
			if (bodyHandler.supports(contentType)) {
				requestLog.setBody(bodyHandler.getBody(responseBodyBytes, contentType));
				break;
			}
		}
	}

	/**
	 * 构建响应日志。
	 *
	 * <p>记录内容类型、字符集、状态码，并依据配置选择性记录响应头。</p>
	 *
	 * @param response      当前响应对象
	 * @param configuration 日志采集配置
	 * @return 响应日志对象，包含基础信息及可选的响应头
	 * @since 1.0.0
	 */
	protected WebLog.Response getResponseLog(HttpServletResponse response, WebLogConfiguration configuration) {
		WebLog.Response responseLog = new WebLog.Response();
		responseLog.setContentType(response.getContentType());
		responseLog.setCharacterEncoding(response.getCharacterEncoding());
		responseLog.setStatus(response.getStatus());
		// 记录响应头信息
		if (configuration.getResponse().isHeaders()) {
			HttpHeaders headers = new HttpHeaders();
			for (String headerName : response.getHeaderNames()) {
				headers.add(headerName, response.getHeader(headerName));
			}
			responseLog.setHeaders(headers);
		}
		return responseLog;
	}

	/**
	 * 写入响应体信息到响应日志。
	 *
	 * <p><b>行为</b></p>
	 * <ul>
	 *   <li>当内容类型为 JSON 时，仅记录符合 {@link Result} 结构的响应，委托 {@link #writeResultResponseBody(byte[], WebLog.Response, WebLogConfiguration)} 解析并写入；其他 JSON 不记录。</li>
	 *   <li>当内容类型在允许列表中时，根据处理器顺序选择首个支持的处理器解析；解析成功后即停止尝试；无法解析时忽略写入。</li>
	 * </ul>
	 *
	 * @param responseBodyBytes 响应体字节数组（来源于响应包装器，例如 {@link io.github.pangju666.framework.boot.web.log.utils.WebLogResponseWrapper}）
	 * @param responseLog       响应日志对象（写入解析后的响应体或结果）
	 * @param contentType       响应内容类型（原始字符串，将尝试解析为 {@link MediaType}）
	 * @param configuration     日志采集配置（决定是否记录结果数据及可接受的内容类型）
	 * @since 1.0.0
	 */
	protected void writeResponseBody(byte[] responseBodyBytes, WebLog.Response responseLog, String contentType,
								 	 WebLogConfiguration configuration) {
		MediaType mediaType;
		try {
			mediaType = MediaType.parseMediaType(contentType);
		} catch (InvalidMimeTypeException ignored) {
			return;
		}

		if (mediaType.equalsTypeAndSubtype(MediaType.APPLICATION_JSON)) {
			writeResultResponseBody(responseBodyBytes, responseLog, configuration);
		}
		if (Objects.isNull(responseLog.getBody()) && isAcceptableMediaType(configuration.getResponse()
			.getAcceptableMediaTypes(), mediaType)) {
			for (MediaTypeBodyHandler bodyHandler : this.bodyHandlers) {
				if (bodyHandler.supports(mediaType)) {
					responseLog.setBody(bodyHandler.getBody(responseBodyBytes, mediaType));
					break;
				}
			}
		}
	}

	/**
	 * 提取 {@code multipart/form-data} 文件部分信息。
	 *
	 * <p>将每个 {@link Part} 映射为 {@link WebLog.FilePart}，包含文件名、类型与大小。</p>
	 *
	 * @param request 已支持 {@code multipart} 的请求对象
	 * @return 文件部分信息映射；在读取失败时返回空映射
	 * @throws ServletException 解析请求体失败时抛出
	 * @throws IOException      读取请求体失败时抛出
	 * @since 1.0.0
	 */
	protected Map<String, WebLog.FilePart> getFileParts(HttpServletRequest request) throws ServletException, IOException {
		try {
			Map<String, Part> partMap = HttpRequestUtils.getParts(request);
			Map<String, WebLog.FilePart> fileInfoMap = new HashMap<>(partMap.size());
			for (Map.Entry<String, Part> stringPartEntry : partMap.entrySet()) {
				Part part = stringPartEntry.getValue();
				WebLog.FilePart fileInfo = new WebLog.FilePart();
				fileInfo.setContentType(part.getContentType());
				fileInfo.setSubmittedFileName(part.getSubmittedFileName());
				fileInfo.setSize(part.getSize());
				fileInfoMap.put(stringPartEntry.getKey(), fileInfo);
			}
			return fileInfoMap;
		} catch (IllegalStateException e) {
			logger.error("FormData请求体读取失败", e);
			return Collections.emptyMap();
		}
	}

	/**
	 * 解析并写入 Result 类型响应体信息。
	 *
	 * <p><b>范围</b></p>
	 * <ul>
	 *   <li>仅处理 {@code application/json} 且结构符合 {@link Result} 的响应体。</li>
	 *   <li>不记录非 Result 结构的 JSON 或其他内容类型。</li>
	 * </ul>
	 *
	 * <p><b>行为</b></p>
	 * <ul>
	 *   <li>通过 UTF-8 解码字节数组，尝试反序列化为 {@link Result}。</li>
	 *   <li>当配置关闭结果数据记录时，仅保留状态码与消息，数据置为 {@code null}。</li>
	 *   <li>解析失败（JSON 语法或结构不匹配）时忽略写入，不抛出异常。</li>
	 * </ul>
	 *
	 * @param responseBodyBytes 响应体字节数组（UTF-8 解码用于 JSON 解析）
	 * @param responseLog       响应日志对象（写入解析后的 {@link Result} 或裁剪后的数据）
	 * @param configuration     日志采集配置（决定是否保留结果数据）
	 * @since 1.0.0
	 */
	protected void writeResultResponseBody(byte[] responseBodyBytes, WebLog.Response responseLog,
									   WebLogConfiguration configuration) {
		try {
			String responseBodyStr = new String(responseBodyBytes, StandardCharsets.UTF_8);
			Result<?> result = JsonUtils.fromString(responseBodyStr, Result.class);
			if (!configuration.getResponse().isResultData()) {
				responseLog.setBody(new Result<Void>(result.code(), result.message(), null));
			} else {
				responseLog.setBody(result);
			}
		} catch (JsonSyntaxException ignored) {
		}
	}

	/**
	 * 判断内容类型是否在可接受列表中。
	 *
	 * <p>仅比较类型与子类型，忽略参数（如字符集）。</p>
	 *
	 * @param acceptableMediaTypes 可接受的内容类型列表
	 * @param mediaType            当前内容类型
	 * @return 当类型与子类型匹配时返回 {@code true}
	 * @since 1.0.0
	 */
	protected boolean isAcceptableMediaType(List<MediaType> acceptableMediaTypes, MediaType mediaType) {
		return acceptableMediaTypes.stream().anyMatch(mediaType::equalsTypeAndSubtype);
	}
}