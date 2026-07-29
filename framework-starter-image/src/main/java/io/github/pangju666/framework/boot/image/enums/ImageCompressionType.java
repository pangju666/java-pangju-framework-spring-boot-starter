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

package io.github.pangju666.framework.boot.image.enums;

/**
 * 图像压缩类型枚举
 * <p>
 * 定义了GraphicsMagick支持的各种图像压缩算法类型。
 * 每个枚举值对应GraphicsMagick中的一种压缩方式，用于控制图像文件的压缩率和质量。
 * </p>
 * <p>
 * 支持的压缩类型包括：
 * <ul>
 *     <li>NONE - 无压缩</li>
 *     <li>BZIP - BZip压缩</li>
 *     <li>FAX - 传真压缩</li>
 *     <li>GROUP3/4 - Group3/Group4传真压缩</li>
 *     <li>JPEG - JPEG有损压缩</li>
 *     <li>LOSSLESS - 无损压缩</li>
 *     <li>LZW - LZW压缩</li>
 *     <li>RLE - 游程编码压缩</li>
 *     <li>ZIP - ZIP压缩</li>
 *     <li>LZMA - LZMA压缩</li>
 *     <li>JPEG2000 - JPEG2000压缩</li>
 *     <li>JBIG/JBIG2 - JBIG压缩</li>
 *     <li>WEBP - WebP压缩</li>
 *     <li>ZSTD - Zstandard压缩</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 2.1.0
 */
public enum ImageCompressionType {
	/**
	 * 无压缩
	 *
	 * @since 2.1.0
	 */
	NONE("None"),
	/**
	 * BZip压缩
	 *
	 * @since 2.1.0
	 */
	BZIP("BZip"),
	/**
	 * 传真压缩
	 *
	 * @since 2.1.0
	 */
	FAX("Fax"),
	/**
	 * Group3传真压缩
	 *
	 * @since 2.1.0
	 */
	GROUP3("Group3"),
	/**
	 * Group4传真压缩
	 *
	 * @since 2.1.0
	 */
	GROUP4("Group4"),
	/**
	 * JPEG有损压缩
	 *
	 * @since 2.1.0
	 */
	JPEG("JPEG"),
	/**
	 * 无损压缩
	 *
	 * @since 2.1.0
	 */
	LOSSLESS("Lossless"),
	/**
	 * LZW压缩
	 *
	 * @since 2.1.0
	 */
	LZW("LZW"),
	/**
	 * 游程编码压缩
	 *
	 * @since 2.1.0
	 */
	RLE("RLE"),
	/**
	 * ZIP压缩
	 *
	 * @since 2.1.0
	 */
	ZIP("Zip"),
	/**
	 * LZMA压缩
	 *
	 * @since 2.1.0
	 */
	LZMA("LZMA"),
	/**
	 * JPEG2000压缩
	 *
	 * @since 2.1.0
	 */
	JPEG2000("JPEG2000"),
	/**
	 * JBIG压缩
	 *
	 * @since 2.1.0
	 */
	JBIG("JBIG"),
	/**
	 * JBIG2压缩
	 *
	 * @since 2.1.0
	 */
	JBIG2("JBIG2"),
	/**
	 * WebP压缩
	 *
	 * @since 2.1.0
	 */
	WEBP("WebP"),
	/**
	 * Zstandard压缩
	 *
	 * @since 2.1.0
	 */
	ZSTD("ZSTD");

	/**
	 * GraphicsMagick压缩类型字符串
	 *
	 * @since 2.1.0
	 */
	public final String graphicsMagickCompressionType;

	/**
	 * 构造函数
	 *
	 * @param graphicsMagickCompressionType GraphicsMagick压缩类型字符串
	 * @since 2.1.0
	 */
	ImageCompressionType(String graphicsMagickCompressionType) {
		this.graphicsMagickCompressionType = graphicsMagickCompressionType;
	}
}
