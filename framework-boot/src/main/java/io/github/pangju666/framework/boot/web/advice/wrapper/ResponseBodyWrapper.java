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

package io.github.pangju666.framework.boot.web.advice.wrapper;

import io.github.pangju666.framework.web.model.common.Result;
import org.springframework.http.ResponseEntity;

import java.lang.annotation.*;

/**
 * 响应体包装注解
 * <p>
 * 该注解用于标注需要进行响应体包装的控制器类或方法。
 * 被标注的方法或类的响应将被自动包装成统一的{@link Result}格式。
 * </p>
 * <p>
 * 注解作用：
 * <ul>
 *     <li>标注在方法上 - 该方法的响应将被包装</li>
 *     <li>标注在类上 - 该类中的所有方法的响应都将被包装</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <p>
 * 用于统一API的响应格式，使所有接口都返回一致的结构。
 * 通常与{@link ResponseBodyWrapperIgnore}注解配合使用，
 * 可以在类级别启用包装，然后在特定方法上禁用包装。
 * </p>
 * </p>
 * <p>
 * 包装规则：
 * <p>
 * 被标注的方法或类的响应会被自动包装为以下格式：
 * <pre>
 * {
 *   "code": 0,           // 成功代码
 *   "message": "请求成功",     // 成功消息
 *   "data": &lt;original data&gt;  // 原始响应数据
 * }
 * </pre>
 * </p>
 * </p>
 * <p>
 * 排除机制：
 * <ul>
 *     <li>返回{@link ResponseEntity}的方法不会被包装</li>
 *     <li>标注了{@link ResponseBodyWrapperIgnore}注解的方法不会被包装</li>
 *     <li>未标注{@link ResponseBodyWrapper}注解的方法不会被包装</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 在方法上使用
 * &#64;GetMapping("/api/user/{id}")
 * &#64;ResponseBodyWrapper
 * public User getUserById(&#64;PathVariable Long id) {
 *     return userService.findById(id);
 *     // 响应将被包装为: {"code":0,"message":"请求成功","data":{...}}
 * }
 *
 * // 在类上使用
 * &#64;RestController
 * &#64;RequestMapping("/api/users")
 * &#64;ResponseBodyWrapper
 * public class UserController {
 *     &#64;GetMapping
 *     public List&lt;User&gt; listUsers() {
 *         // 所有方法的响应都将被包装
 *     }
 *
 *     &#64;DeleteMapping("/{id}")
 *     &#64;ResponseBodyWrapperIgnore
 *     public void deleteUser(&#64;PathVariable Long id) {
 *         // 该方法不会被包装
 *     }
 * }
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see ResponseBodyWrapperIgnore
 * @see Result
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ResponseBodyWrapper {
}