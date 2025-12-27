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
import org.apache.commons.lang3.StringUtils;

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
 *   <li>{@link #format} 图像文件格式（如 {@code JPEG}、{@code PNG}）。</li>
 *   <li>{@link #fileSize} 文件大小（单位：字节）。</li>
 *   <li>{@link #mimeType} 图像的 MIME 类型（如 {@code image/jpeg}）。</li>
 *   <li>{@link #digest} 图像摘要（用于内容唯一性校验或缓存）。</li>
 *   <li>{@link #file} 图像对应的本地文件引用。</li>
 * </ul>
 *
 * <p><b>来源与构建</b></p>
 * <ul>
 *   <li>构造：文件校验 -> 解析扩展名为格式（大写） -> 记录文件大小。</li>
 *   <li>工厂：{@link #fromFile(File)} -> 解析 MIME 类型 -> 计算摘要 -> 返回实例。</li>
 * </ul>
 *
 * <p><b>约束</b></p>
 * <ul>
 *   <li>文件不可为 {@code null} 且必须存在。</li>
 *   <li>格式通过扩展名解析并统一为大写；MIME 类型为小写。</li>
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
	protected final File file;

    /**
     * 基于文件的构造函数：校验文件并初始化基本属性。
     *
     * <p><b>流程</b>：文件校验 -> 记录文件引用 -> 解析扩展名为格式（大写） -> 记录文件大小。</p>
     *
     * @param file 输入图片文件（不可为 {@code null}）
     * @throws IOException 文件不存在或不可读取时抛出
     * @since 1.0.0
     */
    public ImageFile(File file) throws IOException {
		FileUtils.checkFile(file, "输入图片不可为 null");

		this.file = file;
		this.format = FilenameUtils.getExtension(file.getName().toUpperCase());
		this.fileSize = file.length();
	}

    /**
     * 根据文件创建并补全 MIME 类型与摘要信息。
     *
     * <p><b>流程</b>：构造 {@link ImageFile} -> 解析 MIME 类型 -> 计算摘要 -> 返回。</p>
     * <p><b>异常</b>：类型解析失败抛出 {@link ImageParsingException}；摘要计算失败抛出 {@link ImageParsingException}。</p>
     *
     * @param file 输入图片文件
     * @return 含 MIME 与摘要的图像信息
     * @throws IOException 文件不存在或不可读取时抛出
     * @since 1.0.0
     */
    public static ImageFile fromFile(File file) throws IOException {
		ImageFile imageFile = new ImageFile(file);

		try {
			imageFile.mimeType = FileUtils.getMimeType(file);
		} catch (IOException e) {
			throw new ImageParsingException(file, "类型解析失败", e);
		}

		try {
			imageFile.digest = FileUtils.computeDigest(file);
		} catch (IOException e) {
			throw new ImageParsingException(file, "摘要计算失败", e);
		}

		return imageFile;
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
		if (StringUtils.isNotBlank(format)) {
			this.format = format.toUpperCase();
		}
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
		if (StringUtils.isNotBlank(mimeType)) {
			this.mimeType = mimeType.toLowerCase();
		}
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
}
