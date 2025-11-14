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

import io.github.pangju666.framework.web.annotation.HttpException;
import io.github.pangju666.framework.web.enums.HttpExceptionType;
import io.github.pangju666.framework.web.exception.base.ServiceException;

import java.io.File;

/**
 * 图片损坏异常。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>用于标识图像文件在解析、读取或校验过程中被判断为损坏（内容不完整、格式非法、无法解码等）。</li>
 *   <li>继承 {@link ServiceException}，作为服务层的业务异常，并通过 {@link HttpException} 映射为 HTTP 错误。</li>
 * </ul>
 *
 * <p><b>HTTP 映射</b></p>
 * <ul>
 *   <li>错误码：{@code 220}</li>
 *   <li>类型：{@link HttpExceptionType#SERVICE}</li>
 *   <li>描述：{@code 图片损坏错误}</li>
 * </ul>
 *
 * <p><b>示例</b></p>
 * <pre>
 * File image = new File("/path/to/broken.jpg");
 * throw new ImageDamageException(image, "无法解析 JPEG 数据流");
 * </pre>
 *
 * @author pangju666
 * @since 1.0.0
 */
@HttpException(code = 220, type = HttpExceptionType.SERVICE, description = "图片损坏错误")
public class ImageDamageException extends ServiceException {
	/**
	 * 默认异常消息：{@code 图片已损坏}。
	 *
	 * @since 1.0.0
	 */
	private static final String DEFAULT_MESSAGE = "图片已损坏";

	/**
	 * 使用默认消息构造图片损坏异常。
	 *
	 * @param file   发生错误的图片文件
	 * @param reason 详细错误原因（例如：无法解析数据流、格式非法等）
	 *
	 * @since 1.0.0
	 */
	public ImageDamageException(File file, String reason) {
		this(DEFAULT_MESSAGE, file, reason);
	}

	/**
	 * 使用默认消息并携带根因构造图片损坏异常。
	 *
	 * @param file   发生错误的图片文件
	 * @param reason 详细错误原因说明
	 * @param cause  原始异常根因，用于调试与日志定位
	 *
	 * @since 1.0.0
	 */
	public ImageDamageException(File file, String reason, Throwable cause) {
		this(DEFAULT_MESSAGE, file, reason, cause);
	}

	/**
	 * 指定基础消息与详细原因的构造方法。
	 *
	 * <p>最终异常消息格式：{@code 基础消息 + " 图片文件：" + 绝对路径 + " " + 原因}</p>
	 *
	 * @param message 基础异常消息（例如：图片已损坏）
	 * @param file    发生错误的图片文件
	 * @param reason  详细错误原因说明
	 *
	 * @since 1.0.0
	 */
	public ImageDamageException(String message, File file, String reason) {
		super(message, "图片文件：" + file.getAbsolutePath() + " " + reason);
	}

	/**
	 * 指定基础消息、详细原因与根因的构造方法。
	 *
	 * <p>最终异常消息格式：{@code 基础消息 + " 图片文件：" + 绝对路径 + " " + 原因}</p>
	 *
	 * @param message 基础异常消息
	 * @param file    发生错误的图片文件
	 * @param reason  详细错误原因说明
	 * @param cause   原始异常根因
	 *
	 * @since 1.0.0
	 */
	public ImageDamageException(String message, File file, String reason, Throwable cause) {
		super(message, "图片文件：" + file.getAbsolutePath() + " " + reason, cause);
	}
}
