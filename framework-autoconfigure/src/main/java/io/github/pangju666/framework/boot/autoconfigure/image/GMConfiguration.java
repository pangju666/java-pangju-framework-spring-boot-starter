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

package io.github.pangju666.framework.boot.autoconfigure.image;

import io.github.pangju666.framework.boot.image.core.ImageTemplate;
import io.github.pangju666.framework.boot.image.core.impl.GMImageTemplate;
import org.gm4java.engine.support.GMConnectionPoolConfig;
import org.gm4java.engine.support.PooledGMService;
import org.gm4java.engine.support.WhenExhaustedAction;
import org.im4java.core.GMOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * <a href="http://www.graphicsmagick.org/index.html">GraphicsMagick</a> 自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>在检测到 GM 相关类存在时，按条件提供 GM 连接池与模板实现。</li>
 *   <li>当存在配置项 {@code pangju.image.gm.path} 时，创建 {@link PooledGMService}。</li>
 *   <li>当 {@code pangju.image.type=GM} 且已存在连接池时，创建 {@link GMImageTemplate}。</li>
 * </ul>
 *
 * <p><strong>条件说明</strong></p>
 * <ul>
 *   <li>类条件：依赖 {@link PooledGMService} 与 {@link GMOperation}。</li>
 *   <li>属性条件：{@code pangju.image.gm.path} 与 {@code pangju.image.type}。</li>
 *   <li>Bean 条件：避免重复定义，使用缺失 Bean 条件与依赖 Bean 条件。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({PooledGMService.class, GMOperation.class})
class GMConfiguration {
    /**
     * 创建 GraphicsMagick 连接池服务。
     * <p>当 {@code properties.gm.path} 为空白时不创建 Bean，
	 * 配合 {@link org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean}
     * 保持按需注册）。</p>
     *
     * @param properties 自动配置属性
     * @return GM 连接池服务；当 GM 路径为空白时返回 {@code null}
     * @since 1.0.0
     */
	@ConditionalOnMissingBean(PooledGMService.class)
    @Bean
    public PooledGMService pooledGMService(ImageProperties properties) {
		String gmPath = properties.getGm().getPath();
		if (!StringUtils.hasText(gmPath)) {
			// 未配置有效 GM 路径，跳过创建
			return null;
		}

        GMConnectionPoolConfig config = new GMConnectionPoolConfig();
		config.setGMPath(gmPath);
        config.setMaxActive(properties.getGm().getPool().getMaxActive());
		WhenExhaustedAction whenExhaustedAction = switch (properties.getGm().getPool().getWhenExhaustedAction()) {
			case BLOCK -> WhenExhaustedAction.BLOCK;
			case GROW -> WhenExhaustedAction.GROW;
				case FAIL -> WhenExhaustedAction.FAIL;
		};
        config.setWhenExhaustedAction(whenExhaustedAction);
        config.setMaxWait(properties.getGm().getPool().getMaxWaitMills());
        config.setMaxIdle(properties.getGm().getPool().getMaxIdle());
        config.setMinIdle(properties.getGm().getPool().getMinIdle());
        config.setTestOnGet(properties.getGm().getPool().isTestOnGet());
        config.setTestOnReturn(properties.getGm().getPool().isTestOnReturn());
        config.setTimeBetweenEvictionRunsMillis(properties.getGm().getPool().getTimeBetweenEvictionRunsMillis());
        config.setNumTestsPerEvictionRun(properties.getGm().getPool().getNumTestsPerEvictionRun());
        config.setMinEvictableIdleTimeMillis(properties.getGm().getPool().getMinEvictableIdleTimeMillis());
        config.setSoftMinEvictableIdleTimeMillis(properties.getGm().getPool().getSoftMinEvictableIdleTimeMillis());
        config.setTestWhileIdle(properties.getGm().getPool().isTestWhileIdle());
        config.setLifo(properties.getGm().getPool().isLifo());
        config.setEvictAfterNumberOfUse(properties.getGm().getPool().getEvictAfterNumberOfUse());
		return new PooledGMService(config);
	}

	/**
	 * 创建 GM 图像处理模板实现。
	 *
	 * <p>条件：当类型为 {@code GM}、已存在连接池且未定义其它模板实现时注入。</p>
	 *
	 * <p>GraphicsMagick 版本需要 &ge; 1.30</p>
	 *
	 * @param pooledGMService GM 连接池服务
	 * @return GM 图像处理模板
	 * @since 1.0.0
	 */
    @ConditionalOnMissingBean(ImageTemplate.class)
    @ConditionalOnBean(PooledGMService.class)
	@ConditionalOnProperty(prefix = "pangju.image", name = "type", havingValue = "GRAPHICS_MAGICK")
    @Bean
    public GMImageTemplate gmTemplate(PooledGMService pooledGMService) {
        return new GMImageTemplate(pooledGMService);
    }
}
