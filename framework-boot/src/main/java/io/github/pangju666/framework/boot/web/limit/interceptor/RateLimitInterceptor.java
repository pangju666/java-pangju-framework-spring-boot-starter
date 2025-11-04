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

package io.github.pangju666.framework.boot.web.limit.interceptor;

import io.github.pangju666.framework.boot.spring.StaticSpringContext;
import io.github.pangju666.framework.boot.web.limit.annotation.RateLimit;
import io.github.pangju666.framework.boot.web.limit.enums.RateLimitScope;
import io.github.pangju666.framework.boot.web.limit.exception.RateLimitException;
import io.github.pangju666.framework.boot.web.limit.limiter.RateLimiter;
import io.github.pangju666.framework.boot.web.limit.source.RateLimitSourceExtractor;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.servlet.builder.HttpResponseBuilder;
import io.github.pangju666.framework.web.servlet.interceptor.BaseHttpInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求限流拦截器
 * <p>
 * 该拦截器用于在HTTP请求处理前进行速率限制检查。
 * 通过检查方法或类上的{@link RateLimit}注解，根据配置的限流规则对请求进行限流控制。
 * 当请求超过限流阈值时，拦截器会拒绝请求并返回HTTP 429响应。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>识别标记了{@link RateLimit}注解的处理器方法</li>
 *     <li>支持方法级和类级的限流注解</li>
 *     <li>根据注解配置生成限流键</li>
 *     <li>支持SpEL表达式动态生成限流键</li>
 *     <li>支持全局限流和基于源的限流</li>
 *     <li>通过{@link RateLimiter}执行限流检查</li>
 *     <li>拦截超限请求并返回{@link RateLimitException}</li>
 * </ul>
 * </p>
 * <p>
 * 工作流程：
 * <ol>
 *     <li>请求进入时在{@link #preHandle}方法进行处理</li>
 *     <li>检查处理器是否为{@link HandlerMethod}（Spring MVC控制器方法）</li>
 *     <li>查找方法上的{@link RateLimit}注解，如果不存在则查找类上的注解</li>
 *     <li>如果找到注解，生成限流键</li>
 *     <li>调用{@link RateLimiter#tryAcquire}进行限流检查</li>
 *     <li>如果超限，返回false并向客户端写入异常响应</li>
 *     <li>如果未超限，返回true允许请求继续处理</li>
 * </ol>
 * </p>
 * <p>
 * 拦截范围：
 * <ul>
 *     <li>拦截所有路径的请求（/**）</li>
 *     <li>不排除任何路径</li>
 *     <li>仅处理标记了{@link RateLimit}注解的方法</li>
 * </ul>
 * </p>
 * <p>
 * 限流键生成策略：
 * <ol>
 *     <li>如果指定了{@link RateLimit#prefix()}，使用前缀作为基础</li>
 *     <li>如果指定了{@link RateLimit#key()}，使用SpEL表达式解析并追加到键中</li>
 *     <li>如果都未指定，使用请求URI和HTTP方法组成键</li>
 *     <li>如果{@link RateLimit#scope()}为SOURCE，追加源信息（由{@link RateLimitSourceExtractor}提供）</li>
 * </ol>
 * </p>
 * <p>
 * SpEL支持：
 * <p>
 * 在{@link RateLimit#key()}中可以使用SpEL表达式动态生成键：
 * <pre>
 * {@code
 * @RateLimit(
 *     rate = 100,
 *     key = "#{T(java.lang.System).currentTimeMillis()}"
 * )
 * }
 * </pre>
 * 可用的变量：
 * <ul>
 *     <li>#request - 当前的HttpServletRequest对象</li>
 * </ul>
 * </p>
 * </p>
 * <p>
 * 源提取器缓存：
 * <p>
 * 该拦截器维护了一个{@link RateLimitSourceExtractor}的缓存（使用ConcurrentHashMap），
 * 首先尝试从Spring容器中获取源提取器Bean，如果不存在则通过反射实例化。
 * 这样可以避免重复创建源提取器实例，提高性能。
 * </p>
 * </p>
 * <p>
 * 错误处理：
 * <ul>
 *     <li>限流被触发：返回{@link RateLimitException}，HTTP状态码429</li>
 *     <li>限流检查异常：返回{@link ServerException}，HTTP状态码500</li>
 *     <li>所有异常都通过{@link HttpServletResponseUtils#writeHttpExceptionToResponse}写入响应</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * // 方法级限流
 * @RestController
 * public class ApiController {
 *     @GetMapping("/data")
 *     @RateLimit(rate = 10, interval = 1, timeUnit = TimeUnit.SECONDS)
 *     public ResponseEntity<?> getData() {
 *         return ResponseEntity.ok("data");
 *     }
 *
 *     // 基于IP的限流
 *     @PostMapping("/submit")
 *     @RateLimit(
 *         rate = 100,
 *         interval = 1,
 *         timeUnit = TimeUnit.MINUTES,
 *         scope = RateLimitScope.SOURCE,
 *         source = IpRateLimitSourceExtractor.class
 *     )
 *     public ResponseEntity<?> submit(@RequestBody Data data) {
 *         return ResponseEntity.ok("submitted");
 *     }
 * }
 *
 * // 类级限流
 * @RestController
 * @RequestMapping("/api")
 * @RateLimit(rate = 50, interval = 1, timeUnit = TimeUnit.MINUTES)
 * public class UserController {
 *     @GetMapping("/users")
 *     public ResponseEntity<?> listUsers() {
 *         return ResponseEntity.ok(users);
 *     }
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * 与其他组件的关系：
 * <ul>
 *     <li>由Spring MVC的拦截器链自动调用</li>
 *     <li>依赖{@link RateLimiter}接口的实现进行限流计数</li>
 *     <li>使用{@link RateLimitSourceExtractor}提取请求源信息</li>
 *     <li>在超限时抛出{@link RateLimitException}异常</li>
 * </ul>
 * </p>
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
	 * 限流器实例
	 * <p>
	 * 用于执行实际的限流检查逻辑，支持多种实现方式（Resilience4j、Redisson等）
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final RateLimiter rateLimiter;
	/**
	 * SpEL表达式解析器
	 * <p>
	 * 用于解析{@link RateLimit#key()}中的SpEL表达式，支持动态生成限流键
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final SpelExpressionParser parser;
	/**
	 * 源提取器缓存
	 * <p>
	 * 使用ConcurrentHashMap缓存已创建的{@link RateLimitSourceExtractor}实例，
	 * 避免重复创建和提高性能。
	 * 键为提取器类，值为提取器实例
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final Map<Class<? extends RateLimitSourceExtractor>, RateLimitSourceExtractor> sourceExtractorMap = new ConcurrentHashMap<>(10);

	/**
	 * 构造方法
	 * <p>
	 * 初始化速率限制拦截器，配置为拦截所有路径（/**）。
	 * </p>
	 *
	 * @param requestLimiter {@link RateLimiter}实现，用于执行限流检查。
	 *                       不能为null
	 * @since 1.0.0
	 */
	public RateLimitInterceptor(RateLimiter requestLimiter) {
		super(Collections.singleton("/**"), Collections.emptySet());
		this.rateLimiter = requestLimiter;
		this.parser = new SpelExpressionParser();
	}

	/**
	 * 请求处理前的拦截处理
	 * <p>
	 * 该方法在HTTP请求被处理前调用，用于进行速率限制检查。
	 * 执行流程：
	 * </p>
	 * <ol>
	 *     <li>检查处理器是否为{@link HandlerMethod}</li>
	 *     <li>查找方法上的{@link RateLimit}注解</li>
	 *     <li>如果方法上没有注解，查找类上的注解</li>
	 *     <li>如果没有找到注解，允许请求继续（return true）</li>
	 *     <li>生成限流键</li>
	 *     <li>调用限流器进行检查</li>
	 *     <li>如果超限，返回false并向客户端返回异常响应</li>
	 *     <li>如果未超限，返回true允许请求继续</li>
	 * </ol>
	 *
	 * @param request 当前HTTP请求
	 * @param response 当前HTTP响应
	 * @param handler 当前处理器（通常是HandlerMethod）
	 * @return 如果请求被允许则返回true，如果被限流拦截则返回false
	 */
	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
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
					HttpResponseBuilder.from(response).buffer(false).writeHttpException(new RateLimitException(annotation));
					return false;
				}
			} catch (Exception e) {
				HttpResponseBuilder.from(response).buffer(false).writeHttpException(new ServerException(e));
				return false;
			}
		}
		return true;
	}

	/**
	 * 根据注解和请求信息生成限流键
	 * <p>
	 * 限流键生成逻辑如下：
	 * </p>
	 * <ol>
	 *     <li><strong>基础键生成</strong>
	 *         <ul>
	 *             <li>如果指定了{@link RateLimit#prefix()}，作为基础</li>
	 *             <li>如果指定了{@link RateLimit#key()}，使用SpEL表达式解析并追加</li>
	 *             <li>如果都未指定，使用请求URI + "_" + HTTP方法</li>
	 *         </ul>
	 *     </li>
	 *     <li><strong>源信息追加</strong>
	 *         <ul>
	 *             <li>如果{@link RateLimit#scope()}为SOURCE，通过源提取器获取源信息并追加</li>
	 *             <li>源信息与基础键之间用"_"分隔</li>
	 *         </ul>
	 *     </li>
	 * </ol>
	 * <p>
	 * SpEL表达式支持：
	 * <ul>
	 *     <li>可以访问#request变量，表示当前的HttpServletRequest对象</li>
	 *     <li>支持所有标准的SpEL操作，如方法调用、对象导航等</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 示例：
	 * <pre>
	 * {@code
	 * // 键生成结果示例
	 * 1. prefix="api", key="", scope=GLOBAL
	 *    结果：/api/data_GET
	 *
	 * 2. prefix="", key="#{#request.getHeader('userId')}", scope=GLOBAL
	 *    结果：user123
	 *
	 * 3. prefix="api", key="", scope=SOURCE, source=IpRateLimitSourceExtractor
	 *    结果：/api/data_GET_192.168.1.1
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param annotation {@link RateLimit}注解，包含限流配置信息
	 * @param request 当前HTTP请求，用于获取请求信息和执行源提取
	 * @return 生成的限流键，用于在限流器中进行计数和检查
	 * @throws NoSuchMethodException 通过反射实例化源提取器时发生
	 * @throws InvocationTargetException 通过反射实例化源提取器时发生
	 * @throws InstantiationException 通过反射实例化源提取器时发生
	 * @throws IllegalAccessException 通过反射实例化源提取器时发生
	 * @since 1.0.0
	 */
	private String generateKey(RateLimit annotation, HttpServletRequest request) throws NoSuchMethodException,
		InvocationTargetException, InstantiationException, IllegalAccessException {
		StringBuilder keyBuilder = new StringBuilder(annotation.prefix().trim());
		if (StringUtils.isNotBlank(annotation.key())) {
			EvaluationContext context = new StandardEvaluationContext();
			context.setVariable("request", request);
			Expression expression = parser.parseExpression(annotation.key());
			keyBuilder.append(expression.getValue(context, String.class));
		}
		if (keyBuilder.isEmpty()) {
			keyBuilder
				.append(request.getRequestURI())
				.append("_")
				.append(request.getMethod());
		}

		if (annotation.scope() == RateLimitScope.SOURCE) {
			RateLimitSourceExtractor sourceExtractor;
			try {
				sourceExtractor = sourceExtractorMap.putIfAbsent(annotation.source(),
					StaticSpringContext.getBeanFactory().getBean(annotation.source()));
				if (Objects.isNull(sourceExtractor)) {
					sourceExtractor = sourceExtractorMap.get(annotation.source());
				}
			} catch (NoSuchBeanDefinitionException e) {
				sourceExtractor = sourceExtractorMap.putIfAbsent(annotation.source(),
					annotation.source().getDeclaredConstructor().newInstance());
				if (Objects.isNull(sourceExtractor)) {
					sourceExtractor = sourceExtractorMap.get(annotation.source());
				}
			}

			keyBuilder
				.append("_")
				.append(sourceExtractor.getSource(request));
		}

		return keyBuilder.toString();
	}
}
