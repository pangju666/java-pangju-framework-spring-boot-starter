package io.github.pangju666.framework.autoconfigure.web.advice.wrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonElement;
import io.github.pangju666.framework.autoconfigure.web.annotation.wrapper.ResponseBodyWrapper;
import io.github.pangju666.framework.autoconfigure.web.annotation.wrapper.ResponseBodyWrapperIgnore;
import io.github.pangju666.framework.web.model.vo.Result;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJsonHttpMessageConverter;
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
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		if (Objects.nonNull(returnType.getMethodAnnotation(ResponseBodyWrapperIgnore.class))) {
			return false;
		}
		return Objects.nonNull(returnType.getMethodAnnotation(ResponseBodyWrapper.class)) ||
			Objects.nonNull(returnType.getDeclaringClass().getAnnotation(ResponseBodyWrapper.class));
	}

	@Override
	public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
								  Class<? extends HttpMessageConverter<?>> selectedConverterType,
								  ServerHttpRequest request, ServerHttpResponse response) {
		if (!AbstractJackson2HttpMessageConverter.class.isAssignableFrom(selectedConverterType) &&
			!AbstractJsonHttpMessageConverter.class.isAssignableFrom(selectedConverterType) &&
			!ByteArrayHttpMessageConverter.class.isAssignableFrom(selectedConverterType) &&
			!StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
			return body;
		}
		if (body instanceof Result<?>) {
			return body.toString().getBytes();
		} else if (body instanceof JsonElement element) {
			return Result.ok(element.toString());
		} else if (body instanceof JsonNode node) {
			return Result.ok(node.toString());
		} else if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			return Result.ok(body).toString();
		} else if (ByteArrayHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			return Result.ok(body).toString().getBytes();
		}
		return Result.ok(body);
	}
}