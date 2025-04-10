package io.github.pangju666.framework.autoconfigure.web.crypto.resolver;

import io.github.pangju666.framework.autoconfigure.spring.context.StaticSpringContext;
import io.github.pangju666.framework.autoconfigure.web.crypto.annotation.EncryptRequestParam;
import io.github.pangju666.framework.autoconfigure.web.crypto.utils.CryptoUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.exception.base.ServiceException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class EncryptRequestParamArgumentResolver implements HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(EncryptRequestParam.class) &&
			parameter.getParameterType().isAssignableFrom(String.class);
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
			throw new ServerException("属性：" + annotation.key() + "值为空");
		}

		try {
			parameterValue = CryptoUtils.decryptToString(parameterValue, key, annotation.algorithm(), annotation.encoding());
		} catch (EncryptionOperationNotPossibleException e) {
			throw new ServiceException("无效的加密请求参数", "请求参数解密失败", e);
		} catch (DecoderException e) {
			throw new ServiceException("无效的加密请求参数", "请求参数十六进制解码失败", e);
		}

		mavContainer.addAttribute(parameterName, parameterValue);
		return parameterValue;
	}
}
