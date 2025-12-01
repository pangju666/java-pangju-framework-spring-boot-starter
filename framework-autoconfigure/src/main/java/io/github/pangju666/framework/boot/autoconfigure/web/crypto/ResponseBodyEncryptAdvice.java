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
import io.github.pangju666.commons.lang.utils.JsonUtils;
import io.github.pangju666.framework.boot.crypto.enums.Encoding;
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.crypto.utils.CryptoUtils;
import io.github.pangju666.framework.boot.web.annotation.EncryptResponseBody;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.model.Result;
import jakarta.servlet.Servlet;
import org.apache.commons.lang3.ArrayUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.AbstractJsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 响应体加密全局通知
 * <p>
 * 以统一的加密策略在响应体序列化前对内容进行加密，适用于标注了
 * {@link EncryptResponseBody} 的控制器方法或类。
 * </p>
 * <p>
 * 适用范围：
 * <ul>
 *     <li>仅适用于 Servlet Web 应用。</li>
 *     <li>类路径需存在 {@link Servlet}、{@link DispatcherServlet}、{@link RSAKeyPair}。</li>
 *     <li>方法或类标注 {@link EncryptResponseBody} 才会生效（方法级优先）。</li>
 * </ul>
 * </p>
 * <p>
 * 行为说明：
 * <ul>
 *     <li>在响应体序列化前执行加密，避免在客户端看到明文。</li>
 *     <li>响应类型支持：{@code String}、{@code byte[]}、{@link Result}（仅加密 {@code data}）、其他对象（转 JSON 后加密）。</li>
 *     <li>消息转换器支持：{@link MappingJackson2HttpMessageConverter}、{@link AbstractJsonHttpMessageConverter}、{@link ByteArrayHttpMessageConverter}、{@link StringHttpMessageConverter}。</li>
 *     <li>方法级注解优先于类级注解，便于细粒度控制。</li>
 * </ul>
 * </p>
 * <p>
 * 异常说明：
 * <ul>
 *     <li>{@link ServerException}：加密失败或密钥格式无效时抛出（内部包裹 {@link EncryptionOperationNotPossibleException} 或 {@link InvalidKeySpecException}）。</li>
 * </ul>
 * </p>
 * <p>
 * 执行顺序：
 * <ul>
 *     <li>优先级为 {@link Ordered#HIGHEST_PRECEDENCE} + 2。</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *     <li>空响应体不加密；{@link Result} 的 {@code code} 与 {@code message} 不加密，仅加密 {@code data}。</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see EncryptResponseBody
 * @see ResponseBodyAdvice
 * @see CryptoFactory
 * @see CryptoUtils
 * @see RestControllerAdvice
 * @see Result
 * @since 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE  + 2)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, RSAKeyPair.class, Result.class})
@ConditionalOnBean(CryptoFactory.class)
@RestControllerAdvice
public class ResponseBodyEncryptAdvice implements ResponseBodyAdvice<Object> {
	private final Map<String, CryptoFactory> cryptoFactoryMap;

	public ResponseBodyEncryptAdvice(List<CryptoFactory> cryptoFactories) {
		this.cryptoFactoryMap = new HashMap<>(cryptoFactories.size());
		for (CryptoFactory cryptoFactory : cryptoFactories) {
			cryptoFactoryMap.put(cryptoFactory.getClass().getName(), cryptoFactory);
		}
	}

