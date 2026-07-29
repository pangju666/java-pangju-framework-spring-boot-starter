/*
 *   Copyright 2026 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.framework.boot.image.core;

import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.framework.boot.image.exception.ImageOperationException;
import io.github.pangju666.framework.boot.image.exception.ImageParsingException;
import io.github.pangju666.framework.boot.image.model.opeartions.ImageOperations;
import org.springframework.util.Assert;

import java.io.File;
import java.io.OutputStream;

/**
 * 图像操作模板接口。
 * <p>
 * 定义了图像处理的核心操作接口，包括处理图像资源到输出流或文件，
 * 以及检查读写能力的方法。
 * </p>
 *
 * @since 2.1.0
 */
public interface ImageOperationsTemplate {
	/**
	 * 处理图像资源并输出到输出流。
	 * <p>
	 * 使用资源本身的格式作为输出格式。
	 * </p>
	 *
	 * @param resource     图像资源
	 * @param outputStream 输出流
	 * @param operations   图像操作配置
	 * @throws UnsupportedResourceException 不支持的资源异常
	 * @throws ImageParsingException        图像解析异常
	 * @throws ImageOperationException      图像操作异常
	 * @since 2.1.0
	 */
	default void process(IOResource resource, OutputStream outputStream, ImageOperations<?> operations)
		throws UnsupportedResourceException, ImageParsingException, ImageOperationException {
		Assert.notNull(resource, "resource 不可为 null");

		process(resource, outputStream, resource.getFormat(), operations);
	}

	/**
	 * 处理图像资源并输出到输出流。
	 *
	 * @param resource     图像资源
	 * @param outputStream 输出流
	 * @param outputFormat 输出格式
	 * @param operations   图像操作配置
	 * @throws UnsupportedResourceException 不支持的资源异常
	 * @throws ImageParsingException        图像解析异常
	 * @throws ImageOperationException      图像操作异常
	 * @since 2.1.0
	 */
	void process(IOResource resource, OutputStream outputStream, String outputFormat, ImageOperations<?> operations)
		throws UnsupportedResourceException, ImageParsingException, ImageOperationException;

	/**
	 * 处理图像资源并输出到文件。
	 *
	 * @param resource   图像资源
	 * @param outputFile 输出文件
	 * @param operations 图像操作配置
	 * @throws UnsupportedResourceException 不支持的资源异常
	 * @throws ImageParsingException        图像解析异常
	 * @throws ImageOperationException      图像操作异常
	 * @since 2.1.0
	 */
	void process(IOResource resource, File outputFile, ImageOperations<?> operations)
		throws UnsupportedResourceException, ImageParsingException, ImageOperationException;

	/**
	 * 检查是否支持读取指定资源。
	 *
	 * @param resource 图像资源
	 * @return 如果支持读取返回true，否则返回false
	 * @since 2.1.0
	 */
	boolean canRead(IOResource resource);

	/**
	 * 检查是否支持写入指定格式。
	 *
	 * @param format 图像格式
	 * @return 如果支持写入返回true，否则返回false
	 * @since 2.1.0
	 */
	boolean canWrite(String format);
}
