package io.github.pangju666.framework.autoconfigure.cache.spring.registrar;

import io.github.pangju666.framework.autoconfigure.cache.spring.utils.DynamicRedisCacheUtils;
import io.github.pangju666.framework.autoconfigure.data.dynamic.redis.properties.DynamicRedisProperties;
import io.github.pangju666.framework.autoconfigure.data.dynamic.redis.utils.DynamicRedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class DynamicRedisCacheManagerRegistrar implements BeanFactoryAware, EnvironmentAware, ImportBeanDefinitionRegistrar {
	private static final Logger log = LoggerFactory.getLogger(DynamicRedisCacheManagerRegistrar.class);

	private BeanFactory beanFactory;
	private Binder binder;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.binder = Binder.get(environment);
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
		DynamicRedisProperties dynamicRedisProperties;
		CacheProperties.Redis redisCacheProperties;
		try {
			CacheProperties cacheProperties = binder.bind("spring.cache", CacheProperties.class).get();
			if (cacheProperties.getType() != CacheType.REDIS) {
				return;
			}

			dynamicRedisProperties = binder.bind(DynamicRedisProperties.PREFIX, DynamicRedisProperties.class).get();
			Assert.notEmpty(dynamicRedisProperties.getDatabases(), "动态redis缓存配置：数据源集合不可为空");
			Assert.hasText(dynamicRedisProperties.getPrimary(), "动态redis缓存配置：主数据源不可为空");
			if (!dynamicRedisProperties.getDatabases().containsKey(dynamicRedisProperties.getPrimary())) {
				throw new IllegalArgumentException("动态redis配置：主数据源必须为存在的数据源");
			}
			redisCacheProperties = cacheProperties.getRedis();
		} catch (NoSuchElementException e) {
			log.error("动态redis缓存配置失败，请检查是否存在相关配置");
			return;
		}

		Map<String, DynamicRedisProperties.RedisProperties> redisDatabases = dynamicRedisProperties.getDatabases();
		if (!CollectionUtils.isEmpty(redisDatabases)) {
			redisDatabases.forEach((name, redisProperties) -> {
				BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RedisCacheManager.class, () -> {
					String connectionFactoryBeanName = DynamicRedisUtils.getRedisConnectionFactoryBeanName(name);
					RedisConnectionFactory connectionFactory = this.beanFactory.getBean(connectionFactoryBeanName, RedisConnectionFactory.class);
					RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);

					RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
					if (!redisCacheProperties.isUseKeyPrefix()) {
						redisCacheConfiguration = redisCacheConfiguration.disableKeyPrefix();
					}
					if (!redisCacheProperties.isCacheNullValues()) {
						redisCacheConfiguration = redisCacheConfiguration.disableCachingNullValues();
					}
					if (StringUtils.hasText(redisCacheProperties.getKeyPrefix())) {
						redisCacheConfiguration = redisCacheConfiguration.prefixCacheNameWith(redisCacheProperties.getKeyPrefix());
					}
					if (Objects.nonNull(redisCacheProperties.getTimeToLive())) {
						redisCacheConfiguration = redisCacheConfiguration.entryTtl(redisCacheProperties.getTimeToLive());
					}
					if (redisCacheProperties.isEnableStatistics()) {
						cacheWriter.withStatisticsCollector(CacheStatisticsCollector.create());
					}
					redisCacheConfiguration = redisCacheConfiguration.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));
					redisCacheConfiguration = redisCacheConfiguration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisProperties.getValueSerializer().getSerializer()));

					return new RedisCacheManager(cacheWriter, redisCacheConfiguration, true, new LinkedHashMap<>());
				});
				AbstractBeanDefinition redisCacheManagerBeanDefinition = beanDefinitionBuilder.getRawBeanDefinition();

				if (dynamicRedisProperties.getPrimary().equals(name)) {
					GenericBeanDefinition primaryRedisCacheManagerBeanDefinition = new GenericBeanDefinition(redisCacheManagerBeanDefinition);
					primaryRedisCacheManagerBeanDefinition.setPrimary(true);
					beanDefinitionRegistry.registerBeanDefinition("cacheResolver", primaryRedisCacheManagerBeanDefinition);
				}
				beanDefinitionRegistry.registerBeanDefinition(DynamicRedisCacheUtils.getCacheManagerBeanName(name), redisCacheManagerBeanDefinition);
			});
		}
	}
}
