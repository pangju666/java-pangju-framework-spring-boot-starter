package io.github.pangju666.framework.boot.data.redis.autoconfigure;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions.Builder;
import io.lettuce.core.resource.ClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.data.redis.autoconfigure.DataRedisConnectionDetails;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientOptionsBuilderCustomizer;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslOptions;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.LettuceClientConfigurationBuilder;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * <p>copy from org.springframework.boot.data.redis.autoconfigure.LettuceConnectionConfiguration</p>
 */
class LettuceConnectionConfiguration extends DataRedisConnectionConfiguration {

	LettuceConnectionConfiguration(DataRedisProperties properties,
								   ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
								   ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
								   ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
								   ObjectProvider<RedisStaticMasterReplicaConfiguration> masterReplicaConfiguration,
								   DataRedisConnectionDetails connectionDetails) {
		super(properties, connectionDetails, standaloneConfigurationProvider, sentinelConfigurationProvider,
			clusterConfigurationProvider, masterReplicaConfiguration);
	}

	LettuceConnectionFactory redisConnectionFactory(
		ObjectProvider<LettuceClientConfigurationBuilderCustomizer> clientConfigurationBuilderCustomizers,
		ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers,
		ClientResources clientResources) {
		return createConnectionFactory(clientConfigurationBuilderCustomizers, clientOptionsBuilderCustomizers,
			clientResources);
	}

	LettuceConnectionFactory redisConnectionFactoryVirtualThreads(
		ObjectProvider<LettuceClientConfigurationBuilderCustomizer> clientConfigurationBuilderCustomizers,
		ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers,
		ClientResources clientResources) {
		LettuceConnectionFactory factory = createConnectionFactory(clientConfigurationBuilderCustomizers,
			clientOptionsBuilderCustomizers, clientResources);
		SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("redis-");
		executor.setVirtualThreads(true);
		factory.setExecutor(executor);
		return factory;
	}

	private LettuceConnectionFactory createConnectionFactory(
		ObjectProvider<LettuceClientConfigurationBuilderCustomizer> clientConfigurationBuilderCustomizers,
		ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers,
		ClientResources clientResources) {
		LettuceClientConfiguration clientConfiguration = getLettuceClientConfiguration(
			clientConfigurationBuilderCustomizers, clientOptionsBuilderCustomizers, clientResources,
			getProperties().getLettuce().getPool());
		return switch (this.mode) {
			case STANDALONE -> new LettuceConnectionFactory(getStandaloneConfig(), clientConfiguration);
			case CLUSTER -> {
				RedisClusterConfiguration clusterConfiguration = getClusterConfiguration();
				Assert.state(clusterConfiguration != null, "'clusterConfiguration' must not be null");
				yield new LettuceConnectionFactory(clusterConfiguration, clientConfiguration);
			}
			case SENTINEL -> {
				RedisSentinelConfiguration sentinelConfig = getSentinelConfig();
				Assert.state(sentinelConfig != null, "'sentinelConfig' must not be null");
				yield new LettuceConnectionFactory(sentinelConfig, clientConfiguration);
			}
			case MASTER_REPLICA -> {
				RedisStaticMasterReplicaConfiguration masterReplicaConfiguration = getMasterReplicaConfiguration();
				Assert.state(masterReplicaConfiguration != null, "'masterReplicaConfig' must not be null");
				yield new LettuceConnectionFactory(masterReplicaConfiguration, clientConfiguration);
			}
		};
	}

	private LettuceClientConfiguration getLettuceClientConfiguration(
		ObjectProvider<LettuceClientConfigurationBuilderCustomizer> clientConfigurationBuilderCustomizers,
		ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers,
		ClientResources clientResources, DataRedisProperties.Pool pool) {
		LettuceClientConfigurationBuilder builder = createBuilder(pool);
		SslBundle sslBundle = getSslBundle();
		applyProperties(builder, sslBundle);
		String url = getProperties().getUrl();
		if (StringUtils.hasText(url)) {
			customizeConfigurationFromUrl(builder, url);
		}
		builder.clientOptions(createClientOptions(clientOptionsBuilderCustomizers, sslBundle));
		builder.clientResources(clientResources);
		clientConfigurationBuilderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
		return builder.build();
	}

	private LettuceClientConfigurationBuilder createBuilder(DataRedisProperties.Pool pool) {
		if (isPoolEnabled(pool)) {
			return new LettuceConnectionConfiguration.PoolBuilderFactory().createBuilder(pool);
		}
		return LettuceClientConfiguration.builder();
	}

