package io.github.pangju666.framework.autoconfigure.cache.hash.listener;

import io.github.pangju666.framework.autoconfigure.cache.hash.HashCacheManager;
import io.github.pangju666.framework.autoconfigure.cache.hash.HashCacheProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Map;

public class HashCacheClearApplicationListener implements ApplicationListener<ContextClosedEvent> {
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		HashCacheManager redisCacheManager = event.getApplicationContext().getBean(HashCacheManager.class);
		Map<String, HashCacheProcessor> beanMap = event.getApplicationContext().getBeansOfType(HashCacheProcessor.class);
		for (HashCacheProcessor processor : beanMap.values()) {
			processor.destroy(redisCacheManager);
		}
	}
}
