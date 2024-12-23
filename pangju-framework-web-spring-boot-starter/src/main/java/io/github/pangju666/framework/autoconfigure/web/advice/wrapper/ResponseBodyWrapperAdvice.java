package io.github.pangju666.framework.autoconfigure.web.advice.wrapper;

import io.github.pangju666.framework.autoconfigure.web.annotation.wrapper.ResponseBodyWrapper;
import io.github.pangju666.framework.autoconfigure.web.annotation.wrapper.ResponseBodyWrapperIgnore;
import io.github.pangju666.framework.web.model.Result;
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
		if (body instanceof Result<?>) {
			return body;
		}
		if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType) && body instanceof String bodyStr) {
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			return Result.ok(bodyStr).toString();
		}
		if (ByteArrayHttpMessageConverter.class.isAssignableFrom(selectedConverterType) && body instanceof byte[] bodyBytes) {
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			return Result.ok(bodyBytes).toString().getBytes();
		}
		if (Objects.isNull(body)) {
			return Result.ok();
		}
		return Result.ok(body);
	}
}