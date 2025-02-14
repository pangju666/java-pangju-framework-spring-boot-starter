package io.github.pangju666.framework.autoconfigure.cache.redis.listener;

import io.github.pangju666.framework.autoconfigure.cache.redis.RedisCacheManager;
import io.github.pangju666.framework.autoconfigure.cache.redis.RedisCacheProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.Map;

public class RedisCacheInitApplicationListener implements ApplicationListener<ApplicationReadyEvent> {
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		RedisCacheManager redisCacheManager = event.getApplicationContext().getBean(RedisCacheManager.class);
		Map<String, RedisCacheProcessor> beanMap = event.getApplicationContext().getBeansOfType(RedisCacheProcessor.class);
		for (RedisCacheProcessor processor : beanMap.values()) {
			processor.init(redisCacheManager);
		}
	}
}
