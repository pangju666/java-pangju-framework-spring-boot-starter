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

package io.github.pangju666.framework.boot.web.idempotent.validator.impl;

import io.github.pangju666.framework.boot.web.idempotent.annotation.Idempotent;
import io.github.pangju666.framework.boot.web.idempotent.validator.IdempotentValidator;
import net.jodah.expiringmap.ExpiringMap;

/**
 * 基于本地内存的幂等验证器
 * <p>
 * 使用 {@link ExpiringMap} 在本地内存中存储临时键值对，
 * 在指定时间间隔内阻止重复请求，适用于单节点应用的幂等性管理。
 * </p>
 * <p><b>主要功能</b></p>
 * <ul>
 *   <li>校验请求是否为重复请求（按键值在有效期内是否已存在）。</li>
 *   <li>维护带有过期时间的键值缓存，并在到期后自动移除。</li>
 *   <li>提供显式移除接口以在业务完成后清理记录。</li>
 * </ul>
 * <p><b>适用场景</b></p>
 * <ul>
 *   <li>单节点/本地内存幂等校验。</li>
 *   <li>不适用于分布式/多节点共享场景（请使用分布式实现）。</li>
 * </ul>
 * <p><b>行为说明</b></p>
 * <ul>
 *   <li>第一次校验通过时写入键值并设置过期时间；有效期内再次校验返回重复。</li>
 *   <li>过期时间来源于 {@link Idempotent#interval()} 与 {@link Idempotent#timeUnit()}。</li>
 * </ul>
 * <p><b>注意事项</b></p>
 * <ul>
 *   <li>并发下存在极短竞争窗口（先检查后写入）：同时到达的同一键的多个请求可能均通过；对强一致性需求可考虑分布式锁或原子写入方案。</li>
 *   <li>过期时间必须为正数；非正数可能导致底层实现抛出异常或立即过期。</li>
 * </ul>
 *
 * @author pangju666
 * @see Idempotent
 * @see IdempotentValidator
 * @since 1.0.0
 */
public class ExpireMapIdempotentValidator implements IdempotentValidator {
    /**
     * 本地过期键值映射。
     * <p>
     * 基于 {@link ExpiringMap} 存储请求标识（键）与处理标记（值），并自动管理过期。
     * 支持可变过期时间与到期自动移除，适合轻量级、单节点的幂等校验。
     * </p>
     *
     * @see ExpiringMap
     * @since 1.0.0
     */
    private final ExpiringMap<String, Boolean> expiringMap;

    /**
     * 创建基于 {@link ExpiringMap} 的幂等验证器实例。
     * <p>
     * 初始化为可变过期策略（{@code variableExpiration}）。
     * </p>
     */
    public ExpireMapIdempotentValidator() {
        this.expiringMap = ExpiringMap.builder()
            .variableExpiration()
            .build();
    }

    /**
     * 校验当前请求是否重复。
     * <p>
     * 若键已存在（有效期内已处理过），返回重复；否则写入并设置过期时间。
     * 并发场景下可能存在短暂竞争窗口导致极端情况下多个并发请求均通过。
     * </p>
     *
     * @param key    唯一标识当前请求的键
     * @param repeat 幂等性配置注解 {@link Idempotent}
     * @return 非重复请求返回 {@code true}；重复请求返回 {@code false}
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
     * 移除指定键的幂等记录。
     * <p>
     * 在业务流程完成或需要主动清理时使用，立即删除对应缓存键。
     * </p>
     *
     * @param key    唯一标识的请求键
     * @param repeat 幂等性配置注解 {@link Idempotent}
     */
    @Override
    public void remove(String key, Idempotent repeat) {
        expiringMap.remove(key);
    }
}
