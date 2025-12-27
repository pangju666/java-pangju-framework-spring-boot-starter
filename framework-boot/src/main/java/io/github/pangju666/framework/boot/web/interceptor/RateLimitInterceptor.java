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

package io.github.pangju666.framework.boot.web.interceptor;

import io.github.pangju666.framework.boot.spring.StaticSpringContext;
import io.github.pangju666.framework.boot.web.annotation.RateLimit;
import io.github.pangju666.framework.boot.web.exception.RateLimitException;
import io.github.pangju666.framework.boot.web.limit.RateLimitSourceExtractor;
import io.github.pangju666.framework.boot.web.limit.RateLimiter;
import io.github.pangju666.framework.spring.utils.SpELUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.servlet.BaseHttpInterceptor;
import io.github.pangju666.framework.web.servlet.HttpResponseBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.method.HandlerMethod;

import java.util.Collections;
import java.util.Objects;

/**
 * 请求限流拦截器。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>在 HTTP 请求进入控制器前，根据 {@link RateLimit} 执行速率限制检查。</li>
 * </ul>
 *
 * <p><strong>行为</strong></p>
 * <ul>
 *   <li>识别方法或类上的注解，生成限流键（支持前缀、SpEL、源维度）。</li>
 *   <li>调用 {@link RateLimiter#tryAcquire(String, RateLimit, HttpServletRequest)} 非阻塞判定是否允许请求。</li>
 *   <li>超限时写入 429 响应；异常时写入 500 响应。</li>
 *   <li>拦截范围：拦截所有路径（{@code /**}），不排除路径。</li>
 * </ul>
 *
 * @author pangju666
 * @see RateLimit
 * @see RateLimiter
 * @see RateLimitSourceExtractor
 * @see RateLimitException
 * @see BaseHttpInterceptor
 * @since 1.0.0
 */
public class RateLimitInterceptor extends BaseHttpInterceptor {
    /**
	 * 限流器实现，用于执行速率限制检查。
	 *
	 * @since 1.0.0
	 */
    private final RateLimiter rateLimiter;

    /**
     * 初始化拦截器，拦截所有路径（{@code /**}）。
     *
     * @param requestLimiter 限流器实现，用于执行限流检查（不可为 null）。
     * @since 1.0.0
     */
    public RateLimitInterceptor(RateLimiter requestLimiter) {
        super(Collections.emptySet());
        this.rateLimiter = requestLimiter;
    }

    /**
     * 请求处理前进行限流检查：查找注解→生成键→尝试获取→写入响应。
     *
     * @param request  当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler  当前处理器（通常是 {@link HandlerMethod}）
     * @return 允许请求返回 {@code true}；被限流或异常返回 {@code false}
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (handler instanceof HandlerMethod handlerMethod) {
			RateLimit annotation = handlerMethod.getMethodAnnotation(RateLimit.class);
			if (Objects.isNull(annotation)) {
				Class<?> targetClass = handlerMethod.getBeanType();
				annotation = targetClass.getAnnotation(RateLimit.class);
			}
			if (Objects.isNull(annotation)) {
				return true;
			}

			try {
				String key = generateKey(annotation, request);
				if (!rateLimiter.tryAcquire(key, annotation, request)) {
					HttpResponseBuilder.from(response).writeHttpException(new RateLimitException(annotation));
					return false;
				}
			} catch (Exception e) {
				HttpResponseBuilder.from(response).writeHttpException(new ServerException(e));
				return false;
			}
		}
		return true;
	}

    /**
     * 根据注解与请求生成限流键。
     *
     * <p><strong>规则</strong></p>
     * <ul>
     *   <li>支持前缀与 SpEL（可选）；缺省为 {@code URI + "_" + 方法}。</li>
     *   <li>当 scope 为 SOURCE 时追加源信息。</li>
     * </ul>
     *
     * @param annotation 限流配置注解
     * @param request    当前 HTTP 请求
     * @return 生成的限流键
     * @since 1.0.0
     */
    private String generateKey(RateLimit annotation, HttpServletRequest request) {
		StringBuilder keyBuilder = new StringBuilder();
		if (StringUtils.isNotBlank(annotation.key())) {
			EvaluationContext context = new StandardEvaluationContext();
			context.setVariable("request", request);
			try {
				Expression expression = SpELUtils.DEFAULT_EXPRESSION_PARSER.parseExpression(annotation.key());
				keyBuilder.append(expression.getValue(context, String.class));
			} catch (ParseException | EvaluationException e) {
				keyBuilder.append(annotation.key());
			}
		} else {
			keyBuilder
				.append(request.getRequestURI())
				.append("_")
				.append(request.getMethod());
		}
		if (annotation.scope() == RateLimit.RateLimitScope.SOURCE) {
			RateLimitSourceExtractor sourceExtractor = StaticSpringContext.getBeanFactory().getBean(annotation.source());
			keyBuilder
				.append("_")
				.append(sourceExtractor.getSource(request));
		}
		return keyBuilder.toString();
	}
}
