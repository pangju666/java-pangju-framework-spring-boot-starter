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

import io.github.pangju666.commons.image.utils.ImageEditor;
import io.github.pangju666.framework.boot.image.core.ImageTemplate;
import io.github.pangju666.framework.boot.image.core.impl.BufferedImageTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ImageIO 自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>当类路径存在 {@link ImageEditor} 且属性 {@code pangju.image.type=IMAGEIO}（默认为该值）时生效。</li>
 *   <li>在缺少其它 {@link ImageTemplate} Bean 时，注册 {@link BufferedImageTemplate} 实现。</li>
 * </ul>
 *
 * <p><strong>条件说明</strong></p>
 * <ul>
 *   <li>类条件：依赖 {@link ImageEditor}。</li>
 *   <li>属性条件：{@code pangju.image.type} 为 {@code IMAGEIO} 或未配置。</li>
 *   <li>Bean 条件：仅在没有其它 {@link ImageTemplate} Bean 时注入，避免冲突。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ImageEditor.class})
@ConditionalOnProperty(prefix = "pangju.image", name = "type", havingValue = "IMAGEIO", matchIfMissing = true)
class ImageIOConfiguration {
	/**
	 * 注册基于 {@link java.awt.image.BufferedImage} 的模板实现。
	 *
	 * <p>条件：当无其它 {@link ImageTemplate} Bean，并满足类与属性条件时注入。</p>
	 *
	 * @return {@link BufferedImageTemplate} 实例
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(ImageTemplate.class)
	@Bean
	public BufferedImageTemplate bufferImageTemplate() {
		return new BufferedImageTemplate();
	}
}
