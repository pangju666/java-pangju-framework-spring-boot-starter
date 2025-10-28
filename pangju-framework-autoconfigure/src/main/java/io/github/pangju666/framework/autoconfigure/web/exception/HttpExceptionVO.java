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

package io.github.pangju666.framework.autoconfigure.web.exception;

import io.github.pangju666.framework.web.enums.HttpExceptionType;

/**
 * HTTP异常信息值对象
 * <p>
 * 用于传输HTTP异常的基本信息，包含：
 * <ul>
 *     <li>异常类型：标识异常的分类</li>
 *     <li>错误码：唯一标识异常的编码</li>
 *     <li>描述：对异常的文字说明</li>
 * </ul>
 * </p>
 *
 * @param type        HTTP异常类型
 * @param code        错误码
 * @param description 异常描述
 * @author pangju666
 * @since 1.0.0
 */
public record HttpExceptionVO(HttpExceptionType type, int code, String description) {
}