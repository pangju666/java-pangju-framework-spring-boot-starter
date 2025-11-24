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
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.framework.boot.image.exception.ImageParsingException;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;

import java.io.File;
import java.io.IOException;

/**
 * 图像信息模型。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>封装图像的基础元数据与文件引用，包括尺寸、方向、格式、MIME 类型、大小与摘要等。</li>
 *   <li>用于图像上传、处理与展示等场景的通用数据载体。</li>
 * </ul>
 *
 * <p><b>字段</b></p>
 * <ul>
 *   <li>{@link #imageSize} 图像宽高信息，见 {@link io.github.pangju666.commons.image.model.ImageSize}。</li>
 *   <li>{@link #orientation} 图像方向标记（如 EXIF Orientation），整数表示，默认值见 {@link io.github.pangju666.framework.boot.image.lang.ImageConstants#NORMAL_EXIF_ORIENTATION}。</li>
 *   <li>{@link #format} 图像文件格式（如 {@code JPEG}、{@code PNG}）。</li>
 *   <li>{@link #fileSize} 文件大小，单位字节。</li>
 *   <li>{@link #mimeType} 图像的 MIME 类型（如 {@code image/jpeg}）。</li>
 *   <li>{@link #digest} 图像摘要（用于内容唯一性校验或缓存）。</li>
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
public class ImageFile {
    /**
     * 图像摘要（字符串）。
     *
     * @since 1.0.0
     */
    private String digest;
	/**
	 * 图像尺寸（宽、高）。
	 *
	 * @since 1.0.0
	 */
	private ImageSize imageSize;
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
	public ImageFile() {
	}

	/**
	 * 基于本地文件构造图像信息。
	 * <p>
	 * 初始化并填充文件相关元数据：MIME 类型、摘要、格式、大小与文件引用。
	 * </p>
	 * <ul>
	 *     <li>MIME 类型通过 {@link io.github.pangju666.commons.io.utils.FileUtils#getMimeType(File)} 解析</li>
	 *     <li>摘要通过 {@link io.github.pangju666.commons.io.utils.FileUtils#computeDigest(File)} 计算</li>
	 *     <li>格式来源于文件扩展名（{@link io.github.pangju666.commons.io.utils.FilenameUtils#getExtension(String)}） </li>
	 * </ul>
	 *
	 * @param file 图像文件
	 * @throws IOException 文件校验或读取发生 I/O 错误
	 * @throws ImageParsingException 当 MIME 类型解析或摘要计算失败时抛出
	 * @since 1.0.0
	 */
	public ImageFile(File file) throws IOException {
		FileUtils.checkFile(file, "输入图片不可为null");

		try {
			this.mimeType = FileUtils.getMimeType(file);
		} catch (IOException e) {
			throw new ImageParsingException("图像类型解析失败", e);
		}

		try {
			this.digest = FileUtils.computeDigest(file);
		} catch (IOException e) {
			throw new ImageParsingException("图像摘要计算失败", e);
		}

		this.format = FilenameUtils.getExtension(file.getName());
		this.fileSize = file.length();
		this.file = file;
	}

	/**
	 * 获取图像摘要。
	 *
	 * @return 图像摘要（字符串）
	 * @since 1.0.0
	 */
	public String getDigest() {
		return digest;
	}

	/**
	 * 设置图像摘要。
	 *
	 * @param digest 图像摘要（字符串）
	 * @since 1.0.0
	 */
	public void setDigest(String digest) {
		this.digest = digest;
	}

	/**
	 * 获取图像尺寸（宽、高）。
	 *
	 * @return 图像尺寸
	 * @since 1.0.0
	 */
	public ImageSize getImageSize() {
		return imageSize;
	}

	/**
	 * 设置图像尺寸（宽、高）。
	 *
	 * @param imageSize 图像尺寸
	 * @since 1.0.0
	 */
	public void setImageSize(ImageSize imageSize) {
		this.imageSize = imageSize;
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

	/**
	 * 获取图像对应的本地文件引用。
	 *
	 * @return 图像文件引用
	 * @since 1.0.0
	 */
	public File getFile() {
		return file;
	}

	/**
	 * 设置图像对应的本地文件引用。
	 *
	 * @param file 图像文件引用
	 * @since 1.0.0
	 */
	public void setFile(File file) {
		this.file = file;
	}
}
