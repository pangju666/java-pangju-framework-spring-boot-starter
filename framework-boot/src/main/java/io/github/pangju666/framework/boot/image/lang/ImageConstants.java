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
 * 图像能力常量集合。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>在框架层面聚合不同图像处理引擎/库的读写能力，提供统一的类型集合。</li>
 *   <li>涵盖 GraphicsMagick、TwelveMonkeys ImageIO 插件与 Krpano Tools 的支持范围。</li>
 *   <li>便于在运行时进行能力判定与类型校验（例如选择可用的编解码方案）。</li>
 * </ul>
 *
 * <p><b>字段</b></p>
 * <ul>
 *   <li>{@link #GRAPHICS_MAGICK_SUPPORT_WRITE_IMAGE_TYPE_SET}：GraphicsMagick 可写出的图像 MIME 类型集合。</li>
 *   <li>{@link #GRAPHICS_MAGICK_SUPPORT_READ_IMAGE_TYPE_SET}：GraphicsMagick 可读取的图像 MIME 类型集合。</li>
 *   <li>{@link #KRPANO_TOOLS_SUPPORT_FORMAT_SET}：Krpano Tools 支持的图像格式扩展名集合（非 MIME 类型）。</li>
 * </ul>
 *
 * <p><b>备注</b></p>
 * <ul>
 *   <li>不同平台与安装的依赖库版本可能影响具体支持范围，请以实际环境为准。</li>
 *   <li>GraphicsMagick 与 TwelveMonkeys 的集合为 MIME 类型，Krpano 为文件扩展名，两者不可直接混用。</li>
 *   <li>本类仅聚合能力信息，不参与具体的读写实现，具体处理由上层服务/工具决定。</li>
 * </ul>
 *
 * <p><b>GraphicsMagick 图片类型依赖库说明</b></p>
 * <ul>
 *   <li>CGM(image/cgm)：Requires ralcgm to render CGM files.</li>
 *   <li>JPEG(image/jpeg)：Requires jpegsrc.v6b.tar.gz or later.</li>
 *   <li>PNG(image/png)：Requires libpng-1.0.2 or later, libpng-1.2.5 or later recommended.</li>
 *   <li>SVG(image/svg+xml)：Requires libxml2 and freetype2. Note that SVG is a very complex specification so support
 *   is still not complete.</li>
 *   <li>TIF(image/tiff)：Also known as "TIF". Requires tiff-v3.5.4.tar.gz or later. Since the Unisys LZW patent
 *   recently expired, libtiff may still require a separate LZW patch in order to support LZW. LZW is included in
 *   libtiff by default since v3.7.0.</li>
 *   <li>WEBP(image/webp)：Requires libwebp from <a href="https://developers.google.com/speed/webp/">https://developers.google.com/speed/webp/</a>.
 *   WebP is good for small photos for the web and is supported by Google's Chrome and Firefox.</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class ImageConstants extends io.github.pangju666.commons.image.lang.ImageConstants {
    /**
     * GraphicsMagick 支持写出的图像类型（MIME 类型）集合。
     *
     * <p>用于在选择输出格式时进行能力校验；具体支持取决于安装的 GM 版本与依赖库。</p>
	 *
	 * @since 1.0.0
     */
	public static final Set<String> GRAPHICS_MAGICK_SUPPORT_WRITE_IMAGE_TYPE_SET = Set.of(
		"image/bmp", "image/x-dcx", "image/vnd.fpx", "image/gif", "image/jp2", "image/jpeg", "image/x-portable-bitmap",
		"image/x-pcx", "image/x-portable-graymap", "image/x-pict", "image/png", "image/x-portable-pixmap", "image/x-rgb",
		"image/svg+xml", "image/x-tga", "image/tiff", "image/vnd.wap.wbmp", "image/webp", "image/x-xbitmap",
		"image/x-xpixmap", "image/xwd"
	);

    /**
     * GraphicsMagick 支持读取的图像类型（MIME 类型）集合。
     *
     * <p>用于判定输入资源是否可由 GM 解析；实际支持范围随平台与依赖变化。</p>
	 *
	 * @since 1.0.0
	 */
	public static final Set<String> GRAPHICS_MAGICK_SUPPORT_READ_IMAGE_TYPE_SET = Set.of(
		"image/avif", "image/bmp", "image/cgm", "image/x-cursor", "image/x-dcx", "image/vnd.fpx", "image/gif",
		"image/heic", "image/x-icon", "image/jp2", "image/jpeg", "image/x-portable-bitmap", "image/x-pcx",
		"image/x-portable-graymap", "image/x-pict", "image/png", "image/x-portable-anymap", "image/x-portable-pixmap",
		"image/x-rgb", "image/svg+xml", "image/x-tga", "image/tiff", "image/vnd.wap.wbmp", "image/webp",
		"image/x-xbitmap", "image/x-xcf", "image/x-xpixmap", "image/xwd"
	);

    /**
     * Krpano Tools 支持的图像格式扩展名集合。
     *
     * <p>注意：此集合为文件扩展名（如 {@code jpg}、{@code tiff}），非 MIME 类型。</p>
	 *
	 * @since 1.0.0
	 */
	public static final Set<String> KRPANO_TOOLS_SUPPORT_FORMAT_SET = Set.of(
		"tif", "tiff", "btf", "tf8", "bigtiff", "jpg", "jpeg", "png", "psd", "psb");

    protected ImageConstants() {
    }
}