	private void applyProperties(LettuceClientConfigurationBuilder builder, @Nullable SslBundle sslBundle) {
		if (sslBundle != null) {
			builder.useSsl();
		}
		if (getProperties().getTimeout() != null) {
			builder.commandTimeout(getProperties().getTimeout());
		}
		if (getProperties().getLettuce() != null) {
			DataRedisProperties.Lettuce lettuce = getProperties().getLettuce();
			if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
				builder.shutdownTimeout(getProperties().getLettuce().getShutdownTimeout());
			}
			String readFrom = lettuce.getReadFrom();
			if (readFrom != null) {
				builder.readFrom(getReadFrom(readFrom));
			}
		}
		if (StringUtils.hasText(getProperties().getClientName())) {
			builder.clientName(getProperties().getClientName());
		}
	}

	private ReadFrom getReadFrom(String readFrom) {
		int index = readFrom.indexOf(':');
		if (index == -1) {
			return ReadFrom.valueOf(getCanonicalReadFromName(readFrom));
		}
		String name = getCanonicalReadFromName(readFrom.substring(0, index));
		String value = readFrom.substring(index + 1);
		return ReadFrom.valueOf(name + ":" + value);
	}

	private String getCanonicalReadFromName(String name) {
		StringBuilder canonicalName = new StringBuilder(name.length());
		name.chars()
			.filter(Character::isLetterOrDigit)
			.map(Character::toLowerCase)
			.forEach((c) -> canonicalName.append((char) c));
		return canonicalName.toString();
	}

	private ClientOptions createClientOptions(
		ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientConfigurationBuilderCustomizers,
		@Nullable SslBundle sslBundle) {
		ClientOptions.Builder builder = initializeClientOptionsBuilder();
		Duration connectTimeout = getProperties().getConnectTimeout();
		if (connectTimeout != null) {
			builder.socketOptions(SocketOptions.builder().connectTimeout(connectTimeout).build());
		}
		if (sslBundle != null) {
			io.lettuce.core.SslOptions.Builder sslOptionsBuilder = io.lettuce.core.SslOptions.builder();
			sslOptionsBuilder.keyManager(sslBundle.getManagers().getKeyManagerFactory());
			sslOptionsBuilder.trustManager(sslBundle.getManagers().getTrustManagerFactory());
			SslOptions sslOptions = sslBundle.getOptions();
			if (sslOptions.getCiphers() != null) {
				sslOptionsBuilder.cipherSuites(sslOptions.getCiphers());
			}
			if (sslOptions.getEnabledProtocols() != null) {
				sslOptionsBuilder.protocols(sslOptions.getEnabledProtocols());
			}
			builder.sslOptions(sslOptionsBuilder.build());
		}
		builder.timeoutOptions(TimeoutOptions.enabled());
		clientConfigurationBuilderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
		return builder.build();
	}

	private ClientOptions.Builder initializeClientOptionsBuilder() {
		if (getProperties().getCluster() != null) {
			ClusterClientOptions.Builder builder = ClusterClientOptions.builder();
			DataRedisProperties.Lettuce.Cluster.Refresh refreshProperties = getProperties().getLettuce().getCluster().getRefresh();
			Builder refreshBuilder = ClusterTopologyRefreshOptions.builder()
				.dynamicRefreshSources(refreshProperties.isDynamicRefreshSources());
			if (refreshProperties.getPeriod() != null) {
				refreshBuilder.enablePeriodicRefresh(refreshProperties.getPeriod());
			}
			if (refreshProperties.isAdaptive()) {
				refreshBuilder.enableAllAdaptiveRefreshTriggers();
			}
			return builder.topologyRefreshOptions(refreshBuilder.build());
		}
		return ClientOptions.builder();
	}

	private void customizeConfigurationFromUrl(LettuceClientConfiguration.LettuceClientConfigurationBuilder builder,
											   String url) {
		if (urlUsesSsl(url)) {
			builder.useSsl();
		}
	}

	/**
	 * Inner class to allow optional commons-pool2 dependency.
	 */
	private static final class PoolBuilderFactory {

		LettuceClientConfigurationBuilder createBuilder(DataRedisProperties.Pool properties) {
			return LettucePoolingClientConfiguration.builder().poolConfig(getPoolConfig(properties));
		}

		private GenericObjectPoolConfig<StatefulConnection<?, ?>> getPoolConfig(DataRedisProperties.Pool properties) {
			GenericObjectPoolConfig<StatefulConnection<?, ?>> config = new GenericObjectPoolConfig<>();
			config.setMaxTotal(properties.getMaxActive());
			config.setMaxIdle(properties.getMaxIdle());
			config.setMinIdle(properties.getMinIdle());
			if (properties.getTimeBetweenEvictionRuns() != null) {
				config.setTimeBetweenEvictionRuns(properties.getTimeBetweenEvictionRuns());
			}
			if (properties.getMaxWait() != null) {
				config.setMaxWait(properties.getMaxWait());
			}
			return config;
		}

	}

}
