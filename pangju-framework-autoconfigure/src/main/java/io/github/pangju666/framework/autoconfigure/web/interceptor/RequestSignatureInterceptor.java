package io.github.pangju666.framework.autoconfigure.web.interceptor;

import io.github.pangju666.commons.lang.utils.DateUtils;
import io.github.pangju666.framework.autoconfigure.web.annotation.validation.Signature;
import io.github.pangju666.framework.autoconfigure.web.enums.SignatureAlgorithm;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestSignatureProperties;
import io.github.pangju666.framework.autoconfigure.web.store.SignatureSecretKeyStore;
import io.github.pangju666.framework.web.exception.base.ValidationException;
import io.github.pangju666.framework.web.interceptor.BaseHttpHandlerInterceptor;
import io.github.pangju666.framework.web.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.HandlerMethod;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RequestSignatureInterceptor extends BaseHttpHandlerInterceptor {
	private final RequestSignatureProperties properties;
	private final SignatureSecretKeyStore secretKeyStore;

	public RequestSignatureInterceptor(RequestSignatureProperties properties, SignatureSecretKeyStore secretKeyStore) {
		super(null, null);
		this.properties = properties;
		this.secretKeyStore = secretKeyStore;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws MissingRequestValueException {
		if (handler instanceof HandlerMethod handlerMethod) {
			Signature annotation = handlerMethod.getMethodAnnotation(Signature.class);
			if (Objects.isNull(annotation)) {
				Class<?> targetClass = handlerMethod.getBeanType();
				annotation = targetClass.getAnnotation(Signature.class);
				if (Objects.isNull(annotation)) {
					return true;
				}
			}

			return switch (annotation.type()) {
				case HEADER -> validateSignatureByHeaders(request, response, annotation);
				case PARAMS -> validateSignatureByParams(request, response, annotation);
				default -> {
					String signatureHeader = request.getHeader(properties.getSignatureHeaderName());
					String timestampHeader = request.getHeader(properties.getTimestampHeaderName());
					if (StringUtils.isAllBlank(signatureHeader, timestampHeader)) {
						yield validateSignatureByParams(request, response, annotation);
					} else {
						yield validateSignatureByHeaders(request, response, annotation);
					}
				}
			};
		}
		return true;
	}

	private boolean validateSignatureByParams(HttpServletRequest request, HttpServletResponse response,
											  Signature annotation) throws MissingServletRequestParameterException {
		String appId = request.getParameter(properties.getAppIdParamName());
		if (StringUtils.isBlank(appId)) {
			throw new MissingServletRequestParameterException(properties.getAppIdParamName(), "string");
		}
		if (ArrayUtils.isNotEmpty(annotation.appId()) && !ArrayUtils.contains(annotation.appId(), appId)) {
			ResponseUtils.writeHttpExceptionToResponse(new ValidationException("不是指定的appId"), response);
			return false;
		}

		String signature = request.getParameter(properties.getSignatureParamName());
		if (StringUtils.isBlank(signature)) {
			throw new MissingServletRequestParameterException(properties.getSignatureParamName(), "string");
		}

		String secretKey = secretKeyStore.loadSecretKey(appId);
		if (StringUtils.isBlank(secretKey)) {
			ResponseUtils.writeHttpExceptionToResponse(new ValidationException("应用标识符不存在"), response);
			return false;
		}

		String requestUrl = getRequestUrl(request);
		String signStr = StringUtils.joinWith("&", appId, secretKey, requestUrl);
		String expectSignature = computeSignature(signStr, annotation.algorithm());

		if (!StringUtils.equals(expectSignature, signature)) {
			ResponseUtils.writeHttpExceptionToResponse(new ValidationException("签名错误"), response);
			return false;
		}
		return true;
	}

	private boolean validateSignatureByHeaders(HttpServletRequest request, HttpServletResponse response,
											   Signature annotation) throws MissingRequestValueException {
		try {
			String appId = request.getHeader(properties.getAppIdHeaderName());
			if (StringUtils.isBlank(appId)) {
				throw new MissingRequestValueException("缺少请求头：" + properties.getAppIdHeaderName());
			}
			if (ArrayUtils.isNotEmpty(annotation.appId()) && !ArrayUtils.contains(annotation.appId(), appId)) {
				ResponseUtils.writeHttpExceptionToResponse(new ValidationException("不是指定的appId"), response);
				return false;
			}

			String signature = request.getHeader(properties.getSignatureParamName());
			if (StringUtils.isBlank(signature)) {
				throw new MissingRequestValueException("缺少请求头：" + properties.getSignatureParamName());
			}

			String timestamp = request.getHeader(properties.getTimestampHeaderName());
			if (StringUtils.isBlank(timestamp)) {
				throw new MissingRequestValueException("缺少请求头：" + properties.getTimestampHeaderName());
			}
			Long requestTimestamp = Long.parseLong(timestamp);
			Long nowTimestamp = DateUtils.nowDate().getTime();
			if (nowTimestamp - requestTimestamp > annotation.timeUnit().toMillis(annotation.timeout())) {
				ResponseUtils.writeHttpExceptionToResponse(new ValidationException("签名已过期"), response);
				return false;
			}

			String secretKey = secretKeyStore.loadSecretKey(appId);
			if (StringUtils.isBlank(secretKey)) {
				ResponseUtils.writeHttpExceptionToResponse(new ValidationException("应用标识符不存在"), response);
				return false;
			}

			String requestUrl = URLEncoder.encode(request.getRequestURL().toString(), StandardCharsets.UTF_8);
			String signStr = StringUtils.joinWith("&", appId, secretKey, requestUrl, timestamp);
			String expectSignature = computeSignature(signStr, annotation.algorithm());

			if (!StringUtils.equals(expectSignature, signature)) {
				ResponseUtils.writeHttpExceptionToResponse(new ValidationException("签名错误"), response);
				return false;
			}
			return true;
		} catch (NumberFormatException e) {
			ResponseUtils.writeHttpExceptionToResponse(new ValidationException("无效的时间戳"), response);
			return false;
		}
	}

	private String getRequestUrl(HttpServletRequest request) {
		String requestUrl = request.getRequestURL().toString();
		String queryString = request.getQueryString();
		List<String> queryParams = new ArrayList<>();
		for (String queryParam : queryString.split("&")) {
			if (!StringUtils.startsWithAny(queryParam, properties.getSignatureParamName(), properties.getAppIdParamName())) {
				queryParams.add(queryParam);
			}
		}
		if (queryParams.isEmpty()) {
			return URLEncoder.encode(requestUrl, StandardCharsets.UTF_8);
		}
		return URLEncoder.encode(requestUrl + "?" + StringUtils.join(queryParams, "&"), StandardCharsets.UTF_8);
	}

	private String computeSignature(String str, SignatureAlgorithm algorithm) {
		return switch (algorithm) {
			case MD5 -> DigestUtils.md5Hex(str);
			case SHA1 -> DigestUtils.sha1Hex(str);
			case SHA256 -> DigestUtils.sha256Hex(str);
			case SHA512 -> DigestUtils.sha512Hex(str);
		};
	}
}
