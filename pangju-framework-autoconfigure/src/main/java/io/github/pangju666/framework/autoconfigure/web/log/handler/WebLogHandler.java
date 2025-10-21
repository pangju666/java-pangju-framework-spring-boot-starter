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

package io.github.pangju666.framework.autoconfigure.web.log.handler;

import io.github.pangju666.framework.autoconfigure.web.log.model.WebLog;
import org.springframework.lang.Nullable;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.lang.reflect.Method;

/**
 * Web 日志处理器接口
 * <p>
 * 该接口用于定义扩展 Web 日志处理逻辑的标准方法。实现此接口的类可以对采集到的
 * {@link io.github.pangju666.framework.autoconfigure.web.log.model.WebLog} 日志数据进行修改、
 * 增强或自定义处理，并在日志发送之前完成相关操作。
 * </p>
 *
 * <p>功能说明：</p>
 * <ul>
 *     <li>允许对 Web 日志数据进行自定义的处理或增强。</li>
 *     <li>通过实现多个处理器类，可以在日志生成后以责任链的形式链式增强日志数据。</li>
 *     <li>该接口通常被日志过滤器 {@link io.github.pangju666.framework.autoconfigure.web.log.filter.WebLogFilter} 调用。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>需要为日志添加额外的业务字段或上下文信息。</li>
 *     <li>对日志数据进行过滤、格式化或转换（如敏感数据脱敏）。</li>
 *     <li>自定义日志的持久化或异步处理逻辑。</li>
 * </ul>
 *
 * <p>实现示例：</p>
 * <pre>
 * // 自定义日志处理器，用于脱敏日志中的某些字段
 * &#64;Component
 * public class SensitiveDataMaskingHandler implements WebLogHandler {
 *     &#64;Override
 *     public void handle(WebLog webLog, ContentCachingRequestWrapper request,
 *                        ContentCachingResponseWrapper response, Class&lt;?&gt; targetClass, Method targetMethod) {
 *         if (webLog.getRequest() != null &amp;&amp; webLog.getRequest().getQueryParams() != null) {
 *             webLog.getRequest().getQueryParams().forEach((key, value) -> {
 *                 if ("password".equals(key)) {
 *                     value.replaceAll(v -> "****"); // 脱敏处理
 *                 }
 *             });
 *         }
 *     }
 * }
 * </pre>
 *
 * @author pangju666
 * @see io.github.pangju666.framework.autoconfigure.web.log.filter.WebLogFilter
 * @see io.github.pangju666.framework.autoconfigure.web.log.model.WebLog
 * @since 1.0.0
 */
public interface WebLogHandler {
	/**
	 * 自定义日志处理方法
	 * <p>
	 * 定义日志增强或处理逻辑的方法，在生成 {@link WebLog} 日志对象后调用该方法。
	 * 可通过此方法对日志数据进行修改、添加额外信息或执行特定处理逻辑。
	 * </p>
	 *
	 * @param webLog       当前生成的 Web 日志对象，包含请求和响应的详细信息
	 * @param request      包装的请求对象 {@link ContentCachingRequestWrapper}
	 * @param response     包装的响应对象 {@link ContentCachingResponseWrapper}
	 * @param targetClass  被请求处理的目标类（可为空）
	 * @param targetMethod 被请求处理的目标方法（可为空）
	 * @since 1.0.0
	 */
	void handle(WebLog webLog, ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
				@Nullable Class<?> targetClass, @Nullable Method targetMethod);
}
