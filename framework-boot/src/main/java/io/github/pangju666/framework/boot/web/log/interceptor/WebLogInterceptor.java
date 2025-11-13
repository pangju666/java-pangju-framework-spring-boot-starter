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
import io.github.pangju666.framework.web.exception.base.BaseHttpException;
import io.github.pangju666.framework.web.servlet.BaseHttpInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Web 日志拦截器。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>在请求完成阶段（处理链结束后）对日志进行补充处理。</li>
 *   <li>基于方法级注解 {@link WebLogOperation} 设置操作描述，并通过 {@link WebLogHandler} 扩展处理。</li>
 *   <li>与过滤器产生的 {@link WebLog} 协作，增强业务维度信息。</li>
 * </ul>
 *
 * <p><b>使用约束</b></p>
 * <ul>
 *   <li>仅当处理器为 {@link HandlerMethod} 且响应为 {@link io.github.pangju666.framework.boot.web.log.utils.WebLogResponseWrapper} 时生效。</li>
 *   <li>依赖 {@link io.github.pangju666.framework.boot.web.log.utils.WebLogResponseWrapper} 携带的 {@link WebLog}（通常由过滤器或网关在链路前置阶段设置）。</li>
 * </ul>
 *
 * <p><b>行为</b></p>
 * <ul>
 *   <li>读取 {@link WebLogOperation} 注解以填充操作描述。</li>
 *   <li>依次执行所有 {@link WebLogHandler}，允许对日志进行增强或自定义处理。</li>
 *   <li>处理器异常按 {@link BaseHttpException} 与普通异常分别记录。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see WebLogHandler
 * @see WebLogFilter
 */
public class WebLogInterceptor extends BaseHttpInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(WebLogInterceptor.class);

	/**
	 * Web 日志处理器列表
	 * <p>
	 * 用于处理日志的扩展逻辑，支持对日志数据进行增强或自定义处理。
	 * 开发者可以实现 {@link WebLogHandler} 接口并在容器中进行注册。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final List<WebLogHandler> webLogHandlers;

    /**
     * 构造方法。
     *
     * @param excludePathPatterns 排除的路径模式集合（不应用拦截处理）
     * @param webLogHandlers 日志处理器列表（按顺序执行）
     */
    public WebLogInterceptor(Set<String> excludePathPatterns, List<WebLogHandler> webLogHandlers) {
        super(excludePathPatterns);
        this.webLogHandlers = webLogHandlers;
    }

    /**
     * 请求完成后执行日志增强与处理。
     *
     * <p><b>行为</b></p>
     * <ul>
     *   <li>当满足处理器与响应包装器条件时，从响应中获取 {@link WebLog} 并填充操作描述。</li>
     *   <li>按顺序执行所有 {@link WebLogHandler}，可对日志进行增强或自定义处理。</li>
     *   <li>将处理过程中的异常分类记录（{@link BaseHttpException} 与普通异常）。</li>
     * </ul>
     *
     * <p><b>参数</b></p>
     * <ul>
     *   <li>{@code request} 当前 HTTP 请求。</li>
     *   <li>{@code response} 当前 HTTP 响应（必须为 {@link io.github.pangju666.framework.boot.web.log.utils.WebLogResponseWrapper}）。</li>
     *   <li>{@code handler} 处理器对象，期望为 {@link HandlerMethod}。</li>
     *   <li>{@code ex} 处理链末端抛出的异常，可能为 {@code null}。</li>
     * </ul>
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (handler instanceof HandlerMethod handlerMethod && response instanceof WebLogResponseWrapper webLogResponseWrapper) {
			Class<?> targetClass = handlerMethod.getBeanType();
			Method targetMethod = handlerMethod.getMethod();
			
			WebLogOperation operation = targetMethod.getAnnotation(WebLogOperation.class);
			if (Objects.nonNull(operation)) {
				webLogResponseWrapper.getWebLog().setOperation(operation.value());
			}

			try {
				for (WebLogHandler webLogHandler : webLogHandlers) {
					webLogHandler.handle(webLogResponseWrapper.getWebLog(), targetClass, targetMethod);
				}
			} catch (Exception e) {
				if (e instanceof BaseHttpException baseHttpException) {
					baseHttpException.log(logger, Level.ERROR);
				} else {
					logger.error("自定义网络日志处理器错误", e);
				}
			}
		}
	}
}