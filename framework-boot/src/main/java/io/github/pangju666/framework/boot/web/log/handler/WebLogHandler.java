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

package io.github.pangju666.framework.boot.web.log.handler;

import io.github.pangju666.framework.boot.web.log.model.WebLog;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.lang.reflect.Method;

/**
 * Web 日志处理器接口。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>定义扩展 Web 日志处理的标准契约，用于在日志采集后进行增强、修改或自定义处理。</li>
 *   <li>在日志发送前执行，适用于追加业务上下文、脱敏、格式化或过滤等场景。</li>
 * </ul>
 *
 * <p><b>行为</b></p>
 * <ul>
 *   <li>接收已构建的 {@link WebLog} 以及内容缓存包装的请求/响应对象。</li>
 *   <li>支持链式处理：多个处理器将按顺序依次执行。</li>
 *   <li>通常由拦截器 {@link io.github.pangju666.framework.boot.web.log.interceptor.WebLogInterceptor} 或过滤器 {@link io.github.pangju666.framework.boot.web.log.filter.WebLogFilter} 调用。</li>
 * </ul>
 *
 * <p><b>注意事项</b></p>
 * <ul>
 *   <li>{@code targetClass}/{@code targetMethod} 可能为 {@code null}（例如由过滤器调用时），实现时需进行空值判断。</li>
 *   <li>响应体的写回由调用方负责；处理器无需操作底层输出流。</li>
 * </ul>
 *
 * <p><b>实现示例</b></p>
 * <pre>{@code
 * // 自定义日志处理器：脱敏查询参数中的敏感字段
 * @Component
 * public class SensitiveDataMaskingHandler implements WebLogHandler {
 *     @Override
 *     public void handle(WebLog webLog, ContentCachingRequestWrapper request,
 *                        ContentCachingResponseWrapper response, Class<?> targetClass, Method targetMethod) {
 *         if (webLog.getRequest() != null && webLog.getRequest().getQueryParams() != null) {
 *             webLog.getRequest().getQueryParams().forEach((key, value) -> {
 *                 if ("password".equals(key)) {
 *                     value.replaceAll(v -> "****");
 *                 }
 *             });
 *         }
 *     }
 * }
 * }</pre>
 *
 * @author pangju666
 * @see io.github.pangju666.framework.boot.web.log.interceptor.WebLogInterceptor
 * @see io.github.pangju666.framework.boot.web.log.filter.WebLogFilter
 * @see WebLog
 * @since 1.0.0
 */
public interface WebLogHandler {
	/**
	 * 处理日志数据。
	 *
	 * <p><b>行为</b></p>
	 * <ul>
	 *   <li>在 {@link WebLog} 构建后调用，用于增强或修改日志内容。</li>
	 *   <li>建议保持无副作用、快速执行，以保证整体请求性能。</li>
	 * </ul>
	 *
	 * @param webLog        当前采集的 Web 日志对象，包含请求与响应信息
	 * @param request       内容缓存的请求包装器 {@link ContentCachingRequestWrapper}
	 * @param response      内容缓存的响应包装器 {@link ContentCachingResponseWrapper}
	 * @param targetClass   目标类（通常为控制器类）
	 * @param targetMethod  目标方法（通常为控制器方法）
	 * @since 1.0.0
	 */
	void handle(WebLog webLog, ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
				Class<?> targetClass, Method targetMethod);
}
