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

package io.github.pangju666.framework.boot.image.lang;

import java.util.Set;

/**
 * 图像相关常量集合。
 *
 * <p>
 * 聚合GraphicsMagick支持的图像格式，提供统一的读写能力集合，便于运行时能力判定与类型校验。
 * </p>
 *
 * <p><b>备注</b></p>
 * <ul>
 *   <li>集合元素为文件扩展名（如 {@code jpg}、{@code png}），非 MIME 类型</li>
 *   <li>实际支持范围受平台和依赖库版本影响，请以实际环境为准</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class ImageConstants extends io.github.pangju666.commons.image.lang.ImageConstants {
	/**
	 * TTF 字体文件的 MIME 类型
	 * <p>用于文字水印功能中的字体文件识别</p>
	 *
	 * @since 2.1.0
	 */
	public static final String TTF_FONT_MIME_TYPE = "application/x-font-ttf";

	/**
	 * GraphicsMagick 可写出的图像格式扩展名集合。
	 *
	 * <p>参考：<a href="http://www.graphicsmagick.org/formats.html">GraphicsMagick 支持格式文档</a></p>
	 * <p>说明：集合元素为文件扩展名（如 {@code jpg}、{@code png}），非 MIME 类型。</p>
	 *
	 * @since 1.0.0
	 */
	public static final Set<String> GRAPHICS_MAGICK_SUPPORTED_WRITE_IMAGE_FORMATS = Set.of(
		"bmp", "cmyk", "dib", "dpx", "fits", "gif", "gray", "graya", "jpg", "jng", "jp2", "jpc", "jpeg", "jxl", "miff",
		"otb", "p7", "palm", "pam", "pbm", "pcx", "pgm", "picon", "pict", "png", "pnm", "ppm", "ptif", "rgb", "rgba",
		"sgi", "sun", "svg", "tga", "icb", "vda", "vst", "tiff", "tif", "webp", "xbm", "xpm", "xwd"
	);

	/**
	 * GraphicsMagick 可读取的图像格式扩展名集合。
	 *
	 * <p>参考：<a href="http://www.graphicsmagick.org/formats.html">GraphicsMagick 支持格式文档</a></p>
	 * <p>说明：集合元素为文件扩展名（如 {@code jpg}、{@code png}），非 MIME 类型。</p>
	 *
	 * @since 1.0.0
	 */
	public static final Set<String> GRAPHICS_MAGICK_SUPPORTED_READ_IMAGE_FORMATS = Set.of(
		"avif", "bmp", "cmyk", "cur", "dib", "dpx", "emf", "fits", "gif", "gray", "graya", "heif", "ico", "jpg", "jng",
		"jp2", "jpc", "jpeg", "jxl", "miff", "otb", "p7", "palm", "pam", "pbm", "pcx", "pgm", "picon", "pict", "pix",
		"png", "pnm", "ppm", "ptif", "ras", "rgb", "rgba", "sgi", "sun", "svg", "tga", "icb", "vda", "vst", "tiff",
		"tif", "webp", "xbm", "xpm", "xwd"
	);

	protected ImageConstants() {
	}
}
