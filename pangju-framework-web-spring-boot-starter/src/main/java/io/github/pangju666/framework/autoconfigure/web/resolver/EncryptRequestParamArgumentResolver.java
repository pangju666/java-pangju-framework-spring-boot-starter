package io.github.pangju666.framework.autoconfigure.web.resolver;

import io.github.pangju666.framework.autoconfigure.context.StaticSpringContext;
import io.github.pangju666.framework.autoconfigure.web.annotation.crypto.EncryptRequestParam;
import io.github.pangju666.framework.autoconfigure.web.utils.CryptoUtils;
import io.github.pangju666.framework.core.exception.base.ServiceException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class EncryptRequestParamArgumentResolver implements HandlerMethodArgumentResolver {
	private static final Logger logger = LoggerFactory.getLogger(EncryptRequestParamArgumentResolver.class);

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(EncryptRequestParam.class) && parameter.getParameterType().isAssignableFrom(String.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
								  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		EncryptRequestParam annotation = parameter.getParameterAnnotation(EncryptRequestParam.class);
		String parameterName = annotation.name();
		if (StringUtils.isBlank(parameterName)) {
			parameterName = parameter.getParameterName();
		}

		String parameterValue = webRequest.getParameter(parameterName);
		if (StringUtils.isBlank(parameterValue)) {
			if (annotation.required()) {
				throw new MissingServletRequestParameterException(parameterName, String.class.getName());
			}
			return annotation.defaultValue();
		}

		String key = StaticSpringContext.getProperty(annotation.key());
		if (StringUtils.isBlank(key)) {
			logger.error("属性：{} 值为空", annotation.key());
			throw new ServiceException("秘钥读取失败");
		}

		try {
			parameterValue = CryptoUtils.decryptToString(parameterValue, key, annotation.algorithm(), annotation.encoding());
		} catch (EncryptionOperationNotPossibleException e) {
			logger.error("请求参数解密失败", e);
			throw new ServiceException("请求参数解密失败");
		} catch (DecoderException e) {
			logger.error("十六进制解码失败", e);
			throw new ServiceException("请求参数十六进制解码失败");
		}

		mavContainer.addAttribute(parameterName, parameterValue);
		return parameterValue;
	}
}
