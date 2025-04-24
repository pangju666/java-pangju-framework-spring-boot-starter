package io.github.pangju666.framework.autoconfigure.cache.hash.listener;

import io.github.pangju666.framework.autoconfigure.cache.hash.HashCacheManager;
import io.github.pangju666.framework.autoconfigure.cache.hash.HashCacheProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.Map;

public class HashCacheInitApplicationListener implements ApplicationListener<ApplicationReadyEvent> {
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		HashCacheManager hashCacheManager = event.getApplicationContext().getBean(HashCacheManager.class);
		Map<String, HashCacheProcessor> beanMap = event.getApplicationContext().getBeansOfType(HashCacheProcessor.class);
		for (HashCacheProcessor processor : beanMap.values()) {
			processor.init(hashCacheManager);
		}
	}
}
