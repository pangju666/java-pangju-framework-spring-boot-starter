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

package io.github.pangju666.framework.boot.web.log.interceptor;

import io.github.pangju666.framework.boot.web.log.annotation.WebLogOperation;
import io.github.pangju666.framework.boot.web.log.filter.WebLogFilter;
import io.github.pangju666.framework.boot.web.log.handler.WebLogHandler;
import io.github.pangju666.framework.boot.web.log.model.WebLog;
import io.github.pangju666.framework.boot.web.log.utils.WebLogResponseWrapper;
import io.github.pangju666.framework.web.servlet.BaseHttpInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

/**
 * Web 日志拦截器。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>在控制器方法调用前（preHandle 阶段）对日志进行补充处理，仅填充操作描述。</li>
 *   <li>基于方法级注解 {@link WebLogOperation} 设置操作描述，协作已由过滤器创建的 {@link WebLog}。</li>
 * </ul>
 *
 * <p><b>使用约束</b></p>
 * <ul>
 *   <li>仅当处理器为 {@link HandlerMethod} 且响应为 {@link io.github.pangju666.framework.boot.web.log.utils.WebLogResponseWrapper} 时生效。</li>
 *   <li>依赖 {@link io.github.pangju666.framework.boot.web.log.utils.WebLogResponseWrapper} 携带的 {@link WebLog}（通常由过滤器在链路前置阶段设置）；若不存在则不进行任何处理。</li>
 * </ul>
 *
 * <p><b>行为</b></p>
 * <ul>
 *   <li>读取 {@link WebLogOperation} 注解以填充操作描述，未标注时保持原值。</li>
 *   <li>不在此拦截器中执行 {@link WebLogHandler} 或记录异常；异常记录与日志派发由过滤器或统一异常处理负责。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see WebLogHandler
 * @see WebLogFilter
 */
public class WebLogInterceptor extends BaseHttpInterceptor {
    public WebLogInterceptor(Set<String> excludePathPatterns) {
        super(excludePathPatterns);
    }

	/**
	 * 在处理链前置阶段执行：当处理器为 {@link HandlerMethod} 且响应包装为
	 * {@link WebLogResponseWrapper} 时，读取目标方法上的 {@link WebLogOperation}
	 * 注解并将其值写入响应包装器中的 {@link WebLog} 的操作描述。
	 *
	 * <p>该拦截器仅补充操作描述，不阻断链路，也不负责异常记录或日志派发。</p>
	 *
	 * @return 始终返回 {@code true} 以继续处理链。
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (handler instanceof HandlerMethod handlerMethod && response instanceof WebLogResponseWrapper webLogResponseWrapper) {
			Method targetMethod = handlerMethod.getMethod();
			WebLogOperation operation = targetMethod.getAnnotation(WebLogOperation.class);
			if (Objects.nonNull(operation)) {
				webLogResponseWrapper.getWebLog().setOperation(operation.value());
			}
		}
		return true;
	}
}