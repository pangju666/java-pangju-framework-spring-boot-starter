package io.github.pangju666.framework.boot.data.redis.autoconfigure;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.data.redis.autoconfigure.DataRedisConnectionDetails;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.data.redis.connection.*;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>copy from org.springframework.boot.data.redis.autoconfigure.DataRedisConnectionConfiguration</p>
 */
abstract class DataRedisConnectionConfiguration {

	private static final boolean COMMONS_POOL2_AVAILABLE = ClassUtils.isPresent("org.apache.commons.pool2.ObjectPool",
		DataRedisConnectionConfiguration.class.getClassLoader());

	private final DataRedisProperties properties;

	private final @Nullable RedisStandaloneConfiguration standaloneConfiguration;

	private final @Nullable RedisSentinelConfiguration sentinelConfiguration;

	private final @Nullable RedisClusterConfiguration clusterConfiguration;

	private final @Nullable RedisStaticMasterReplicaConfiguration masterReplicaConfiguration;

	private final DataRedisConnectionDetails connectionDetails;

	protected final Mode mode;

	protected DataRedisConnectionConfiguration(DataRedisProperties properties,
											   DataRedisConnectionDetails connectionDetails,
											   ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
											   ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
											   ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
											   ObjectProvider<RedisStaticMasterReplicaConfiguration> masterReplicaConfiguration) {
		this.properties = properties;
		this.standaloneConfiguration = standaloneConfigurationProvider.getIfAvailable();
		this.sentinelConfiguration = sentinelConfigurationProvider.getIfAvailable();
		this.clusterConfiguration = clusterConfigurationProvider.getIfAvailable();
		this.masterReplicaConfiguration = masterReplicaConfiguration.getIfAvailable();
		this.connectionDetails = connectionDetails;
		this.mode = determineMode();
	}

	protected final RedisStandaloneConfiguration getStandaloneConfig() {
		if (this.standaloneConfiguration != null) {
			return this.standaloneConfiguration;
		}
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
		DataRedisConnectionDetails.Standalone standalone = this.connectionDetails.getStandalone();
		Assert.state(standalone != null, "'standalone' must not be null");
		config.setHostName(standalone.getHost());
		config.setPort(standalone.getPort());
		config.setUsername(this.connectionDetails.getUsername());
		config.setPassword(RedisPassword.of(this.connectionDetails.getPassword()));
		config.setDatabase(standalone.getDatabase());
		return config;
	}

	protected final @Nullable RedisSentinelConfiguration getSentinelConfig() {
		if (this.sentinelConfiguration != null) {
			return this.sentinelConfiguration;
		}
		if (this.connectionDetails.getSentinel() != null) {
			RedisSentinelConfiguration config = new RedisSentinelConfiguration();
			config.master(this.connectionDetails.getSentinel().getMaster());
			config.setSentinels(createSentinels(this.connectionDetails.getSentinel()));
			config.setUsername(this.connectionDetails.getUsername());
			String password = this.connectionDetails.getPassword();
			if (password != null) {
				config.setPassword(RedisPassword.of(password));
			}
			config.setSentinelUsername(this.connectionDetails.getSentinel().getUsername());
			String sentinelPassword = this.connectionDetails.getSentinel().getPassword();
			if (sentinelPassword != null) {
				config.setSentinelPassword(RedisPassword.of(sentinelPassword));
			}
			config.setDatabase(this.connectionDetails.getSentinel().getDatabase());
			return config;
		}
		return null;
	}

	/**
	 * Create a {@link RedisClusterConfiguration} if necessary.
	 * @return {@literal null} if no cluster settings are set.
	 */
	protected final @Nullable RedisClusterConfiguration getClusterConfiguration() {
		if (this.clusterConfiguration != null) {
			return this.clusterConfiguration;
		}
		DataRedisProperties.Cluster clusterProperties = this.properties.getCluster();
		if (this.connectionDetails.getCluster() != null) {
			RedisClusterConfiguration config = new RedisClusterConfiguration();
			config.setClusterNodes(getNodes(this.connectionDetails.getCluster()));
			if (clusterProperties != null && clusterProperties.getMaxRedirects() != null) {
				config.setMaxRedirects(clusterProperties.getMaxRedirects());
			}
			config.setUsername(this.connectionDetails.getUsername());
			String password = this.connectionDetails.getPassword();
			if (password != null) {
				config.setPassword(RedisPassword.of(password));
			}
			return config;
		}
		return null;
	}

	protected final @Nullable RedisStaticMasterReplicaConfiguration getMasterReplicaConfiguration() {
		if (this.masterReplicaConfiguration != null) {
			return this.masterReplicaConfiguration;
		}
		if (this.connectionDetails.getMasterReplica() != null) {
			List<DataRedisConnectionDetails.Node> nodes = this.connectionDetails.getMasterReplica().getNodes();
			Assert.state(!nodes.isEmpty(), "At least one node is required for master-replica configuration");
			RedisStaticMasterReplicaConfiguration config = new RedisStaticMasterReplicaConfiguration(
				nodes.get(0).host(), nodes.get(0).port());
			nodes.stream().skip(1).forEach((node) -> config.addNode(node.host(), node.port()));
			config.setUsername(this.connectionDetails.getUsername());
			String password = this.connectionDetails.getPassword();
			if (password != null) {
				config.setPassword(RedisPassword.of(password));
			}
			return config;
		}
		return null;
	}

	private List<RedisNode> getNodes(DataRedisConnectionDetails.Cluster cluster) {
		return cluster.getNodes().stream().map(this::asRedisNode).toList();
	}

	private RedisNode asRedisNode(DataRedisConnectionDetails.Node node) {
		return new RedisNode(node.host(), node.port());
	}

	protected final DataRedisProperties getProperties() {
		return this.properties;
	}

	protected @Nullable SslBundle getSslBundle() {
		return this.connectionDetails.getSslBundle();
	}

	protected final boolean isSslEnabled() {
		return getProperties().getSsl().isEnabled();
	}

	protected final boolean urlUsesSsl(String url) {
		return DataRedisUrl.of(url).useSsl();
	}

	protected boolean isPoolEnabled(DataRedisProperties.Pool pool) {
		Boolean enabled = pool.getEnabled();
		return (enabled != null) ? enabled : COMMONS_POOL2_AVAILABLE;
	}

	private List<RedisNode> createSentinels(DataRedisConnectionDetails.Sentinel sentinel) {
		List<RedisNode> nodes = new ArrayList<>();
		for (DataRedisConnectionDetails.Node node : sentinel.getNodes()) {
			nodes.add(asRedisNode(node));
		}
		return nodes;
	}

	protected final DataRedisConnectionDetails getConnectionDetails() {
		return this.connectionDetails;
	}

	private Mode determineMode() {
		if (getSentinelConfig() != null) {
			return Mode.SENTINEL;
		}
		if (getClusterConfiguration() != null) {
			return Mode.CLUSTER;
		}
		if (getMasterReplicaConfiguration() != null) {
			return Mode.MASTER_REPLICA;
		}
		return Mode.STANDALONE;
	}

	enum Mode {

		STANDALONE, CLUSTER, MASTER_REPLICA, SENTINEL

	}

}