package io.github.pangju666.framework.autoconfigure.web.advice.crypto;

import io.github.pangju666.commons.lang.utils.JsonUtils;
import io.github.pangju666.commons.lang.utils.ReflectionUtils;
import io.github.pangju666.framework.autoconfigure.spring.StaticSpringContext;
import io.github.pangju666.framework.autoconfigure.web.annotation.crypto.EncryptResponseBody;
import io.github.pangju666.framework.autoconfigure.web.annotation.crypto.EncryptResponseBodyField;
import io.github.pangju666.framework.autoconfigure.web.utils.CryptoUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.model.common.Result;
import jakarta.servlet.Servlet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
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

import java.lang.reflect.Field;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@RestControllerAdvice
public class ResponseBodyEncryptAdvice implements ResponseBodyAdvice<Object> {
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
		EncryptResponseBody annotation = returnType.getMethodAnnotation(EncryptResponseBody.class);
		if (Objects.isNull(annotation)) {
			annotation = returnType.getDeclaringClass().getAnnotation(EncryptResponseBody.class);
		}
		if (Objects.nonNull(annotation)) {
			String key = StaticSpringContext.getProperty(annotation.key());
			if (StringUtils.isBlank(key)) {
				throw new ServerException("属性：" + annotation.key() + "值为空");
			}
			return encryptBody(body, annotation, key);
		}
		return body;
	}

	private Object encryptBody(Object body, EncryptResponseBody annotation, String key) {
		try {
			if (body instanceof byte[] bytes) {
				return CryptoUtils.encrypt(ArrayUtils.nullToEmpty(bytes), key, annotation.algorithm(), annotation.encoding());
			}
			if (body instanceof String content) {
				return CryptoUtils.encryptToString(StringUtils.defaultString(content).getBytes(), key, annotation.algorithm(), annotation.encoding());
			}
			if (body instanceof Result<?> data) {
				if (Objects.isNull(data.getData())) {
					return data;
				}
				if (data.getData().getClass().isAssignableFrom(String.class)) {
					String bodyData = StringUtils.defaultString((String) data.getData());
					String result = CryptoUtils.encryptToString(bodyData.getBytes(), key, annotation.algorithm(), annotation.encoding());
					return Result.ok(result);
				} else if (data.getData().getClass().isAssignableFrom(byte[].class)) {
					byte[] bodyData = ArrayUtils.nullToEmpty((byte[]) data.getData());
					byte[] result = CryptoUtils.encrypt(bodyData, key, annotation.algorithm(), annotation.encoding());
					return Result.ok(result);
				}
				String content = JsonUtils.toString(data.getData());
				String result = CryptoUtils.encryptToString(content.getBytes(), key, annotation.algorithm(), annotation.encoding());
				return Result.ok(result);
			}
			String content = JsonUtils.toString(body);
			return CryptoUtils.encrypt(content.getBytes(), key, annotation.algorithm(), annotation.encoding());
		} catch (EncryptionOperationNotPossibleException e) {
			throw new ServerException("响应数据对象加密失败", e);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void encryptFields(Object body) {
		Field[] fields;
		if (body instanceof Result<?> result) {
			if (Objects.isNull(result.getData())) {
				return;
			}
			fields = result.getData().getClass().getDeclaredFields();
		} else {
			fields = body.getClass().getDeclaredFields();
		}

		for (Field field : fields) {
			EncryptResponseBodyField annotation = field.getAnnotation(EncryptResponseBodyField.class);
			if (Objects.isNull(annotation)) {
				continue;
			}
			if (ClassUtils.isPrimitiveOrWrapper(field.getType())) {
				continue;
			}

			try {
				Object fieldValue = ReflectionUtils.getFieldValue(body, field);
				if (Objects.isNull(fieldValue)) {
					continue;
				}

				String key = StaticSpringContext.getProperty(annotation.key());
				if (StringUtils.isBlank(key)) {
					throw new ServerException("属性：" + annotation.key() + "值为空");
				}

				if (String.class.isAssignableFrom(field.getType())) {
					String value = (String) fieldValue;
					if (StringUtils.isNotBlank(value)) {
						value = CryptoUtils.encryptToString(value.getBytes(), key, annotation.algorithm(), annotation.encoding());
						ReflectionUtils.setFieldValue(body, field, value);
					}
				} else if (Map.class.isAssignableFrom(field.getType())) {
					Class<?> genericType = ReflectionUtils.getClassGenericType(field.getType(), 1);
					if (String.class.isAssignableFrom(genericType)) {
						Map<?, String> map = (Map<?, String>) fieldValue;
						if (MapUtils.isNotEmpty(map)) {
							Map<Object, String> newMap = new HashMap<>(map.size());
							for (Map.Entry<?, String> entry : map.entrySet()) {
								if (StringUtils.isBlank(entry.getValue())) {
									newMap.put(entry.getKey(), entry.getValue());
								} else {
									newMap.put(entry.getKey(), CryptoUtils.encryptToString(entry.getValue().getBytes(),
										key, annotation.algorithm(), annotation.encoding()));
								}
							}
							ReflectionUtils.setFieldValue(body, field, newMap);
						}
					}
				} else if (Collection.class.isAssignableFrom(field.getType())) {
					Class<?> genericType = ReflectionUtils.getClassGenericType(body.getClass());
					if (String.class.isAssignableFrom(genericType)) {
						Collection<String> collection = (Collection<String>) fieldValue;
						if (CollectionUtils.isNotEmpty(collection)) {
							if (List.class.isAssignableFrom(field.getType())) {
								List<String> list = new ArrayList<>(collection.size());
								for (String element : collection) {
									if (StringUtils.isBlank(element)) {
										list.add(element);
									} else {
										list.add(CryptoUtils.encryptToString(element.getBytes(), key, annotation.algorithm(), annotation.encoding()));
									}
								}
								ReflectionUtils.setFieldValue(body, field, list);
							} else if (Set.class.isAssignableFrom(field.getType())) {
								Set<String> set = new HashSet<>(collection.size());
								for (String element : collection) {
									if (StringUtils.isBlank(element)) {
										set.add(element);
									} else {
										set.add(CryptoUtils.encryptToString(element.getBytes(), key, annotation.algorithm(), annotation.encoding()));
									}
								}
								ReflectionUtils.setFieldValue(body, field, set);
							}
						}
					}
				}
			} catch (EncryptionOperationNotPossibleException | InvalidKeySpecException e) {
				throw new ServerException("响应数据对象字段加密失败", e);
			}
		}
	}
}