package io.github.pangju666.framework.boot.autoconfigure.data.dynamic.mongo;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.mongo.PropertiesMongoConnectionDetails;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 将 {@link MongoProperties} 转换为 {@link MongoConnectionDetails}。
 *
 * <p>我将{@link SslBundles}做了延迟获取，防止提早创建 {@link MongoConnectionDetails} Bean，导致 {@link SslBundles} Bean 初始化失败</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class DynamicPropertiesMongoConnectionDetails extends PropertiesMongoConnectionDetails {
	private final ObjectProvider<SslBundles> sslBundlesObjectProvider;
	private final MongoProperties properties;

	public DynamicPropertiesMongoConnectionDetails(MongoProperties properties,
												   ObjectProvider<SslBundles> sslBundlesObjectProvider,
												   SslBundles sslBundles) {
		super(properties, sslBundles);
		this.properties = properties;
		this.sslBundlesObjectProvider = sslBundlesObjectProvider;
	}

	@Override
	public SslBundle getSslBundle() {
		MongoProperties.Ssl ssl = this.properties.getSsl();
		if (!ssl.isEnabled()) {
			return null;
		}
		if (StringUtils.hasLength(ssl.getBundle())) {
			SslBundles sslBundles = sslBundlesObjectProvider.getIfAvailable();
			Assert.notNull(sslBundles, "SSL bundle name has been set but no SSL bundles found in context");
			return sslBundles.getBundle(ssl.getBundle());
		}
		return SslBundle.systemDefault();
	}
}
