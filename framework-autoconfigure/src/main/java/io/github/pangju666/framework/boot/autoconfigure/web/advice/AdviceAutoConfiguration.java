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

package io.github.pangju666.framework.boot.autoconfigure.web.advice;

import io.github.pangju666.framework.boot.autoconfigure.web.advice.bind.RequestParamBindingAdvice;
import io.github.pangju666.framework.boot.autoconfigure.web.advice.exception.*;
import io.github.pangju666.framework.boot.autoconfigure.web.advice.wrapper.ResponseBodyWrapperAdvice;
import io.github.pangju666.framework.web.model.Result;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Web 增强自动配置。
 *
 * <p><strong>启用条件</strong></p>
 * <ul>
 *   <li>Servlet Web 应用，类路径存在 {@code Servlet}、{@code DispatcherServlet}</li>
 * </ul>
 *
 * <p><strong>配置项（{@code pangju.web.advice.*}）</strong></p>
 * <ul>
 *   <li>{@code enable-binder}（默认启用）：请求参数绑定增强</li>
 *   <li>{@code enable-exception}（默认启用）：全局异常处理</li>
 *   <li>{@code enable-wrapper}（默认启用）：统一响应包装</li>
 * </ul>
 *
 * <p><strong>行为说明</strong></p>
 * <ul>
 *   <li>启用配置属性绑定：{@link AdviceProperties}</li>
 *   <li>各增强组件通过自身的条件注解生效：
 *     <ul>
 *       <li>请求参数绑定增强：{@link RequestParamBindingAdvice}</li>
 *       <li>全局异常处理：{@link GlobalTomcatFileUploadExceptionAdvice}、{@link GlobalValidationExceptionAdvice}、
 *       {@link GlobalDataExceptionAdvice}、{@link GlobalWebExceptionAdvice}、{@link GlobalInternalExceptionAdvice}</li>
 *       <li>统一响应包装：{@link ResponseBodyWrapperAdvice}</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * @author pangju666
 * @see AdviceProperties
 * @see RequestParamBindingAdvice
 * @see GlobalWebExceptionAdvice
 * @see io.github.pangju666.framework.boot.autoconfigure.web.advice.wrapper.ResponseBodyWrapperAdvice
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, Result.class})
@EnableConfigurationProperties({AdviceProperties.class})
public class AdviceAutoConfiguration {
}
