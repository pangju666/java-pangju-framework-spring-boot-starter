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

package io.github.pangju666.framework.autoconfigure.web.advice;

import io.github.pangju666.framework.autoconfigure.web.advice.bind.RequestParamBindingAdvice;
import io.github.pangju666.framework.autoconfigure.web.advice.exception.GlobalExceptionAdvice;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Web增强功能自动配置类
 * <p>
 * 该类用于在Spring Boot应用启动时自动配置Web层面的各种增强功能。
 * 包括请求参数绑定增强、全局异常处理等功能的自动配置。
 * </p>
 * <p>
 * 配置的主要功能：
 * <ul>
 *     <li>启用Web增强功能配置属性的支持</li>
 *     <li>根据配置动态启用或禁用各种增强功能</li>
 *     <li>提供统一的Web层面的增强处理</li>
 * </ul>
 * </p>
 * <p>
 * 配置条件：
 * <ul>
 *     <li>应用必须是Servlet类型的Web应用</li>
 *     <li>Classpath中必须存在Servlet和DispatcherServlet类</li>
 * </ul>
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * pangju:
 *   web:
 *     advice:
 *       binding: true    # 启用请求参数绑定增强（默认为true）
 *       exception: true  # 启用全局异常处理（默认为true）
 * </pre>
 * </p>
 * <p>
 * 支持的增强功能：
 * <ul>
 *     <li>
 *         <strong>请求参数绑定增强</strong>
 *         <p>
 *         启用后，会自动将请求参数中的时间戳（毫秒级）转换为Date、LocalDate、LocalDateTime对象。
 *         由{@link RequestParamBindingAdvice}处理。配置属性：{@code pangju.web.advice.binding}
 *         </p>
 *     </li>
 *     <li>
 *         <strong>全局异常处理</strong>
 *         <p>
 *         启用后，会统一处理应用中抛出的各种异常，并返回统一的错误响应格式。
 *         由{@link GlobalExceptionAdvice}处理。配置属性：{@code pangju.web.advice.exception}
 *         </p>
 *     </li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see AdviceProperties
 * @see RequestParamBindingAdvice
 * @see GlobalExceptionAdvice
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@EnableConfigurationProperties({AdviceProperties.class})
public class AdviceAutoConfiguration {
}
