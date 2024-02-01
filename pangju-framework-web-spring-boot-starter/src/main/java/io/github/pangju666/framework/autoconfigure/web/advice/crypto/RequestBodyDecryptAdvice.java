package io.github.pangju666.framework.autoconfigure.web.advice.crypto;

import io.github.pangju666.commons.lang.utils.ReflectionUtils;
import io.github.pangju666.framework.autoconfigure.context.StaticSpringContext;
import io.github.pangju666.framework.autoconfigure.web.annotation.crypto.RequestBodyDecrypt;
import io.github.pangju666.framework.autoconfigure.web.annotation.crypto.RequestBodyFieldDecrypt;
import io.github.pangju666.framework.autoconfigure.web.utils.CryptoUtils;
import io.github.pangju666.framework.core.exception.base.ServiceException;
import jakarta.servlet.Servlet;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@RestControllerAdvice
public class RequestBodyDecryptAdvice implements RequestBodyAdvice {
	private static final Logger logger = LoggerFactory.getLogger(RequestBodyDecryptAdvice.class);

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
							Class<? extends HttpMessageConverter<?>> converterType) {
		return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
	}

	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
										   Class<? extends HttpMessageConverter<?>> converterType) {
		RequestBodyDecrypt annotation = parameter.getParameterAnnotation(RequestBodyDecrypt.class);
		if (Objects.isNull(annotation)) {
			annotation = parameter.getMethodAnnotation(RequestBodyDecrypt.class);
		}
		if (Objects.isNull(annotation)) {
			return inputMessage;
		}

		String key = StaticSpringContext.getProperty(annotation.key());
		try (InputStream inputStream = inputMessage.getBody()) {
			String requestBodyStr = new String(inputStream.readAllBytes());
			if (StringUtils.isBlank(requestBodyStr)) {
				return inputMessage;
			}
			byte[] plainText = CryptoUtils.decrypt(requestBodyStr, key, annotation.algorithm(), annotation.encoding(), annotation.transformation());
			return new MappingJacksonInputMessage(new ByteArrayInputStream(plainText), inputMessage.getHeaders());
		} catch (IOException e) {
			logger.error("请求体读取失败", e);
			throw new ServiceException("请求体读取失败");
		} catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
				 InvalidKeyException | InvalidKeySpecException | InvalidAlgorithmParameterException e) {
			logger.error("请求数据解密失败", e);
			throw new ServiceException("请求数据解密失败");
		} catch (DecoderException e) {
			logger.error("十六进制解码失败", e);
			throw new ServiceException("请求数据十六进制解码失败");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
								Class<? extends HttpMessageConverter<?>> converterType) {
		Field[] fields = body.getClass().getDeclaredFields();
		for (Field field : fields) {
			RequestBodyFieldDecrypt annotation = field.getAnnotation(RequestBodyFieldDecrypt.class);
			if (Objects.isNull(annotation)) {
				continue;
			}
			try {
				if (String.class.isAssignableFrom(field.getType())) {
					ReflectionUtils.setAccessible(field, body);
					String value = (String) field.get(body);
					if (StringUtils.isNotBlank(value)) {
						String key = StaticSpringContext.getProperty(annotation.key());
						value = CryptoUtils.decryptToString(value, key, annotation.algorithm(), annotation.encoding(), annotation.transformation());
						field.set(body, value);
						field.setAccessible(false);
					}
				} else if (Collection.class.isAssignableFrom(field.getType())) {
					Class<?> genericType = ReflectionUtils.getClassGenericType(field.getType());
					if (String.class.isAssignableFrom(genericType)) {
						ReflectionUtils.setAccessible(field, body);
						Collection<String> collection = (Collection<String>) field.get(body);
						String key = StaticSpringContext.getProperty(annotation.key());
						if (Objects.nonNull(collection)) {
							if (List.class.isAssignableFrom(field.getType())) {
								List<String> values = new ArrayList<>(collection.size());
								for (String element : collection) {
									if (StringUtils.isBlank(element)) {
										values.add(element);
									}
									values.add(CryptoUtils.decryptToString(element, key, annotation.algorithm(), annotation.encoding(), annotation.transformation()));
								}
								field.set(body, values);
							} else if (Set.class.isAssignableFrom(field.getType())) {
								Set<String> values = new HashSet<>(collection.size());
								for (String element : collection) {
									if (StringUtils.isBlank(element)) {
										values.add(element);
									}
									values.add(CryptoUtils.decryptToString(element, key, annotation.algorithm(), annotation.encoding(), annotation.transformation()));
								}
								field.set(body, values);
							}
							field.setAccessible(false);
						}
					}
				}
			} catch (IllegalAccessException e) {
				logger.error("请求体读取失败", e);
				throw new ServiceException("请求体读取失败");
			} catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
					 BadPaddingException |
					 InvalidKeyException | InvalidKeySpecException | InvalidAlgorithmParameterException e) {
				logger.error("请求数据解密失败", e);
				throw new ServiceException("请求数据解密失败");
			} catch (DecoderException e) {
				logger.error("十六进制解码失败", e);
				throw new ServiceException("请求数据十六进制解码失败");
			}
		}
		return body;
	}

	@Override
	public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}
}
