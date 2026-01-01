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

package io.github.pangju666.framework.boot.image.exception;

import org.springframework.core.NestedRuntimeException;

import java.io.File;

/**
 * 图像操作异常。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>用于标识图像处理过程中的操作失败，包括解析、读写、校验、转换等。</li>
 *   <li>适用于读取图像信息、计算摘要、识别格式、尺寸/方向处理、编码/解码、缩放/裁剪、存储/删除失败等场景。</li>
 * </ul>
 *
 * <p><strong>消息格式</strong></p>
 * <ul>
 *   <li>基于文件的构造方法会拼接消息：{@code "图片文件：<absolute-path> <reason>"}。</li>
 *   <li>使用自定义消息的构造方法原样透传调用方提供的内容。</li>
 * </ul>
 *
 * <p><strong>异常类型</strong></p>
 * <ul>
 *   <li>为运行时异常（继承 {@link org.springframework.core.NestedRuntimeException}），适合在业务层统一捕获与处理。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see org.springframework.core.NestedRuntimeException
 */
public class ImageOperationException extends NestedRuntimeException {
	/**
	 * 指定文件与失败原因的构造方法。
	 *
	 * @param file   发生错误的图片文件
	 * @param reason 失败原因描述
	 * @throws NullPointerException 当 {@code file} 为 {@code null} 时可能引发空指针（消息拼接依赖绝对路径）
	 * @since 1.0.0
	 */
	public ImageOperationException(File file, String reason) {
		super("图片文件：" + file.getAbsolutePath() + " " + reason);
	}

	/**
	 * 使用自定义消息构造异常。
	 *
	 * @param message 异常消息
	 * @since 1.0.0
	 */
	public ImageOperationException(String message) {
		super(message);
	}

	/**
	 * 指定文件、失败原因与原始异常原因的构造方法。
	 *
	 * @param file   发生错误的图片文件
	 * @param reason 失败原因描述
	 * @param cause  原始异常原因
	 * @throws NullPointerException 当 {@code file} 为 {@code null} 时可能引发空指针（消息拼接依赖绝对路径）
	 * @since 1.0.0
	 */
	public ImageOperationException(File file, String reason, Throwable cause) {
		super("图片文件：" + file.getAbsolutePath() + " " + reason, cause);
	}

	/**
	 * 使用自定义消息与原因构造异常。
	 *
	 * @param message 异常消息
	 * @param cause   原始异常原因
	 * @since 1.0.0
	 */
	public ImageOperationException(String message, Throwable cause) {
		super(message, cause);
	}
}
