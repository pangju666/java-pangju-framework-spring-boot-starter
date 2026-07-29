package io.github.pangju666.framework.boot.compress.core.impl;

import io.github.pangju666.commons.compress.utils.ZipUtils;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.framework.boot.compress.autoconfigure.CompressProperties;
import io.github.pangju666.framework.boot.compress.core.ArchiveEncryptTemplate;
import io.github.pangju666.framework.boot.compress.core.ArchiveTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * ZIP归档模板实现类。
 * <p>
 * 基于ZIP格式实现文件归档功能，支持加密归档和压缩级别配置。
 * ZIP是最广泛使用的归档压缩格式之一，具有良好的跨平台兼容性和广泛的支持。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>支持单个文件和多个文件的归档操作</li>
 *   <li>支持密码保护的加密归档</li>
 *   <li>可配置压缩级别（0-9，级别越高压缩率越好）</li>
 *   <li>支持输出到文件或输出流</li>
 *   <li>通过临时文件实现输出流输出，自动清理临时文件</li>
 *   <li>跨平台兼容性极佳</li>
 * </ul>
 *
 * <p><strong>适用场景</strong></p>
 * <ul>
 *   <li>需要广泛兼容性的归档场景</li>
 *   <li>跨平台文件分发</li>
 *   <li>需要密码保护的敏感数据归档</li>
 *   <li>软件安装包和资源包</li>
 *   <li>Windows系统文件归档</li>
 * </ul>
 *
 * @since 2.1.0
 */
public class ZipArchiveTemplate implements ArchiveTemplate, ArchiveEncryptTemplate {
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
	public ZipArchiveTemplate(CompressProperties properties) {
		this.compressionLevel = properties.getZip().getCompressionLevel();
	}

	/**
	 * 将单个文件加密归档到输出文件。
	 *
	 * @param inputFile 输入文件
	 * @param outputFile 输出文件
	 * @param password 加密密码
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(File inputFile, File outputFile, String password) throws IOException {
		ZipUtils.archive(inputFile, outputFile, password);
	}

	/**
	 * 将多个文件加密归档到输出文件。
	 *
	 * @param inputFiles 输入文件列表
	 * @param outputFile 输出文件
	 * @param password 加密密码
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(List<File> inputFiles, File outputFile, String password) throws IOException {
		ZipUtils.archive(inputFiles, outputFile, password);
	}

	/**
	 * 将单个文件加密归档到输出流。
	 * <p>
	 * 通过临时文件实现输出流输出，处理完成后自动清理临时文件。
	 * </p>
	 *
	 * @param inputFile 输入文件
	 * @param outputStream 输出流
	 * @param password 加密密码
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(File inputFile, OutputStream outputStream, String password) throws IOException {
		File tmpOutputFile = new File(FileUtils.getTempDirectory(), UUID.randomUUID() + ".zip");

		ZipUtils.archive(inputFile, tmpOutputFile, password);

		try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tmpOutputFile)) {
			inputStream.transferTo(outputStream);
		} finally {
			FileUtils.forceDeleteIfExist(tmpOutputFile);
		}
	}

	/**
	 * 将多个文件加密归档到输出流。
	 * <p>
	 * 通过临时文件实现输出流输出，处理完成后自动清理临时文件。
	 * </p>
	 *
	 * @param inputFiles 输入文件列表
	 * @param outputStream 输出流
	 * @param password 加密密码
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(List<File> inputFiles, OutputStream outputStream, String password) throws IOException {
		File tmpOutputFile = new File(FileUtils.getTempDirectory(), UUID.randomUUID() + ".zip");

		ZipUtils.archive(inputFiles, tmpOutputFile, password);

		try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tmpOutputFile)) {
			inputStream.transferTo(outputStream);
		} finally {
			FileUtils.forceDeleteIfExist(tmpOutputFile);
		}
	}

	/**
	 * 将单个文件归档到输出文件。
	 * <p>
	 * 如果配置了压缩级别，则使用指定的压缩级别进行归档。
	 * </p>
	 *
	 * @param inputFile 输入文件
	 * @param outputFile 输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(File inputFile, File outputFile) throws IOException {
		if (Objects.nonNull(compressionLevel)) {
			ZipUtils.archive(inputFile, outputFile, compressionLevel.getValue());
		} else {
			ZipUtils.archive(inputFile, outputFile);
		}
	}

	/**
	 * 将单个文件归档到输出流。
	 * <p>
	 * 如果配置了压缩级别，则使用指定的压缩级别进行归档。
	 * </p>
	 *
	 * @param inputFile 输入文件
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(File inputFile, OutputStream outputStream) throws IOException {
		if (Objects.nonNull(compressionLevel)) {
			ZipUtils.archive(inputFile, outputStream, compressionLevel.getValue());
		} else {
			ZipUtils.archive(inputFile, outputStream);
		}
	}

	/**
	 * 将多个文件归档到输出文件。
	 * <p>
	 * 如果配置了压缩级别，则使用指定的压缩级别进行归档。
	 * </p>
	 *
	 * @param inputFiles 输入文件集合
	 * @param outputFile 输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(Collection<File> inputFiles, File outputFile) throws IOException {
		if (Objects.nonNull(compressionLevel)) {
			ZipUtils.archive(inputFiles, outputFile, compressionLevel.getValue());
		} else {
			ZipUtils.archive(inputFiles, outputFile);
		}
	}

	/**
	 * 将多个文件归档到输出流。
	 * <p>
	 * 如果配置了压缩级别，则使用指定的压缩级别进行归档。
	 * </p>
	 *
	 * @param inputFiles 输入文件集合
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(Collection<File> inputFiles, OutputStream outputStream) throws IOException {
		if (Objects.nonNull(compressionLevel)) {
			ZipUtils.archive(inputFiles, outputStream, compressionLevel.getValue());
		} else {
			ZipUtils.archive(inputFiles, outputStream);
		}
	}
}