	/**
	 * 判断是否支持当前消息转换器并且存在加密注解
	 * <p>
	 * 返回 {@code true} 当且仅当：
	 * <ul>
	 *     <li>消息转换器属于以下任一类型：{@link MappingJackson2HttpMessageConverter}、{@link AbstractJsonHttpMessageConverter}、{@link ByteArrayHttpMessageConverter}、{@link StringHttpMessageConverter}。</li>
	 *     <li>方法或其声明类标注了 {@link EncryptResponseBody} 注解（方法级优先）。</li>
	 * </ul>
	 * </p>
	 *
	 * @param returnType    当前处理的方法返回类型参数
	 * @param converterType 当前使用的HTTP消息转换器类型
	 * @return 支持则返回 {@code true}，否则返回 {@code false}
	 */
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		if (MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType) ||
			AbstractJsonHttpMessageConverter.class.isAssignableFrom(converterType) ||
			ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType) ||
			StringHttpMessageConverter.class.isAssignableFrom(converterType)) {
			return Objects.nonNull(returnType.getMethodAnnotation(EncryptResponseBody.class)) ||
				Objects.nonNull(returnType.getDeclaringClass().getAnnotation(EncryptResponseBody.class));
		}
		return false;
	}

	/**
	 * 在响应体序列化前执行加密
	 * <p>
	 * 处理步骤：
	 * <ul>
	 *     <li>获取注解（方法级优先）。</li>
	 *     <li>解析密钥：支持明文或占位符（如 {@code ${app.encryption.key}}）。解析失败时抛出{@link ServerException}异常。</li>
	 *     <li>选择工厂：从注解中获取对应的加密工厂。</li>
	 *     <li>加密策略：
	 *         <ul>
	 *             <li>{@code String}：{@link CryptoUtils#encryptString(CryptoFactory, String, String, Encoding)}。</li>
	 *             <li>{@code byte[]}：{@link CryptoUtils#encrypt(CryptoFactory, byte[], String)}。</li>
	 *             <li>{@link Result}：仅加密 {@code data}。若 {@code data} 为 {@code String} 或 {@code byte[]} 按类型直接加密；否则转 JSON 后加密。</li>
	 *             <li>其他对象：先转 JSON 字符串，再执行加密。</li>
	 *         </ul>
	 *     </li>
	 *     <li>异常处理：加密失败或密钥无效时抛出 {@link ServerException}。</li>
	 * </ul>
	 * </p>
	 *
	 * @param body                  原始响应体
	 * @param returnType            当前方法返回类型参数
	 * @param selectedContentType   选中的内容类型
	 * @param selectedConverterType 选中的HTTP消息转换器类型
	 * @param request               当前HTTP请求
	 * @param response              当前HTTP响应
	 * @return 加密后的响应体；未加密时返回原始响应；密钥解析失败时返回 {@code null}
	 * @throws ServerException 加密失败或密钥格式无效
	 */
	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
								  Class<? extends HttpMessageConverter<?>> selectedConverterType,
								  ServerHttpRequest request, ServerHttpResponse response) {
		EncryptResponseBody annotation = returnType.getMethodAnnotation(EncryptResponseBody.class);
		if (Objects.isNull(annotation)) {
			annotation = returnType.getDeclaringClass().getAnnotation(EncryptResponseBody.class);
		}

		String key;
		try {
			key = CryptoUtils.getKey(annotation.key());
		} catch (IllegalArgumentException e) {
			throw new ServerException(e);
		}

		Class<? extends CryptoFactory> factoryClass;
		if (ArrayUtils.isNotEmpty(annotation.factory())) {
			factoryClass = annotation.factory()[0];
		} else {
			factoryClass = annotation.algorithm().getFactoryClass();
		}
		CryptoFactory cryptoFactory = cryptoFactoryMap.get(factoryClass.getName());
		if (Objects.isNull(cryptoFactory)) {
			throw new ServerException("未找到加密工厂：" + factoryClass.getSimpleName() + "，请检查是否已注册为 Spring Bean");
		}

		try {
			if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType) && body instanceof CharSequence charSequence) {
				return CryptoUtils.encryptString(cryptoFactory, charSequence.toString(), key, annotation.encoding());
			} else if (ByteArrayHttpMessageConverter.class.isAssignableFrom(selectedConverterType) && body instanceof byte[] bytes) {
				return CryptoUtils.encrypt(cryptoFactory, bytes, key);
			} else if (body instanceof Result<?> result) {
				if (Objects.isNull(result.data())) {
					return body;
				}
				if (result.data() instanceof CharSequence charSequence) {
					return new Result<>(result.code(), result.message(), CryptoUtils.encryptString(cryptoFactory,
						charSequence.toString(), key, annotation.encoding()));
				} else if (result.data() instanceof byte[] bytes) {
					return new Result<>(result.code(), result.message(), CryptoUtils.encrypt(cryptoFactory, bytes, key));
				} else {
					String data = JsonUtils.toString(result.data());
					return new Result<>(result.code(), result.message(), CryptoUtils.encryptString(cryptoFactory, data,
						key, annotation.encoding()));
				}
			} else {
				return Result.ok(CryptoUtils.encryptString(cryptoFactory, JsonUtils.toString(body), key,
					annotation.encoding()));
			}
		} catch (EncryptionOperationNotPossibleException e) {
			throw new ServerException("响应数据加密失败", e);
		} catch (IllegalArgumentException e) {
			throw new ServerException(e);
		}
	}
}