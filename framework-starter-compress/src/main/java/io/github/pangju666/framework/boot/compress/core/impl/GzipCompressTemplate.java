package io.github.pangju666.framework.boot.compress.core.impl;

import io.github.pangju666.commons.compress.utils.GzipUtils;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.io.utils.IOUtils;
import io.github.pangju666.framework.boot.compress.autoconfigure.CompressProperties;
import io.github.pangju666.framework.boot.compress.core.CompressTemplate;
import org.apache.commons.compress.compressors.gzip.GzipParameters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * GZIP压缩模板实现类。
 * <p>
 * 基于GZIP格式实现数据压缩功能，支持配置压缩策略和压缩级别。
 * GZIP是一种广泛使用的压缩格式，具有良好的压缩率和兼容性。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>支持输入流、文件和IO资源的压缩操作</li>
 *   <li>可配置压缩策略（FILTERED、HUFFMAN_ONLY等）</li>
 *   <li>可配置压缩级别（0-9，级别越高压缩率越好但速度越慢）</li>
 *   <li>针对IO资源自动优化缓冲区大小以提升性能</li>
 * </ul>
 *
 * <p><strong>适用场景</strong></p>
 * <ul>
 *   <li>需要广泛兼容性的压缩场景</li>
 *   <li>Web应用中的HTTP响应压缩</li>
 *   <li>日志文件压缩</li>
 *   <li>文本数据压缩</li>
 * </ul>
 *
 * @since 2.1.0
 */
public class GzipCompressTemplate implements CompressTemplate {
	/**
	 * 压缩策略。
	 *
	 * @since 2.1.0
	 */
	private final CompressProperties.DeflaterDeflateStrategy deflateStrategy;
	/**
	 * 压缩级别。
	 *
	 * @since 2.1.0
	 */
	private final CompressProperties.DeflaterCompressionLevel compressionLevel;

	/**
	 * 构造函数。
	 *
	 * @param properties 压缩配置属性
	 * @since 2.1.0
	 */
	public GzipCompressTemplate(CompressProperties properties) {
		this.deflateStrategy = properties.getGzip().getDeflateStrategy();
		this.compressionLevel = properties.getGzip().getCompressionLevel();
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
		GzipUtils.compress(inputStream, outputStream, getGzipParameters());
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
		GzipUtils.compress(inputStream, outputFile, getGzipParameters());
	}

	/**
	 * 将IO资源压缩到输出流。
	 * <p>
	 * 根据资源大小自动设置缓冲区大小以优化性能。
	 * </p>
	 *
	 * @param resource     IO资源
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void compress(IOResource resource, OutputStream outputStream) throws IOException {
		GzipParameters gzipParameters = getGzipParameters();
		gzipParameters.setBufferSize(IOUtils.getBufferSize(resource.getSize().toBytes()));
		GzipUtils.compress(resource, outputStream, gzipParameters);
	}

	/**
	 * 将IO资源压缩到输出文件。
	 * <p>
	 * 根据资源大小自动设置缓冲区大小以优化性能。
	 * </p>
	 *
	 * @param resource   IO资源
	 * @param outputFile 输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void compress(IOResource resource, File outputFile) throws IOException {
		GzipParameters gzipParameters = getGzipParameters();
		gzipParameters.setBufferSize(IOUtils.getBufferSize(resource.getSize().toBytes()));
		GzipUtils.compress(resource, outputFile, gzipParameters);
	}

	/**
	 * 获取GZIP压缩参数。
	 *
	 * @return GZIP压缩参数
	 * @since 2.1.0
	 */
	public GzipParameters getGzipParameters() {
		GzipParameters gzipParameters = new GzipParameters();
		if (Objects.nonNull(deflateStrategy)) {
			gzipParameters.setDeflateStrategy(deflateStrategy.getValue());
		}
		if (Objects.nonNull(compressionLevel)) {
			gzipParameters.setCompressionLevel(compressionLevel.getValue());
		}
		return gzipParameters;
	}
}
