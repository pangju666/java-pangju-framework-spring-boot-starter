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

package io.github.pangju666.framework.boot.autoconfigure.web.crypto;

import io.github.pangju666.commons.crypto.key.RSAKeyPair;
import io.github.pangju666.commons.lang.pool.Constants;
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.crypto.utils.CryptoUtils;
import io.github.pangju666.framework.boot.spring.StaticSpringContext;
import io.github.pangju666.framework.boot.web.advice.DecryptRequestBody;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.exception.base.ServiceException;
import jakarta.servlet.Servlet;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.AbstractJsonHttpMessageConverter;
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
import java.util.Objects;

/**
 * 请求体解密全局通知
 * <p>
 * 在请求体反序列化前对加密内容进行统一解密，适用于标注了
 * {@link DecryptRequestBody} 的方法参数。
 * </p>
 * <p>
 * 适用范围：
 * <ul>
 *     <li>仅适用于 Servlet Web 应用。</li>
 *     <li>类路径需存在 {@link Servlet}、{@link DispatcherServlet}、{@link RSAKeyPair}。</li>
 *     <li>方法参数标注 {@link DecryptRequestBody} 才会生效。</li>
 * </ul>
 * </p>
 * <p>
 * 行为说明：
 * <ul>
 *     <li>在反序列化前执行解密，使后续反序列化接收明文。</li>
 *     <li>请求体类型支持：{@code String}、JSON 字符串（空体保留为空或替换为空 JSON）。</li>
 *     <li>消息转换器支持：{@link MappingJackson2HttpMessageConverter}、{@link AbstractJsonHttpMessageConverter}、{@link StringHttpMessageConverter}。</li>
 * </ul>
 * </p>
 * <p>
 * 异常说明：
 * <ul>
 *     <li>{@link ServerException}：密钥为空或格式无效、请求体读取异常。</li>
 *     <li>{@link ServiceException}：解密失败或十六进制解码失败。</li>
 * </ul>
 * </p>
 * <p>
 * 执行顺序：
 * <ul>
 *     <li>优先级为 {@link Ordered#HIGHEST_PRECEDENCE} + 2。</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see DecryptRequestBody
 * @see RequestBodyAdvice
 * @see CryptoUtils
 * @see RestControllerAdvice
 * @since 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, RSAKeyPair.class})
@ConditionalOnBean(CryptoFactory.class)
@RestControllerAdvice
public class RequestBodyDecryptAdvice implements RequestBodyAdvice {
	/**
	 * 判断是否支持当前消息转换器并且存在解密注解
	 * <p>
	 * 返回 {@code true} 当且仅当：
	 * <ul>
	 *     <li>消息转换器属于以下任一类型：{@link MappingJackson2HttpMessageConverter}、{@link AbstractJsonHttpMessageConverter}、{@link StringHttpMessageConverter}。</li>
	 *     <li>方法参数标注了 {@link DecryptRequestBody} 注解。</li>
	 * </ul>
	 * </p>
	 *
	 * @param methodParameter 当前处理的方法参数
	 * @param targetType      目标类型
	 * @param converterType   当前使用的HTTP消息转换器类型
	 * @return 支持则返回 {@code true}，否则返回 {@code false}
	 */
	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
							Class<? extends HttpMessageConverter<?>> converterType) {
		if (MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType) ||
			AbstractJsonHttpMessageConverter.class.isAssignableFrom(converterType) ||
			StringHttpMessageConverter.class.isAssignableFrom(converterType)) {
			return Objects.nonNull(methodParameter.getParameterAnnotation(DecryptRequestBody.class));
		}
		return false;
	}

	/**
	 * 在请求体读取与反序列化前执行解密
	 * <p>
	 * 处理步骤：
	 * <ul>
	 *     <li>字符串消息转换器（{@link StringHttpMessageConverter}）直接放行，改由 {@link #afterBodyRead(Object, HttpInputMessage, MethodParameter, Type, Class)} 处理。</li>
	 *     <li>获取注解与解析密钥（失败抛 {@link ServerException}）。</li>
	 *     <li>读取原始请求体并按注解编码解密为明文字符串。</li>
	 *     <li>将明文字节封装为新的 {@link MappingJacksonInputMessage}，供后续 JSON 反序列化。</li>
	 * </ul>
	 * </p>
	 *
	 * @param inputMessage  原始HTTP输入消息
	 * @param parameter     当前处理的方法参数
	 * @param targetType    目标类型
	 * @param converterType 当前使用的HTTP消息转换器类型
	 * @return 包含解密明文的新输入消息；不处理字符串转换器
	 * @throws ServerException  密钥无效或请求体读取失败
	 * @throws ServiceException 解密或十六进制解码失败
	 */
	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
										   Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
		if (StringHttpMessageConverter.class.isAssignableFrom(converterType)) {
			return inputMessage;
		}

		DecryptRequestBody annotation = parameter.getParameterAnnotation(DecryptRequestBody.class);
		String key;
		try {
			key = CryptoUtils.getKey(annotation.key());
		} catch (IllegalArgumentException e) {
			throw new ServerException(e);
		}
		CryptoFactory factory = getCryptoFactory(annotation);

		try (InputStream inputStream = inputMessage.getBody()) {
			byte[] requestBody;
			String rawRequestBodyStr = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			if (StringUtils.isBlank(rawRequestBodyStr)) {
				requestBody = Constants.EMPTY_JSON_OBJECT_STR.getBytes();
			} else {
				requestBody = CryptoUtils.decryptString(factory, rawRequestBodyStr, key, annotation.encoding()).getBytes();
			}
			return new MappingJacksonInputMessage(new ByteArrayInputStream(requestBody), inputMessage.getHeaders());
		} catch (EncryptionOperationNotPossibleException e) {
			throw new ServiceException("无效的加密请求数据", "请求数据对象解密失败", e);
		} catch (DecoderException e) {
			throw new ServiceException("无效的加密请求数据", "请求数据对象十六进制解码失败", e);
		} catch (IllegalArgumentException e) {
			throw new ServerException("无效的密钥", e);
		}
	}

	/**
	 * 在反序列化后处理原始值（字符串转换器场景）
	 * <p>
	 * 当消息转换器为 {@link StringHttpMessageConverter} 时，尝试对字符串正文按注解配置进行解密；
	 * 其他转换器保持原值不变。
	 * </p>
	 *
	 * @param body          反序列化后的对象
	 * @param inputMessage  HTTP输入消息
	 * @param parameter     当前处理的方法参数
	 * @param targetType    目标类型
	 * @param converterType 当前使用的HTTP消息转换器类型
	 * @return 解密后的字符串或原值
	 */
	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
								Class<? extends HttpMessageConverter<?>> converterType) {
		if (!StringHttpMessageConverter.class.isAssignableFrom(converterType)) {
			return body;
		}

		DecryptRequestBody annotation = parameter.getParameterAnnotation(DecryptRequestBody.class);
		String key;
		try {
			key = CryptoUtils.getKey(annotation.key());
		} catch (IllegalArgumentException e) {
			throw new ServerException(e);
		}
		CryptoFactory factory = getCryptoFactory(annotation);

		try {
			if (body instanceof String string) {
				if (StringUtils.isBlank(string)) {
					return body;
				}
				return CryptoUtils.decryptString(factory, string, key, annotation.encoding());
			} else {
				return body;
			}
		} catch (EncryptionOperationNotPossibleException e) {
			throw new ServiceException("无效的加密请求数据", "请求数据对象解密失败", e);
		} catch (DecoderException e) {
			throw new ServiceException("无效的加密请求数据", "请求数据对象十六进制解码失败", e);
		} catch (IllegalArgumentException e) {
			throw new ServerException("无效的密钥", e);
		}
	}

	/**
	 * 处理空请求体：保持原样返回
	 *
	 * @param body          空请求体对象
	 * @param inputMessage  HTTP输入消息
	 * @param parameter     当前处理的方法参数
	 * @param targetType    目标类型
	 * @param converterType 当前使用的HTTP消息转换器类型
	 * @return 原值
	 */
	@Override
	public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
								  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}

	// todo 改成list注入
	protected CryptoFactory getCryptoFactory(DecryptRequestBody annotation) {
		if (ArrayUtils.isNotEmpty(annotation.factory())) {
			return StaticSpringContext.getBeanFactory().getBean(annotation.factory()[0]);
		} else {
			return annotation.algorithm().getFactory();
		}
	}
}
