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

package io.github.pangju666.framework.autoconfigure.web.resolver;

import io.github.pangju666.framework.web.exception.base.ValidationException;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Objects;

public class EnumRequestParamArgumentResolver implements HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(EnumRequestParam.class) && parameter.getParameterType().isEnum();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
								  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		EnumRequestParam annotation = parameter.getParameterAnnotation(EnumRequestParam.class);
		Class<? extends Enum> enumClass = (Class<? extends Enum>) parameter.getParameterType();

		String parameterName = annotation.value();
		if (StringUtils.isBlank(parameterName)) {
			parameterName = parameter.getParameterName();
		}

		String enumName = webRequest.getParameter(parameterName);
		if (StringUtils.isBlank(enumName)) {
			String defaultValue = annotation.defaultValue();
			if (StringUtils.isNotBlank(defaultValue)) {
				enumName = defaultValue;
			} else {
				if (annotation.required()) {
					throw new MissingServletRequestParameterException(parameterName, enumClass.getName());
				}
				return null;
			}
		}

		Enum<?> enumValue = EnumUtils.getEnumIgnoreCase(enumClass, enumName);
		if (Objects.isNull(enumValue)) {
			throw new ValidationException("无效的" + annotation.description());
		}
		mavContainer.addAttribute(parameterName, enumValue);
		return enumValue;
	}
}