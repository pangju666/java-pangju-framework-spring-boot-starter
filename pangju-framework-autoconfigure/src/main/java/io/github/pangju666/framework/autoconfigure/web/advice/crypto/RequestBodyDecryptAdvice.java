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

package io.github.pangju666.framework.autoconfigure.web.advice.crypto;

import io.github.pangju666.framework.autoconfigure.spring.StaticSpringContext;
import io.github.pangju666.framework.autoconfigure.web.utils.CryptoUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.exception.base.ServiceException;
import jakarta.servlet.Servlet;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@RestControllerAdvice
public class RequestBodyDecryptAdvice implements RequestBodyAdvice {
	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
							Class<? extends HttpMessageConverter<?>> converterType) {
		return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
	}

	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
										   Class<? extends HttpMessageConverter<?>> converterType) {
		DecryptRequestBody annotation = parameter.getParameterAnnotation(DecryptRequestBody.class);
		if (Objects.isNull(annotation)) {
			return inputMessage;
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

		try (InputStream inputStream = inputMessage.getBody()) {
			String requestBodyStr = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			if (StringUtils.isBlank(requestBodyStr)) {
				return inputMessage;
			}
			byte[] plainContent = CryptoUtils.decrypt(requestBodyStr, key, annotation.algorithm(), annotation.encoding());
			return new MappingJacksonInputMessage(new ByteArrayInputStream(plainContent), inputMessage.getHeaders());
		} catch (IOException e) {
			throw new ServerException("请求体读取失败", e);
		} catch (EncryptionOperationNotPossibleException e) {
			throw new ServiceException("无效的请求数据", "请求数据对象解密失败", e);
		} catch (DecoderException e) {
			throw new ServiceException("无效的请求数据", "请求数据对象十六进制解码失败", e);
		} catch (InvalidKeySpecException e) {
			throw new ServerException("无效的密钥", e);
		}
	}

	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
								Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}

	@Override
	public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
								  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}
}
