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

package io.github.pangju666.framework.autoconfigure.web.advice.wrapper;

import org.springframework.http.ResponseEntity;

import java.lang.annotation.*;

/**
 * 响应体包装忽略注解
 * <p>
 * 该注解用于在类级别启用了{@link ResponseBodyWrapper}注解的情况下，
 * 在特定方法上禁用响应体的自动包装功能。
 * 被标注的方法的响应将不会被包装，而是直接返回原始数据。
 * </p>
 * <p>
 * 注解作用：
 * <ul>
 *     <li>只能标注在方法上</li>
 *     <li>用于排除特定方法的响应包装</li>
 *     <li>通常与{@link ResponseBodyWrapper}配合使用</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <p>
 * 当在控制器类上标注了{@link ResponseBodyWrapper}注解，
 * 使该类的所有方法响应都被包装时，某些方法可能需要返回自定义的响应格式
 * （如{@link org.springframework.http.ResponseEntity}或其他格式）。
 * 此时可以在这些方法上标注该注解以排除包装处理。
 * </p>
 * </p>
 * <p>
 * 优先级说明：
 * <p>
 * 该注解的优先级高于{@link ResponseBodyWrapper}注解。
 * 如果方法同时被两个注解标注，则以{@link ResponseBodyWrapperIgnore}为准，
 * 不进行包装处理。
 * </p>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 在类上启用包装
 * &#64;RestController
 * &#64;RequestMapping("/api/users")
 * &#64;ResponseBodyWrapper
 * public class UserController {
 *
 *     // 该方法的响应将被包装
 *     &#64;GetMapping
 *     public List&lt;User&gt; listUsers() {
 *         return userService.findAll();
 *         // 响应将被包装为: {"code":0,"message":"请求成功","data":[...]}
 *     }
 *
 *     // 该方法的响应将被包装
 *     &#64;GetMapping("/{id}")
 *     public User getUserById(&#64;PathVariable Long id) {
 *         return userService.findById(id);
 *         // 响应将被包装为: {"code":0,"message":"请求成功","data":{...}}
 *     }
 *
 *     // 该方法的响应不会被包装，排除包装处理
 *     &#64;DeleteMapping("/{id}")
 *     &#64;ResponseBodyWrapperIgnore
 *     public ResponseEntity&lt;Void&gt; deleteUser(&#64;PathVariable Long id) {
 *         userService.deleteById(id);
 *         return ResponseEntity.noContent().build();
 *         // 响应直接返回，不被包装
 *     }
 *
 *     // 该方法的响应不会被包装
 *     &#64;PostMapping
 *     &#64;ResponseBodyWrapperIgnore
 *     public void createUser(&#64;RequestBody User user) {
 *         userService.save(user);
 *         // 响应直接返回，不被包装
 *     }
 * }
 * </pre>
 * </p>
 * <p>
 * 与ResponseEntity的关系：
 * <p>
 * 返回{@link ResponseEntity}的方法会自动被排除包装，
 * 无需显式标注该注解。但如果需要明确表示意图或提高代码可读性，
 * 可以显式标注该注解。
 * </p>
 * </p>
 *
 * @author pangju666
 * @see ResponseBodyWrapper
 * @see ResponseBodyWrapperAdvice
 * @see org.springframework.http.ResponseEntity
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ResponseBodyWrapperIgnore {
}