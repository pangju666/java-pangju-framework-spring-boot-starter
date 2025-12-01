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

package io.github.pangju666.framework.boot.web.exception;

import io.github.pangju666.framework.web.annotation.HttpException;
import io.github.pangju666.framework.web.enums.HttpExceptionType;
import io.github.pangju666.framework.web.exception.base.ValidationException;
import org.springframework.http.HttpStatus;

/**
 * 请求数据解密失败异常。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>用于标识请求参数或请求体在解密过程中发生的校验类错误。</li>
 *   <li>使用场景：密文解密失败或解码错误。</li>
 * </ul>
 *
 * 特点：
 * <ul>
 *     <li>错误码：-4510（{@link HttpExceptionType#VALIDATION} + 510）</li>
 *     <li>HTTP状态码：400（{@link HttpStatus#BAD_REQUEST}）</li>
 *     <li>记录日志</li>
 *     <li>直接使用错误消息作为原因</li>
 * </ul>
 * </p>
 *
 * <p><strong>说明</strong></p>
 * <ul>
 *   <li>建议传入明确的错误信息以便客户端识别具体问题（例如“解密失败：数据被篡改”）。</li>
 *   <li>通常由请求体解密通知或参数解析器在进入控制器前抛出。</li>
 *   <li>避免在错误信息或日志中包含密钥、密文或原始参数等敏感数据。</li>
 * </ul>
 *
 * @author pangju666
 * @see ValidationException
 * @see HttpExceptionType
 * @see io.github.pangju666.framework.web.annotation.HttpException
 * @since 1.0.0
 */
@HttpException(code = 510, type = HttpExceptionType.VALIDATION, description = "请求数据解密失败", status = HttpStatus.BAD_REQUEST)
public class RequestDataDecryptFailureException extends ValidationException {
    /**
     * 基于描述信息构造解密异常。
     *
     * @param message 错误描述信息（面向客户端的可读提示）
     * @since 1.0.0
     */
	public RequestDataDecryptFailureException(String message) {
		super(message);
	}

    /**
     * 基于描述信息与根因构造解密异常。
     *
     * @param message 错误描述信息（面向客户端的可读提示）
     * @param cause   导致异常的根因（便于日志与问题排查）
     * @since 1.0.0
     */
	public RequestDataDecryptFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}
