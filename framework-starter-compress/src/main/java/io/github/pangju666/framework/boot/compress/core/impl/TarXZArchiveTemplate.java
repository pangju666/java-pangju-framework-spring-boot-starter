package io.github.pangju666.framework.boot.compress.core.impl;

import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.framework.boot.compress.autoconfigure.CompressProperties;
import io.github.pangju666.framework.boot.compress.core.ArchiveTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.UUID;

/**
 * TAR.XZ归档模板实现类。
 * <p>
 * 基于TAR归档和XZ压缩的组合格式实现文件归档功能，
 * 先进行TAR归档将多个文件打包，再进行XZ压缩以获得更高的压缩率。
 * .tar.xz格式使用LZMA2压缩算法，通常比.tar.gz有更好的压缩率但压缩速度较慢。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>支持单个文件和多个文件集合的归档压缩</li>
 *   <li>先TAR归档保留文件结构，再XZ压缩获得高压缩率</li>
 *   <li>支持XZ压缩模式和预设级别配置</li>
 *   <li>通过临时文件实现两步处理，自动清理临时文件</li>
 *   <li>支持输出到文件或输出流</li>
 * </ul>
 *
 * <p><strong>适用场景</strong></p>
 * <ul>
 *   <li>需要最高压缩率的归档场景</li>
 *   <li>软件发布包（特别是开源项目）</li>
 *   <li>长期存储的归档文件</li>
 *   <li>网络传输受限的场景（节省带宽）</li>
 * </ul>
 *
 * @since 2.1.0
 */
public class TarXZArchiveTemplate implements ArchiveTemplate {
	/**
	 * 日志记录器
	 *
	 * @since 2.1.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TarXZArchiveTemplate.class);

	/**
	 * TAR归档模板。
	 *
	 * @since 2.1.0
	 */
	private final TarArchiveTemplate archiveTemplate;
	/**
	 * XZ压缩模板。
	 *
	 * @since 2.1.0
	 */
	private final XZCompressTemplate compressTemplate;

	/**
	 * 构造函数。
	 *
	 * @param properties 压缩配置属性
	 * @since 2.1.0
	 */
	public TarXZArchiveTemplate(CompressProperties properties) {
		this.archiveTemplate = new TarArchiveTemplate();
		this.compressTemplate = new XZCompressTemplate(properties);
	}

	/**
	 * 将单个文件归档到输出文件。
	 * <p>
	 * 先进行TAR归档，再进行XZ压缩，通过临时文件实现两步处理。
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
			if (!FileUtils.deleteQuietly(tmpOutputFile)) {
				LOGGER.error("临时输出文件：{} 删除失败", tmpOutputFile.getAbsolutePath());
			}
		}
	}

	/**
	 * 将单个文件归档到输出流。
	 * <p>
	 * 先进行TAR归档，再进行XZ压缩，通过临时文件实现两步处理。
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
			if (!FileUtils.deleteQuietly(tmpOutputFile)) {
				LOGGER.error("临时输出文件：{} 删除失败", tmpOutputFile.getAbsolutePath());
			}
		}
	}

	/**
	 * 将多个文件归档到输出文件。
	 * <p>
	 * 先进行TAR归档，再进行XZ压缩，通过临时文件实现两步处理。
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
			if (!FileUtils.deleteQuietly(tmpOutputFile)) {
				LOGGER.error("临时输出文件：{} 删除失败", tmpOutputFile.getAbsolutePath());
			}
		}
	}

	/**
	 * 将多个文件归档到输出流。
	 * <p>
	 * 先进行TAR归档，再进行XZ压缩，通过临时文件实现两步处理。
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
			if (!FileUtils.deleteQuietly(tmpOutputFile)) {
				LOGGER.error("临时输出文件：{} 删除失败", tmpOutputFile.getAbsolutePath());
			}
		}
	}
}
