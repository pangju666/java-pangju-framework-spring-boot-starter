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

package io.github.pangju666.framework.autoconfigure.web.resolver.crypto;

import io.github.pangju666.framework.autoconfigure.spring.StaticSpringContext;
import io.github.pangju666.framework.autoconfigure.web.utils.CryptoUtils;
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

		String key = null;
		if (annotation.algorithm().needKey()) {
			if (StringUtils.isBlank(annotation.key())) {
				throw new ServerException("无效的密钥属性值");
			}
			key = StaticSpringContext.getProperty(annotation.key());
			if (StringUtils.isBlank(key)) {
				throw new ServerException("未找到密钥，属性：" + key);
			}
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
