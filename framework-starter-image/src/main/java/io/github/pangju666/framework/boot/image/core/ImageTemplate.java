/*
 *   Copyright 2025 pangju666
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

import io.github.pangju666.framework.boot.image.exception.ImageOperationException;
import io.github.pangju666.framework.boot.image.exception.ImageParsingException;
import io.github.pangju666.framework.boot.image.exception.UnSupportedTypeException;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;
import io.github.pangju666.framework.boot.image.model.ImageFile;
import io.github.pangju666.framework.boot.image.model.ImageOperation;

import java.io.File;
import java.io.IOException;

/**
 * 图像处理模板接口。
 *
 * <p><strong>概述</strong>：抽象常见图像处理能力（信息读取、图像操作）。</p>
 * <p><strong>能力判定</strong>：结合能力集合进行读写支持探测（如 {@link ImageConstants} 的可读/可写类型集合）；不受支持类型可抛出服务层异常用于 HTTP 映射。</p>
 * <p><strong>I/O 约束</strong>：输入与输出文件建议分离；输出覆盖策略由实现决定；回调为 {@code null} 时不进行中间态处理；线程安全与资源管理由各实现保证。</p>
 * <p><strong>异常</strong>：方法可能抛出 {@link IOException}（底层 I/O 或解码错误）；也可能抛出 {@link UnSupportedTypeException}（类型不受支持）、{@link ImageParsingException}（解析失败）与 {@link ImageOperationException}（操作失败）。</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public interface ImageTemplate {
	/**
	 * 读取并返回图像信息（尺寸、格式、MIME 类型、文件大小等）。
 	 *
 	 * @param file 待解析的图像文件
 	 * @return 图像信息
 	 * @throws UnSupportedTypeException 图像类型不受支持
	 * @throws ImageParsingException       图像类型、摘要或尺寸等信息解析失败
 	 * @throws ImageOperationException     图像底层操作错误
 	 * @throws IOException                 图像文件读取或图像解码失败
 	 * @since 1.0.0
 	 */
 	ImageFile read(File file) throws UnSupportedTypeException, ImageParsingException, ImageOperationException, IOException;

 	/**
 	 * 执行图像操作并写入输出文件（使用文件作为输入；不提供回调）。
 	 *
 	 * <p>说明：相当于调用带回调的重载方法并传入 {@code null}，不触发中间处理回调。</p>
 	 *
 	 * @param inputFile  输入文件
 	 * @param outputFile 输出文件
 	 * @param operation  操作配置，可为 {@code null}
	 * @throws UnSupportedTypeException 图像类型不受支持时抛出
	 * @throws ImageParsingException       图像类型、摘要或尺寸解析失败时抛出
	 * @throws ImageOperationException     图像底层操作错误时抛出
	 * @throws IOException                 文件读取失败时抛出
 	 * @since 1.0.0
 	 */
 	default void process(File inputFile, File outputFile, ImageOperation operation) throws UnSupportedTypeException,
 		ImageParsingException, ImageOperationException, IOException {
		process(read(inputFile), outputFile, operation);
 	}

 	/**
 	 * 执行图像操作并写入输出文件（使用已解析的图像信息；不提供回调）。
 	 *
 	 * <p>说明：相当于调用带回调的重载方法并传入 {@code null}，不触发中间处理回调。</p>
 	 *
 	 * @param imageFile  已解析的图像信息
 	 * @param outputFile 输出文件
 	 * @param operation  操作配置，可为 {@code null}
	 * @throws UnSupportedTypeException 图像类型不受支持时抛出
	 * @throws ImageParsingException       图像类型、摘要或尺寸解析失败时抛出
	 * @throws ImageOperationException     图像底层操作错误时抛出
	 * @throws IOException                 文件读取失败时抛出
 	 * @since 1.0.0
 	 */
	 void process(ImageFile imageFile, File outputFile, ImageOperation operation) throws UnSupportedTypeException,
		 ImageParsingException, ImageOperationException, IOException;

	/**
	 * 判断实现是否支持读取图像文件。
	 *
	 * <p>说明：实现可结合能力集合进行判定（例如 {@link ImageConstants} 的可读类型集合）。</p>
	 *
	 * @param file 待判定文件
	 * @return {@code true} 表示支持读取
	 * @throws IOException 文件读取失败时抛出
	 * @since 1.0.0
	 */
	boolean canRead(File file) throws IOException;

	/**
	 * 判断实现是否支持输出为指定的图像格式。
	 *
	 * <p>说明：实现可结合能力集合进行判定（例如 {@link ImageConstants} 的可写类型集合）。</p>
	 *
	 * @param format 待判定图像格式
	 * @return {@code true} 表示支持写出
	 * @since 1.0.0
	 */
	boolean canWrite(String format);
}
