package io.github.pangju666.framework.autoconfigure.web.advice.crypto;

import io.github.pangju666.commons.lang.utils.ReflectionUtils;
import io.github.pangju666.framework.autoconfigure.spring.StaticSpringContext;
import io.github.pangju666.framework.autoconfigure.web.annotation.crypto.DecryptRequestBody;
import io.github.pangju666.framework.autoconfigure.web.annotation.crypto.DecryptRequestBodyField;
import io.github.pangju666.framework.autoconfigure.web.utils.CryptoUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.exception.base.ServiceException;
import jakarta.servlet.Servlet;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@RestControllerAdvice
public class RequestBodyDecryptAdvice implements RequestBodyAdvice {
	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
							Class<? extends HttpMessageConverter<?>> converterType) {
		return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
	}

	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
										   Class<? extends HttpMessageConverter<?>> converterType) {
		DecryptRequestBody annotation = parameter.getParameterAnnotation(DecryptRequestBody.class);
		if (Objects.isNull(annotation)) {
			annotation = parameter.getMethodAnnotation(DecryptRequestBody.class);
		}
		if (Objects.isNull(annotation)) {
			return inputMessage;
		}

		String key = StaticSpringContext.getProperty(annotation.key());
		try (InputStream inputStream = inputMessage.getBody()) {
			String requestBodyStr = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			if (StringUtils.isBlank(requestBodyStr)) {
				return inputMessage;
			}
			byte[] plainText = CryptoUtils.decrypt(requestBodyStr, key, annotation.algorithm(), annotation.encoding());
			return new MappingJacksonInputMessage(new ByteArrayInputStream(plainText), inputMessage.getHeaders());
		} catch (IOException e) {
			throw new ServerException("请求体读取失败", e);
		} catch (EncryptionOperationNotPossibleException e) {
			throw new ServiceException("无效的请求数据", "请求数据对象解密失败", e);
		} catch (DecoderException e) {
			throw new ServiceException("无效的请求数据", "请求数据对象十六进制解码失败", e);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
								Class<? extends HttpMessageConverter<?>> converterType) {
		Field[] fields = body.getClass().getDeclaredFields();
		for (Field field : fields) {
			DecryptRequestBodyField annotation = field.getAnnotation(DecryptRequestBodyField.class);
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
						value = CryptoUtils.decryptToString(value, key, annotation.algorithm(), annotation.encoding());
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
									newMap.put(entry.getKey(), CryptoUtils.decryptToString(entry.getValue(),
										key, annotation.algorithm(), annotation.encoding()));
								}
							}
							ReflectionUtils.setFieldValue(body, field, newMap);
						}
					}
				} else if (Collection.class.isAssignableFrom(field.getType())) {
					Class<?> genericType = ReflectionUtils.getClassGenericType(field.getType());
					if (String.class.isAssignableFrom(genericType)) {
						Collection<String> collection = (Collection<String>) fieldValue;
						if (CollectionUtils.isNotEmpty(collection)) {
							if (List.class.isAssignableFrom(field.getType())) {
								List<String> list = new ArrayList<>(collection.size());
								for (String element : collection) {
									if (StringUtils.isBlank(element)) {
										list.add(element);
									} else {
										list.add(CryptoUtils.decryptToString(element, key, annotation.algorithm(), annotation.encoding()));
									}
								}
								ReflectionUtils.setFieldValue(body, field, list);
							} else if (Set.class.isAssignableFrom(field.getType())) {
								Set<String> set = new HashSet<>(collection.size());
								for (String element : collection) {
									if (StringUtils.isBlank(element)) {
										set.add(element);
									} else {
										set.add(CryptoUtils.decryptToString(element, key, annotation.algorithm(), annotation.encoding()));
									}
								}
								ReflectionUtils.setFieldValue(body, field, set);
							}
						}
					}
				}
			} catch (EncryptionOperationNotPossibleException e) {
				throw new ServiceException("无效的请求数据", "请求数据对象字段解密失败", e);
			} catch (DecoderException e) {
				throw new ServiceException("无效的请求数据", "请求数据对象字段十六进制解码失败", e);
			} catch (InvalidKeySpecException e) {
				throw new RuntimeException(e);
			}
		}
		return body;
	}

	@Override
	public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
								  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}
}
