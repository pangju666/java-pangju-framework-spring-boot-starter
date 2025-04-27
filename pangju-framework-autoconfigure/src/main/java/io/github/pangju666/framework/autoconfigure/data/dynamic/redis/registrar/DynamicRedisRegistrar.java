package io.github.pangju666.framework.autoconfigure.data.dynamic.redis.registrar;

import io.github.pangju666.framework.autoconfigure.data.dynamic.redis.properties.DynamicRedisProperties;
import io.github.pangju666.framework.autoconfigure.data.dynamic.redis.utils.DynamicRedisUtils;
import io.github.pangju666.framework.autoconfigure.data.redis.utils.RedisSerializerUtils;
import io.lettuce.core.resource.ClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

public class DynamicRedisRegistrar implements EnvironmentAware, BeanFactoryAware, ImportBeanDefinitionRegistrar {
	private static final Logger log = LoggerFactory.getLogger(DynamicRedisRegistrar.class);
	private static final boolean COMMONS_POOL2_AVAILABLE = ClassUtils.isPresent(
		"org.apache.commons.pool2.ObjectPool", DynamicRedisRegistrar.class.getClassLoader());

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
			log.error("配置动态redis数据源失败，请检查是否存在相关配置");
			return;
		}

		Map<String, DynamicRedisProperties.RedisProperties> redisDatabases = dynamicRedisProperties.getDatabases();
		if (!CollectionUtils.isEmpty(redisDatabases)) {
			redisDatabases.forEach((name, redisProperties) -> {
				Supplier<RedisConnectionFactory> redisConnectionFactorySupplier = () -> {
					RedisConnectionFactory connectionFactory;
					if (redisProperties.getClientType() == RedisProperties.ClientType.JEDIS) {
						connectionFactory = createJedisConnectionFactory(redisProperties);
					} else {
						ClientResources clientResources = beanFactory.getBean(ClientResources.class);
						connectionFactory = createLettuceConnectionFactory(redisProperties, clientResources);
					}
					return connectionFactory;
				};

				BeanDefinitionBuilder connectionFactoryBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(RedisConnectionFactory.class, redisConnectionFactorySupplier);
				AbstractBeanDefinition connectionFactoryBeanDefinition = connectionFactoryBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(DynamicRedisUtils.getRedisConnectionFactoryBeanName(name), connectionFactoryBeanDefinition);

				RedisConnectionFactory connectionFactory = redisConnectionFactorySupplier.get();
				GenericBeanDefinition redisTemplateBeanDefinition = new GenericBeanDefinition();
				redisTemplateBeanDefinition.setBeanClass(RedisTemplate.class);
				redisTemplateBeanDefinition.getPropertyValues().add("connectionFactory", connectionFactory);
				redisTemplateBeanDefinition.getPropertyValues().add("keySerializer", RedisSerializerUtils.getSerializer(redisProperties.getKeySerializer()));
				redisTemplateBeanDefinition.getPropertyValues().add("hashKeySerializer", RedisSerializerUtils.getSerializer(redisProperties.getHashKeySerializer()));
				redisTemplateBeanDefinition.getPropertyValues().add("valueSerializer", RedisSerializerUtils.getSerializer(redisProperties.getValueSerializer()));
				redisTemplateBeanDefinition.getPropertyValues().add("hashValueSerializer", RedisSerializerUtils.getSerializer(redisProperties.getHashValueSerializer()));

				if (dynamicRedisProperties.getPrimary().equals(name)) {
					GenericBeanDefinition primaryRedisTemplateBeanDefinition = new GenericBeanDefinition(redisTemplateBeanDefinition);
					connectionFactoryBeanDefinition.setPrimary(true);
					primaryRedisTemplateBeanDefinition.setPrimary(true);
					beanDefinitionRegistry.registerBeanDefinition("redisTemplate", primaryRedisTemplateBeanDefinition);
				}
				beanDefinitionRegistry.registerBeanDefinition(DynamicRedisUtils.getRedisTemplateBeanName(name), redisTemplateBeanDefinition);

				log.info("dynamic-redis - add a database named [{}] success", name);
			});
			log.info("dynamic-redis initial loaded [{}] database,primary database named [{}]", redisDatabases.size(), dynamicRedisProperties.getPrimary());
		}
	}

	private RedisConnectionFactory createJedisConnectionFactory(DynamicRedisProperties.RedisProperties redisProperties) {
		JedisClientConfiguration.JedisClientConfigurationBuilder builder = JedisClientConfiguration.builder();
		if (redisProperties.getSsl().isEnabled()) {
			builder.useSsl();
		}
		if (Objects.nonNull(redisProperties.getTimeout())) {
			builder.readTimeout(redisProperties.getTimeout());
		}
		if (Objects.nonNull(redisProperties.getConnectTimeout())) {
			builder.connectTimeout(redisProperties.getConnectTimeout());
		}
		if (StringUtils.hasText(redisProperties.getClientName())) {
			builder.clientName(redisProperties.getClientName());
		}
		if (isPoolEnabled(redisProperties.getJedis().getPool())) {
			GenericObjectPoolConfig<?> poolConfig = getPoolConfig(redisProperties.getJedis().getPool());
			poolConfig.setTestWhileIdle(true);
			poolConfig.setMinEvictableIdleDuration(Duration.ofMillis(60000));
			poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(30000));
			poolConfig.setNumTestsPerEvictionRun(-1);
			builder.usePooling().poolConfig(poolConfig);
		}

		RedisStandaloneConfiguration standaloneConfiguration = getRedisConfig(redisProperties);
		JedisClientConfiguration clientConfiguration = builder.build();
		JedisConnectionFactory connectionFactory = new JedisConnectionFactory(standaloneConfiguration, clientConfiguration);
		connectionFactory.afterPropertiesSet();
		return connectionFactory;
	}

	private RedisConnectionFactory createLettuceConnectionFactory(DynamicRedisProperties.RedisProperties redisProperties, ClientResources clientResources) {
		LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();
		if (isPoolEnabled(redisProperties.getLettuce().getPool())) {
			GenericObjectPoolConfig<?> poolConfig = getPoolConfig(redisProperties.getLettuce().getPool());
			builder = LettucePoolingClientConfiguration.builder()
				.clientResources(clientResources)
				.poolConfig(poolConfig);
		}
		if (redisProperties.getSsl().isEnabled()) {
			builder.useSsl();
		}
		if (Objects.nonNull(redisProperties.getTimeout())) {
			builder.commandTimeout(redisProperties.getTimeout());
		}
		if (redisProperties.getLettuce() != null) {
			RedisProperties.Lettuce lettuce = redisProperties.getLettuce();
			if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
				builder.shutdownTimeout(redisProperties.getLettuce().getShutdownTimeout());
			}
		}
		if (StringUtils.hasText(redisProperties.getClientName())) {
			builder.clientName(redisProperties.getClientName());
		}

		RedisStandaloneConfiguration redisConfig = getRedisConfig(redisProperties);
		LettuceClientConfiguration clientConfiguration = builder.build();
		LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisConfig, clientConfiguration);
		connectionFactory.afterPropertiesSet();
		return connectionFactory;
	}

	private GenericObjectPoolConfig<?> getPoolConfig(RedisProperties.Pool pool) {
		GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
		config.setMaxTotal(pool.getMaxActive());
		config.setMaxIdle(pool.getMaxIdle());
		config.setMinIdle(pool.getMinIdle());
		if (pool.getTimeBetweenEvictionRuns() != null) {
			config.setTimeBetweenEvictionRuns(pool.getTimeBetweenEvictionRuns());
		}
		if (pool.getMaxWait() != null) {
			config.setMaxWait(pool.getMaxWait());
		}
		return config;
	}

	private RedisStandaloneConfiguration getRedisConfig(DynamicRedisProperties.RedisProperties redisProperties) {
		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
		redisConfig.setHostName(redisProperties.getHost());
		redisConfig.setPort(redisProperties.getPort());
		redisConfig.setUsername(redisProperties.getUsername());
		redisConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));
		redisConfig.setDatabase(redisProperties.getDatabase());
		return redisConfig;
	}

	private boolean isPoolEnabled(RedisProperties.Pool pool) {
		Boolean enabled = pool.getEnabled();
		return (enabled != null) ? enabled : COMMONS_POOL2_AVAILABLE;
	}
}
