package io.github.pangju666.framework.boot.compress.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * 归档模板接口。
 * <p>
 * 提供文件归档功能，支持单个文件或多个文件集合的归档操作。
 * </p>
 *
 * @since 2.1.0
 */
public interface ArchiveTemplate {
	/**
	 * 将单个文件归档到输出文件。
	 *
	 * @param inputFile  输入文件
	 * @param outputFile 输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	void archive(File inputFile, File outputFile) throws IOException;

	/**
	 * 将单个文件归档到输出流。
	 *
	 * @param inputFile    输入文件
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	void archive(File inputFile, OutputStream outputStream) throws IOException;

	/**
	 * 将多个文件归档到输出文件。
	 *
	 * @param inputFiles 输入文件集合
	 * @param outputFile 输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	void archive(Collection<File> inputFiles, File outputFile) throws IOException;

	/**
	 * 将多个文件归档到输出流。
	 *
	 * @param inputFiles   输入文件集合
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	void archive(Collection<File> inputFiles, OutputStream outputStream) throws IOException;
}
