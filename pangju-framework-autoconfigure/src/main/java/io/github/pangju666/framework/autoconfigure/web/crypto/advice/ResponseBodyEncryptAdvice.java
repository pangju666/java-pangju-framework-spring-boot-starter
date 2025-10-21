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
import io.github.pangju666.framework.autoconfigure.enums.Algorithm;
import io.github.pangju666.framework.autoconfigure.enums.Encoding;
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

/**
 * 响应体加密通知类
 * <p>
 * 该类实现Spring MVC的{@link ResponseBodyAdvice}接口，用于在HTTP响应体被序列化为JSON之后，
 * 自动对其进行加密处理。它作为全局的{@link RestControllerAdvice}，对所有使用
 * {@link EncryptResponseBody}注解标记的控制器方法进行拦截和加密。
 * </p>
 * <p>
 * 工作流程：
 * <ol>
 *     <li>控制器方法返回响应对象</li>
 *     <li>Spring MVC框架识别方法或类上的{@link EncryptResponseBody}注解</li>
 *     <li>该通知类的{@link #supports}方法检查是否使用了支持的HTTP消息转换器</li>
 *     <li>如果支持，调用{@link #beforeBodyWrite}方法进行加密处理</li>
 *     <li>加密后的密文被序列化为JSON传递给客户端</li>
 * </ol>
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>拦截HTTP响应体在序列化后的阶段</li>
 *     <li>获取{@link EncryptResponseBody}注解的配置信息（支持方法级和类级注解）</li>
 *     <li>根据配置的算法和密钥对响应体进行加密</li>
 *     <li>支持多种响应类型（String、byte[]、{@link Result}、JSON对象等）</li>
 *     <li>支持多种加密算法（AES256、RSA等）和编码方式（Base64、十六进制）</li>
 *     <li>支持多种HTTP消息转换器（Jackson、Gson、字节数组、字符串等）</li>
 *     <li>处理加密过程中的各类异常</li>
 *     <li>提供详细的错误信息便于问题排查</li>
 * </ul>
 * </p>
 * <p>
 * 支持的响应类型：
 * <ul>
 *     <li>String - 直接加密字符串内容</li>
 *     <li>byte[] - 直接加密字节数组内容</li>
 *     <li>{@link Result} - 加密Result对象中的data字段，保留code和msg</li>
 *     <li>其他JSON对象 - 先序列化为JSON字符串再加密</li>
 * </ul>
 * </p>
 * <p>
 * 支持的HTTP消息转换器：
 * <ul>
 *     <li>MappingJackson2HttpMessageConverter - Jackson JSON转换器</li>
 *     <li>GsonHttpMessageConverter - Gson JSON转换器</li>
 *     <li>ByteArrayHttpMessageConverter - 字节数组转换器</li>
 *     <li>StringHttpMessageConverter - 字符串转换器</li>
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
 * // 方法级注解示例
 * @GetMapping("/secure-data")
 * @EncryptResponseBody(
 *     key = "app.encryption.key",
 *     algorithm = Algorithm.AES256,
 *     encoding = Encoding.BASE64
 * )
 * public Result<UserResponse> getSecureData() {
 *     return Result.ok(new UserResponse("John", "john@example.com"));
 * }
 *
 * // 类级注解示例 - 作用于类中的所有方法
 * @RestController
 * @EncryptResponseBody(key = "app.encryption.key")
 * public class SecureController {
 *     @GetMapping("/data")
 *     public Result<String> getData() {
 *         return Result.ok("sensitive data");
 *     }
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
 *     <li>{@link ServerException} - 密钥配置无效、密钥不存在、加密失败或密钥格式错误时抛出</li>
 * </ul>
 * </p>
 * <p>
 * 执行顺序：
 * <p>
 * 该通知的执行顺序为{@link Ordered#LOWEST_PRECEDENCE} - 1，确保它在其他ResponseBodyAdvice之后执行，
 * 以保证其他通知已完成对响应体的处理。
 * </p>
 * </p>
 * <p>
 * 生效条件：
 * <ul>
 *     <li>仅在Servlet Web应用环境中生效</li>
 *     <li>要求Servlet和DispatcherServlet类在类路径中可用</li>
 *     <li>方法或类必须标注{@link EncryptResponseBody}注解</li>
 *     <li>HTTP消息转换器必须是支持的类型之一</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *     <li>方法级注解优先于类级注解</li>
 *     <li>空响应体将不进行加密处理</li>
 *     <li>Result对象中的code和msg字段不进行加密，只加密data字段</li>
 *     <li>建议将密钥等敏感信息存储在外部配置中，而不是硬编码</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see EncryptResponseBody
 * @see ResponseBodyAdvice
 * @see CryptoUtils
 * @see RestControllerAdvice
 * @see Result
 * @since 1.0.0
 */
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@RestControllerAdvice
public class ResponseBodyEncryptAdvice implements ResponseBodyAdvice<Object> {
	/**
	 * 检查该通知是否支持处理当前的HTTP消息转换器
	 * <p>
	 * 当HTTP消息转换器是以下类型之一时返回true：
	 * <ul>
	 *     <li>MappingJackson2HttpMessageConverter - Jackson JSON转换器</li>
	 *     <li>GsonHttpMessageConverter - Gson JSON转换器</li>
	 *     <li>ByteArrayHttpMessageConverter - 字节数组转换器</li>
	 *     <li>StringHttpMessageConverter - 字符串转换器</li>
	 * </ul>
	 * 这确保了支持多种常见的内容类型加密。
	 * </p>
	 *
	 * @param returnType    当前处理的方法返回类型参数
	 * @param converterType 当前使用的HTTP消息转换器类型
	 * @return 如果转换器是支持的类型之一则返回true，否则返回false
	 */
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType) ||
			GsonHttpMessageConverter.class.isAssignableFrom(converterType) ||
			ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType) ||
			StringHttpMessageConverter.class.isAssignableFrom(converterType);
	}

	/**
	 * 在响应体被序列化之前进行加密处理
	 * <p>
	 * 该方法在Spring MVC框架序列化响应体之前被调用。处理流程如下：
	 * <ol>
	 *     <li>检查方法或类是否标注了{@link EncryptResponseBody}注解，如果没有则直接返回原始响应</li>
	 *     <li>获取注解中配置的密钥名称，从Spring配置中读取实际的密钥值</li>
	 *     <li>若加密算法需要密钥但密钥配置为空或未找到，抛出{@link ServerException}</li>
	 *     <li>根据响应体的类型采用不同的加密策略：
	 *         <ul>
	 *             <li>String类型 - 直接使用{@link CryptoUtils#encryptToString}加密</li>
	 *             <li>byte[]类型 - 直接使用{@link CryptoUtils#encrypt(byte[], String, Algorithm, Encoding)}加密</li>
	 *             <li>{@link Result}类型 - 加密其data字段，返回新的Result对象</li>
	 *             <li>其他JSON对象 - 先序列化为JSON字符串再加密</li>
	 *         </ul>
	 *     </li>
	 *     <li>异常处理：
	 *         <ul>
	 *             <li>加密操作异常 - 抛出ServerException提示加密失败</li>
	 *             <li>密钥格式异常 - 抛出ServerException提示密钥无效</li>
	 *         </ul>
	 *     </li>
	 *     <li>返回加密后的响应体供后续序列化使用</li>
	 * </ol>
	 * </p>
	 * <p>
	 * 特殊处理说明：
	 * <ul>
	 *     <li>空响应体不进行加密，直接返回</li>
	 *     <li>对于Result对象，仅加密data字段，code和msg保持不变</li>
	 *     <li>对于Result中data为String或byte[]类型，进行直接加密</li>
	 *     <li>对于Result中data为其他JSON对象，先序列化再加密</li>
	 * </ul>
	 * </p>
	 *
	 * @param body                 原始的响应体对象
	 * @param returnType           当前处理的方法返回类型参数
	 * @param selectedContentType  选中的内容类型
	 * @param selectedConverterType 选中的HTTP消息转换器类型
	 * @param request              当前HTTP请求
	 * @param response             当前HTTP响应
	 * @return 加密后的响应体，若无需加密则返回原始响应
	 * @throws ServerException 当密钥配置无效、加密失败或密钥格式错误时抛出
	 */
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