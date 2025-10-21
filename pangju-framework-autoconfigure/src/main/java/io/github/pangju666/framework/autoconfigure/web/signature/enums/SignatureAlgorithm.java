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

package io.github.pangju666.framework.autoconfigure.web.signature.enums;

/**
 * 签名算法枚举。
 * <p>
 * 定义了可用于计算签名的哈希算法，用于对字符串进行加密计算生成唯一签名。
 * 不同的算法在安全性和性能上各有差异，开发者可根据业务需求选择合适的算法。
 * </p>
 *
 * <p>支持的算法包括：</p>
 * <ul>
 *     <li>{@link #SHA1}：SHA-1 哈希算法，具有较好的性能，但安全性较低，已逐渐被弃用。</li>
 *     <li>{@link #SHA256}：SHA-256 哈希算法，安全性较高，广泛应用于多数场景。</li>
 *     <li>{@link #SHA512}：SHA-512 哈希算法，提供更高的安全性，但计算性能相对较低。</li>
 *     <li>{@link #MD5}：MD5 哈希算法，计算速度快，但安全性较弱，不适用于高安全性需求场景。</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * SignatureAlgorithm algorithm = SignatureAlgorithm.SHA256;
 * String signature = computeSignature(data, algorithm);  // 调用计算签名方法
 * }
 * </pre>
 *
 * @author pangju666
 * @see io.github.pangju666.framework.autoconfigure.web.signature.annotation.Signature
 * @since 1.0.0
 */
public enum SignatureAlgorithm {
	/**
	 * SHA-1 哈希算法。
	 * <p>安全性较低，已逐渐被弃用。</p>
	 *
	 * @since 1.0.0
	 */
	SHA1,
	/**
	 * SHA-256 哈希算法。
	 * <p>兼顾安全性与性能的主流算法，适用于大多数应用场景。</p>
	 *
	 * @since 1.0.0
	 */
	SHA256,
	/**
	 * SHA-512 哈希算法。
	 * <p>提供更高的安全性，适合高安全性需求场景。</p>
	 *
	 * @since 1.0.0
	 */
	SHA512,
	/**
	 * MD5 哈希算法。
	 * <p>计算速度快，但安全性较弱，已不推荐用于高安全性需求场景。</p>
	 *
	 * @since 1.0.0
	 */
	MD5
}