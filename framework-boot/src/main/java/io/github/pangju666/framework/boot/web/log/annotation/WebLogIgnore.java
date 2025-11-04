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

package io.github.pangju666.framework.boot.web.log.annotation;

import io.github.pangju666.framework.boot.web.log.filter.WebLogFilter;

import java.lang.annotation.*;

/**
 * Web 日志忽略注解
 * <p>
 * 该注解用于标记需要忽略 Web 日志记录的控制器类或方法。
 * 当类或方法上标记了此注解时，任何针对该类或方法的 HTTP 请求和响应数据
 * 都不会被 {@link WebLogFilter} 拦截和记录。
 * </p>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>敏感信息接口：通过标记此注解，防止敏感信息的访问被记录到日志中。</li>
 *     <li>非必要记录的接口：对于某些无需记录日志的接口（如心跳检测接口），可以用此注解进行排除。</li>
 * </ul>
 *
 * <p>注解作用范围：</p>
 * <ul>
 *     <li>标注在 {@code 类} 上时：忽略该类下所有方法的日志记录。</li>
 *     <li>标注在 {@code 方法} 上时：仅忽略该方法的日志记录。</li>
 * </ul>
 *
 * <p>示例代码：</p>
 * <pre>
 * // 忽略整个控制器的日志
 * &#64;RestController
 * &#64;RequestMapping("/api/internal")
 * &#64;WebLogIgnore
 * public class InternalController {
 *
 *     &#64;GetMapping("/heartbeat")
 *     public String heartbeat() {
 *         return "OK";
 *     }
 * }
 *
 * // 忽略特定方法的日志
 * &#64;RestController
 * &#64;RequestMapping("/api/users")
 * public class UserController {
 *
 *     &#64;GetMapping("/{id}")
 *     public User getUserById(&#64;PathVariable Long id) {
 *         return userService.findById(id);
 *     }
 *
 *     &#64;DeleteMapping("/{id}")
 *     &#64;WebLogIgnore
 *     public void deleteUser(&#64;PathVariable Long id) {
 *         userService.deleteById(id);
 *     }
 * }
 * </pre>
 *
 * @author pangju666
 * @see WebLogFilter
 * @see WebLogOperation
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebLogIgnore {
}
