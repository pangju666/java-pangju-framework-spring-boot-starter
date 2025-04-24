package io.github.pangju666.framework.autoconfigure.data.dynamic.redis.utils;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class DynamicRedisUtils {
	private static final String CONNECTION_FACTORY_BEAN_NAME_TEMPLATE = "redis-%s-connection-factory";
	private static final String TEMPLATE_BEAN_NAME_TEMPLATE = "redis-%s-template";

	protected DynamicRedisUtils() {
	}

	public static String getRedisConnectionFactoryBeanName(String name) {
		return CONNECTION_FACTORY_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static RedisConnectionFactory getRedisConnectionFactory(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(CONNECTION_FACTORY_BEAN_NAME_TEMPLATE.formatted(name), RedisConnectionFactory.class);
	}

	public static String getRedisTemplateBeanName(String name) {
		return TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static RedisTemplate getRedisTemplate(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name), RedisTemplate.class);
	}
}
