package io.github.pangju666.framework.autoconfigure.web.advice.crypto;

import io.github.pangju666.commons.lang.utils.JsonUtils;
import io.github.pangju666.commons.lang.utils.ReflectionUtils;
import io.github.pangju666.framework.autoconfigure.context.StaticSpringContext;
import io.github.pangju666.framework.autoconfigure.web.annotation.crypto.ResponseBodyEncrypt;
import io.github.pangju666.framework.autoconfigure.web.annotation.crypto.ResponseBodyFieldEncrypt;
import io.github.pangju666.framework.autoconfigure.web.utils.CryptoUtils;
import io.github.pangju666.framework.core.exception.base.ServiceException;
import io.github.pangju666.framework.web.model.Result;
import jakarta.servlet.Servlet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.lang.reflect.Field;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@RestControllerAdvice
public class ResponseBodyEncryptAdvice implements ResponseBodyAdvice<Object> {
	private static final Logger logger = LoggerFactory.getLogger(ResponseBodyEncryptAdvice.class);

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
								  Class<? extends HttpMessageConverter<?>> selectedConverterType,
								  ServerHttpRequest request, ServerHttpResponse response) {
		if (Objects.isNull(body)) {
			return null;
		}
		if (selectedConverterType.isAssignableFrom(MappingJackson2HttpMessageConverter.class)) {
			encryptFields(body);
		}
		ResponseBodyEncrypt annotation = returnType.getMethodAnnotation(ResponseBodyEncrypt.class);
		if (Objects.isNull(annotation)) {
			annotation = returnType.getDeclaringClass().getAnnotation(ResponseBodyEncrypt.class);
		}
		if (Objects.nonNull(annotation)) {
			String key = StaticSpringContext.getProperty(annotation.key());
			if (StringUtils.isBlank(key)) {
				logger.error("属性：{} 值为空", annotation.key());
				throw new ServiceException("秘钥读取失败");
			}
			return encryptBody(body, annotation, key);
		}
		return body;
	}

	private Object encryptBody(Object body, ResponseBodyEncrypt annotation, String key) {
		try {
			if (body instanceof byte[] bytes) {
				return CryptoUtils.encrypt(ArrayUtils.nullToEmpty(bytes), key, annotation.algorithm(), annotation.encoding(), annotation.transformation());
			}
			if (body instanceof String content) {
				return CryptoUtils.encryptToString(StringUtils.defaultString(content).getBytes(), key, annotation.algorithm(), annotation.encoding(), annotation.transformation());
			}
			if (body instanceof Result<?> data) {
				if (Objects.isNull(data.data())) {
					return data;
				}
				if (data.data().getClass().isAssignableFrom(String.class)) {
					String bodyData = StringUtils.defaultString((String) data.data());
					String result = CryptoUtils.encryptToString(bodyData.getBytes(), key, annotation.algorithm(), annotation.encoding(), annotation.transformation());
					return Result.ok(result);
				} else if (data.data().getClass().isAssignableFrom(byte[].class)) {
					byte[] bodyData = ArrayUtils.nullToEmpty((byte[]) data.data());
					byte[] result = CryptoUtils.encrypt(bodyData, key, annotation.algorithm(), annotation.encoding(), annotation.transformation());
					return Result.ok(result);
				}
				String content = JsonUtils.toString(data.data());
				String result = CryptoUtils.encryptToString(content.getBytes(), key, annotation.algorithm(), annotation.encoding(), annotation.transformation());
				return Result.ok(result);
			}
			String content = JsonUtils.toString(body);
			return CryptoUtils.encrypt(content.getBytes(), key, annotation.algorithm(), annotation.encoding(), annotation.transformation());
		} catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
				 InvalidKeyException | InvalidKeySpecException | InvalidAlgorithmParameterException e) {
			logger.error("响应数据加密失败", e);
			throw new ServiceException("响应数据加密失败");
		}
	}

	@SuppressWarnings("unchecked")
	private void encryptFields(Object body) {
		Field[] fields;
		if (body instanceof Result<?> result) {
			if (Objects.isNull(result.data())) {
				return;
			}
			fields = result.data().getClass().getDeclaredFields();
		} else {
			fields = body.getClass().getDeclaredFields();
		}
		for (Field field : fields) {
			ResponseBodyFieldEncrypt annotation = field.getAnnotation(ResponseBodyFieldEncrypt.class);
			if (Objects.isNull(annotation)) {
				continue;
			}
			try {
				if (String.class.isAssignableFrom(field.getType())) {
					ReflectionUtils.setAccessible(field, body);
					String value = (String) field.get(body);
					if (StringUtils.isNotBlank(value)) {
						String key = StaticSpringContext.getProperty(annotation.key());
						if (StringUtils.isBlank(key)) {
							logger.error("属性：{} 值为空", annotation.key());
							throw new ServiceException("秘钥读取失败");
						}
						value = CryptoUtils.encryptToString(value.getBytes(), key, annotation.algorithm(), annotation.encoding(), annotation.transformation());
						field.set(body, value);
						field.setAccessible(false);
					}
				} else if (Collection.class.isAssignableFrom(field.getType())) {
					Class<?> genericType = ReflectionUtils.getClassGenericType(body.getClass());
					if (String.class.isAssignableFrom(genericType)) {
						ReflectionUtils.setAccessible(field, body);
						Collection<String> collection = (Collection<String>) field.get(body);
						String key = StaticSpringContext.getProperty(annotation.key());
						if (StringUtils.isBlank(key)) {
							logger.error("属性：{} 值为空", annotation.key());
							throw new ServiceException("秘钥读取失败");
						}
						if (Objects.nonNull(collection)) {
							if (List.class.isAssignableFrom(field.getType())) {
								List<String> values = new ArrayList<>(collection.size());
								for (String element : collection) {
									if (StringUtils.isBlank(element)) {
										values.add(element);
									}
									values.add(CryptoUtils.encryptToString(element.getBytes(), key, annotation.algorithm(), annotation.encoding(), annotation.transformation()));
								}
								field.set(body, values);
							} else if (Set.class.isAssignableFrom(field.getType())) {
								Set<String> values = new HashSet<>(collection.size());
								for (String element : collection) {
									if (StringUtils.isBlank(element)) {
										values.add(element);
									}
									values.add(CryptoUtils.encryptToString(element.getBytes(), key, annotation.algorithm(), annotation.encoding(), annotation.transformation()));
								}
								field.set(body, values);
							}
							field.setAccessible(false);
						}
					}
				}
			} catch (IllegalAccessException e) {
				logger.error("响应体读取失败", e);
				throw new ServiceException("响应体读取失败");
			} catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
					 BadPaddingException |
					 InvalidKeyException | InvalidKeySpecException | InvalidAlgorithmParameterException e) {
				logger.error("响应数据加密失败", e);
				throw new ServiceException("响应数据加密失败");
			}
		}
	}
}
