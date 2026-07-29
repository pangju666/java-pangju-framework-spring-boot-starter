package io.github.pangju666.framework.boot.compress.core.impl;

import io.github.pangju666.commons.compress.utils.SevenZUtils;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.framework.boot.compress.autoconfigure.CompressProperties;
import io.github.pangju666.framework.boot.compress.core.ArchiveEncryptTemplate;
import io.github.pangju666.framework.boot.compress.core.ArchiveTemplate;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 7-Zip归档模板实现类。
 * <p>
 * 基于7-Zip格式实现文件归档功能，支持加密归档和压缩方法配置。
 * 7-Zip是一种高压缩率的归档格式，支持多种压缩算法。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>支持单个文件和多个文件的归档操作</li>
 *   <li>支持密码保护的加密归档</li>
 *   <li>可配置压缩方法（LZMA、LZMA2、PPMD等）</li>
 *   <li>支持输出到文件或输出流</li>
 *   <li>通过临时文件实现输出流输出，自动清理临时文件</li>
 * </ul>
 *
 * <p><strong>适用场景</strong></p>
 * <ul>
 *   <li>需要高压缩率的归档场景</li>
 *   <li>需要密码保护的敏感数据归档</li>
 *   <li>大文件或大量文件的归档</li>
 *   <li>需要跨平台的归档格式</li>
 * </ul>
 *
 * @since 2.1.0
 */
public class SevenZArchiveTemplate implements ArchiveTemplate, ArchiveEncryptTemplate {
	/**
	 * 7-Zip压缩方法。
	 *
	 * @since 2.1.0
	 */
	private final SevenZMethod method;

	/**
	 * 构造函数。
	 *
	 * @param properties 压缩配置属性
	 * @since 2.1.0
	 */
	public SevenZArchiveTemplate(CompressProperties properties) {
		this.method = properties.getSevenZ().getMethod();
	}

	/**
	 * 将单个文件加密归档到输出文件。
	 * <p>
	 * 如果配置了压缩方法，则使用指定的压缩方法进行归档。
	 * </p>
	 *
	 * @param inputFile 输入文件
	 * @param outputFile 输出文件
	 * @param password 加密密码
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(File inputFile, File outputFile, String password) throws IOException {
		if (Objects.nonNull(method)) {
			SevenZUtils.archive(inputFile, outputFile, password, method);
		} else {
			SevenZUtils.archive(inputFile, outputFile, password);
		}
	}

	/**
	 * 将多个文件加密归档到输出文件。
	 * <p>
	 * 如果配置了压缩方法，则使用指定的压缩方法进行归档。
	 * </p>
	 *
	 * @param inputFiles 输入文件列表
	 * @param outputFile 输出文件
	 * @param password 加密密码
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(List<File> inputFiles, File outputFile, String password) throws IOException {
		if (Objects.nonNull(method)) {
			SevenZUtils.archive(inputFiles, outputFile, password, method);
		} else {
			SevenZUtils.archive(inputFiles, outputFile, password);
		}
	}

	/**
	 * 将单个文件加密归档到输出流。
	 * <p>
	 * 通过临时文件实现输出流输出，处理完成后自动清理临时文件。
	 * 如果配置了压缩方法，则使用指定的压缩方法进行归档。
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
		File tmpOutputFile = new File(FileUtils.getTempDirectory(), UUID.randomUUID() + ".7z");

		if (Objects.nonNull(method)) {
			SevenZUtils.archive(inputFile, tmpOutputFile, password, method);
		} else {
			SevenZUtils.archive(inputFile, tmpOutputFile, password);
		}

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
	 * 如果配置了压缩方法，则使用指定的压缩方法进行归档。
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
		File tmpOutputFile = new File(FileUtils.getTempDirectory(), UUID.randomUUID() + ".7z");

		if (Objects.nonNull(method)) {
			SevenZUtils.archive(inputFiles, tmpOutputFile, password, method);
		} else {
			SevenZUtils.archive(inputFiles, tmpOutputFile, password);
		}

		try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tmpOutputFile)) {
			inputStream.transferTo(outputStream);
		} finally {
			FileUtils.forceDeleteIfExist(tmpOutputFile);
		}
	}

	/**
	 * 将单个文件归档到输出文件。
	 * <p>
	 * 如果配置了压缩方法，则使用指定的压缩方法进行归档。
	 * </p>
	 *
	 * @param inputFile 输入文件
	 * @param outputFile 输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(File inputFile, File outputFile) throws IOException {
		if (Objects.nonNull(method)) {
			SevenZUtils.archive(inputFile, outputFile, method);
		} else {
			SevenZUtils.archive(inputFile, outputFile);
		}
	}

	/**
	 * 将单个文件归档到输出流。
	 * <p>
	 * 通过临时文件实现输出流输出，处理完成后自动清理临时文件。
	 * 如果配置了压缩方法，则使用指定的压缩方法进行归档。
	 * </p>
	 *
	 * @param inputFile 输入文件
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(File inputFile, OutputStream outputStream) throws IOException {
		File tmpOutputFile = new File(FileUtils.getTempDirectory(), UUID.randomUUID() + ".7z");

		if (Objects.nonNull(method)) {
			SevenZUtils.archive(inputFile, tmpOutputFile, method);
		} else {
			SevenZUtils.archive(inputFile, tmpOutputFile);
		}

		try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tmpOutputFile)) {
			inputStream.transferTo(outputStream);
		} finally {
			FileUtils.forceDeleteIfExist(tmpOutputFile);
		}
	}

	/**
	 * 将多个文件归档到输出文件。
	 * <p>
	 * 如果配置了压缩方法，则使用指定的压缩方法进行归档。
	 * </p>
	 *
	 * @param inputFiles 输入文件集合
	 * @param outputFile 输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(Collection<File> inputFiles, File outputFile) throws IOException {
		if (Objects.nonNull(method)) {
			SevenZUtils.archive(inputFiles, outputFile, method);
		} else {
			SevenZUtils.archive(inputFiles, outputFile);
		}
	}

	/**
	 * 将多个文件归档到输出流。
	 * <p>
	 * 通过临时文件实现输出流输出，处理完成后自动清理临时文件。
	 * 如果配置了压缩方法，则使用指定的压缩方法进行归档。
	 * </p>
	 *
	 * @param inputFiles 输入文件集合
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(Collection<File> inputFiles, OutputStream outputStream) throws IOException {
		File tmpOutputFile = new File(FileUtils.getTempDirectory(), UUID.randomUUID() + ".7z");

		if (Objects.nonNull(method)) {
			SevenZUtils.archive(inputFiles, tmpOutputFile, method);
		} else {
			SevenZUtils.archive(inputFiles, tmpOutputFile);
		}

		try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tmpOutputFile)) {
			inputStream.transferTo(outputStream);
		} finally {
			FileUtils.forceDeleteIfExist(tmpOutputFile);
		}
	}
}
