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

package io.github.pangju666.framework.autoconfigure.web.log.annotation;

import java.lang.annotation.*;

/**
 * Web 日志操作描述注解
 * <p>
 * 该注解用于标记在记录 Web 请求日志时，对操作的功能进行描述。
 * 它可以应用于控制器的方法，为该方法的日志附加操作说明，便于日志检索与分析。
 * </p>
 *
 * <p>功能说明：</p>
 * <ul>
 *     <li>为控制器方法配置操作描述，用于标明该方法的功能或用途。</li>
 *     <li>操作描述将记录在 {@link io.github.pangju666.framework.autoconfigure.web.log.model.WebLog#getOperation()} 字段中。</li>
 *     <li>结合 {@link io.github.pangju666.framework.autoconfigure.web.log.filter.WebLogFilter} 使用，在日志中显示操作说明。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>在方法级别为接口标识功能描述，例如 "查询用户信息"、"更新订单状态"。</li>
 *     <li>配合日志分析工具便于快速定位和统计操作类型。</li>
 * </ul>
 *
 * <p>适用范围：</p>
 * <ul>
 *     <li>仅能标注在方法上。</li>
 * </ul>
 *
 * <p>示例代码：</p>
 * <pre>
 * &#64;RestController
 * &#64;RequestMapping("/api/users")
 * public class UserController {
 *
 *     &#64;GetMapping("/{id}")
 *     &#64;WebLogOperation("查询用户信息")
 *     public User getUserById(&#64;PathVariable Long id) {
 *         return userService.findById(id);
 *     }
 *
 *     &#64;PostMapping
 *     &#64;WebLogOperation("创建新用户")
 *     public void createUser(&#64;RequestBody User user) {
 *         userService.save(user);
 *     }
 * }
 * </pre>
 *
 * @author pangju666
 * @see io.github.pangju666.framework.autoconfigure.web.log.filter.WebLogFilter
 * @see io.github.pangju666.framework.autoconfigure.web.log.model.WebLog
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebLogOperation {
	/**
	 * 操作描述
	 * <p>
	 * 设置当前方法的操作功能描述，用于日志记录中标识该方法的具体用途或操作。
	 * </p>
	 *
	 * <p>示例：</p>
	 * <pre>
	 * &#64;WebLogOperation("查询用户信息")
	 * public User getUserById(Long id);
	 * </pre>
	 *
	 * @return 操作描述字符串
	 * @since 1.0.0
	 */
	String value();
}
