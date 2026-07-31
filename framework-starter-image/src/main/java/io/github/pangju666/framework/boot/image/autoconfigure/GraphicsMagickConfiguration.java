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

package io.github.pangju666.framework.boot.image.autoconfigure;

import io.github.pangju666.framework.boot.image.core.ImageOperationsTemplate;
import io.github.pangju666.framework.boot.image.core.ImageSplitTemplate;
import io.github.pangju666.framework.boot.image.core.ImageTemplate;
import io.github.pangju666.framework.boot.image.core.impl.GMImageTemplate;
import io.github.pangju666.framework.boot.image.core.impl.GraphicsMagickOperationsTemplate;
import io.github.pangju666.framework.boot.image.core.impl.GraphicsMagickSplitTemplate;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.gm4java.engine.support.GMConnectionPoolConfig;
import org.gm4java.engine.support.PooledGMService;
import org.gm4java.engine.support.WhenExhaustedAction;
import org.im4java.core.GMOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * <a href="http://www.graphicsmagick.org/index.html">GraphicsMagick</a> 自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>在检测到 GM 相关类存在时，按条件提供 GM 连接池与模板实现。</li>
 *   <li>创建 {@link PooledGMService} GM 连接池。</li>
 *   <li>当 {@code pangju.image.type=GRAPHICS_MAGICK} 且已存在连接池时，创建 {@link GMImageTemplate}。</li>
 *   <li>当 {@code pangju.image.type=GRAPHICS_MAGICK} 且已存在连接池时，创建 {@link GraphicsMagickOperationsTemplate}。</li>
 *   <li>当 {@code pangju.image.split-type=GRAPHICS_MAGICK} 且已存在连接池时，创建 {@link ImageSplitTemplate}。当配置项未设置时，默认使用 GraphicsMagick 作为分割实现。</li>
 * </ul>
 *
 * <p><strong>条件说明</strong></p>
 * <ul>
 *   <li>类条件：依赖 {@link PooledGMService} 与 {@link GMOperation}。</li>
 *   <li>属性条件：{@code pangju.image.graphics-magick.path}、{@code pangju.image.type} 与 {@code pangju.image.split-type}。</li>
 *   <li>Bean 条件：避免重复定义，使用缺失 Bean 条件与依赖 Bean 条件。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({PooledGMService.class, GMOperation.class})
