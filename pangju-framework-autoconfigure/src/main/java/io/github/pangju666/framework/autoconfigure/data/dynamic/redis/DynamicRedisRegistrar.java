/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.framework.autoconfigure.data.dynamic.redis;

import io.github.pangju666.framework.autoconfigure.data.dynamic.redis.config.JedisConnectionConfiguration;
import io.github.pangju666.framework.autoconfigure.data.dynamic.redis.config.LettuceConnectionConfiguration;
import io.github.pangju666.framework.autoconfigure.data.dynamic.redis.config.PropertiesRedisConnectionDetails;
import io.github.pangju666.framework.autoconfigure.data.dynamic.redis.utils.DynamicRedisUtils;
import io.github.pangju666.framework.autoconfigure.data.redis.utils.RedisSerializerUtils;
import io.github.pangju666.framework.data.redis.bean.ScanRedisTemplate;
import io.lettuce.core.resource.ClientResources;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientOptionsBuilderCustomizer;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class DynamicRedisRegistrar implements EnvironmentAware, BeanFactoryAware, ImportBeanDefinitionRegistrar {
	private static final Logger log = LoggerFactory.getLogger(DynamicRedisRegistrar.class);

	private BeanFactory beanFactory;
	private Binder binder;

	@Override
	public void setEnvironment(Environment environment) {
		this.binder = Binder.get(environment);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
		DynamicRedisProperties dynamicRedisProperties;

		try {
			dynamicRedisProperties = binder.bind(DynamicRedisProperties.PREFIX, DynamicRedisProperties.class).get();
			Assert.notEmpty(dynamicRedisProperties.getDatabases(), "动态redis配置：数据源集合不可为空");
			Assert.hasText(dynamicRedisProperties.getPrimary(), "动态redis配置：主数据源不可为空");
			if (!dynamicRedisProperties.getDatabases().containsKey(dynamicRedisProperties.getPrimary())) {
				throw new IllegalArgumentException("动态redis配置：主数据源必须为存在的数据源");
			}
		} catch (NoSuchElementException e) {
			return;
		}

		Map<String, DynamicRedisProperties.RedisProperties> redisDatabases = dynamicRedisProperties.getDatabases();
		if (!CollectionUtils.isEmpty(redisDatabases)) {
			redisDatabases.forEach((name, redisProperties) -> {
				Supplier<RedisConnectionFactory> connectionFactorySupplier = () -> {
					PropertiesRedisConnectionDetails connectionDetails = new PropertiesRedisConnectionDetails(redisProperties,
						beanFactory.getBeanProvider(SslBundles.class).getIfAvailable());

					RedisConnectionFactory connectionFactory;
					if (redisProperties.getClientType() == DynamicRedisProperties.RedisProperties.ClientType.JEDIS) {
						JedisConnectionConfiguration connectionConfiguration = new JedisConnectionConfiguration(redisProperties,
							beanFactory.getBeanProvider(RedisStandaloneConfiguration.class),
							beanFactory.getBeanProvider(RedisSentinelConfiguration.class),
							beanFactory.getBeanProvider(RedisClusterConfiguration.class),
							connectionDetails);
						if (isVirtualThreadSupported()) {
							connectionFactory = connectionConfiguration.createRedisConnectionFactoryVirtualThreads(
								beanFactory.getBeanProvider(JedisClientConfigurationBuilderCustomizer.class));
						} else {
							connectionFactory = connectionConfiguration.createRedisConnectionFactory(
								beanFactory.getBeanProvider(JedisClientConfigurationBuilderCustomizer.class));
						}
					} else {
						ClientResources clientResources = beanFactory.getBean(ClientResources.class);
						LettuceConnectionConfiguration connectionConfiguration = new LettuceConnectionConfiguration(redisProperties,
							beanFactory.getBeanProvider(RedisStandaloneConfiguration.class),
							beanFactory.getBeanProvider(RedisSentinelConfiguration.class),
							beanFactory.getBeanProvider(RedisClusterConfiguration.class),
							connectionDetails);
						if (isVirtualThreadSupported()) {
							connectionFactory = connectionConfiguration.createRedisConnectionFactoryVirtualThreads(
								beanFactory.getBeanProvider(LettuceClientConfigurationBuilderCustomizer.class),
								beanFactory.getBeanProvider(LettuceClientOptionsBuilderCustomizer.class),
								clientResources);
						} else {
							connectionFactory = connectionConfiguration.createRedisConnectionFactory(
								beanFactory.getBeanProvider(LettuceClientConfigurationBuilderCustomizer.class),
								beanFactory.getBeanProvider(LettuceClientOptionsBuilderCustomizer.class),
								clientResources);
						}
					}
					return connectionFactory;
				};
				String connectionFactoryBeanName = DynamicRedisUtils.getRedisConnectionFactoryBeanName(name);
				BeanDefinitionBuilder connectionFactoryBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					RedisConnectionFactory.class, connectionFactorySupplier);
				AbstractBeanDefinition connectionFactoryBeanDefinition = connectionFactoryBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(connectionFactoryBeanName, connectionFactoryBeanDefinition);

				Supplier<ScanRedisTemplate> redisTemplateSupplier = () -> {
					ScanRedisTemplate<Object, Object> redisTemplate = new ScanRedisTemplate<>();
					redisTemplate.setKeySerializer(RedisSerializerUtils.getSerializer(redisProperties.getKeySerializer()));
					redisTemplate.setValueSerializer(RedisSerializerUtils.getSerializer(redisProperties.getValueSerializer()));
					redisTemplate.setHashKeySerializer(RedisSerializerUtils.getSerializer(redisProperties.getHashKeySerializer()));
					redisTemplate.setHashValueSerializer(RedisSerializerUtils.getSerializer(redisProperties.getHashValueSerializer()));
					redisTemplate.setConnectionFactory(beanFactory.getBean(connectionFactoryBeanName, RedisConnectionFactory.class));
					return redisTemplate;
				};
				BeanDefinitionBuilder redisTemplateBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					ScanRedisTemplate.class, redisTemplateSupplier);
				redisTemplateBeanBuilder.addDependsOn(connectionFactoryBeanName);
				AbstractBeanDefinition redisTemplateBeanDefinition = redisTemplateBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(DynamicRedisUtils.getRedisTemplateBeanName(name), redisTemplateBeanDefinition);

				if (dynamicRedisProperties.getPrimary().equals(name)) {
					connectionFactoryBeanDefinition.setPrimary(true);

					GenericBeanDefinition primaryRedisTemplateBeanDefinition = new GenericBeanDefinition(redisTemplateBeanDefinition);
					primaryRedisTemplateBeanDefinition.setPrimary(true);
					beanDefinitionRegistry.registerBeanDefinition("redisTemplate", primaryRedisTemplateBeanDefinition);
				}

				log.info("dynamic-redis - add a database named [{}] success", name);
			});
			log.info("dynamic-redis initial loaded [{}] database,primary database named [{}]", redisDatabases.size(),
				dynamicRedisProperties.getPrimary());
		}
	}

	private boolean isVirtualThreadSupported() {
		// Java 版本必须 >= 21
		String version = System.getProperty("java.version");
		if (Integer.parseInt(StringUtils.substringBefore(version, ".")) < 21) {
			return false;
		}

		try {
			// 检查 Thread 中是否存在 ofVirtual() 方法
			Class<?> threadClass = Class.forName("java.lang.Thread");
			threadClass.getMethod("ofVirtual");
			return true;
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			return false;
		}
	}
}
