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

package io.github.pangju666.framework.autoconfigure.web.idempotent.validator;

import io.github.pangju666.framework.autoconfigure.web.idempotent.annotation.Idempotent;

/**
 * 幂等验证器接口。
 * <p>
 * 该接口定义了幂等验证的核心操作，用于检测请求是否为重复请求并提供相关控制方法。
 * 可通过不同的实现（如基于 Redis 或基于本地内存）实现幂等性验证逻辑。
 * </p>
 * <p>
 * 接口提供以下两个方法：
 * <ul>
 *     <li>{@link #validate(String, Idempotent)}：验证指定的键是否已被记录，判定请求是否重复。</li>
 *     <li>{@link #remove(String, Idempotent)}：移除指定键的幂等性记录。</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see io.github.pangju666.framework.autoconfigure.web.idempotent.annotation.Idempotent
 * @since 1.0.0
 */
public interface IdempotentValidator {
	/**
	 * 验证请求是否为重复请求。
	 * <p>
	 * 基于给定的键和 {@link Idempotent} 注解信息，判断是否允许当前请求执行。
	 * </p>
	 * <p>
	 * 实现方式包括：
	 * <ul>
	 *     <li>Redis 验证：通过 Redis 键值存储来标记请求。</li>
	 *     <li>本地内存映射验证：在本地内存中存储键值。</li>
	 * </ul>
	 * </p>
	 *
	 * @param key    唯一标识当前请求的键值。
	 * @param repeat 包含幂等性配置的 {@link Idempotent} 注解信息。
	 * @return 如果是重复请求，则返回 {@code false}；否则返回 {@code true}。
	 * @since 1.0.0
	 */
	boolean validate(String key, Idempotent repeat);

	/**
	 * 移除指定键的幂等性记录。
	 * <p>
	 * 当幂等性验证完成，或在某些特定场景下需要手动清理幂等记录时，
	 * 可通过此方法移除已保存的幂等记录。
	 * </p>
	 *
	 * @param key 唯一标识的请求键值。
	 * @param repeat 包含幂等性配置的 {@link Idempotent} 注解信息。
	 * @since 1.0.0
	 */
	void remove(String key, Idempotent repeat);
}