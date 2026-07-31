/*
 *   Copyright 2026 pangju666
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

import io.github.pangju666.commons.opencv.processor.ImageProcessor;
import io.github.pangju666.framework.boot.image.core.ImageOperationsTemplate;
import io.github.pangju666.framework.boot.image.core.impl.OpenCvOperationsTemplate;
import org.bytedeco.opencv.global.opencv_core;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenCV 自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>当类路径存在 OpenCV 相关类且属性 {@code pangju.image.type=OPENCV} 时生效。</li>
 *   <li>注册 {@link OpenCvOperationsTemplate} 实现用于图像处理操作。</li>
 * </ul>
 *
 * <p><strong>条件说明</strong></p>
 * <ul>
 *   <li>类条件：依赖 {@link ImageProcessor} 和 {@link opencv_core}。</li>
 *   <li>属性条件：{@code pangju.image.type} 为 {@code OPENCV}。</li>
 *   <li>Bean 条件：仅在没有其它 {@link ImageOperationsTemplate} Bean 时注入，避免冲突。</li>
 * </ul>
 *
 * @since 2.1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ImageProcessor.class, opencv_core.class})
@ConditionalOnProperty(prefix = "pangju.image", name = "type", havingValue = "OPENCV")
class OpenCvConfiguration {
	/**
	 * 注册基于 OpenCV 的图像操作模板实现。
	 *
	 * <p>条件：当无其它 {@link ImageOperationsTemplate} Bean，并满足类与属性条件时注入。</p>
	 *
	 * @return {@link ImageOperationsTemplate} 实例
	 * @since 2.1.0
	 */
	@ConditionalOnMissingBean(ImageOperationsTemplate.class)
	@Bean
	public ImageOperationsTemplate imageOperationsTemplate() {
		return new OpenCvOperationsTemplate();
	}
}
