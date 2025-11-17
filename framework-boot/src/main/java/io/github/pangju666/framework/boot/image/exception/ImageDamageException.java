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
 * 图片损坏或解析失败异常。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>用于标识图像文件无法被正常解析或内容损坏。</li>
 *   <li>适用于读取图像信息、解码图像等失败的场景。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class ImageDamageException extends NestedRuntimeException {
	/**
	 * 默认异常消息：图片解析失败。
	 *
	 * @since 1.0.0
	 */
	private static final String DEFAULT_MESSAGE = "图片解析失败";

	/**
	 * 使用默认消息构造异常。
	 *
	 * @since 1.0.0
	 */
	public ImageDamageException() {
		super(DEFAULT_MESSAGE);
	}

	/**
	 * 指定文件与失败原因的构造方法。
	 *
	 * @param file   发生错误的图片文件
	 * @param reason 失败原因描述
	 * @since 1.0.0
	 */
	public ImageDamageException(File file, String reason) {
		super("图片文件：" + file.getAbsolutePath() + " " + reason);
	}

	/**
	 * 使用自定义消息构造异常。
	 *
	 * @param message 异常消息
	 * @since 1.0.0
	 */
	public ImageDamageException(String message) {
		super(message);
	}

	/**
	 * 使用默认消息与原因构造异常。
	 *
	 * @param cause 原始异常原因
	 * @since 1.0.0
	 */
	public ImageDamageException(Throwable cause) {
		super(DEFAULT_MESSAGE, cause);
	}

	/**
	 * 指定文件、失败原因与原始异常原因的构造方法。
	 *
	 * @param file   发生错误的图片文件
	 * @param reason 失败原因描述
	 * @param cause  原始异常原因
	 * @since 1.0.0
	 */
	public ImageDamageException(File file, String reason, Throwable cause) {
		super("图片文件：" + file.getAbsolutePath() + " " + reason, cause);
	}

	/**
	 * 使用自定义消息与原因构造异常。
	 *
	 * @param message 异常消息
	 * @param cause   原始异常原因
	 * @since 1.0.0
	 */
	public ImageDamageException(String message, Throwable cause) {
		super(message, cause);
	}
}
