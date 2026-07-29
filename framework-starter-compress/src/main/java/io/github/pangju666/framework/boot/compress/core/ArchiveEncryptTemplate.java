package io.github.pangju666.framework.boot.compress.core;

import io.github.pangju666.commons.compress.io.resource.CompressResource;
import io.github.pangju666.commons.compress.utils.CompressUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 加密归档模板接口。
 * <p>
 * 提供带密码保护的文件归档功能，支持单个文件或多个文件列表的加密归档操作。
 * </p>
 *
 * <p>
 * 需要解压可以使用{@link CompressUtils#uncompress(CompressResource, File, String)}
 * </p>
 *
 * @since 2.1.0
 */
public interface ArchiveEncryptTemplate {
	/**
	 * 将单个文件或目录加密归档到输出文件。
	 *
	 * @param inputFile  输入文件
	 * @param outputFile 输出文件
	 * @param password   加密密码
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	void archive(File inputFile, File outputFile, String password) throws IOException;

	/**
	 * 将多个文件或目录加密归档到输出文件。
	 *
	 * @param inputFiles 输入文件列表
	 * @param outputFile 输出文件
	 * @param password   加密密码
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	void archive(List<File> inputFiles, File outputFile, String password) throws IOException;

	/**
	 * 将单个文件或目录加密归档到输出流。
	 *
	 * @param inputFile    输入文件
	 * @param outputStream 输出流
	 * @param password     加密密码
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	void archive(File inputFile, OutputStream outputStream, String password) throws IOException;

	/**
	 * 将多个文件或目录加密归档到输出流。
	 *
	 * @param inputFiles   输入文件列表
	 * @param outputStream 输出流
	 * @param password     加密密码
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	void archive(List<File> inputFiles, OutputStream outputStream, String password) throws IOException;
}
