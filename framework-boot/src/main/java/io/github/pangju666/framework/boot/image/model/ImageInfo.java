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

package io.github.pangju666.framework.boot.image.model;

import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;

import java.io.File;

/**
 * 图像信息模型。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>封装图像的基础元数据与文件引用，包括尺寸、方向、格式、MIME 类型与大小等。</li>
 *   <li>用于图像上传、处理与展示等场景的通用数据载体。</li>
 * </ul>
 *
 * <p><b>字段</b></p>
 * <ul>
 *   <li>{@link #size} 图像宽高信息，见 {@link io.github.pangju666.commons.image.model.ImageSize}。</li>
 *   <li>{@link #orientation} 图像方向标记（如 EXIF Orientation），整数表示。</li>
 *   <li>{@link #format} 图像文件格式（如 {@code JPEG}、{@code PNG}）。</li>
 *   <li>{@link #fileSize} 文件大小，单位字节。</li>
 *   <li>{@link #mimeType} 图像的 MIME 类型（如 {@code image/jpeg}）。</li>
 *   <li>{@link #file} 图像对应的本地文件引用。</li>
 * </ul>
 *
 * <p><b>备注</b></p>
 * <ul>
 *   <li>当图像来源非文件系统或为流式处理场景时，{@link #file} 可能为空。</li>
 *   <li>{@link #orientation} 的具体取值含义依赖于上游解析逻辑（例如 EXIF 1–8 值或角度）。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class ImageInfo {
	/**
	 * 图像尺寸（宽、高）。
	 *
	 * @since 1.0.0
	 */
	private ImageSize size;
	/**
	 * 图像方向标记（如 EXIF Orientation）。
	 *
	 * @since 1.0.0
	 */
	private int orientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
	/**
	 * 图像文件格式（如 JPEG、PNG）。
	 *
	 * @since 1.0.0
	 */
	private String format;
	/**
	 * 文件大小（字节）。
	 *
	 * @since 1.0.0
	 */
	private long fileSize;
	/**
	 * 图像 MIME 类型（如 image/jpeg）。
	 *
	 * @since 1.0.0
	 */
	private String mimeType;
	/**
	 * 图像对应的本地文件引用。
	 *
	 * @since 1.0.0
	 */
	private File file;

	/**
	 * 无参构造。
	 *
	 * @since 1.0.0
	 */
	public ImageInfo() {
	}

	/**
	 * 获取图像尺寸。
	 *
	 * @return 图像宽高信息
	 * @since 1.0.0
	 */
	public ImageSize getSize() {
		return size;
	}

	/**
	 * 设置图像尺寸。
	 *
	 * @param size 图像宽高信息
	 * @since 1.0.0
	 */
	public void setSize(ImageSize size) {
		this.size = size;
	}

	/**
	 * 获取文件大小（字节）。
	 *
	 * @return 文件大小（字节）
	 * @since 1.0.0
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * 设置文件大小（字节）。
	 *
	 * @param fileSize 文件大小（字节）
	 * @since 1.0.0
	 */
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * 获取图像方向标记。
	 *
	 * @return 图像方向标记（如 EXIF Orientation）
	 * @since 1.0.0
	 */
	public int getOrientation() {
		return orientation;
	}

	/**
	 * 设置图像方向标记。
	 *
	 * @param orientation 图像方向标记（如 EXIF Orientation）
	 * @since 1.0.0
	 */
	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	/**
	 * 获取图像文件格式。
	 *
	 * @return 图像文件格式（如 JPEG、PNG）
	 * @since 1.0.0
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * 设置图像文件格式。
	 *
	 * @param format 图像文件格式（如 JPEG、PNG）
	 * @since 1.0.0
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * 获取图像 MIME 类型。
	 *
	 * @return 图像 MIME 类型（如 image/jpeg）
	 * @since 1.0.0
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * 设置图像 MIME 类型。
	 *
	 * @param mimeType 图像 MIME 类型（如 image/jpeg）
	 * @since 1.0.0
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}