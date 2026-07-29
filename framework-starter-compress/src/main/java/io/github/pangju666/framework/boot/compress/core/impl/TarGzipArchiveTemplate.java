package io.github.pangju666.framework.boot.compress.core.impl;

import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.framework.boot.compress.autoconfigure.CompressProperties;
import io.github.pangju666.framework.boot.compress.core.ArchiveTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.UUID;

/**
 * TAR.GZIP归档模板实现类。
 * <p>
 * 基于TAR归档和GZIP压缩的组合格式实现文件归档功能，
 * 先进行TAR归档将多个文件打包，再进行GZIP压缩以减小文件大小。
 * .tar.gz（或.tgz）是Linux系统中最常见的归档压缩格式之一。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>支持单个文件和多个文件集合的归档压缩</li>
 *   <li>先TAR归档保留文件结构，再GZIP压缩减小体积</li>
 *   <li>支持GZIP压缩策略和压缩级别配置</li>
 *   <li>通过临时文件实现两步处理，自动清理临时文件</li>
 *   <li>支持输出到文件或输出流</li>
 * </ul>
 *
 * <p><strong>适用场景</strong></p>
 * <ul>
 *   <li>Linux系统软件发布包</li>
 *   <li>源代码归档分发</li>
 *   <li>需要良好兼容性和压缩率的场景</li>
 *   <li>日志文件归档压缩</li>
 * </ul>
 *
 * @since 2.1.0
 */
public class TarGzipArchiveTemplate implements ArchiveTemplate {
	/**
	 * TAR归档模板。
	 *
	 * @since 2.1.0
	 */
	private final TarArchiveTemplate archiveTemplate;
	/**
	 * GZIP压缩模板。
	 *
	 * @since 2.1.0
	 */
	private final GzipCompressTemplate compressTemplate;

	/**
	 * 构造函数。
	 *
	 * @param properties 压缩配置属性
	 * @since 2.1.0
	 */
	public TarGzipArchiveTemplate(CompressProperties properties) {
		this.archiveTemplate = new TarArchiveTemplate();
		this.compressTemplate = new GzipCompressTemplate(properties);
	}

	/**
	 * 将单个文件归档到输出文件。
	 * <p>
	 * 先进行TAR归档，再进行GZIP压缩，通过临时文件实现两步处理。
	 * </p>
	 *
	 * @param inputFile  输入文件
	 * @param outputFile 输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(File inputFile, File outputFile) throws IOException {
		File tmpOutputFile = new File(FileUtils.getTempDirectory(), UUID.randomUUID() + ".tar");
		archiveTemplate.archive(inputFile, tmpOutputFile);

		try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tmpOutputFile)) {
			compressTemplate.compress(inputStream, outputFile);
		} finally {
			FileUtils.forceDeleteIfExist(tmpOutputFile);
		}
	}

	/**
	 * 将单个文件归档到输出流。
	 * <p>
	 * 先进行TAR归档，再进行GZIP压缩，通过临时文件实现两步处理。
	 * </p>
	 *
	 * @param inputFile    输入文件
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(File inputFile, OutputStream outputStream) throws IOException {
		File tmpOutputFile = new File(FileUtils.getTempDirectory(), UUID.randomUUID() + ".tar");
		archiveTemplate.archive(inputFile, tmpOutputFile);

		try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tmpOutputFile)) {
			compressTemplate.compress(inputStream, outputStream);
		} finally {
			FileUtils.forceDeleteIfExist(tmpOutputFile);
		}
	}

	/**
	 * 将多个文件归档到输出文件。
	 * <p>
	 * 先进行TAR归档，再进行GZIP压缩，通过临时文件实现两步处理。
	 * </p>
	 *
	 * @param inputFiles 输入文件集合
	 * @param outputFile 输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(Collection<File> inputFiles, File outputFile) throws IOException {
		File tmpOutputFile = new File(FileUtils.getTempDirectory(), UUID.randomUUID() + ".tar");
		archiveTemplate.archive(inputFiles, tmpOutputFile);

		try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tmpOutputFile)) {
			compressTemplate.compress(inputStream, outputFile);
		} finally {
			FileUtils.forceDeleteIfExist(tmpOutputFile);
		}
	}

	/**
	 * 将多个文件归档到输出流。
	 * <p>
	 * 先进行TAR归档，再进行GZIP压缩，通过临时文件实现两步处理。
	 * </p>
	 *
	 * @param inputFiles   输入文件集合
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(Collection<File> inputFiles, OutputStream outputStream) throws IOException {
		File tmpOutputFile = new File(FileUtils.getTempDirectory(), UUID.randomUUID() + ".tar");
		archiveTemplate.archive(inputFiles, tmpOutputFile);

		try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tmpOutputFile)) {
			compressTemplate.compress(inputStream, outputStream);
		} finally {
			FileUtils.forceDeleteIfExist(tmpOutputFile);
		}
	}
}
