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

package io.github.pangju666.framework.boot.web.signature.storer.impl;

import io.github.pangju666.framework.boot.web.signature.annotation.Signature;
import io.github.pangju666.framework.boot.web.signature.storer.SignatureSecretKeyStorer;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 默认的签名密钥存储器实现。
 * <p>
 * 基于内存中维护的密钥映射（{@code Map<String, String>}），为签名校验提供密钥加载能力。
 * 该实现适用于应用启动时静态加载密钥的场景，只需将应用 ID 与密钥的对应关系传入即可。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *     <li>根据应用 ID 从内存中的映射表加载签名密钥。</li>
 *     <li>提供简单高效的密钥存储方案，适用于多应用场景中的密钥管理。</li>
 * </ul>
 *
 * <p>适用场景：</p>
 * <ul>
 *     <li>签名密钥在应用启动时已知，且无需动态更新。</li>
 *     <li>需要快速实现签名密钥管理的场景。</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * Map<String, String> secretKeys = new HashMap<>();
 * secretKeys.put("app1", "secretKey1");
 * secretKeys.put("app2", "secretKey2");
 *
 * SignatureSecretKeyStorer storer = new DefaultSignatureSecretKeyStorer(secretKeys);
 * String secretKey = storer.loadSecretKey("app1");  // 获取 "app1" 对应的密钥
 * }
 * </pre>
 *
 * <p>注意事项：</p>
 * <ul>
 *     <li>如果传入的密钥映射中不包含应用 ID 的对应关系，则返回 {@code null}。</li>
 *     <li>建议结合 {@link Signature} 注解使用，实现全面的签名校验功能。</li>
 * </ul>
 *
 * @author pangju666
 * @see SignatureSecretKeyStorer
 * @see Signature
 * @since 1.0.0
 */
public class DefaultSignatureSecretKeyStorer implements SignatureSecretKeyStorer {
	/**
	 * 内存中维护的应用 ID 与签名密钥的映射表。
	 *
	 * @since 1.0.0
	 */
	private final Map<String, String> secretKeyMap;

	/**
	 * 构造方法，初始化密钥存储器。
	 * <p>
	 * 通过传入的 {@code secretKeyMap} 映射表，设置应用 ID 与密钥的对应关系。
	 * </p>
	 *
	 * @param secretKeyMap 应用 ID 与密钥的对应关系，用于内存中存储的映射表。
	 * @since 1.0.0
	 */
	public DefaultSignatureSecretKeyStorer(Map<String, String> secretKeyMap) {
		this.secretKeyMap = Objects.isNull(secretKeyMap) ? Collections.emptyMap() : secretKeyMap;
	}

	/**
	 * 根据应用 ID 加载签名密钥。
	 * <p>
	 * 从内存中的密钥映射表中，根据传入的 {@code appId} 获取对应的签名密钥。
	 * 如果指定的应用 ID 不存在对应关系，则返回 {@code null}。
	 * </p>
	 *
	 * @param appId 应用 ID，用于标识具体的签名客户端。
	 * @return 对应的签名密钥；如果未找到，返回 {@code null}。
	 */
	@Override
	public String loadSecretKey(String appId) {
		return secretKeyMap.get(appId);
	}
}
