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

package io.github.pangju666.framework.boot.autoconfigure.web.idempotent;

import io.github.pangju666.framework.boot.web.idempotent.annotation.Idempotent;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 幂等功能的配置属性
 * <p>
 * 通过前缀 {@code pangju.web.idempotent} 配置幂等实现与相关参数，
 * 支持本地内存与 Redis 两种实现，配合自动配置与 {@link Idempotent} 注解使用。
 * </p>
 * <p><b>支持的实现</b></p>
 * <ul>
 *   <li>{@link Type#EXPIRE_MAP}：基于本地内存（过期映射）的轻量实现，适合单节点。</li>
 *   <li>{@link Type#REDIS}：基于 Redis 的分布式实现，适合多节点/集群环境。</li>
 * </ul>
 * <p><b>核心属性</b></p>
 * <ul>
 *   <li>{@code type}：幂等实现类型，默认 {@link Type#EXPIRE_MAP}。</li>
 *   <li>{@code redis}：Redis 相关配置（模板 Bean 引用、键前缀）。</li>
 * </ul>
 * <p><b>配置示例（YAML）</b></p>
 * <pre>
 * {@code
 * pangju:
 *   web:
 *     idempotent:
 *       type: REDIS
 *       redis:
 *         redis-template-ref: myRedisTemplate
 *         key-prefix: myKeyPrefix
 * }
 * </pre>
 * <p><b>配合组件</b></p>
 * <ul>
 *   <li>{@link Idempotent}：标注需要幂等校验的方法。</li>
 *   <li>{@link IdempotentAutoConfiguration}：根据 {@code type} 自动选择并装配幂等验证器。</li>
 * </ul>
 *
 * @author pangju666
 * @see Idempotent
 * @see IdempotentAutoConfiguration
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "pangju.web.idempotent")
public class IdempotentProperties {
    /**
     * 幂等实现类型，决定选用本地或分布式方案。
     * <p>默认值：{@link Type#EXPIRE_MAP}</p>
     *
     * @since 1.0.0
     */
    private Type type = Type.EXPIRE_MAP;
    /**
     * Redis 相关配置（仅在使用 {@link Type#REDIS} 时生效）。
     *
     * @since 1.0.0
     */
    private Redis redis = new Redis();

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Redis getRedis() {
		return redis;
	}

	public void setRedis(Redis redis) {
		this.redis = redis;
	}

    /**
     * 幂等实现类型枚举。
     */
    public enum Type {
        /**
         * 基于 Redis 的分布式幂等。
         */
        REDIS,
        /**
         * 基于本地内存过期映射的幂等。
         */
        EXPIRE_MAP
    }

    /**
     * Redis 相关配置。
     *
     * @author pangju666
     * @since 1.0.0
     */
    public static class Redis {
        /**
         * RedisTemplate Bean 引用名称。
         * <p>
         * 对应属性：{@code pangju.web.idempotent.redis.redis-template-ref}
         * </p>
         *
         * @since 1.0.0
         */
        private String redisTemplateRef;
        /**
         * Redis 键前缀，默认值 {@code "idempotent"}。
         * <p>
         * 对应属性：{@code pangju.web.idempotent.redis.key-prefix}
         * </p>
         *
         * @since 1.0.0
         */
        private String keyPrefix = "idempotent";

		public String getRedisTemplateRef() {
			return redisTemplateRef;
		}

		public void setRedisTemplateRef(String redisTemplateRef) {
			this.redisTemplateRef = redisTemplateRef;
		}

		public String getKeyPrefix() {
			return keyPrefix;
		}

		public void setKeyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}
	}
}
