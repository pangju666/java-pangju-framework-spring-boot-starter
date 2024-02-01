package io.github.pangju666.framework.autoconfigure.web.interceptor;

import io.github.pangju666.commons.lang.utils.SystemClock;
import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RequestLimit;
import io.github.pangju666.framework.autoconfigure.web.exception.RequestLimitException;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestLimitProperties;
import io.github.pangju666.framework.core.lang.pool.ConstantPool;
import io.github.pangju666.framework.web.interceptor.BaseRequestInterceptor;
import io.github.pangju666.framework.web.utils.RequestUtils;
import io.github.pangju666.framework.web.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class RequestLimitInterceptor extends BaseRequestInterceptor {
	private final RequestLimitProperties properties;
	private ExpiringMap<String, AtomicLong> expiringMap;
	private RedisTemplate<String, Object> redisTemplate;

	public RequestLimitInterceptor(RequestLimitProperties properties, BeanFactory beanFactory) {
		this.properties = properties;
		if (properties.getType() == RequestLimitProperties.Type.REDIS) {
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
		long currentTimeMillis = SystemClock.now();

		if (handler instanceof HandlerMethod handlerMethod) {
			Class<?> targetClass = handlerMethod.getBeanType();
			Method targetMethod = handlerMethod.getMethod();
			RequestLimit annotation = targetMethod.getAnnotation(RequestLimit.class);
			if (Objects.isNull(annotation)) {
				annotation = targetClass.getAnnotation(RequestLimit.class);
			}
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
			if (!annotation.global()) {
				if (Objects.nonNull(redisTemplate)) {
					keyBuilder.append(ConstantPool.REDIS_PATH_DELIMITER);
				} else {
					keyBuilder.append("_");
				}
				keyBuilder.append(RequestUtils.getIpAddress(request));
			}
			String key = keyBuilder.toString();

			Long count;
			if (Objects.nonNull(redisTemplate)) {
				ZSetOperations<String, Object> operations = redisTemplate.opsForZSet();
				operations.add(key, currentTimeMillis, currentTimeMillis);
				redisTemplate.expire(key, annotation.duration(), annotation.timeUnit());
				long millis = annotation.timeUnit().toMillis(annotation.duration());
				// 删除当前时间之前不在时间范围的统计次数
				operations.removeRangeByScore(key, 0, (double) currentTimeMillis - (millis * 1000L));
				count = operations.zCard(key);
			} else {
				if (!expiringMap.containsKey(key)) {
					expiringMap.put(key, new AtomicLong(0L), annotation.duration(), annotation.timeUnit());
				}
				count = expiringMap.get(key).incrementAndGet();
			}

			count = ObjectUtils.defaultIfNull(count, 0L);
			if (count > annotation.count()) {
				ResponseUtils.writeExceptionToResponse(new RequestLimitException(annotation), response);
				return false;
			}
		}
		return true;
	}
}
