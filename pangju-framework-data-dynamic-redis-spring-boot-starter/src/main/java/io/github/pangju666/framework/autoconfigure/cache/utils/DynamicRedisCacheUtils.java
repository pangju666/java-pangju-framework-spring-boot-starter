package io.github.pangju666.framework.autoconfigure.cache.utils;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cache.CacheManager;

public class DynamicRedisCacheUtils {
	private static final String CACHE_MANAGER_BEAN_NAME_TEMPLATE = "redis-%s-cache-manager";

	protected DynamicRedisCacheUtils() {
	}

	public static String getCacheManagerBeanName(String name) {
		return CACHE_MANAGER_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static CacheManager getCacheManager(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(CACHE_MANAGER_BEAN_NAME_TEMPLATE.formatted(name), CacheManager.class);
	}
}