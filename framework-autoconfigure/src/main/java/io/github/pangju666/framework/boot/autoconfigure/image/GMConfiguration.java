package io.github.pangju666.framework.boot.autoconfigure.image;

import io.github.pangju666.framework.boot.image.core.ImageTemplate;
import io.github.pangju666.framework.boot.image.core.impl.GMImageTemplate;
import org.gm4java.engine.support.GMConnectionPoolConfig;
import org.gm4java.engine.support.PooledGMService;
import org.im4java.core.GMOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({PooledGMService.class, GMOperation.class})
class GMConfiguration {
	@ConditionalOnProperty(prefix = "pangju.image.gm", name = "path")
	@ConditionalOnMissingBean(PooledGMService.class)
	@Bean
	public PooledGMService pooledGMService(ImageProperties properties) {
		Assert.hasText(properties.getGm().getPath(), "gm执行文件路径不可为空");

		GMConnectionPoolConfig config = new GMConnectionPoolConfig();
		config.setGMPath(properties.getGm().getPath());
		config.setMaxActive(properties.getGm().getPool().getMaxActive());
		config.setMaxIdle(properties.getGm().getPool().getMaxIdle());
		config.setMinIdle(properties.getGm().getPool().getMinIdle());
		config.setMinEvictableIdleTimeMillis(properties.getGm().getPool().getMinEvictableIdleTimeMillis());
		config.setWhenExhaustedAction(properties.getGm().getPool().getWhenExhaustedAction());
		config.setMaxWait(properties.getGm().getPool().getMaxWait().toMillis());
		config.setTestWhileIdle(properties.getGm().getPool().isTestWhileIdle());
		config.setTimeBetweenEvictionRunsMillis(properties.getGm().getPool().getTimeBetweenEvictionRunsMillis());
		return new PooledGMService(config);
	}

	@ConditionalOnProperty(prefix = "pangju.image", name = "type", havingValue = "GM")
	@ConditionalOnMissingBean(ImageTemplate.class)
	@ConditionalOnBean(PooledGMService.class)
	@Bean
	public GMImageTemplate gmTemplate(PooledGMService pooledGMService) {
		return new GMImageTemplate(pooledGMService);
	}
}
