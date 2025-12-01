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

package io.github.pangju666.framework.boot.autoconfigure.web.advice.wrapper;

import io.github.pangju666.framework.boot.web.annotation.UnwrappedResponse;
import io.github.pangju666.framework.web.model.Result;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.AbstractJsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

/**
 * 响应体统一包装通知
 * <p>
 * 基于{@link RestControllerAdvice}与{@link ResponseBodyAdvice}，对未被排除的方法返回值进行统一包装，
 * 输出一致的API响应结构。
 * </p>
 * <p>
 * 启用与排除：
 * <ul>
 *   <li>仅在Servlet类型Web应用，且Classpath存在{@link Servlet}与{@link DispatcherServlet}时启用</li>
 *   <li>启用条件：配置项{@code pangju.web.advice.enable-wrapper=true}（默认启用）</li>
 *   <li>排除：返回类型为{@link ResponseEntity}或{@link Result}、方法标注{@link UnwrappedResponse}</li>
 * </ul>
 * </p>
 * <p>
 * 支持的消息转换器：
 * <ul>
 *   <li>{@link MappingJackson2HttpMessageConverter}</li>
 *   <li>{@link AbstractJsonHttpMessageConverter}</li>
 *   <li>{@link StringHttpMessageConverter}</li>
 * </ul>
 * </p>
 * <p>
 * 执行顺序：
 * <ul>
 *     <li>优先级为 {@link Ordered#HIGHEST_PRECEDENCE} + 1。</li>
 * </ul>
 * </p>
 * <p>
 * 包装规则：
 * <ul>
 *   <li>String：设置{@code Content-Type}为{@code application/json}，返回{@code Result.ok(body).toString()}内容</li>
 *   <li>其他类型：包装为{@code Result.ok(body)}</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see UnwrappedResponse
 * @see ResponseBodyAdvice
 * @see Result
 * @since 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE  + 1)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, Result.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "enable-wrapper", matchIfMissing = true)
@RestControllerAdvice
public class ResponseBodyWrapperAdvice implements ResponseBodyAdvice<Object> {
	/**
	 * 判断是否对当前方法返回值启用统一包装
	 * <p>
	 * 规则：
	 * </p>
	 * <ul>
	 *   <li>支持的转换器：{@link MappingJackson2HttpMessageConverter}、{@link AbstractJsonHttpMessageConverter}、{@link StringHttpMessageConverter}</li>
	 *   <li>排除条件：返回类型为{@link ResponseEntity}或{@link Result}，或方法标注{@link UnwrappedResponse}</li>
	 * </ul>
	 * <p>
	 * 满足支持且未触发排除时返回true，否则返回false。
	 * </p>
	 *
	 * @param returnType    当前处理的方法返回类型参数
	 * @param converterType 当前HTTP消息转换器类型
	 * @return 支持则返回true，否则返回false
	 */
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		if (ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType)) {
			return false;
		}
		if (MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType) ||
			AbstractJsonHttpMessageConverter.class.isAssignableFrom(converterType) ||
			StringHttpMessageConverter.class.isAssignableFrom(converterType)) {
			return !ResponseEntity.class.equals(returnType.getNestedParameterType()) &&
				!Result.class.equals(returnType.getNestedParameterType()) &&
				Objects.isNull(returnType.getMethodAnnotation(UnwrappedResponse.class));
		}
		return false;
	}

	/**
	 * 写出前按规则包装响应体
	 * <p>
	 * 根据响应体类型与所选消息转换器应用不同策略：
	 * </p>
	 * <ul>
	 *   <li>String：设置Content-Type为application/json，返回包装后字符串</li>
	 *   <li>Result：直接返回</li>
	 *   <li>其他类型：统一包装为{@link Result#ok(Object)}</li>
	 * </ul>
	 *
	 * @param body                  原始响应体，可能为null
	 * @param returnType            方法返回类型参数
	 * @param selectedContentType   选中的内容类型
	 * @param selectedConverterType 选中的HTTP消息转换器类型
	 * @param request               当前HTTP请求
	 * @param response              当前HTTP响应
	 * @return 包装后的响应体对象
	 */
	@Override
	public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
								  Class<? extends HttpMessageConverter<?>> selectedConverterType,
								  ServerHttpRequest request, ServerHttpResponse response) {
		if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			return Result.ok(body).toString();
		} else {
			return Result.ok(body);
		}
	}
}
