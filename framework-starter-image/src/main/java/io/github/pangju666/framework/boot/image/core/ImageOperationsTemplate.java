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
 * 定义了图像处理的核心操作接口，提供统一的图像处理能力。
 * 支持将图像资源处理并输出到输出流或文件，以及检查读写能力的方法。
 * </p>
 *
 * <p><strong>核心功能</strong></p>
 * <ul>
 *   <li>支持图像处理到输出流或文件</li>
 *   <li>支持自定义输出格式</li>
 *   <li>支持检查资源读取能力</li>
 *   <li>支持检查格式写入能力</li>
 *   <li>提供默认方法简化使用</li>
 * </ul>
 *
 * <p><strong>使用方式</strong></p>
 * <ul>
 *   <li>通过Spring自动配置注入对应的实现类</li>
 *   <li>根据配置的引擎类型选择不同的实现（GraphicsMagick、OpenCV、ImageIO）</li>
 *   <li>使用ImageOperations配置具体的图像操作</li>
 * </ul>
 *
 * @since 2.1.0
 * @see ImageOperations
 * @see IOResource
 */
public interface ImageOperationsTemplate {
	/**
	 * 处理图像资源并输出到输出流。
	 * <p>
	 * 使用资源本身的格式作为输出格式，自动从资源中提取格式信息。
	 * 如果资源格式为空或无法识别，则抛出异常。
	 * </p>
	 *
	 * @param resource     图像资源，不能为null
	 * @param outputStream 输出流，不能为null
	 * @param operations   图像操作配置，不能为null
	 * @throws UnsupportedResourceException 不支持的资源异常，当资源类型不被支持时抛出
	 * @throws ImageParsingException        图像解析异常，当图像解析失败时抛出
	 * @throws ImageOperationException      图像操作异常，当图像操作失败时抛出
	 * @since 2.1.0
	 */
	default void process(IOResource resource, OutputStream outputStream, ImageOperations<?> operations)
		throws UnsupportedResourceException, ImageParsingException, ImageOperationException {
		Assert.notNull(resource, "resource 不可为 null");

		process(resource, outputStream, resource.getFormat(), operations);
	}

	/**
	 * 处理图像资源并输出到输出流。
	 * <p>
	 * 使用指定的输出格式进行图像处理和输出。
	 * 需要先通过{@link #canWrite(String)}检查是否支持该格式。
	 * </p>
	 *
	 * @param resource     图像资源，不能为null
	 * @param outputStream 输出流，不能为null
	 * @param outputFormat 输出格式，不能为null
	 * @param operations   图像操作配置，不能为null
	 * @throws UnsupportedResourceException 不支持的资源异常，当资源类型不被支持时抛出
	 * @throws ImageParsingException        图像解析异常，当图像解析失败时抛出
	 * @throws ImageOperationException      图像操作异常，当图像操作失败时抛出
	 * @since 2.1.0
	 */
	void process(IOResource resource, OutputStream outputStream, String outputFormat, ImageOperations<?> operations)
		throws UnsupportedResourceException, ImageParsingException, ImageOperationException;

	/**
	 * 处理图像资源并输出到文件。
	 * <p>
	 * 使用输出文件的扩展名作为输出格式，自动从文件名中提取格式信息。
	 * 如果文件扩展名无法识别，则抛出异常。
	 * </p>
	 *
	 * @param resource   图像资源，不能为null
	 * @param outputFile 输出文件，不能为null
	 * @param operations 图像操作配置，不能为null
	 * @throws UnsupportedResourceException 不支持的资源异常，当资源类型不被支持时抛出
	 * @throws ImageParsingException        图像解析异常，当图像解析失败时抛出
	 * @throws ImageOperationException      图像操作异常，当图像操作失败时抛出
	 * @since 2.1.0
	 */
	void process(IOResource resource, File outputFile, ImageOperations<?> operations)
		throws UnsupportedResourceException, ImageParsingException, ImageOperationException;

	/**
	 * 检查是否支持读取指定资源。
	 * <p>
	 * 根据资源类型和实现引擎判断是否支持读取该资源。
	 * 不同的实现类可能支持不同的资源类型。
	 * </p>
	 *
	 * @param resource 图像资源，不能为null
	 * @return 如果支持读取返回true，否则返回false
	 * @since 2.1.0
	 */
	boolean canRead(IOResource resource);

	/**
	 * 检查是否支持写入指定格式。
	 * <p>
	 * 根据格式和实现引擎判断是否支持写入该格式。
	 * 不同的实现类可能支持不同的输出格式。
	 * </p>
	 *
	 * @param format 图像格式，不能为null
	 * @return 如果支持写入返回true，否则返回false
	 * @since 2.1.0
	 */
	boolean canWrite(String format);
}
