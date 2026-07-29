package io.github.pangju666.framework.boot.compress.core.impl;

import io.github.pangju666.commons.compress.utils.ZstdUtils;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.framework.boot.compress.autoconfigure.CompressProperties;
import io.github.pangju666.framework.boot.compress.core.CompressTemplate;
import org.apache.commons.compress.compressors.zstandard.ZstdConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Zstd压缩模板实现类。
 * <p>
 * 基于Zstandard格式实现数据压缩功能，支持配置压缩级别。
 * Zstandard是一种现代压缩算法，由Facebook开发，提供接近LZMA的压缩率，
 * 但具有更快的压缩和解压速度，特别适合实时应用场景。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>支持输入流、文件和IO资源的压缩操作</li>
 *   <li>可配置压缩级别（负数到22，级别越高压缩率越好）</li>
 *   <li>提供接近LZMA的高压缩率</li>
 *   <li>压缩和解压速度都很快</li>
 *   <li>支持实时压缩场景</li>
 * </ul>
 *
 * <p><strong>适用场景</strong></p>
 * <ul>
 *   <li>需要平衡压缩率和速度的场景</li>
 *   <li>实时数据压缩</li>
 *   <li>数据库压缩</li>
 *   <li>日志实时压缩</li>
 *   <li>现代应用的数据压缩</li>
 * </ul>
 *
 * @since 2.1.0
 */
public class ZstdCompressTemplate implements CompressTemplate {
	/**
	 * 压缩级别。
	 *
	 * @since 2.1.0
	 */
	private final Integer compressionLevel;

	/**
	 * 构造函数。
	 *
	 * @param properties 压缩配置属性
	 * @since 2.1.0
	 */
	public ZstdCompressTemplate(CompressProperties properties) {
		this.compressionLevel = properties.getZstd().getCompressionLevel();
	}

	/**
	 * 将输入流压缩到输出流。
	 * <p>
	 * 如果配置了压缩级别且在有效范围内，则使用指定的压缩级别进行压缩。
	 * </p>
	 *
	 * @param inputStream  输入流
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void compress(InputStream inputStream, OutputStream outputStream) throws IOException {
		if (Objects.nonNull(compressionLevel) && compressionLevel >= ZstdConstants.ZSTD_CLEVEL_MIN &&
			compressionLevel <= ZstdConstants.ZSTD_CLEVEL_MAX) {
			ZstdUtils.compress(inputStream, outputStream, compressionLevel);
		} else {
			ZstdUtils.compress(inputStream, outputStream);
		}
	}

	/**
	 * 将输入流压缩到输出文件。
	 * <p>
	 * 如果配置了压缩级别且在有效范围内，则使用指定的压缩级别进行压缩。
	 * </p>
	 *
	 * @param inputStream 输入流
	 * @param outputFile  输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void compress(InputStream inputStream, File outputFile) throws IOException {
		if (Objects.nonNull(compressionLevel) && compressionLevel >= ZstdConstants.ZSTD_CLEVEL_MIN &&
			compressionLevel <= ZstdConstants.ZSTD_CLEVEL_MAX) {
			ZstdUtils.compress(inputStream, outputFile, compressionLevel);
		} else {
			ZstdUtils.compress(inputStream, outputFile);
		}
	}

	/**
	 * 将IO资源压缩到输出流。
	 * <p>
	 * 如果配置了压缩级别且在有效范围内，则使用指定的压缩级别进行压缩。
	 * </p>
	 *
	 * @param resource     IO资源
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void compress(IOResource resource, OutputStream outputStream) throws IOException {
		if (Objects.nonNull(compressionLevel) && compressionLevel >= ZstdConstants.ZSTD_CLEVEL_MIN &&
			compressionLevel <= ZstdConstants.ZSTD_CLEVEL_MAX) {
			ZstdUtils.compress(resource, outputStream, compressionLevel);
		} else {
			ZstdUtils.compress(resource, outputStream);
		}
	}

	/**
	 * 将IO资源压缩到输出文件。
	 * <p>
	 * 如果配置了压缩级别且在有效范围内，则使用指定的压缩级别进行压缩。
	 * </p>
	 *
	 * @param resource   IO资源
	 * @param outputFile 输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void compress(IOResource resource, File outputFile) throws IOException {
		if (Objects.nonNull(compressionLevel) && compressionLevel >= ZstdConstants.ZSTD_CLEVEL_MIN &&
			compressionLevel <= ZstdConstants.ZSTD_CLEVEL_MAX) {
			ZstdUtils.compress(resource, outputFile, compressionLevel);
		} else {
			ZstdUtils.compress(resource, outputFile);
		}
	}
}
