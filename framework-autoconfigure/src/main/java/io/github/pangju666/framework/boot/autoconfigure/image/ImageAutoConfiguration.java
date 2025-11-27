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

import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.framework.boot.autoconfigure.task.OnceTaskExecutorAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 图像处理自动配置入口。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>启用 {@link ImageProperties} 作为配置载体（{@link EnableConfigurationProperties}）。</li>
 *   <li>按条件导入 {@link GMConfiguration} 与 {@link ImageIOConfiguration} 两个子配置（{@link Import}）。</li>
 * </ul>
 *
 * <p><strong>生效条件</strong></p>
 * <ul>
 *   <li>GM：配置 {@code pangju.image.gm.path} 且类型为 {@code GM}。</li>
 *   <li>IMAGEIO：类路径存在 {@code ImageEditor} 且类型为 {@code IMAGEIO}（默认）。</li>
 * </ul>
 *
 * @since 1.0.0
 * @author pangju666
 */
@AutoConfiguration(after = {OnceTaskExecutorAutoConfiguration.class, TaskExecutionAutoConfiguration.class})
@ConditionalOnClass({ImageSize.class})
@EnableConfigurationProperties(ImageProperties.class)
@Import({GMConfiguration.class, ImageIOConfiguration.class})
public class ImageAutoConfiguration {
}
