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

package io.github.pangju666.framework.boot.autoconfigure.web.crypto.advice;

import io.github.pangju666.framework.boot.autoconfigure.spring.StaticSpringContext;
import io.github.pangju666.framework.boot.enums.Algorithm;
import io.github.pangju666.framework.boot.enums.Encoding;
import io.github.pangju666.framework.boot.utils.CryptoUtils;
import io.github.pangju666.framework.boot.web.crypto.advice.DecryptRequestBody;
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
import java.security.interfaces.RSAKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

/**
 * 请求体解密通知类
 * <p>
 * 该类实现Spring MVC的{@link RequestBodyAdvice}接口，用于在HTTP请求体被反序列化为Java对象之前，
 * 自动对其进行解密处理。它作为全局的{@link RestControllerAdvice}，对所有使用
 * {@link DecryptRequestBody}注解标记的请求参数进行拦截和解密。
 * </p>
 * <p>
 * 工作流程：
 * <ol>
 *     <li>Spring MVC框架在处理请求时，识别方法参数上的{@link DecryptRequestBody}注解</li>
 *     <li>该通知类的{@link #supports}方法检查是否使用了MappingJackson2HttpMessageConverter</li>
 *     <li>如果支持，调用{@link #beforeBodyRead}方法进行解密处理</li>
 *     <li>解密后的明文请求体被传递给标准的JSON反序列化器</li>
 *     <li>最终反序列化后的对象注入到控制器方法中</li>
 * </ol>
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>拦截HTTP请求体在反序列化前的阶段</li>
 *     <li>获取{@link DecryptRequestBody}注解的配置信息</li>
 *     <li>根据配置的算法和密钥对请求体进行解密</li>
 *     <li>支持多种加密算法（AES256、RSA等）和编码方式（Base64、十六进制）</li>
 *     <li>处理解密过程中的各类异常</li>
 *     <li>提供详细的错误信息便于问题排查</li>
 * </ul>
 * </p>
 * <p>
 * 支持的加密算法和编码方式：
 * <ul>
 *     <li>{@link Algorithm#AES256} + {@link Encoding#BASE64} - AES-256加密+Base64编码（推荐）</li>
 *     <li>{@link Algorithm#AES256} + {@link Encoding#HEX} - AES-256加密+十六进制编码</li>
 *     <li>{@link Algorithm#RSA} + {@link Encoding#BASE64} - RSA非对称加密+Base64编码</li>
 *     <li>{@link Algorithm#RSA} + {@link Encoding#HEX} - RSA非对称加密+十六进制编码</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * @PostMapping("/secure-data")
 * public ResponseEntity<?> createSecureData(
 *     @DecryptRequestBody(
 *         key = "app.encryption.key",
 *         algorithm = Algorithm.AES256,
 *         encoding = Encoding.BASE64
 *     ) UserRequest request
 * ) {
 *     // request 对象包含解密后的明文数据
 *     return ResponseEntity.ok(request);
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * {@code
 * # application.yml
 * app:
 *   encryption:
 *     key: your-secret-key-here
 * }
 * </pre>
 * </p>
 * <p>
 * 异常处理：
 * <ul>
 *     <li>{@link ServerException} - 密钥配置无效、密钥不存在、请求体读取失败、密钥格式错误时抛出</li>
 *     <li>{@link ServiceException} - 请求数据解密失败或十六进制解码失败时抛出</li>
 * </ul>
 * </p>
 * <p>
 * 生效条件：
 * <ul>
 *     <li>仅在Servlet Web应用环境中生效</li>
 *     <li>要求Servlet和DispatcherServlet类在类路径中可用</li>
 *     <li>方法参数必须标注{@link DecryptRequestBody}注解</li>
 *     <li>HTTP消息转换器必须是MappingJackson2HttpMessageConverter</li>
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
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, RSAKey.class})
@RestControllerAdvice
public class RequestBodyDecryptAdvice implements RequestBodyAdvice {
	/**
	 * 检查该通知是否支持处理当前的HTTP消息转换器
	 * <p>
	 * 仅当HTTP消息转换器是{@link MappingJackson2HttpMessageConverter}的实例或其子类时，
	 * 该通知才会进行解密处理。这确保了只有JSON格式的请求体才会被解密。
	 * </p>
	 *
	 * @param methodParameter 当前处理的方法参数
	 * @param targetType      目标类型
	 * @param converterType   当前使用的HTTP消息转换器类型
	 * @return 如果转换器是MappingJackson2HttpMessageConverter则返回true，否则返回false
	 */
	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
							Class<? extends HttpMessageConverter<?>> converterType) {
		return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
	}

	/**
	 * 在请求体被读取和反序列化之前进行解密处理
	 * <p>
	 * 该方法在Spring MVC框架反序列化请求体之前被调用。处理流程如下：
	 * <ol>
	 *     <li>检查方法参数是否标注了{@link DecryptRequestBody}注解，如果没有则直接返回原始消息</li>
	 *     <li>获取注解中配置的密钥名称，从Spring配置中读取实际的密钥值</li>
	 *     <li>若加密算法需要密钥但密钥配置为空或未找到，抛出{@link ServerException}</li>
	 *     <li>读取HTTP请求体的字节内容，并转换为字符串</li>
	 *     <li>若请求体为空，直接返回原始消息</li>
	 *     <li>使用{@link CryptoUtils#decrypt}方法根据注解配置的算法、编码方式和密钥进行解密</li>
	 *     <li>将解密后的明文字节内容包装成新的{@link MappingJacksonInputMessage}对象</li>
	 *     <li>返回新的消息对象供后续的反序列化使用</li>
	 * </ol>
	 * </p>
	 * <p>
	 * 异常处理：
	 * <ul>
	 *     <li>读取请求体失败 - 抛出{@link ServerException}</li>
	 *     <li>加密操作异常 - 抛出{@link ServiceException}提示解密失败</li>
	 *     <li>十六进制解码异常 - 抛出{@link ServiceException}提示十六进制解码失败</li>
	 *     <li>密钥格式异常 - 抛出{@link ServerException}提示密钥无效</li>
	 * </ul>
	 * </p>
	 *
	 * @param inputMessage 原始的HTTP输入消息
	 * @param parameter    当前处理的方法参数
	 * @param targetType   目标类型
	 * @param converterType 当前使用的HTTP消息转换器类型
	 * @return 包含解密后明文的新HTTP输入消息，若无需解密则返回原始消息
	 * @throws ServerException 当密钥配置无效、读取请求体失败或密钥格式错误时抛出
	 * @throws ServiceException 当解密或解码过程中发生错误时抛出
	 */
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

	/**
	 * 在请求体被反序列化为Java对象之后的处理方法
	 * <p>
	 * 该实现不对反序列化后的对象进行任何修改，直接返回原值。
	 * </p>
	 *
	 * @param body         反序列化后的Java对象
	 * @param inputMessage HTTP输入消息
	 * @param parameter    当前处理的方法参数
	 * @param targetType   目标类型
	 * @param converterType 当前使用的HTTP消息转换器类型
	 * @return 原始的反序列化对象，无修改
	 */
	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
								Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}

	/**
	 * 处理空请求体的情况
	 * <p>
	 * 当HTTP请求体为空时，该方法被调用以提供默认处理。该实现不对空体进行任何修改，直接返回。
	 * </p>
	 *
	 * @param body         空的请求体对象
	 * @param inputMessage HTTP输入消息
	 * @param parameter    当前处理的方法参数
	 * @param targetType   目标类型
	 * @param converterType 当前使用的HTTP消息转换器类型
	 * @return 原始的空体对象，无修改
	 */
	@Override
	public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
								  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}
}
