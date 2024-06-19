package io.github.pangju666.framework.autoconfigure.cache.redis;

import io.github.pangju666.framework.autoconfigure.cache.redis.aspect.RedisCacheAspect;
import io.github.pangju666.framework.autoconfigure.cache.redis.properties.RedisCacheProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
@EnableConfigurationProperties(RedisCacheProperties.class)
public class RedisCacheAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "cacheRedisTemplate")
    @ConditionalOnSingleCandidate(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> cacheRedisTemplate(RedisConnectionFactory redisConnectionFactory,
                                                            RedisCacheProperties redisCacheProperties) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(redisCacheProperties.getValueSerializer().getSerializer());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(redisCacheProperties.getValueSerializer().getSerializer());
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(value = RedisTemplate.class, name = "cacheRedisTemplate")
    public RedisCacheManager redisCacheManager(@Qualifier("cacheRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                                               RedisCacheProperties redisCacheProperties) {
        return new RedisCacheManager(redisTemplate, redisCacheProperties);
    }

    @Bean
    @ConditionalOnBean(RedisCacheManager.class)
    public RedisCacheAspect redisCacheAspect(RedisCacheManager redisCacheManager) {
        return new RedisCacheAspect(redisCacheManager);
    }
}
