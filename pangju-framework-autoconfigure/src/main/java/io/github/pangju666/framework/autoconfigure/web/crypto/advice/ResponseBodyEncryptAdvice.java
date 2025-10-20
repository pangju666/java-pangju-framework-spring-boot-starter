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

package io.github.pangju666.framework.autoconfigure.web.crypto.advice;

import io.github.pangju666.commons.lang.utils.JsonUtils;
import io.github.pangju666.framework.autoconfigure.spring.StaticSpringContext;
import io.github.pangju666.framework.autoconfigure.web.crypto.utils.CryptoUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.model.common.Result;
import jakarta.servlet.Servlet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

@Order(Ordered.LOWEST_PRECEDENCE - 1)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@RestControllerAdvice
public class ResponseBodyEncryptAdvice implements ResponseBodyAdvice<Object> {
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType) ||
			GsonHttpMessageConverter.class.isAssignableFrom(converterType) ||
			ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType) ||
			StringHttpMessageConverter.class.isAssignableFrom(converterType);
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
								  Class<? extends HttpMessageConverter<?>> selectedConverterType,
								  ServerHttpRequest request, ServerHttpResponse response) {
		EncryptResponseBody annotation = returnType.getMethodAnnotation(EncryptResponseBody.class);
		if (Objects.isNull(annotation)) {
			annotation = returnType.getDeclaringClass().getAnnotation(EncryptResponseBody.class);
		}
		if (Objects.isNull(annotation)) {
			return body;
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
			if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType) && body instanceof String string) {
				if (StringUtils.isBlank(string)) {
					return body;
				}
				return CryptoUtils.encryptToString(StringUtils.defaultString(string).getBytes(), key, annotation.algorithm(), annotation.encoding());
			} else if (ByteArrayHttpMessageConverter.class.isAssignableFrom(selectedConverterType) && body instanceof byte[] bytes) {
				if (ArrayUtils.isEmpty(bytes)) {
					return body;
				}
				return CryptoUtils.encrypt(bytes, key, annotation.algorithm(), annotation.encoding());
			} else if (body instanceof Result<?> data) {
				if (Objects.isNull(data.data())) {
					return data;
				}
				if (data.data().getClass().isAssignableFrom(String.class)) {
					String bodyData = StringUtils.defaultString((String) data.data());
					String result = CryptoUtils.encryptToString(bodyData.getBytes(), key, annotation.algorithm(), annotation.encoding());
					return Result.ok(result);
				} else if (data.data().getClass().isAssignableFrom(byte[].class)) {
					byte[] bodyData = ArrayUtils.nullToEmpty((byte[]) data.data());
					byte[] result = CryptoUtils.encrypt(bodyData, key, annotation.algorithm(), annotation.encoding());
					return Result.ok(result);
				}
				String content = JsonUtils.toString(data.data());
				String result = CryptoUtils.encryptToString(content.getBytes(), key, annotation.algorithm(), annotation.encoding());
				return Result.ok(result);
			} else {
				String content = JsonUtils.toString(body);
				return CryptoUtils.encrypt(content.getBytes(), key, annotation.algorithm(), annotation.encoding());
			}
		} catch (EncryptionOperationNotPossibleException e) {
			throw new ServerException("响应数据对象加密失败", e);
		} catch (InvalidKeySpecException e) {
			throw new ServerException("无效的密钥", e);
		}
	}
}