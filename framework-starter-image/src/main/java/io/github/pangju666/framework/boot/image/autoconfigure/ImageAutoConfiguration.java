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
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 图像处理自动配置类。
 * <p>
 * 图像处理功能的主自动配置入口，负责导入所有图像处理相关的配置类，并启用图像处理配置属性。
 * 支持多种图像处理引擎，包括GraphicsMagick、OpenCV和ImageIO，可根据需求选择不同的实现。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>支持多种图像处理引擎：GraphicsMagick、OpenCV、ImageIO</li>
 *   <li>支持图像缩放、裁剪、旋转、水印等常用操作</li>
 *   <li>支持多种图像格式：JPEG、PNG、GIF、BMP等</li>
 *   <li>提供统一的图像处理接口，简化使用</li>
 *   <li>支持自定义图像处理参数和质量设置</li>
 * </ul>
 *
 * <p><strong>支持的图像处理引擎</strong></p>
 * <ul>
 *   <li>{@code GRAPHICS_MAGICK}：GraphicsMagick引擎，功能强大，支持多种图像格式和操作</li>
 *   <li>{@code OPENCV}：OpenCV引擎，基于计算机视觉库，适合复杂的图像处理任务</li>
 *   <li>{@code IMAGEIO}：ImageIO引擎，基于Java原生API，轻量级，兼容性好（默认）</li>
 * </ul>
 *
 * <p><strong>导入的配置类</strong></p>
 * <ul>
 *   <li>{@link GraphicsMagickConfiguration}：GraphicsMagick图像处理配置，当engine为GRAPHICS_MAGICK时生效</li>
 *   <li>{@link OpenCvConfiguration}：OpenCV图像处理配置，当engine为OPENCV时生效</li>
 *   <li>{@link ImageIOConfiguration}：ImageIO图像处理配置，当engine为IMAGEIO时生效（默认）</li>
 * </ul>
 *
 * <p><strong>生效条件</strong></p>
 * <ul>
 *   <li>类路径中存在ImageSize类</li>
 *   <li>GraphicsMagick：配置{@code pangju.image.graphics-magick.path}且类型为{@code GRAPHICS_MAGICK}</li>
 *   <li>OpenCV：类路径存在ImageProcessor和opencv_core相关类且类型为{@code OPENCV}</li>
 *   <li>IMAGEIO：类路径存在ImageProcessor和ImageEditor且类型为{@code IMAGEIO}（默认）</li>
 * </ul>
 *
 * <p><strong>配置属性</strong></p>
 * <p>启用{@link ImageProperties}配置属性绑定，配置前缀为{@code pangju.image}。</p>
 * <ul>
 *   <li>{@code pangju.image.engine}：图像处理引擎类型（默认：IMAGEIO）</li>
 *   <li>{@code pangju.image.graphics-magick}：GraphicsMagick配置</li>
 *   <li>{@code pangju.image.opencv}：OpenCV配置</li>
 *   <li>{@code pangju.image.imageio}：ImageIO配置</li>
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
