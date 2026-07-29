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

import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.image.utils.ImageEditor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 图像处理自动配置入口。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>启用 {@link ImageProperties} 作为配置载体（{@link EnableConfigurationProperties}）。</li>
 *   <li>按条件导入 {@link GraphicsMagickConfiguration}、{@link OpenCvConfiguration} 与 {@link ImageIOConfiguration} 三个子配置（{@link Import}）。</li>
 * </ul>
 *
 * <p><strong>生效条件</strong></p>
 * <ul>
 *   <li>GraphicsMagick：配置 {@code pangju.image.graphics-magick.path} 且类型为 {@code GRAPHICS_MAGICK}。</li>
 *   <li>OpenCV：类路径存在 {@link io.github.pangju666.commons.opencv.processor.ImageProcessor} 和 {@link org.bytedeco.opencv.global.opencv_core} 相关类且类型为 {@code OPENCV}。</li>
 *   <li>IMAGEIO：类路径存在 {@link io.github.pangju666.commons.image.processor.ImageProcessor} 和 {@link ImageEditor} 且类型为 {@code IMAGEIO}（默认）。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass({ImageSize.class})
@EnableConfigurationProperties(ImageProperties.class)
@Import({GraphicsMagickConfiguration.class, OpenCvConfiguration.class, ImageIOConfiguration.class})
public class ImageAutoConfiguration {
}
