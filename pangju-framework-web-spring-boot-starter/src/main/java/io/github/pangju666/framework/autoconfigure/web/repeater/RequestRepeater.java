package io.github.pangju666.framework.autoconfigure.web.repeater;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.github.pangju666.commons.lang.utils.JsonUtils;
import io.github.pangju666.framework.autoconfigure.web.annotation.validation.Repeat;
import io.github.pangju666.framework.web.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class RequestRepeater {
	protected final String delimiter;

	protected RequestRepeater(String delimiter) {
		this.delimiter = delimiter;
	}

	abstract public boolean tryAcquire(Repeat repeat, HttpServletRequest request);

	protected String generateKey(Repeat annotation, HttpServletRequest request) {
		StringBuilder stringBuilder = new StringBuilder()
			.append(request.getMethod())
			.append(delimiter)
			.append(RequestUtils.getRequestPath(request))
			.append(delimiter)
			.append(RequestUtils.getIpAddress(request));
		String digest = computeRequestContentDigest(annotation, request);
		if (StringUtils.isNotBlank(digest)) {
			stringBuilder
				.append(delimiter)
				.append(digest);
		}
		return stringBuilder.toString();
	}

	protected String computeRequestContentDigest(Repeat annotation, HttpServletRequest request) {
		Map<String, Object> header = getHeaderMap(annotation, request);
		Map<String, Object> param = getParamMap(annotation, request);
		Map<String, Object> body = getBodyMap(annotation, request);

		Map<String, Object> contentMap = new HashMap<>(3);
		if (MapUtils.isNotEmpty(header)) {
			contentMap.put("header", header);
		}
		if (MapUtils.isNotEmpty(param)) {
			contentMap.put("param", param);
		}
		if (MapUtils.isNotEmpty(body)) {
			contentMap.put("body", body);
		}
		if (contentMap.isEmpty()) {
			return StringUtils.EMPTY;
		}
		return DigestUtils.sha256Hex(JsonUtils.toString(contentMap));
	}

	protected Map<String, Object> getHeaderMap(Repeat annotation, HttpServletRequest request) {
		if (annotation.allHeaders()) {
			return RequestUtils.getHeaderMap(request);
		}
		if (annotation.headers().length == 0) {
			return Collections.emptyMap();
		}
		return Arrays.stream(annotation.headers())
			.filter(StringUtils::isNotBlank)
			.map(header -> Pair.of(header, request.getHeader(header)))
			.filter(header -> StringUtils.isNotBlank(header.getValue()))
			.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
	}

	protected Map<String, Object> getParamMap(Repeat annotation, HttpServletRequest request) {
		if (annotation.allParams()) {
			return RequestUtils.getParameterMap(request);
		}
		if (annotation.params().length == 0) {
			return Collections.emptyMap();
		}
		return Arrays.stream(annotation.params())
			.filter(StringUtils::isNotBlank)
			.map(param -> Pair.of(param, request.getParameter(param)))
			.filter(param -> StringUtils.isNotBlank(param.getValue()))
			.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
	}

	protected Map<String, Object> getBodyMap(Repeat annotation, HttpServletRequest request) {
		if (annotation.allBody()) {
			try {
				return RequestUtils.getRequestBodyMap(request);
			} catch (IOException e) {
				ExceptionUtils.rethrow(e);
			}
		}
		if (annotation.bodyJsonPaths().length == 0) {
			return Collections.emptyMap();
		}
		if (request instanceof ContentCachingRequestWrapper requestWrapper) {
			DocumentContext documentContext = JsonPath.parse(requestWrapper.getContentAsString());
			return Arrays.stream(annotation.bodyJsonPaths())
				.filter(StringUtils::isNotBlank)
				.map(path -> {
					String jsonPath = path;
					if (!jsonPath.startsWith("$")) {
						jsonPath = "$" + path;
					}
					return Pair.of(jsonPath, documentContext.read(jsonPath));
				})
				.filter(body -> Objects.nonNull(body.getValue()))
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		}
		return Collections.emptyMap();
	}
}
