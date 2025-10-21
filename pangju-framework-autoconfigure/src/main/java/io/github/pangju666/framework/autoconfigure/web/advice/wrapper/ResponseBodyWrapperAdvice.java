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