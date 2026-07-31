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

package io.github.pangju666.framework.boot.image.model.gm;

import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.framework.boot.image.enums.CompressionType;
import org.jspecify.annotations.Nullable;

/**
 * GraphicsMagick图像识别结果。
 * <p>
 * 封装通过GraphicsMagick的identify命令获取的图像元数据信息。
 * </p>
 *
 * @param format      图像格式，如"jpg"、"png"等
 * @param signature   图像签名/哈希值，用于唯一标识图像
 * @param imageSize   图像尺寸对象，包含宽度、高度和EXIF方向
 * @param hasAlpha    是否包含透明通道
 * @param depth       图像位深度，如8、16、32等
 * @param compression 图像压缩类型
 * @param quality     图像质量值（0-100）
 * @author pangju666
 * @since 2.1.0
 */
public record IdentifyResult(@Nullable String format, @Nullable String signature, @Nullable ImageSize imageSize,
                             @Nullable Boolean hasAlpha, @Nullable Integer depth, @Nullable CompressionType compression,
                             @Nullable Integer quality) {
}