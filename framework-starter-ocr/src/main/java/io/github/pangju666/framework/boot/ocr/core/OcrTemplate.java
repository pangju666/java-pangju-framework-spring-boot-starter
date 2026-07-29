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

package io.github.pangju666.framework.boot.ocr.core;

import io.github.pangju666.commons.io.resource.IOResource;
import org.jspecify.annotations.Nullable;

/**
 * OCR模板接口。
 * <p>
 * 定义光学字符识别（OCR）的核心操作接口，支持对图像资源进行文字识别。
 * 提供带DPI参数和不带DPI参数的两种识别方法。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>支持多种图像资源的OCR识别</li>
 *   <li>支持自定义DPI参数以优化识别精度</li>
 *   <li>提供默认方法简化调用</li>
 * </ul>
 *
 * @since 2.1.0
 */
public interface OcrTemplate {
	/**
	 * 对图像资源进行OCR识别。
	 * <p>
	 * 使用默认DPI参数进行识别，内部调用{@link #ocrImage(IOResource, Integer)}方法。
	 * </p>
	 *
	 * @param resource 图像资源
	 * @return 识别出的文本内容
	 * @since 2.1.0
	 */
	default String ocrImage(IOResource resource) {
		return ocrImage(resource, null);
	}

	/**
	 * 对图像资源进行OCR识别。
	 * <p>
	 * 使用指定的DPI参数进行识别，DPI值可以影响识别精度。
	 * </p>
	 *
	 * @param resource 图像资源
	 * @param dpi 每英寸点数（DPI），可为null表示使用默认值
	 * @return 识别出的文本内容
	 * @since 2.1.0
	 */
	String ocrImage(IOResource resource, @Nullable Integer dpi);
}
