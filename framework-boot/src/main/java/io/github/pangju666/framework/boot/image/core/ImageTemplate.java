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
 *   <li>抽象常见图像处理能力：信息读取、缩放与尺寸调整。</li>
 *   <li>方法语义明确：{@code resize} 不保持长宽比，{@code scale} 保持长宽比；重采样策略由实现类定义。</li>
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
public interface ImageTemplate<T> {
	/**
	 * 读取并返回图像基础信息（尺寸、格式、MIME 类型、文件大小等）。
	 *
	 * @param imageFile 待解析的图像文件
	 * @return 图像信息
	 * @throws IOException 文件读取或图像解码失败
	 * @since 1.0.0
	 */
	ImageInfo readImageInfo(File imageFile) throws IOException;

	default void execute(File inputImage, File outputImageFile, ImageOperation operation) throws IOException {
		execute(inputImage, outputImageFile, operation, null);
	}

	default void execute(ImageInfo imageInfo, File outputImageFile, ImageOperation operation) throws IOException {
		execute(imageInfo, outputImageFile, operation, null);
	}

	default void execute(File inputImage, File outputImageFile, ImageOperation operation, Consumer<T> imageConsumer) throws IOException {
		execute(readImageInfo(inputImage), outputImageFile, operation, null);
	}

	void execute(ImageInfo imageInfo, File outputImageFile, ImageOperation operation, Consumer<T> imageConsumer) throws IOException;

	boolean canRead(String mimeType);

	boolean canWrite(String mimeType);
}
