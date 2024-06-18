package io.github.pangju666.framework.autoconfigure.cache.redis.listener;

import io.github.pangju666.framework.autoconfigure.cache.redis.RedisCacheManager;
import io.github.pangju666.framework.autoconfigure.cache.redis.RedisCacheProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Map;

public class RedisCacheClearApplicationListener implements ApplicationListener<ContextClosedEvent> {
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        RedisCacheManager redisCacheManager = event.getApplicationContext().getBean(RedisCacheManager.class);
        Map<String, RedisCacheProcessor> beanMap = event.getApplicationContext().getBeansOfType(RedisCacheProcessor.class);
        for (RedisCacheProcessor processor : beanMap.values()) {
            processor.destroy(redisCacheManager);
        }
    }
}
