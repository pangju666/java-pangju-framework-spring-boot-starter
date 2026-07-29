package io.github.pangju666.framework.boot.compress.core.impl;

import io.github.pangju666.commons.compress.utils.XZUtils;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.framework.boot.compress.autoconfigure.CompressProperties;
import io.github.pangju666.framework.boot.compress.core.CompressTemplate;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.UnsupportedOptionsException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * XZ压缩模板实现类。
 * <p>
 * 基于XZ格式实现数据压缩功能，支持配置压缩模式和预设级别。
 * XZ使用LZMA2压缩算法，提供极高的压缩率，但压缩速度相对较慢，解压速度较快。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>支持输入流、文件和IO资源的压缩操作</li>
 *   <li>可配置压缩模式（快速、普通、最佳等）</li>
 *   <li>可配置预设级别（0-9，级别越高压缩率越好）</li>
 *   <li>提供接近LZMA的最高压缩率</li>
 *   <li>解压速度相对较快，适合长期存储</li>
 * </ul>
 *
 * <p><strong>适用场景</strong></p>
 * <ul>
 *   <li>需要最高压缩率的压缩场景</li>
 *   <li>长期数据存储</li>
 *   <li>网络传输受限的场景（节省带宽）</li>
 *   <li>软件发布包压缩</li>
 * </ul>
 *
 * @since 2.1.0
 */
public class XZCompressTemplate implements CompressTemplate {
	/**
	 * 压缩模式。
	 *
	 * @since 2.1.0
	 */
	private final CompressProperties.XZCompressionMode compressionMode;
	/**
	 * 预设级别。
	 *
	 * @since 2.1.0
	 */
	private final Integer preset;

	/**
	 * 构造函数。
	 *
	 * @param properties 压缩配置属性
	 * @since 2.1.0
	 */
	public XZCompressTemplate(CompressProperties properties) {
		this.compressionMode = properties.getXz().getCompressionMode();
		this.preset = properties.getXz().getPreset();
	}

	/**
	 * 将输入流压缩到输出流。
	 *
	 * @param inputStream  输入流
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void compress(InputStream inputStream, OutputStream outputStream) throws IOException {
		XZUtils.compress(inputStream, outputStream, getLZMA2Options());
	}

	/**
	 * 将输入流压缩到输出文件。
	 *
	 * @param inputStream 输入流
	 * @param outputFile  输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void compress(InputStream inputStream, File outputFile) throws IOException {
		XZUtils.compress(inputStream, outputFile, getLZMA2Options());
	}

	/**
	 * 将IO资源压缩到输出流。
	 *
	 * @param resource     IO资源
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void compress(IOResource resource, OutputStream outputStream) throws IOException {
		XZUtils.compress(resource, outputStream, getLZMA2Options());
	}

	/**
	 * 将IO资源压缩到输出文件。
	 *
	 * @param resource   IO资源
	 * @param outputFile 输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void compress(IOResource resource, File outputFile) throws IOException {
		XZUtils.compress(resource, outputFile, getLZMA2Options());
	}

	/**
	 * 获取LZMA2压缩选项。
	 *
	 * @return LZMA2压缩选项
	 * @since 2.1.0
	 */
	public LZMA2Options getLZMA2Options() {
		LZMA2Options lzma2Options = new LZMA2Options();
		if (Objects.nonNull(preset) && preset >= LZMA2Options.PRESET_MIN && preset <= LZMA2Options.PRESET_MAX) {
			try {
				lzma2Options.setPreset(preset);
			} catch (UnsupportedOptionsException e) {
				//... 不会抛出
				return ExceptionUtils.rethrow(e);
			}
		}
		try {
			if (Objects.nonNull(compressionMode)) {
				int mode = compressionMode.getValue();
				if (mode >= LZMA2Options.MODE_UNCOMPRESSED && mode <= LZMA2Options.MODE_NORMAL) {
					lzma2Options.setMode(mode);
				}
			}
		} catch (UnsupportedOptionsException e) {
			//... 不会抛出
			return ExceptionUtils.rethrow(e);
		}
		return lzma2Options;
	}
}
