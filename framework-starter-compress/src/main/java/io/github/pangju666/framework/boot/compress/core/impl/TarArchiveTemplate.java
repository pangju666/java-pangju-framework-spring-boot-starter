package io.github.pangju666.framework.boot.compress.core.impl;

import io.github.pangju666.commons.compress.utils.TarUtils;
import io.github.pangju666.framework.boot.compress.core.ArchiveTemplate;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * TAR归档模板实现类。
 * <p>
 * 基于TAR格式实现文件归档功能，支持单个文件或多个文件集合的归档操作。
 * TAR（Tape Archive）是一种经典的Unix归档格式，用于将多个文件打包成一个文件，但不进行压缩。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>支持单个文件和多个文件集合的归档操作</li>
 *   <li>支持输出到文件或输出流</li>
 *   <li>保留文件权限和元数据信息</li>
 *   <li>支持目录结构归档</li>
 * </ul>
 *
 * <p><strong>适用场景</strong></p>
 * <ul>
 *   <li>需要保留文件权限和元数据的归档场景</li>
 *   <li>Unix/Linux系统文件备份</li>
 *   <li>作为其他压缩格式的基础（如.tar.gz、.tar.xz）</li>
 *   <li>需要快速打包但不压缩的场景</li>
 * </ul>
 *
 * @since 2.1.0
 */
public class TarArchiveTemplate implements ArchiveTemplate {
	/**
	 * 将单个文件归档到输出文件。
	 *
	 * @param inputFile  输入文件
	 * @param outputFile 输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(File inputFile, File outputFile) throws IOException {
		TarUtils.archive(inputFile, outputFile);
	}

	/**
	 * 将单个文件归档到输出流。
	 *
	 * @param inputFile    输入文件
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(File inputFile, OutputStream outputStream) throws IOException {
		TarUtils.archive(inputFile, outputStream);
	}

	/**
	 * 将多个文件归档到输出文件。
	 *
	 * @param inputFiles 输入文件集合
	 * @param outputFile 输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(Collection<File> inputFiles, File outputFile) throws IOException {
		TarUtils.archive(inputFiles, outputFile);
	}

	/**
	 * 将多个文件归档到输出流。
	 *
	 * @param inputFiles   输入文件集合
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	@Override
	public void archive(Collection<File> inputFiles, OutputStream outputStream) throws IOException {
		TarUtils.archive(inputFiles, outputStream);
	}
}
