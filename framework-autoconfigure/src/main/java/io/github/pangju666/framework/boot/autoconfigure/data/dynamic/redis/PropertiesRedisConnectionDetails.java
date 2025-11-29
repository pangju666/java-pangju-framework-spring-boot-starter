package io.github.pangju666.framework.boot.autoconfigure.data.dynamic.redis;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Adapts {@link RedisProperties} to {@link RedisConnectionDetails}.
 *
 * <p>我将{@link SslBundles}做了延迟获取，防止提早创建 {@link RedisConnectionDetails} Bean，导致 {@link SslBundles} Bean 初始化失败</p>
 *
 * <p>copy from org.springframework.boot.autoconfigure.data.redis.PropertiesRedisConnectionDetails</p>
 *
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @author Scott Frederick
 * @author Yanming Zhou
 * @author Phillip Webb
 */
class PropertiesRedisConnectionDetails implements RedisConnectionDetails {

	private final RedisProperties properties;

	private final ObjectProvider<SslBundles> sslBundlesObjectProvider;

	PropertiesRedisConnectionDetails(RedisProperties properties, ObjectProvider<SslBundles> sslBundlesObjectProvider) {
		this.properties = properties;
		this.sslBundlesObjectProvider = sslBundlesObjectProvider;
	}

	@Override
	public String getUsername() {
		RedisUrl redisUrl = getRedisUrl();
		return (redisUrl != null) ? redisUrl.credentials().username() : this.properties.getUsername();
	}

	@Override
	public String getPassword() {
		RedisUrl redisUrl = getRedisUrl();
		return (redisUrl != null) ? redisUrl.credentials().password() : this.properties.getPassword();
	}

	@Override
	public Standalone getStandalone() {
		RedisUrl redisUrl = getRedisUrl();
		return (redisUrl != null)
			? Standalone.of(redisUrl.uri().getHost(), redisUrl.uri().getPort(), redisUrl.database(), getSslBundle())
			: Standalone.of(this.properties.getHost(), this.properties.getPort(), this.properties.getDatabase(),
			getSslBundle());
	}

	private SslBundle getSslBundle() {
		if (!this.properties.getSsl().isEnabled()) {
			return null;
		}
		String bundleName = this.properties.getSsl().getBundle();
		if (StringUtils.hasLength(bundleName)) {
			SslBundles sslBundles = this.sslBundlesObjectProvider.getIfAvailable();
			Assert.notNull(sslBundles, "SSL bundle name has been set but no SSL bundles found in context");
			return sslBundles.getBundle(bundleName);
		}
		return SslBundle.systemDefault();
	}

	@Override
	public Sentinel getSentinel() {
		RedisProperties.Sentinel sentinel = this.properties.getSentinel();
		return (sentinel != null) ? new PropertiesRedisConnectionDetails.PropertiesSentinel(getStandalone().getDatabase(), sentinel) : null;
	}

	@Override
	public Cluster getCluster() {
		RedisProperties.Cluster cluster = this.properties.getCluster();
		return (cluster != null) ? new PropertiesRedisConnectionDetails.PropertiesCluster(cluster) : null;
	}

	private RedisUrl getRedisUrl() {
		return RedisUrl.of(this.properties.getUrl());
	}

	private List<Node> asNodes(List<String> nodes) {
		return nodes.stream().map(this::asNode).toList();
	}

	private Node asNode(String node) {
		int portSeparatorIndex = node.lastIndexOf(':');
		String host = node.substring(0, portSeparatorIndex);
		int port = Integer.parseInt(node.substring(portSeparatorIndex + 1));
		return new Node(host, port);
	}

	/**
	 * {@link Cluster} implementation backed by properties.
	 */
	private class PropertiesCluster implements Cluster {

		private final List<Node> nodes;

		PropertiesCluster(RedisProperties.Cluster properties) {
			this.nodes = asNodes(properties.getNodes());
		}

		@Override
		public List<Node> getNodes() {
			return this.nodes;
		}

		@Override
		public SslBundle getSslBundle() {
			return PropertiesRedisConnectionDetails.this.getSslBundle();
		}

	}

	/**
	 * {@link Sentinel} implementation backed by properties.
	 */
	private class PropertiesSentinel implements Sentinel {

		private final int database;

		private final RedisProperties.Sentinel properties;

		PropertiesSentinel(int database, RedisProperties.Sentinel properties) {
			this.database = database;
			this.properties = properties;
		}

		@Override
		public int getDatabase() {
			return this.database;
		}

		@Override
		public String getMaster() {
			return this.properties.getMaster();
		}

		@Override
		public List<Node> getNodes() {
			return asNodes(this.properties.getNodes());
		}

		@Override
		public String getUsername() {
			return this.properties.getUsername();
		}

		@Override
		public String getPassword() {
			return this.properties.getPassword();
		}

		@Override
		public SslBundle getSslBundle() {
			return PropertiesRedisConnectionDetails.this.getSslBundle();
		}

	}

}
