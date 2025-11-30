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

package io.github.pangju666.framework.boot.autoconfigure.web.advice.bind;

import io.github.pangju666.commons.lang.utils.DateUtils;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 请求参数绑定增强类
 * <p>
 * 该类用于处理HTTP请求参数的类型绑定和转换。
 * 为控制器方法的日期时间类型参数提供统一的转换处理，
 * 支持将时间戳（毫秒级）自动转换为{@link Date}、{@link LocalDate}和{@link LocalDateTime}。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>将请求参数中的时间戳（毫秒）转换为{@link Date}类型</li>
 *     <li>将请求参数中的时间戳（毫秒）转换为{@link LocalDate}类型</li>
 *     <li>将请求参数中的时间戳（毫秒）转换为{@link LocalDateTime}类型</li>
 *     <li>提供统一的日期时间参数处理方式</li>
 * </ul>
 * </p>
 * <p>
 * 支持的场景：
 * <ul>
 *     <li>GET请求的Query参数</li>
 *     <li>POST表单提交的参数</li>
 *     <li>Controller方法的@RequestParam参数</li>
 *     <li>所有通过WebDataBinder绑定的参数</li>
 * </ul>
 * </p>
 * <p>
 * 配置条件：
 * <ul>
 *     <li>应用必须是Servlet类型的Web应用</li>
 *     <li>Classpath中必须存在Servlet和DispatcherServlet类</li>
 *     <li>配置属性{@code pangju.web.advice.binder}必须为true（默认为true）</li>
 * </ul>
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * pangju:
 *   web:
 *     advice:
 *       binder: true  # 默认为true，可选配置
 * </pre>
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/users")
 * public class UserController {
 *     // GET请求示例：/api/users/query?createTime=1704067200000
 *     @GetMapping("/query")
 *     public ResponseEntity<?> query(@RequestParam Date createTime) {
 *         // createTime会被自动从时间戳转换为Date对象
 *         return ResponseEntity.ok.ok(createTime).build();
 *     }
 *
 *     // 支持LocalDate
 *     @GetMapping("/search")
 *     public ResponseEntity<?> search(@RequestParam LocalDate birthDate) {
 *         // birthDate会被自动从时间戳转换为LocalDate对象
 *         return ResponseEntity.ok.ok(birthDate).build();
 *     }
 *
 *     // 支持LocalDateTime
 *     @GetMapping("/list")
 *     public ResponseEntity<?> list(@RequestParam LocalDateTime startTime) {
 *         // startTime会被自动从时间戳转换为LocalDateTime对象
 *         return ResponseEntity.ok.ok(startTime).build();
 *     }
 * }
 * }</pre>
 * </p>
 * <p>
 * 参数转换规则：
 * <ul>
 *     <li>输入参数必须是毫秒级时间戳的字符串表示</li>
 *     <li>例如：1704067200000 表示 2024-01-01 00:00:00</li>
 *     <li>如果参数不是合法的数字字符串，会抛出{@link IllegalArgumentException}异常</li>
 * </ul>
 * </p>
 * <p>
 * 异常处理：
 * <ul>
 *     <li>如果参数不能转换为Long类型，会抛出{@link IllegalArgumentException}，由Spring的数据绑定机制处理</li>
 *     <li>最终会导致请求返回400 Bad Request</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see WebDataBinder
 * @see InitBinder
 * @see DateUtils
 * @since 1.0.0
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, DateUtils.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "binder", matchIfMissing = true)
@RestControllerAdvice
public class RequestParamBindingAdvice {
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				try {
					setValue(DateUtils.toDate(Long.valueOf(text)));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			}
		});

		binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				try {
					setValue(DateUtils.toLocalDate(Long.valueOf(text)));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			}
		});

		binder.registerCustomEditor(LocalDateTime.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				try {
					setValue(DateUtils.toLocalDateTime(Long.valueOf(text)));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			}
		});
	}
}
