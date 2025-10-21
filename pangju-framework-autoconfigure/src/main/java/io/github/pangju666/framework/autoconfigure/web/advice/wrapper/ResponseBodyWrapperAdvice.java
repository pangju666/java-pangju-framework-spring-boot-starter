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

package io.github.pangju666.framework.autoconfigure.web.advice.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.pangju666.commons.lang.utils.JsonUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.model.common.Result;
import io.github.pangju666.framework.web.pool.WebConstants;
import jakarta.servlet.Servlet;
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
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

/**
 * 响应体包装处理器
 * <p>
 * 该类用于对Web应用的响应体进行统一包装和格式化处理。
 * 通过{@link RestControllerAdvice}和{@link ResponseBodyAdvice}机制，
 * 为标注了{@link ResponseBodyWrapper}注解的方法返回值自动包装成统一的响应格式。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>统一包装API响应格式</li>
 *     <li>支持多种序列化器（Jackson、Gson等）</li>
 *     <li>支持多种数据类型的包装</li>
 *     <li>提供灵活的包装排除机制</li>
 * </ul>
 * </p>
 * <p>
 * 配置条件：
 * <ul>
 *     <li>应用必须是Servlet类型的Web应用</li>
 *     <li>Classpath中必须存在Servlet和DispatcherServlet类</li>
 *     <li>方法或类需要标注{@link ResponseBodyWrapper}注解</li>
 * </ul>
 * </p>
 * <p>
 * 处理优先级：
 * <p>
 * 该类的执行优先级为{@link Ordered#HIGHEST_PRECEDENCE} + 1，
 * 确保在其他{@link ResponseBodyAdvice}之前执行，最先对响应体进行包装。
 * </p>
 * </p>
 * <p>
 * 包装规则：
 * <ul>
 *     <li>
 *         <strong>String类型响应</strong>
 *         <p>
 *         将字符串包装到{@link Result}中，并将Content-Type设置为application/json
 *         </p>
 *     </li>
 *     <li>
 *         <strong>字节数组响应</strong>
 *         <p>
 *         将字节数组包装到{@link Result}中，并将Content-Type设置为application/json
 *         </p>
 *     </li>
 *     <li>
 *         <strong>Result类型响应</strong>
 *         <p>
 *         已经是Result类型，直接返回，不再包装
 *         </p>
 *     </li>
 *     <li>
 *         <strong>Gson JsonElement类型响应</strong>
 *         <p>
 *         使用Gson序列化器时，将JsonElement包装在JsonObject中
 *         </p>
 *     </li>
 *     <li>
 *         <strong>Jackson JsonNode类型响应</strong>
 *         <p>
 *         使用Jackson序列化器时，将JsonNode包装在ObjectNode中
 *         </p>
 *     </li>
 *     <li>
 *         <strong>其他类型响应</strong>
 *         <p>
 *         统一包装到{@link Result}中
 *         </p>
 *     </li>
 * </ul>
 * </p>
 * <p>
 * 排除规则：
 * <p>
 * 以下情况下不会进行包装：
 * <ul>
 *     <li>返回类型为{@link ResponseEntity}时</li>
 *     <li>方法标注了{@link ResponseBodyWrapperIgnore}注解时</li>
 *     <li>方法或类没有标注{@link ResponseBodyWrapper}注解时</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 在方法上标注注解
 * &#64;GetMapping("/api/users/{id}")
 * &#64;ResponseBodyWrapper
 * public User getUserById(&#64;PathVariable Long id) {
 *     return userService.findById(id);
 * }
 *
 * // 在类上标注注解，该类的所有方法都会进行包装
 * &#64;RestController
 * &#64;RequestMapping("/api/users")
 * &#64;ResponseBodyWrapper
 * public class UserController {
 *     &#64;GetMapping
 *     public List&lt;User&gt; listUsers() {
 *         return userService.findAll();
 *     }
 *
 *     // 该方法会被排除包装
 *     &#64;GetMapping("/{id}")
 *     &#64;ResponseBodyWrapperIgnore
 *     public ResponseEntity&lt;User&gt; getUserById(&#64;PathVariable Long id) {
 *         return ResponseEntity.ok(userService.findById(id));
 *     }
 * }
 * </pre>
 * </p>
 * <p>
 * 响应格式示例：
 * <pre>
 * // 原始响应
 * {
 *   "id": 1,
 *   "name": "John",
 *   "age": 30
 * }
 *
 * // 包装后的响应
 * {
 *   "code": 0,
 *   "message": "请求成功",
 *   "data": {
 *     "id": 1,
 *     "name": "John",
 *     "age": 30
 *   }
 * }
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see ResponseBodyWrapper
 * @see ResponseBodyWrapperIgnore
 * @see ResponseBodyAdvice
 * @see Result
 * @since 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@RestControllerAdvice
public class ResponseBodyWrapperAdvice implements ResponseBodyAdvice<Object> {
	private final ObjectMapper objectMapper;
	private final ObjectReader objectReader;

	public ResponseBodyWrapperAdvice(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.objectReader = objectMapper.reader();
	}

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		if (MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType) ||
			GsonHttpMessageConverter.class.isAssignableFrom(converterType) ||
			ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType) ||
			StringHttpMessageConverter.class.isAssignableFrom(converterType)) {
			if (returnType.getNestedParameterType().isAssignableFrom(ResponseEntity.class) ||
				Objects.nonNull(returnType.getMethodAnnotation(ResponseBodyWrapperIgnore.class))) {
				return false;
			}
			return Objects.nonNull(returnType.getMethodAnnotation(ResponseBodyWrapper.class)) ||
				Objects.nonNull(returnType.getDeclaringClass().getAnnotation(ResponseBodyWrapper.class));
		}
		return false;
	}

	@Override
	public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
								  Class<? extends HttpMessageConverter<?>> selectedConverterType,
								  ServerHttpRequest request, ServerHttpResponse response) {
		if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			return Result.ok(body).toString();
		} else if (ByteArrayHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			return Result.ok(body).toString().getBytes();
		} else if (body instanceof Result<?>) {
			return body;
		} else if (body instanceof JsonElement element) {
			if (GsonHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
				JsonObject result = new JsonObject();
				result.addProperty("code", WebConstants.SUCCESS_CODE);
				result.addProperty("message", WebConstants.DEFAULT_SUCCESS_MESSAGE);
				result.add("data", element);
				return result;
			} else {
				ObjectNode result = objectMapper.createObjectNode();
				result.put("code", WebConstants.SUCCESS_CODE);
				result.put("message", WebConstants.DEFAULT_SUCCESS_MESSAGE);
				try {
					result.set("data", objectReader.readTree(element.toString()));
				} catch (JsonProcessingException e) {
					throw new ServerException(e);
				}
				return result;
			}
		} else if (body instanceof JsonNode node) {
			if (GsonHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
				JsonObject result = new JsonObject();
				result.addProperty("code", WebConstants.SUCCESS_CODE);
				result.addProperty("message", WebConstants.DEFAULT_SUCCESS_MESSAGE);
				result.add("data", JsonUtils.parseString(node.toString()));
				return result;
			} else {
				ObjectNode result = objectMapper.createObjectNode();
				result.put("code", WebConstants.SUCCESS_CODE);
				result.put("message", WebConstants.DEFAULT_SUCCESS_MESSAGE);
				result.set("data", node);
				return result;
			}
		} else {
			return Result.ok(body);
		}
	}
}