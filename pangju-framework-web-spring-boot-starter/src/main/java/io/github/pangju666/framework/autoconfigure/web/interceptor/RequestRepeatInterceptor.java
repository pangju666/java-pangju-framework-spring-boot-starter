package io.github.pangju666.framework.autoconfigure.web.interceptor;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RequestRepeat;
import io.github.pangju666.framework.autoconfigure.web.exception.RequestRepeatException;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestRepeatProperties;
import io.github.pangju666.framework.core.lang.pool.ConstantPool;
import io.github.pangju666.framework.web.interceptor.BaseRequestInterceptor;
import io.github.pangju666.framework.web.utils.RequestUtils;
import io.github.pangju666.framework.web.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Objects;

public class RequestRepeatInterceptor extends BaseRequestInterceptor {
	private final RequestRepeatProperties properties;
	private ExpiringMap<String, Boolean> expiringMap;
	private RedisTemplate<String, Boolean> redisTemplate;

	public RequestRepeatInterceptor(RequestRepeatProperties properties, BeanFactory beanFactory) {
		this.properties = properties;
		if (properties.getType() == RequestRepeatProperties.Type.REDIS) {
			if (StringUtils.hasText(properties.getRedis().getTemplateBeanName())) {
				this.redisTemplate = beanFactory.getBean(properties.getRedis().getTemplateBeanName(), RedisTemplate.class);
			} else {
				this.redisTemplate = beanFactory.getBean(ConstantPool.DEFAULT_REDIS_TEMPLATE_BEAN_NAME, RedisTemplate.class);
			}
		}
		if (Objects.isNull(this.redisTemplate)) {
			this.expiringMap = ExpiringMap.builder()
				.variableExpiration()
				.build();
		}
	}

	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
		if (handler instanceof HandlerMethod handlerMethod) {
			Method targetMethod = handlerMethod.getMethod();
			RequestRepeat annotation = targetMethod.getAnnotation(RequestRepeat.class);
			if (Objects.isNull(annotation)) {
				return true;
			}

			StringBuilder keyBuilder = new StringBuilder();
			if (Objects.nonNull(redisTemplate)) {
				keyBuilder.append(properties.getRedis().getKeyPrefix())
					.append(ConstantPool.REDIS_PATH_DELIMITER);
			}
			keyBuilder.append(request.getMethod());
			if (Objects.nonNull(redisTemplate)) {
				keyBuilder.append(ConstantPool.REDIS_PATH_DELIMITER);
			} else {
				keyBuilder.append("_");
			}
			keyBuilder.append(RequestUtils.getRequestPath(request));
			if (Objects.nonNull(redisTemplate)) {
				keyBuilder.append(ConstantPool.REDIS_PATH_DELIMITER);
			} else {
				keyBuilder.append("_");
			}
			keyBuilder.append(RequestUtils.getIpAddress(request));

			String key = keyBuilder.toString();

			if (Objects.nonNull(redisTemplate)) {
				if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
					ResponseUtils.writeExceptionToResponse(new RequestRepeatException(annotation), response);
					return false;
				}
				redisTemplate.opsForValue().set(key, true, annotation.duration(), annotation.timeUnit());
			} else {
				if (expiringMap.containsKey(key)) {
					ResponseUtils.writeExceptionToResponse(new RequestRepeatException(annotation), response);
					return false;
				}
				expiringMap.put(key, true, annotation.duration(), annotation.timeUnit());
			}
		}
		return true;
	}
}
