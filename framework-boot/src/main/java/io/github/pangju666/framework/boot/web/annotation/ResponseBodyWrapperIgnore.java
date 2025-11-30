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

package io.github.pangju666.framework.boot.web.annotation;

import org.springframework.http.ResponseEntity;

import java.lang.annotation.*;

/**
 * 响应体统一包装忽略注解
 * <p>
 * 被标注的方法直接返回原始响应内容，不再包装为统一结构。
 * </p>
 * <p>
 * 适用场景：
 * <ul>
 *   <li>方法需要返回自定义格式或第三方协议响应</li>
 *   <li>返回 {@link ResponseEntity}（已自动排除，注解可用于明确意图）</li>
 *   <li>返回原始字节/文件/流等无需包装的内容</li>
 * </ul>
 * </p>
 * <p>
 * 优先级：当全局包装开启时，标注该注解的方法优先被排除，不参与统一包装。
 * </p>
 *
 * @author pangju666
 * @see ResponseEntity
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResponseBodyWrapperIgnore {
}