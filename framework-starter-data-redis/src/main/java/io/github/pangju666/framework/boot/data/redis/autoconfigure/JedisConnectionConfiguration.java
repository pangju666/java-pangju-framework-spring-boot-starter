package io.github.pangju666.framework.boot.data.redis.autoconfigure;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.data.redis.autoconfigure.DataRedisConnectionDetails;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.data.redis.autoconfigure.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslOptions;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisSslClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;

import javax.net.ssl.SSLParameters;

/**
 * <p>copy from org.springframework.boot.data.redis.autoconfigure.JedisConnectionConfiguration</p>
 */
class JedisConnectionConfiguration extends DataRedisConnectionConfiguration {

	JedisConnectionConfiguration(DataRedisProperties properties,
								 ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
								 ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration,
								 ObjectProvider<RedisClusterConfiguration> clusterConfiguration,
								 ObjectProvider<RedisStaticMasterReplicaConfiguration> masterReplicaConfiguration,
								 DataRedisConnectionDetails connectionDetails) {
		super(properties, connectionDetails, standaloneConfigurationProvider, sentinelConfiguration,
			clusterConfiguration, masterReplicaConfiguration);
	}

	JedisConnectionFactory redisConnectionFactory(
		ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) {
		return createJedisConnectionFactory(builderCustomizers);
	}

	JedisConnectionFactory redisConnectionFactoryVirtualThreads(
		ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) {
		JedisConnectionFactory factory = createJedisConnectionFactory(builderCustomizers);
		SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("redis-");
		executor.setVirtualThreads(true);
		factory.setExecutor(executor);
		return factory;
	}

	private JedisConnectionFactory createJedisConnectionFactory(
		ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) {
		JedisClientConfiguration clientConfiguration = getJedisClientConfiguration(builderCustomizers);
		return switch (this.mode) {
			case STANDALONE -> new JedisConnectionFactory(getStandaloneConfig(), clientConfiguration);
			case CLUSTER -> {
				RedisClusterConfiguration clusterConfiguration = getClusterConfiguration();
				Assert.state(clusterConfiguration != null, "'clusterConfiguration' must not be null");
				yield new JedisConnectionFactory(clusterConfiguration, clientConfiguration);
			}
			case SENTINEL -> {
				RedisSentinelConfiguration sentinelConfig = getSentinelConfig();
				Assert.state(sentinelConfig != null, "'sentinelConfig' must not be null");
				yield new JedisConnectionFactory(sentinelConfig, clientConfiguration);
			}
			case MASTER_REPLICA -> throw new IllegalStateException("'masterReplicaConfig' is not supported by Jedis");
		};
	}

	private JedisClientConfiguration getJedisClientConfiguration(
		ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) {
		JedisClientConfigurationBuilder builder = applyProperties(JedisClientConfiguration.builder());
		applySslIfNeeded(builder);
		DataRedisProperties.Pool pool = getProperties().getJedis().getPool();
		if (isPoolEnabled(pool)) {
			applyPooling(pool, builder);
		}
		String url = getProperties().getUrl();
		if (StringUtils.hasText(url)) {
			customizeConfigurationFromUrl(builder, url);
		}
		builderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
		return builder.build();
	}

	private JedisClientConfigurationBuilder applyProperties(JedisClientConfigurationBuilder builder) {
		PropertyMapper map = PropertyMapper.get();
		map.from(getProperties().getTimeout()).to(builder::readTimeout);
		map.from(getProperties().getConnectTimeout()).to(builder::connectTimeout);
		map.from(getProperties().getClientName()).whenHasText().to(builder::clientName);
		return builder;
	}

	private void applySslIfNeeded(JedisClientConfigurationBuilder builder) {
		SslBundle sslBundle = getSslBundle();
		if (sslBundle == null) {
			return;
		}
		JedisSslClientConfigurationBuilder sslBuilder = builder.useSsl();
		sslBuilder.sslSocketFactory(sslBundle.createSslContext().getSocketFactory());
		SslOptions sslOptions = sslBundle.getOptions();
		SSLParameters sslParameters = new SSLParameters();
		PropertyMapper map = PropertyMapper.get();
		map.from(sslOptions.getCiphers()).to(sslParameters::setCipherSuites);
		map.from(sslOptions.getEnabledProtocols()).to(sslParameters::setProtocols);
		sslBuilder.sslParameters(sslParameters);
	}

	private void applyPooling(DataRedisProperties.Pool pool,
							  JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
		builder.usePooling().poolConfig(jedisPoolConfig(pool));
	}

	private JedisPoolConfig jedisPoolConfig(DataRedisProperties.Pool pool) {
		JedisPoolConfig config = new JedisPoolConfig();
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

	private void customizeConfigurationFromUrl(JedisClientConfiguration.JedisClientConfigurationBuilder builder,
											   String url) {
		if (urlUsesSsl(url)) {
			builder.useSsl();
		}
	}

}
