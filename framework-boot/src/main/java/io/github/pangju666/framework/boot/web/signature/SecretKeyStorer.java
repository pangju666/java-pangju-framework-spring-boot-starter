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

package io.github.pangju666.framework.boot.web.signature;

import io.github.pangju666.framework.boot.web.annotation.Signature;
import io.github.pangju666.framework.boot.web.signature.impl.DefaultSecretKeyStorer;

/**
 * 签名密钥存储器接口。
 * <p>
 * 定义了加载签名密钥的核心操作，用于根据应用 ID（appId）获取对应的签名密钥。
 * 该接口支持不同的实现方式，如从配置文件、数据库或其他存储介质中加载密钥。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *     <li>根据指定的应用 ID，动态加载签名所需的密钥。</li>
 *     <li>支持自定义实现，以满足密钥管理的不同需求。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>结合 {@link Signature} 注解进行签名校验。</li>
 *     <li>提供多应用场景下的签名密钥存储和管理。</li>
 * </ul>
 *
 * <p>示例代码：</p>
 * <pre>
 * {@code
 * @Component
 * public class MySecretKeyStorer implements SecretKeyStorer {
 *     @Override
 *     public String loadSecretKey(String appId) {
 *         // 根据应用 ID 从数据库或配置中加载密钥
 *         return database.findSecretKeyByAppId(appId);
 *     }
 * }
 * }
 * </pre>
 *
 * @author pangju666
 * @see DefaultSecretKeyStorer
 * @since 1.0.0
 */
public interface SecretKeyStorer {
	/**
	 * 根据应用 ID加载签名密钥。
	 * <p>
	 * 该方法用于在签名校验过程中，根据传入的 {@code appId} 获取对应的密钥。
	 * 如果指定的应用 ID 找不到密钥，应返回 {@code null} 或抛出异常处理。
	 * </p>
	 *
	 * @param appId 应用 ID，用于标识不同签名客户端。
	 * @return 对应应用 ID 的签名密钥。如果未找到，返回 {@code null}。
	 * @since 1.0.0
	 */
	String loadSecretKey(String appId);
}