class GraphicsMagickConfiguration {
	/**
	 * 日志记录器
	 *
	 * @since 2.1.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphicsMagickConfiguration.class);

	/**
	 * 创建 GraphicsMagick 连接池服务。
	 *
	 * <p>在创建前验证GraphicsMagick可执行文件路径是否已配置且有效。</p>
	 *
	 * <p>
	 * 验证步骤：
	 * <ol>
	 *   <li>检查路径是否已配置，未配置则记录警告日志并返回null</li>
	 *   <li>执行GraphicsMagick命令验证路径有效性，执行失败则记录警告日志并返回null</li>
	 * </ol>
	 * </p>
	 *
	 * @param properties 自动配置属性
	 * @return GM 连接池服务；当 GM 路径为空白或无效时返回 {@code null}
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(PooledGMService.class)
	@Bean
	public PooledGMService pooledGMService(ImageProperties properties) {
		String gmPath = properties.getGraphicsMagick().getPath();
		if (!StringUtils.hasText(gmPath)) {
			LOGGER.warn("未配置 GraphicsMagick 可执行文件路径");
			// 未配置有效 GM 路径，跳过创建
			return null;
		}

		try {
			CommandLine cmdLine = new CommandLine(gmPath);
			cmdLine.addArgument("--version");

			DefaultExecutor executor = DefaultExecutor.builder().get();
			executor.setExitValue(0);
			executor.execute(cmdLine);
		} catch (IOException e) {
			LOGGER.warn("路径：{} 不是有效的 GraphicsMagick 可执行文件路径", gmPath);
			// 未配置有效 GM 路径，跳过创建
			return null;
		}

		GMConnectionPoolConfig config = new GMConnectionPoolConfig();
		config.setGMPath(gmPath);
		config.setMaxActive(properties.getGraphicsMagick().getMaxActive());
		WhenExhaustedAction whenExhaustedAction = switch (properties.getGraphicsMagick().getWhenExhaustedAction()) {
			case BLOCK -> WhenExhaustedAction.BLOCK;
			case GROW -> WhenExhaustedAction.GROW;
			case FAIL -> WhenExhaustedAction.FAIL;
		};
		config.setWhenExhaustedAction(whenExhaustedAction);
		config.setMaxWait(properties.getGraphicsMagick().getMaxWaitMills());
		config.setMaxIdle(properties.getGraphicsMagick().getMaxIdle());
		config.setMinIdle(properties.getGraphicsMagick().getMinIdle());
		config.setTestOnGet(properties.getGraphicsMagick().isTestOnGet());
		config.setTestOnReturn(properties.getGraphicsMagick().isTestOnReturn());
		config.setTimeBetweenEvictionRunsMillis(properties.getGraphicsMagick().getTimeBetweenEvictionRunsMillis());
		config.setNumTestsPerEvictionRun(properties.getGraphicsMagick().getNumTestsPerEvictionRun());
		config.setMinEvictableIdleTimeMillis(properties.getGraphicsMagick().getMinEvictableIdleTimeMillis());
		config.setSoftMinEvictableIdleTimeMillis(properties.getGraphicsMagick().getSoftMinEvictableIdleTimeMillis());
		config.setTestWhileIdle(properties.getGraphicsMagick().isTestWhileIdle());
		config.setLifo(properties.getGraphicsMagick().isLifo());
		config.setEvictAfterNumberOfUse(properties.getGraphicsMagick().getEvictAfterNumberOfUse());
		return new PooledGMService(config);
	}

	/**
	 * 创建 GM 图像处理模板实现。
	 *
	 * <p>条件：当类型为 {@code graphics_magick}、已存在连接池且未定义其它模板实现时注入。</p>
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
	public GMImageTemplate gmImageTemplate(PooledGMService pooledGMService) {
		return new GMImageTemplate(pooledGMService);
	}

	/**
	 * 创建基于 GraphicsMagick 的图像操作模板实现。
	 *
	 * <p>条件：当类型为 {@code GRAPHICS_MAGICK}、已存在连接池且未定义其它操作模板实现时注入。</p>
	 *
	 * @param pooledGMService GM 连接池服务
	 * @return {@link ImageOperationsTemplate} 实例
	 * @since 2.1.0
	 */
	@ConditionalOnMissingBean(ImageOperationsTemplate.class)
	@ConditionalOnBean(PooledGMService.class)
	@ConditionalOnProperty(prefix = "pangju.image", name = "type", havingValue = "GRAPHICS_MAGICK")
	@Bean
	public ImageOperationsTemplate imageOperationsTemplate(PooledGMService pooledGMService) {
		return new GraphicsMagickOperationsTemplate(pooledGMService);
	}

	/**
	 * 创建基于 GraphicsMagick 的图像分割模板实现。
	 *
	 * <p>条件：当分割类型为 {@code GRAPHICS_MAGICK}、已存在连接池且未定义其它分割模板实现时注入。</p>
	 *
	 * <p>当配置项 {@code pangju.image.split-type} 未设置时，默认使用 GraphicsMagick 作为分割实现。</p>
	 *
	 * @param pooledGMService GM 连接池服务
	 * @return {@link ImageSplitTemplate} 实例
	 * @since 2.1.0
	 */
	@ConditionalOnMissingBean(ImageSplitTemplate.class)
	@ConditionalOnBean(PooledGMService.class)
	@ConditionalOnProperty(prefix = "pangju.image", name = "split-type", havingValue = "GRAPHICS_MAGICK", matchIfMissing = true)
	@Bean
	public ImageSplitTemplate imageSplitTemplate(PooledGMService pooledGMService) {
		return new GraphicsMagickSplitTemplate(pooledGMService);
	}
}
