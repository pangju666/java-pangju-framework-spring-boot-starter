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
import java.util.Date;

/**
 * 请求参数时间戳绑定增强。
 * <p>
 * 为控制器方法的 {@link Date} 类型参数提供统一的转换：将毫秒级时间戳字符串自动转换为 {@link Date}。
 * </p>
 * <p>
 * 适用场景：
 * <ul>
 *   <li>GET 请求的 Query 参数</li>
 *   <li>POST 表单提交参数</li>
 *   <li>Controller 方法的 {@code @RequestParam} 参数</li>
 *   <li>所有通过 {@link WebDataBinder} 绑定的参数</li>
 * </ul>
 * </p>
 * <p>
 * 配置条件：
 * <ul>
 *   <li>Servlet 类型 Web 应用</li>
 *   <li>Classpath 中存在 {@link Servlet} 与 {@link DispatcherServlet}</li>
 *   <li>配置项 {@code pangju.web.advice.enable-binder=true} 或缺省</li>
 * </ul>
 * </p>
 * <p>
 * 示例：
 * <pre>{@code
 * @GetMapping("/query")
 * public ResponseEntity<?> query(@RequestParam Date createTime) {
 *     // /query?createTime=1704067200000 -> 自动转换为 Date
 *     return ResponseEntity.ok(createTime);
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * 转换与异常：
 * <ul>
 *   <li>输入必须为毫秒级时间戳的字符串（如 1704067200000）</li>
 *   <li>非法数字将抛出 {@link IllegalArgumentException}，由 Spring 数据绑定机制处理为 400</li>
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
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "enable-binder", matchIfMissing = true)
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
	}
}
