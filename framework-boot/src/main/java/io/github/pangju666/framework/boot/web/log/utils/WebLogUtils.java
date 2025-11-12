package io.github.pangju666.framework.boot.web.log.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.lang.utils.ArrayUtils;
import io.github.pangju666.commons.lang.utils.JsonUtils;
import io.github.pangju666.framework.boot.web.log.configuration.WebLogConfiguration;
import io.github.pangju666.framework.boot.web.log.model.WebLog;
import io.github.pangju666.framework.web.model.Result;
import io.github.pangju666.framework.web.servlet.utils.HttpRequestUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Web 请求/响应日志工具。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>按 {@link io.github.pangju666.framework.boot.web.log.configuration.WebLogConfiguration} 配置提取并结构化请求/响应的日志信息。</li>
 *   <li>支持请求头、查询参数、文件部分、JSON/文本请求体及响应体的采集。</li>
 * </ul>
 *
 * <p><b>使用约束</b></p>
 * <ul>
 *   <li>要采集请求体需将 {@link jakarta.servlet.http.HttpServletRequest} 包装为 {@link org.springframework.web.util.ContentCachingRequestWrapper}。</li>
 *   <li>要采集响应体需将 {@link jakarta.servlet.http.HttpServletResponse} 包装为 {@link org.springframework.web.util.ContentCachingResponseWrapper}。</li>
 *   <li>采集体（body）受配置的可接受媒体类型与开关控制。</li>
 * </ul>
 *
 * <p><b>线程安全</b></p>
 * <ul>
 *   <li>工具类为无状态实现，所有方法均为静态调用，线程安全。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class WebLogUtils {
    private static final Logger logger = LoggerFactory.getLogger(WebLogUtils.class);

    protected WebLogUtils() {
    }

    /**
     * 根据配置采集并构建请求日志。
     *
     * <p><b>行为</b></p>
     * <ul>
     *   <li>采集请求头（当配置开启）。</li>
     *   <li>采集查询参数（当配置开启）。</li>
     *   <li>当请求为 {@code multipart/form-data} 且开启采集文件，提取文件部分。</li>
     *   <li>当请求体媒体类型被允许且请求包装为 {@link ContentCachingRequestWrapper}：</li>
     *   <li>— 对 {@code application/json} 解析为对象；否则按文本类型或二进制处理。</li>
     * </ul>
     *
     * <p><b>前置条件</b></p>
     * <ul>
     *   <li>如需采集请求体，应在过滤器中提前包裹 {@link HttpServletRequest} 为 {@link ContentCachingRequestWrapper}。</li>
     * </ul>
     *
     * @param request 原始或缓存包装的请求对象
     * @param configuration 日志采集配置
     * @return 结构化的请求日志对象
     * @throws IOException 读取请求体可能抛出的 IO 异常
     * @throws ServletException 读取 {@code multipart} 文件部分可能抛出的异常
	 * @since 1.0.0
     */
    public static WebLog.Request getRequestLog(HttpServletRequest request, WebLogConfiguration configuration) throws IOException, ServletException {
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

		// 记录文件上传信息
		String contentType = StringUtils.defaultString(request.getContentType());
		MediaType requestBodyType = MediaType.parseMediaType(contentType);
		if (configuration.getRequest().isMultipart() && MediaType.MULTIPART_FORM_DATA.equalsTypeAndSubtype(requestBodyType)) {
			requestLog.setFileParts(getFileParts(request));
			// 记录请求体信息
		} else if (configuration.getRequest().isBody() && configuration.getRequest().getAcceptableMediaTypes()
			.contains(contentType) && request instanceof ContentCachingRequestWrapper requestWrapper) {
			// 记录JSON请求体
			if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(requestBodyType)) {
				requestLog.setBody(HttpRequestUtils.getJsonRequestBody(requestWrapper, Object.class));
			} else if (configuration.getRequest().isBody()) {
				byte[] requestBodyBytes = requestWrapper.getContentAsByteArray();
				// 判断是否为文本类型请求体
				if (contentType.startsWith(IOConstants.TEXT_MIME_TYPE_PREFIX)) {
					requestLog.setBody(getTextBody(requestBodyBytes, request.getCharacterEncoding()));
				} else {
					requestLog.setBody(requestBodyBytes);
				}
			}
		}

        return requestLog;
    }

    /**
     * 根据配置采集并构建响应日志。
     *
     * <p><b>行为</b></p>
     * <ul>
     *   <li>采集响应头（当配置开启）。</li>
     *   <li>对 3xx 响应记录 {@code Location} 重定向地址。</li>
     *   <li>当响应体媒体类型被允许且响应包装为 {@link ContentCachingResponseWrapper}：</li>
     *   <li>— 对 {@code application/json} 解析为对象；否则按文本类型或二进制处理。</li>
     * </ul>
     *
     * <p><b>前置条件</b></p>
     * <ul>
     *   <li>如需采集响应体，应在过滤器或拦截器中提前包裹 {@link HttpServletResponse} 为 {@link ContentCachingResponseWrapper}。</li>
     * </ul>
     *
     * @param response 原始或缓存包装的响应对象
     * @param configuration 日志采集配置
     * @return 结构化的响应日志对象
	 * @since 1.0.0
	 */
    public static WebLog.Response getResponseLog(HttpServletResponse response, WebLogConfiguration configuration) {
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

		// 判断是否为重定向响应
		if (response.getStatus() >= 300 && response.getStatus() < 400) {
			responseLog.setLocation(response.getHeader(HttpHeaders.LOCATION));
		} else { // 记录响应体信息
			String contentType = StringUtils.defaultString(response.getContentType());
			if (configuration.getResponse().getAcceptableMediaTypes()
				.contains(contentType) && response instanceof ContentCachingResponseWrapper responseWrapper) {
				// 判断是否为JSON响应体
				if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(MediaType.parseMediaType(contentType))) {
					responseLog.setBody(getJsonResponseBody(responseWrapper, configuration));
					// 判断是否允许记录响应体信息
				} else if (configuration.getResponse().isBody()) {
					byte[] responseBodyBytes = responseWrapper.getContentAsByteArray();
					// 判断是否为文本类型响应体
					if (contentType.startsWith(IOConstants.TEXT_MIME_TYPE_PREFIX)) {
						responseLog.setBody(getTextBody(responseBodyBytes, response.getCharacterEncoding()));
					} else {
						responseLog.setBody(responseBodyBytes);
					}
				}
			}
		}

        return responseLog;
    }

    /**
     * 提取 {@code multipart/form-data} 文件部分信息。
     *
     * <p>将每个 {@link Part} 映射为 {@link WebLog.FilePart}，包含文件名、类型与大小。</p>
     *
     * @param request 已支持 {@code multipart} 的请求对象
     * @return 文件部分信息映射；在读取失败时返回空映射
     * @throws ServletException 解析请求体失败时抛出
     * @throws IOException 读取请求体失败时抛出
	 * @since 1.0.0
	 */
    protected static Map<String, WebLog.FilePart> getFileParts(HttpServletRequest request) throws ServletException, IOException {
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
     * 解析 JSON 类型响应体。
     *
     * <p><b>行为</b></p>
     * <ul>
     *   <li>若非 JSON 对象且允许记录响应体，解析为通用对象。</li>
     *   <li>若为 JSON 对象，尝试解析为 {@link Result}：
     *     <ul>
     *       <li>当关闭记录结果数据时，仅保留 {@code code} 与 {@code message}，数据置为 {@code null}。</li>
     *       <li>否则返回完整解析结果。</li>
     *     </ul>
     *   </li>
     *   <li>解析失败记录错误并返回 {@code null}。</li>
     * </ul>
     *
     * @param responseWrapper 包装后的响应对象（用于读取缓存的响应体）
     * @param configuration 日志采集配置
     * @return 解析后的响应体对象；可能为 {@link Result}、通用对象或 {@code null}
	 * @since 1.0.0
	 */
    protected static Object getJsonResponseBody(ContentCachingResponseWrapper responseWrapper, WebLogConfiguration configuration) {
        byte[] responseBodyBytes = responseWrapper.getContentAsByteArray();
        if (ArrayUtils.isEmpty(responseBodyBytes)) {
            return null;
        }

		String responseBodyStr = new String(responseBodyBytes, StandardCharsets.UTF_8);
		try {
			JsonElement responseBodyJson = JsonUtils.parseString(responseBodyStr);
			// 如果非JSON对象响应体，就判断是否允许记录响应体信息
			if (!responseBodyJson.isJsonObject() && configuration.getResponse().isBody()) {
				return JsonUtils.fromJson(responseBodyJson, Object.class);
			}

			JsonObject responseBodyJsonObject = responseBodyJson.getAsJsonObject();
			try {
				Result<?> result = JsonUtils.fromJson(responseBodyJsonObject, Result.class);
				if (!configuration.getResponse().isResultData()) {
					return new Result<Void>(result.code(), result.message(), null);
				}
				return result;
			} catch (JsonSyntaxException e) {
				if (configuration.getResponse().isBody()) {
					return JsonUtils.fromJson(responseBodyJsonObject, Object.class);
				}
			}
		} catch (JsonSyntaxException e) {
			logger.error("JSON响应体解析失败", e);
		}
        return null;
    }

    /**
     * 将二进制内容按指定字符集解码为字符串。
     *
     * <p>当字符集非法或不受支持时回退至 {@link StandardCharsets#UTF_8}。</p>
     *
     * @param bytes 文本字节数组
     * @param encoding 字符集名称，可为空
     * @return 解码后的文本内容
	 * @since 1.0.0
	 */
    protected static String getTextBody(byte[] bytes, String encoding) {
        Charset charset = StandardCharsets.UTF_8;
        try {
            if (StringUtils.isNotBlank(encoding)) {
                charset = Charset.forName(encoding);
            }
        } catch (UnsupportedCharsetException | IllegalCharsetNameException ignored) {
        }
        return new String(bytes, charset);
    }
}
