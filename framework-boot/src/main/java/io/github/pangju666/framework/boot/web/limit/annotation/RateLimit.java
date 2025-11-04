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

package io.github.pangju666.framework.boot.web.limit.annotation;

import io.github.pangju666.framework.boot.web.limit.enums.RateLimitScope;
import io.github.pangju666.framework.boot.web.limit.exception.RateLimitException;
import io.github.pangju666.framework.boot.web.limit.interceptor.RateLimitInterceptor;
import io.github.pangju666.framework.boot.web.limit.source.RateLimitSourceExtractor;
import io.github.pangju666.framework.boot.web.limit.source.impl.IpRateLimitSourceExtractor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 请求限流注解
 * <p>
 * 该注解用于标记需要进行速率限制的控制器类或方法。
 * 支持灵活的限流配置，包括限流时间窗口、限流数量、限流作用域等。
 * 可以应用在类级别（对整个类的所有方法生效）或方法级别（仅对该方法生效）。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>定义限流的时间窗口（秒、分钟、小时等）</li>
 *     <li>定义时间窗口内允许的最大请求数</li>
 *     <li>支持全局限流和基于源（IP、用户等）的限流</li>
 *     <li>支持自定义请求源提取器</li>
 *     <li>支持自定义限流键和前缀</li>
 *     <li>支持自定义错误消息</li>
 * </ul>
 * </p>
 * <p>
 * 限流作用域：
 * <ul>
 *     <li>{@link RateLimitScope#GLOBAL} - 全局限流，所有请求共享配额</li>
 *     <li>{@link RateLimitScope#SOURCE} - 基于请求源的限流，不同源独立计数</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * // 方法级别的限流：每秒最多10个请求
 * @GetMapping("/api/data")
 * @RateLimit(
 *     rate = 10,
 *     interval = 1,
 *     timeUnit = TimeUnit.SECONDS,
 *     scope = RateLimitScope.GLOBAL
 * )
 * public ResponseEntity<?> getData() {
 *     return ResponseEntity.ok("data");
 * }
 *
 * // 基于IP的限流：每个IP每分钟最多100个请求
 * @PostMapping("/api/submit")
 * @RateLimit(
 *     rate = 100,
 *     interval = 1,
 *     timeUnit = TimeUnit.MINUTES,
 *     scope = RateLimitScope.SOURCE,
 *     source = IpRateLimitSourceExtractor.class,
 *     message = "您的请求过于频繁，请在稍后重试"
 * )
 * public ResponseEntity<?> submit(@RequestBody Data data) {
 *     return ResponseEntity.ok("submitted");
 * }
 *
 * // 类级别的限流：对整个控制器的所有方法应用相同的限流策略
 * @RestController
 * @RequestMapping("/api/users")
 * @RateLimit(
 *     rate = 50,
 *     interval = 1,
 *     timeUnit = TimeUnit.MINUTES,
 *     scope = RateLimitScope.SOURCE
 * )
 * public class UserController {
 *     @GetMapping
 *     public ResponseEntity<?> listUsers() {
 *         return ResponseEntity.ok(users);
 *     }
 *
 *     @PostMapping
 *     public ResponseEntity<?> createUser(@RequestBody User user) {
 *         return ResponseEntity.ok(user);
 *     }
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * 配置详解：
 * <ul>
 *     <li><strong>prefix</strong> - Redis键的前缀，用于区分不同应用或模块的限流数据。不指定时使用默认前缀</li>
 *     <li><strong>key</strong> - 限流的业务键，支持使用SpEL表达式动态获取。不指定时使用方法名或类名+方法名</li>
 *     <li><strong>interval</strong> - 时间窗口的数值，结合timeUnit确定完整的时间窗口</li>
 *     <li><strong>timeUnit</strong> - 时间单位，支持SECONDS、MINUTES、HOURS等</li>
 *     <li><strong>rate</strong> - 时间窗口内允许的最大请求数，必需参数</li>
 *     <li><strong>scope</strong> - 限流作用域，决定限流是全局还是按源隔离</li>
 *     <li><strong>source</strong> - 请求源提取器类，用于提取请求标识（如IP地址），在scope为SOURCE时使用</li>
 *     <li><strong>message</strong> - 限流时返回的错误消息，会作为异常信息返回给客户端</li>
 * </ul>
 * </p>
 * <p>
 * 异常处理：
 * <p>
 * 当请求超过限流阈值时，框架会抛出{@link RateLimitException}异常，
 * 该异常会被自动转换为HTTP 429（Too Many Requests）响应。
 * </p>
 * </p>
 * <p>
 * 与其他组件的关系：
 * <ul>
 *     <li>由{@link RateLimitInterceptor}识别和处理</li>
 *     <li>使用配置中的限流实现类型（Resilience4j或Redisson）进行计数</li>
 *     <li>使用{@link RateLimitSourceExtractor}提取请求源信息</li>
 *     <li>触发限流时抛出{@link RateLimitException}</li>
 * </ul>
 * </p>
 * <p>
 * 性能考虑：
 * <ul>
 *     <li>全局限流（GLOBAL）性能最好，仅需单个计数器</li>
 *     <li>基于源的限流（SOURCE）需要为每个源维护一个计数器，性能取决于源的数量</li>
 *     <li>使用Redisson实现的分布式限流支持高并发场景</li>
 *     <li>使用Resilience4j实现的内存限流适合单机应用</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see RateLimitScope
 * @see RateLimitException
 * @see RateLimitSourceExtractor
 * @see IpRateLimitSourceExtractor
 * @see RateLimitInterceptor
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RateLimit {
	/**
	 * 限流键的前缀
	 * <p>
	 * 用于在分布式限流实现（如Redis）中区分不同应用或模块的限流数据。
	 * 完整的限流键格式为：{prefix}:{key}:{source}（当使用源隔离时）
	 * 或 {prefix}:{key}（全局限流时）。
	 * </p>
	 * <p>
	 * 如果不指定，框架会使用配置中的默认前缀（通常为"rate-limit"）。
	 * </p>
	 *
	 * @return 限流键的前缀，默认为空字符串（使用全局默认前缀）
	 * @since 1.0.0
	 */
	String prefix() default "";

	/**
	 * 限流的业务键
	 * <p>
	 * 用于标识一个特定的限流规则。同一个键会共享限流计数，
	 * 不同的键维护独立的限流计数。
	 * </p>
	 * <p>
	 * 支持使用SpEL表达式动态生成键，如：
	 * <ul>
	 *     <li>#{#userId} - 从请求参数或JWT中提取用户ID</li>
	 *     <li>#{T(java.lang.System).currentTimeMillis()} - 使用系统时间戳</li>
	 *     <li>#request.getParameter('param') - 使用系统时间戳</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 如果不指定，框架会使用默认策略：
	 * <ul>
	 *     <li>方法级注解：使用类名 + 方法名</li>
	 *     <li>类级注解：使用类名</li>
	 * </ul>
	 * </p>
	 *
	 * @return 限流的业务键，默认为空字符串（使用默认策略）
	 * @since 1.0.0
	 */
	String key() default "";

	/**
	 * 时间窗口的数值
	 * <p>
	 * 与{@link #timeUnit()}配合使用确定完整的时间窗口。
	 * 例如：interval=1, timeUnit=MINUTES 表示1分钟的时间窗口。
	 * </p>
	 * <p>
	 * 默认值为1，即1个{@link #timeUnit()}单位。
	 * </p>
	 *
	 * @return 时间窗口的数值，默认为1
	 * @since 1.0.0
	 */
	int interval() default 1;

	/**
	 * 时间单位
	 * <p>
	 * 与{@link #interval()}配合使用确定完整的时间窗口。
	 * 支持的单位包括：
	 * <ul>
	 *     <li>SECONDS - 秒</li>
	 *     <li>MINUTES - 分钟</li>
	 *     <li>HOURS - 小时</li>
	 *     <li>DAYS - 天</li>
	 *     <li>WEEKS - 周</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 默认为SECONDS，即秒。
	 * </p>
	 *
	 * @return 时间单位，默认为{@link TimeUnit#SECONDS}
	 * @since 1.0.0
	 */
	TimeUnit timeUnit() default TimeUnit.SECONDS;

	/**
	 * 时间窗口内允许的最大请求数
	 * <p>
	 * 这是一个必需参数，必须大于0。
	 * 当单位时间内的请求数超过此值时，触发限流，后续请求会被拒绝并抛出异常。
	 * </p>
	 * <p>
	 * 示例：
	 * <ul>
	 *     <li>rate=10, interval=1, timeUnit=SECONDS：每秒最多10个请求</li>
	 *     <li>rate=100, interval=1, timeUnit=MINUTES：每分钟最多100个请求</li>
	 *     <li>rate=1000, interval=1, timeUnit=HOURS：每小时最多1000个请求</li>
	 * </ul>
	 * </p>
	 *
	 * @return 时间窗口内允许的最大请求数，必需参数
	 * @since 1.0.0
	 */
	int rate();

	/**
	 * 限流作用域
	 * <p>
	 * 决定限流规则的应用范围：
	 * <ul>
	 *     <li>{@link RateLimitScope#GLOBAL} - 全局限流。
	 *         所有客户端共享同一个限流配额，任何客户端超过限制都会触发限流。
	 *         适用于整体系统限流</li>
	 *     <li>{@link RateLimitScope#SOURCE} - 基于请求源的限流。
	 *         不同的请求源（如不同IP地址）各自维护独立的限流配额，
	 *         互不影响。适用于按客户端粒度的限流</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 默认为{@link RateLimitScope#GLOBAL}，即全局限流。
	 * 当选择SOURCE时，需要通过{@link #source()}指定源提取器。
	 * </p>
	 *
	 * @return 限流作用域，默认为{@link RateLimitScope#GLOBAL}
	 * @see RateLimitScope
	 * @since 1.0.0
	 */
	RateLimitScope scope() default RateLimitScope.GLOBAL;

	/**
	 * 请求源提取器类
	 * <p>
	 * 当{@link #scope()}为{@link RateLimitScope#SOURCE}时，
	 * 该属性指定的提取器用于从请求中提取源标识信息（如IP地址、用户ID等）。
	 * 框架会根据提取的源标识为每个源维护独立的限流计数。
	 * </p>
	 * <p>
	 * 内置的源提取器实现：
	 * <ul>
	 *     <li>{@link IpRateLimitSourceExtractor} - 基于请求IP地址的限流</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 可以实现{@link RateLimitSourceExtractor}接口自定义源提取逻辑，例如：
	 * <pre>
	 * {@code
	 * public class UserIdRateLimitSourceExtractor implements RateLimitSourceExtractor {
	 *     @Override
	 *     public String extract(HttpServletRequest request) {
	 *         String userId = (String) request.getAttribute("userId");
	 *         return userId != null ? userId : request.getRemoteAddr();
	 *     }
	 * }
	 * }
	 * </pre>
	 * </p>
	 * <p>
	 * 默认使用{@link IpRateLimitSourceExtractor}，基于请求的IP地址进行限流。
	 * </p>
	 *
	 * @return 请求源提取器类，默认为{@link IpRateLimitSourceExtractor}
	 * @see RateLimitSourceExtractor
	 * @see IpRateLimitSourceExtractor
	 * @since 1.0.0
	 */
	Class<? extends RateLimitSourceExtractor> source() default IpRateLimitSourceExtractor.class;

	/**
	 * 限流时返回的错误消息
	 * <p>
	 * 当请求被限流时，会抛出{@link RateLimitException}异常，
	 * 该异常会被转换为HTTP 429响应，并包含此消息。
	 * 该消息会返回给客户端，建议提供清晰、友好的提示信息。
	 * </p>
	 * <p>
	 * 默认消息为："请求次数已达上限，请稍候再试"。
	 * </p>
	 * <p>
	 * 自定义消息示例：
	 * <pre>
	 * {@code
	 * @RateLimit(
	 *     rate = 100,
	 *     message = "您的请求过于频繁，请在${waitTime}毫秒后重试"
	 * )
	 * }
	 * </pre>
	 * </p>
	 *
	 * @return 限流时的错误消息，默认为"请求次数已达上限，请稍候再试"
	 * @since 1.0.0
	 */
	String message() default "请求次数已达上限，请稍候再试";
}
