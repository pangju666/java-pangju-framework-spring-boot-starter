package io.github.pangju666.framework.autoconfigure.web.interceptor;

import io.github.pangju666.commons.lang.utils.DateUtils;
import io.github.pangju666.framework.autoconfigure.web.annotation.validation.Signature;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestSignatureProperties;
import io.github.pangju666.framework.autoconfigure.web.store.SignatureSecretKeyStore;
import io.github.pangju666.framework.core.exception.base.ValidationException;
import io.github.pangju666.framework.web.interceptor.BaseRequestInterceptor;
import io.github.pangju666.framework.web.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.HandlerMethod;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RequestSignatureInterceptor extends BaseRequestInterceptor {
	private final RequestSignatureProperties properties;
	private final SignatureSecretKeyStore secretKeyStore;

	public RequestSignatureInterceptor(RequestSignatureProperties properties, SignatureSecretKeyStore secretKeyStore) {
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
											  Signature apiSignature) throws MissingServletRequestParameterException {
		String appId = StringUtils.defaultIfBlank(apiSignature.appId(), request.getParameter(properties.getAppIdParamName()));
		if (StringUtils.isBlank(appId)) {
			throw new MissingServletRequestParameterException(properties.getAppIdParamName(), "string");
		}
		String signature = request.getParameter(properties.getSignatureParamName());
		if (StringUtils.isBlank(signature)) {
			throw new MissingServletRequestParameterException(properties.getSignatureParamName(), "string");
		}

		String secretKey = secretKeyStore.loadSecretKey(appId);
		String requestUrl = getRequestUrl(request);
		String actualSignature = DigestUtils.sha1Hex(StringUtils.joinWith("&", appId, secretKey, requestUrl));

		if (!actualSignature.equals(signature)) {
			ResponseUtils.writeExceptionToResponse(new ValidationException("签名错误"), response, HttpStatus.BAD_REQUEST);
			return false;
		}
		return true;
	}

	private boolean validateSignatureByHeaders(HttpServletRequest request, HttpServletResponse response,
											   Signature apiSignature) throws MissingRequestValueException {
		try {
			String appId = StringUtils.defaultIfBlank(apiSignature.appId(), request.getHeader(properties.getAppIdHeaderName()));
			if (StringUtils.isBlank(appId)) {
				throw new MissingRequestValueException("缺少请求头：" + properties.getAppIdHeaderName());
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
			if (nowTimestamp - requestTimestamp > apiSignature.timeUnit().toMillis(apiSignature.timeout())) {
				ResponseUtils.writeExceptionToResponse(new ValidationException("签名已过期"), response, HttpStatus.BAD_REQUEST);
				return false;
			}

			String secretKey = secretKeyStore.loadSecretKey(appId);
			String requestUrl = URLEncoder.encode(request.getRequestURL().toString(), StandardCharsets.UTF_8);
			String actualSignature = DigestUtils.sha1Hex(StringUtils.joinWith("&", appId, secretKey, requestUrl, timestamp));

			if (!actualSignature.equals(signature)) {
				ResponseUtils.writeExceptionToResponse(new ValidationException("签名错误"), response, HttpStatus.BAD_REQUEST);
				return false;
			}
			return true;
		} catch (NumberFormatException e) {
			ResponseUtils.writeExceptionToResponse(new ValidationException("无效的时间戳"), response, HttpStatus.BAD_REQUEST);
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
}
