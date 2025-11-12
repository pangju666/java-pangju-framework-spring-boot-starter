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

import io.github.pangju666.framework.boot.web.log.interceptor.WebLogInterceptor;
import io.github.pangju666.framework.boot.web.log.model.WebLog;

import java.lang.annotation.*;

/**
 * Web 日志操作描述注解。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>用于在记录 Web 请求日志时，标注接口方法的业务操作描述。</li>
 *   <li>增强日志可读性与可检索性，便于统计与分析。</li>
 * </ul>
 *
 * <p><b>行为</b></p>
 * <ul>
 *   <li>在拦截过程中由 {@link WebLogInterceptor} 读取注解值，并填充到 {@link WebLog#getOperation()}。</li>
 *   <li>若未标注，则操作描述可为空，不影响日志采集与发送。</li>
 * </ul>
 *
 * <p><b>约束</b></p>
 * <ul>
 *   <li>仅可标注在方法级别（{@link java.lang.annotation.ElementType#METHOD}）。</li>
 *   <li>保留到运行时（{@link java.lang.annotation.RetentionPolicy#RUNTIME}），以便运行时读取。</li>
 * </ul>
 *
 * <p><b>示例</b></p>
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
 * @see WebLogInterceptor
 * @see WebLog
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebLogOperation {
	/**
	 * 操作描述。
	 *
	 * <p><b>说明</b></p>
	 * <ul>
	 *   <li>用于标识控制器方法的业务功能描述。</li>
	 *   <li>在拦截器中被读取并赋值到 {@link WebLog#getOperation()}。</li>
	 * </ul>
	 *
	 * @return 操作描述字符串
	 * @since 1.0.0
	 */
	String value();
}
