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

import io.github.pangju666.framework.boot.web.annotation.EnumRequestParam;
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

/**
 * 枚举类型请求参数参数解析器
 * <p>
 * 该解析器用于处理Spring MVC中使用{@link EnumRequestParam}注解标记的枚举类型请求参数。
 * 支持从HTTP请求中提取字符串值，并将其转换为对应的枚举实例。
 * </p>
 * <p>
 * 主要功能包括：
 * <ul>
 *     <li>识别使用{@link EnumRequestParam}注解且参数类型为枚举的方法参数</li>
 *     <li>从请求中获取参数值，支持自定义参数名称</li>
 *     <li>支持默认值配置，当请求参数缺失时使用</li>
 *     <li>支持可选参数配置，当参数缺失且未配置默认值时可返回null</li>
 *     <li>进行不区分大小写的枚举值匹配</li>
 *     <li>验证枚举值有效性，无效值抛出验证异常</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see EnumRequestParam
 * @see HandlerMethodArgumentResolver
 * @see EnumUtils
 * @since 1.0.0
 */
public class EnumRequestParamArgumentResolver implements HandlerMethodArgumentResolver {
	/**
	 * 检查该解析器是否支持处理给定的方法参数
	 * <p>
	 * 当方法参数满足以下条件时返回true：
	 * <ul>
	 *     <li>参数被{@link EnumRequestParam}注解标记</li>
	 *     <li>参数类型是枚举类</li>
	 * </ul>
	 * </p>
	 *
	 * @param parameter 要检查的方法参数
	 * @return 如果该解析器支持处理该参数则返回true，否则返回false
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(EnumRequestParam.class) && parameter.getParameterType().isEnum();
	}

	/**
	 * 从HTTP请求中解析枚举类型参数
	 * <p>
	 * 解析流程如下：
	 * <ol>
	 *     <li>获取{@link EnumRequestParam}注解信息</li>
	 *     <li>确定参数名称（优先使用注解指定的名称，否则使用参数名）</li>
	 *     <li>从请求中获取参数值</li>
	 *     <li>若参数值为空，则按以下优先级处理：
	 *         <ul>
	 *             <li>若配置了默认值，使用默认值</li>
	 *             <li>若标记为必需参数，抛出{@link MissingServletRequestParameterException}</li>
	 *             <li>若为可选参数，返回null</li>
	 *         </ul>
	 *     </li>
	 *     <li>使用{@link EnumUtils#getEnumIgnoreCase}进行不区分大小写的枚举值查找</li>
	 *     <li>若找不到匹配的枚举值，抛出{@link ValidationException}</li>
	 *     <li>将解析后的枚举值添加到ModelAndViewContainer中</li>
	 *     <li>返回解析后的枚举实例</li>
	 * </ol>
	 * </p>
	 *
	 * @param parameter     要解析的方法参数
	 * @param mavContainer  模型和视图容器，用于存储解析后的属性
	 * @param webRequest    当前HTTP请求对象
	 * @param binderFactory 数据绑定工厂（此解析器未使用）
	 * @return 解析后的枚举实例，若参数为可选且缺失则返回null
	 * @throws MissingServletRequestParameterException 当必需参数缺失且未配置默认值时抛出
	 * @throws ValidationException                     当请求参数值无法转换为有效的枚举值时抛出
	 * @throws Exception                               其他处理过程中发生的异常
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
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
					throw new MissingServletRequestParameterException(parameterName, enumClass.getSimpleName());
				}
				return null;
			}
		}

		Enum<?> enumValue = EnumUtils.getEnumIgnoreCase(enumClass, enumName);
		if (Objects.isNull(enumValue)) {
			throw new ValidationException("无效的" + annotation.description());
		}
		return enumValue;
	}
}