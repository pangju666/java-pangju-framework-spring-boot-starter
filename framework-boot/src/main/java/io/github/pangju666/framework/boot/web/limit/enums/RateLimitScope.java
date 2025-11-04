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

package io.github.pangju666.framework.boot.web.limit.enums;

/**
 * 请求限流作用域枚举
 * <p>
 * 该枚举定义了速率限制的应用范围，用于控制速率限制的生效粒度。
 * </p>
 * <p>
 * 枚举值说明：
 * <ul>
 *     <li>
 *         <strong>GLOBAL</strong>
 *         <p>
 *         全局速率限制。限制规则应用于所有客户端和所有请求源。
 *         即所有来自不同客户端的请求共享同一限流配额。
 *         适用于需要限制整个系统请求速率的场景。
 *         </p>
 *     </li>
 *     <li>
 *         <strong>SOURCE</strong>
 *         <p>
 *         基于请求源的速率限制。针对不同的请求源（如IP地址、用户ID等）单独应用限制规则。
 *         每个请求源拥有独立的限流配额，不同源之间的限流计数相互独立。
 *         适用于需要区分不同客户端限流的场景。
 *         </p>
 *     </li>
 * </ul>
 * </p>
 * <p>
 * 使用场景示例：
 * <pre>
 * {@code
 * // 全局限制示例：所有请求共享1000个请求/分钟的配额
 * @RateLimit(
 *     requestsPerMinute = 1000,
 *     scope = RateLimitScope.GLOBAL
 * )
 * @GetMapping("/api/data")
 * public ResponseEntity<?> getData() {
 *     return ResponseEntity.ok("data");
 * }
 *
 * // 源级限制示例：每个IP地址限制100个请求/分钟
 * @RateLimit(
 *     requestsPerMinute = 100,
 *     scope = RateLimitScope.SOURCE
 * )
 * @PostMapping("/api/submit")
 * public ResponseEntity<?> submit(@RequestBody Data data) {
 *     return ResponseEntity.ok("submitted");
 * }
 * }
 * </pre>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public enum RateLimitScope {
	/**
	 * 全局速率限制
	 * <p>
	 * 所有客户端共享同一限流配额，对整个应用进行全局限流控制。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	GLOBAL,
	/**
	 * 基于请求源的速率限制
	 * <p>
	 * 按请求源（IP、用户ID等）分别计算限流配额，不同源独立计数。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	SOURCE
}
