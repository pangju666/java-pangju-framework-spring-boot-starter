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

import io.github.pangju666.framework.boot.image.model.ImageInfo;
import io.github.pangju666.framework.boot.image.model.ImageOperation;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * 图像处理模板接口。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>抽象常见图像处理能力：信息读取、图像操作。</li>
 * </ul>
 *
 * <p><b>类型参数</b></p>
 * <ul>
 *   <li>{@code T}：操作配置类型，需继承 {@link ImageOperation}。</li>
 *   <li>{@code U}：底层图像对象类型（例如 {@code BufferedImage} 或 GM 的中间表示）。</li>
 * </ul>
 *
 * <p><b>能力判定</b></p>
 * <ul>
 *   <li>结合能力集合进行读写支持探测（如 {@code ImageConstants} 的可读/可写类型集合）。</li>
 *   <li>不受支持类型可抛出服务层异常用于 HTTP 映射。</li>
 * </ul>
 *
 * <p><b>I/O 约束</b></p>
 * <ul>
 *   <li>输入与输出文件建议分离，输出覆盖策略由实现决定。</li>
 *   <li>回调为 {@code null} 时不进行中间态处理。</li>
 * </ul>
 *
 * <p><b>异常</b></p>
 * <ul>
 *   <li>方法可能抛出 {@link IOException}，表示底层 I/O 或解码错误。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public interface ImageTemplate<T extends ImageOperation, U> {
	/**
	 * 读取并返回图像基础信息（尺寸、格式、MIME 类型、文件大小等）。
	 *
	 * @param imageFile 待解析的图像文件
	 * @return 图像信息
	 * @throws IOException 文件读取或图像解码失败
	 * @since 1.0.0
	 */
	ImageInfo readImageInfo(File imageFile) throws IOException;

	/**
	 * 执行图像操作并写入输出文件（使用文件作为输入；不提供回调）。
	 *
	 * <p>说明：相当于调用带回调的重载方法并传入 {@code null}，不触发中间处理回调。</p>
	 *
	 * @param inputFile  输入文件
	 * @param outputFile 输出文件
	 * @param operation  操作配置
	 * @throws IOException I/O 或解码错误
	 * @since 1.0.0
	 */
	default void execute(File inputFile, File outputFile, T operation) throws IOException {
		execute(inputFile, outputFile, operation, null);
	}

	/**
	 * 执行图像操作并写入输出文件（使用已解析的图像信息；不提供回调）。
	 *
	 * <p>说明：相当于调用带回调的重载方法并传入 {@code null}，不触发中间处理回调。</p>
	 *
	 * @param imageInfo  已解析的图像信息
	 * @param outputFile 输出文件
	 * @param operation  操作配置
	 * @throws IOException I/O 或解码错误
	 * @since 1.0.0
	 */
	default void execute(ImageInfo imageInfo, File outputFile, T operation) throws IOException {
		execute(imageInfo, outputFile, operation, null);
	}

	/**
	 * 执行图像操作并写入输出文件（以文件为输入）。
	 *
	 * <p>说明：当提供 {@code imageConsumer} 时，实现可在关键步骤向调用方提供底层图像对象以进行额外处理；
	 * 如果 {@code imageConsumer} 为空，则不进行回调。</p>
	 *
	 * @param inputImage   输入图像文件
	 * @param outputFile   输出文件
	 * @param operation    操作配置
	 * @param imageConsumer 中间处理回调，接受底层图像对象
	 * @throws IOException I/O 或解码错误
	 * @since 1.0.0
	 */
	void execute(File inputImage, File outputFile, T operation, Consumer<U> imageConsumer) throws IOException;

	/**
	 * 执行图像操作并写入输出文件（以已解析的图像信息为输入）。
	 *
	 * <p>说明：当提供 {@code imageConsumer} 时，实现可在关键步骤向调用方提供底层图像对象以进行额外处理；
	 * 如果 {@code imageConsumer} 为空，则不进行回调。</p>
	 *
	 * @param imageInfo    已解析的图像信息
	 * @param outputFile   输出文件
	 * @param operation    操作配置
	 * @param imageConsumer 中间处理回调，接受底层图像对象
	 * @throws IOException I/O 或解码错误
	 * @since 1.0.0
	 */
	void execute(ImageInfo imageInfo, File outputFile, T operation, Consumer<U> imageConsumer) throws IOException;

	/**
	 * 判断实现是否支持读取指定文件类型。
	 *
	 * <p>说明：实现可结合能力集合进行判定（例如 {@code ImageConstants} 的可读类型集合）。</p>
	 *
	 * @param file 待判定文件
	 * @return {@code true} 表示支持读取
	 * @throws IOException I/O 或探测错误
	 * @since 1.0.0
	 */
	boolean canRead(File file) throws IOException;

	/**
	 * 判断实现是否支持写出指定文件类型。
	 *
	 * <p>说明：实现可结合能力集合进行判定（例如 {@code ImageConstants} 的可写类型集合）。</p>
	 *
	 * @param file 待判定文件
	 * @return {@code true} 表示支持写出
	 * @throws IOException I/O 或探测错误
	 * @since 1.0.0
	 */
	boolean canWrite(File file) throws IOException;
}
