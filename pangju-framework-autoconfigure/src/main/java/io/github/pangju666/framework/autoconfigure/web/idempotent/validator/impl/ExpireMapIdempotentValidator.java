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

package io.github.pangju666.framework.autoconfigure.web.idempotent.validator.impl;

import io.github.pangju666.framework.autoconfigure.web.idempotent.annotation.Idempotent;
import io.github.pangju666.framework.autoconfigure.web.idempotent.validator.IdempotentValidator;
import net.jodah.expiringmap.ExpiringMap;

/**
 * 基于本地内存的幂等验证器实现。
 * <p>
 * 该类使用 {@link ExpiringMap} 实现幂等验证逻辑，
 * 通过在本地内存中存储临时键值对，在指定的时间间隔内阻止重复请求。
 * 适用于单节点应用场景中对幂等性的管理。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>检查请求是否属于重复请求。</li>
 *   <li>维护一个带有过期时间的键值对缓存。</li>
 *   <li>在幂等性校验完成后可选择性移除相关记录。</li>
 * </ul>
 *
 * @author pangju666
 * @see Idempotent
 * @see io.github.pangju666.framework.autoconfigure.web.idempotent.validator.IdempotentValidator
 * @since 1.0.0
 */
public class ExpireMapIdempotentValidator implements IdempotentValidator {
	/**
	 * 本地内存持有的过期键值映射。
	 * <p>
	 * 使用 {@link ExpiringMap} 提供对键值对的存储与自动过期管理功能。
	 * 存储的键为请求标识，值为是否已处理的布尔值。
	 * </p>
	 * <p>
	 * 功能特性：
	 * <ul>
	 *     <li>支持可变过期时间：每个键值可以设置独立的过期时间。</li>
	 *     <li>自动移除到期数据：当键值达到指定过期时间后会被移除。</li>
	 *     <li>轻量级单节点存储：适用于单节点模式下的幂等校验方案。</li>
	 * </ul>
	 * </p>
	 *
	 * @see ExpiringMap
	 * @since 1.0.0
	 */
	private final ExpiringMap<String, Boolean> expiringMap;

	/**
	 * 创建基于 ExpiringMap 的幂等验证器实例。
	 * <p>
	 * 初始化时会构建带有可变过期时间的 {@link ExpiringMap}。
	 * </p>
	 */
	public ExpireMapIdempotentValidator() {
		this.expiringMap = ExpiringMap.builder()
			.variableExpiration()
			.build();
	}

	/**
	 * 验证请求是否为重复请求。
	 * <p>
	 * 基于给定的 {@code key} 和 {@link Idempotent} 注解中的配置，
	 * 检查是否允许当前请求通过幂等性校验。
	 * 若该键已存在（表示此请求在有效时间内已处理过），则视为重复请求。
	 * 若该键不存在，则添加到缓存，并设置过期时间。
	 * </p>
	 *
	 * @param key 唯一标识当前请求的键。
	 * @param repeat 包含幂等性配置的 {@link Idempotent} 注解。
	 * @return 如果是新请求（非重复请求），返回 {@code true}；如果是重复请求，返回 {@code false}。
	 */
	@Override
	public boolean validate(String key, Idempotent repeat) {
		if (expiringMap.containsKey(key)) {
			return false;
		}
		expiringMap.put(key, Boolean.TRUE, repeat.interval(), repeat.timeUnit());
		return true;
	}

	/**
	 * 移除指定键的幂等性记录。
	 * <p>
	 * 当幂等性验证完成或需要主动清理时，可通过此方法从缓存中移除指定的键。
	 * </p>
	 *
	 * @param key 唯一标识的请求键。
	 * @param repeat 包含幂等性配置的 {@link Idempotent} 注解。
	 */
	@Override
	public void remove(String key, Idempotent repeat) {
		expiringMap.remove(key);
	}
}
