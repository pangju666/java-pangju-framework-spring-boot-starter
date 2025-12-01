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

package io.github.pangju666.framework.boot.web.resolver;

import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.crypto.utils.CryptoUtils;
import io.github.pangju666.framework.boot.web.annotation.EncryptRequestParam;
import io.github.pangju666.framework.boot.web.exception.RequestDataDecryptFailureException;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.exception.base.ServiceException;
import io.github.pangju666.framework.web.exception.base.ValidationException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 加密请求参数解析器。
 *
 * <p><strong>适用范围</strong></p>
 * <ul>
 *   <li>Spring MVC 方法参数解析阶段。</li>
 *   <li>仅处理标注了 {@link EncryptRequestParam} 且类型为 {@link String} 的参数。</li>
 * </ul>
 *
 * <p><strong>行为说明</strong></p>
 * <ul>
 *   <li>根据注解配置从 HTTP 请求中读取加密的字符串参数（名称由 {@link EncryptRequestParam#value()} 指定或回退到方法参数名）。</li>
 *   <li>按密钥、算法与编码进行解密后作为方法参数值返回；密钥支持明文或占位符（形如 {@code ${...}}，通过 {@link CryptoUtils#getKey(String)} 解析）。</li>
 *   <li>当 {@link EncryptRequestParam#required()} 为 false 且参数缺失时，返回 {@link EncryptRequestParam#defaultValue()}。</li>
 * </ul>
 *
 * <p><strong>异常说明</strong></p>
 * <ul>
 *   <li>缺少必需参数：抛出 {@link org.springframework.web.bind.MissingServletRequestParameterException}。</li>
 *   <li>密钥获取失败或配置无效：抛出 {@link ServerException}。</li>
 *   <li>解密失败：抛出 {@link RequestDataDecryptFailureException}。</li>
 *   <li>十六进制解码或格式错误：抛出 {@link ValidationException}。</li>
 * </ul>
 *
 * <p><strong>注意事项</strong></p>
 * <ul>
 *   <li>仅支持字符串类型参数；请确保客户端与服务端的算法与编码一致。</li>
 *   <li>建议将密钥置于外部配置并使用占位符引用，避免硬编码。</li>
 * </ul>
 *
 * @author pangju666
 * @see EncryptRequestParam
 * @see HandlerMethodArgumentResolver
 * @see CryptoUtils
 * @since 1.0.0
 */
public class EncryptRequestParamArgumentResolver implements HandlerMethodArgumentResolver {
	private final Map<String, CryptoFactory> cryptoFactoryMap;

	public EncryptRequestParamArgumentResolver(List<CryptoFactory> cryptoFactories) {
		this.cryptoFactoryMap = new HashMap<>(cryptoFactories.size());
		for (CryptoFactory cryptoFactory : cryptoFactories) {
			cryptoFactoryMap.put(cryptoFactory.getClass().getName(), cryptoFactory);
		}
	}

	/**
	 * 判断是否支持当前方法参数。
	 * <p>
	 * 同时满足：参数标注 {@link EncryptRequestParam} 且类型为 {@link String}。
	 * </p>
	 *
	 * @param parameter 方法参数
	 * @return 支持返回 true，否则返回 false
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(EncryptRequestParam.class) &&
			String.class.isAssignableFrom(parameter.getParameterType());
	}

	/**
	 * 解析并解密加密的字符串请求参数。
	 * <p>
	 * 步骤：读取参数名 → 获取参数值 → 处理缺失（必需抛异常/可选取默认值）→ 解析密钥与工厂 → 解密 → 写入模型并返回。
	 * </p>
	 *
	 * @param parameter     方法参数
	 * @param mavContainer  模型与视图容器
	 * @param webRequest    当前 HTTP 请求
	 * @param binderFactory 数据绑定工厂（未使用）
	 * @return 解密后的字符串；当可选且缺失时返回默认值
	 * @throws MissingServletRequestParameterException 缺少必需参数
	 * @throws ServerException                         密钥配置无效或获取失败
	 * @throws ServiceException                        解密或十六进制解码失败
	 * @throws Exception                               其他处理异常
	 */
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
								  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		EncryptRequestParam annotation = parameter.getParameterAnnotation(EncryptRequestParam.class);
		String parameterName = annotation.value();
		if (StringUtils.isBlank(parameterName)) {
			parameterName = parameter.getParameterName();
		}

		String parameterValue = webRequest.getParameter(parameterName);
		if (StringUtils.isBlank(parameterValue)) {
			if (annotation.required()) {
				throw new MissingServletRequestParameterException(parameterName, String.class.getSimpleName());
			}
			return annotation.defaultValue();
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
			parameterValue = CryptoUtils.decryptString(cryptoFactory, parameterValue, key, annotation.encoding());
		} catch (EncryptionOperationNotPossibleException e) {
			throw new RequestDataDecryptFailureException("加密请求参数：" + parameterName + " 无效，请勿手动修改请求内容", e);
		} catch (DecoderException e) {
			throw new ValidationException("加密请求参数：" + parameterName + " 格式错误，请勿手动修改请求内容");
		} catch (IllegalArgumentException e) {
			throw new ServerException("无效的密钥", e);
		}

		return parameterValue;
	}
}
