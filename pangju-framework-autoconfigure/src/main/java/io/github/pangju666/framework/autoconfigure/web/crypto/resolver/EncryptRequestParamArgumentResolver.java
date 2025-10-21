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

package io.github.pangju666.framework.autoconfigure.web.crypto.resolver;

import io.github.pangju666.framework.autoconfigure.spring.StaticSpringContext;
import io.github.pangju666.framework.autoconfigure.utils.CryptoUtils;
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

/**
 * 加密请求参数解析器
 * <p>
 * 该解析器用于处理Spring MVC中使用{@link EncryptRequestParam}注解标记的加密字符串类型请求参数。
 * 支持从HTTP请求中提取加密的参数值，并根据注解配置的算法和密钥对其进行解密。
 * </p>
 * <p>
 * 主要功能包括：
 * <ul>
 *     <li>识别使用{@link EncryptRequestParam}注解且参数类型为String的方法参数</li>
 *     <li>从请求中获取加密的参数值，支持自定义参数名称</li>
 *     <li>支持多种加密算法（AES256、RSA等）和编码方式（Base64、十六进制）</li>
 *     <li>支持密钥的动态获取和配置</li>
 *     <li>支持默认值配置，当请求参数缺失时使用</li>
 *     <li>支持可选参数配置，当参数缺失时可返回默认值或抛出异常</li>
 *     <li>对加密参数进行解密处理</li>
 *     <li>提供详细的错误信息用于调试</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * @GetMapping("/data")
 * public ResponseEntity<?> getData(
 *     @EncryptRequestParam(
 *         value = "encryptedData",
 *         key = "app.crypto.key",
 *         algorithm = Algorithm.AES256,
 *         encoding = Encoding.BASE64
 *     ) String decryptedData
 * ) {
 *     // decryptedData 参数将被自动解密
 *     return ResponseEntity.ok(decryptedData);
 * }
 * }
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see EncryptRequestParam
 * @see HandlerMethodArgumentResolver
 * @see CryptoUtils
 * @since 1.0.0
 */
public class EncryptRequestParamArgumentResolver implements HandlerMethodArgumentResolver {
	/**
	 * 检查该解析器是否支持处理给定的方法参数
	 * <p>
	 * 当方法参数满足以下条件时返回true：
	 * <ul>
	 *     <li>参数被{@link EncryptRequestParam}注解标记</li>
	 *     <li>参数类型为String</li>
	 * </ul>
	 * </p>
	 *
	 * @param parameter 要检查的方法参数
	 * @return 如果该解析器支持处理该参数则返回true，否则返回false
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(EncryptRequestParam.class) &&
			parameter.getParameterType().isAssignableFrom(String.class);
	}

	/**
	 * 从HTTP请求中解析加密的字符串参数
	 * <p>
	 * 解析流程如下：
	 * <ol>
	 *     <li>获取{@link EncryptRequestParam}注解信息</li>
	 *     <li>确定参数名称（优先使用注解指定的名称，否则使用参数名）</li>
	 *     <li>从请求中获取参数值</li>
	 *     <li>若参数值为空，则按以下优先级处理：
	 *         <ul>
	 *             <li>若标记为必需参数，抛出{@link MissingServletRequestParameterException}</li>
	 *             <li>若为可选参数，返回默认值</li>
	 *         </ul>
	 *     </li>
	 *     <li>获取解密密钥：
	 *         <ul>
	 *             <li>若加密算法需要密钥（RSA、AES256），从配置中获取密钥</li>
	 *             <li>若密钥配置为空或未找到，抛出{@link ServerException}</li>
	 *         </ul>
	 *     </li>
	 *     <li>使用{@link CryptoUtils#decryptToString}根据注解配置的算法和编码方式进行解密</li>
	 *     <li>解密异常处理：
	 *         <ul>
	 *             <li>加密操作异常 - 抛出ServiceException提示解密失败</li>
	 *             <li>十六进制解码异常 - 抛出ServiceException提示十六进制解码失败</li>
	 *         </ul>
	 *     </li>
	 *     <li>将解密后的参数值添加到ModelAndViewContainer中</li>
	 *     <li>返回解密后的字符串</li>
	 * </ol>
	 * </p>
	 *
	 * @param parameter        要解析的方法参数
	 * @param mavContainer     模型和视图容器，用于存储解析后的属性
	 * @param webRequest       当前HTTP请求对象
	 * @param binderFactory    数据绑定工厂（此解析器未使用）
	 * @return 解密后的字符串，若参数为可选且缺失则返回默认值
	 * @throws MissingServletRequestParameterException 当必需参数缺失时抛出
	 * @throws ServerException 当密钥配置无效或不存在时抛出
	 * @throws ServiceException 当解密或解码过程中发生错误时抛出
	 * @throws Exception 其他处理过程中发生的异常
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
