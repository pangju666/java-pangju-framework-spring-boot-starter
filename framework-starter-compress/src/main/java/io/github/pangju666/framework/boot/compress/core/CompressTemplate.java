package io.github.pangju666.framework.boot.compress.core;

import io.github.pangju666.commons.io.resource.IOResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 压缩模板接口。
 * <p>
 * 提供数据压缩功能，支持输入流、文件或IO资源到输出流或文件的压缩操作。
 * </p>
 *
 * @since 2.1.0
 */
public interface CompressTemplate {
	/**
	 * 将输入流压缩到输出流。
	 *
	 * @param inputStream  输入流
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	void compress(InputStream inputStream, OutputStream outputStream) throws IOException;

	/**
	 * 将输入流压缩到输出文件。
	 *
	 * @param inputStream 输入流
	 * @param outputFile  输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	void compress(InputStream inputStream, File outputFile) throws IOException;

	/**
	 * 将IO资源压缩到输出流。
	 *
	 * @param resource     IO资源
	 * @param outputStream 输出流
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	void compress(IOResource resource, OutputStream outputStream) throws IOException;

	/**
	 * 将IO资源压缩到输出文件。
	 *
	 * @param resource   IO资源
	 * @param outputFile 输出文件
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	void compress(IOResource resource, File outputFile) throws IOException;
}
