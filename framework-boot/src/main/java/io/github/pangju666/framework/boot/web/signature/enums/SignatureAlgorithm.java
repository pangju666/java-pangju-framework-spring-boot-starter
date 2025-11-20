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

package io.github.pangju666.framework.boot.web.signature.enums;

import io.github.pangju666.framework.boot.web.signature.annotation.Signature;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.function.Function;

/**
 * 摘要（哈希）算法枚举。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>提供对字符串计算摘要（哈希）的算法封装，返回十六进制字符串。</li>
 *   <li>用于签名计算、数据校验等场景，非加密算法，不可逆。</li>
 * </ul>
 *
 * <p><strong>支持的算法</strong></p>
 * <ul>
 *   <li>{@link #SHA1}：性能较好但安全性不足，逐渐弃用。</li>
 *   <li>{@link #SHA256}：安全性与性能均衡，通用推荐。</li>
 *   <li>{@link #SHA512}：更高安全性，适用于高安全需求。</li>
 *   <li>{@link #MD5}：速度快但安全性较弱，不建议用于安全场景。</li>
 * </ul>
 *
 * <p><strong>使用示例</strong></p>
 * <pre>
 * {@code
 * DigestAlgorithm algorithm = DigestAlgorithm.SHA256;
 * String digest = algorithm.computeDigest("plain-text");
 * }
 * </pre>
 *
 * <p><strong>注意事项</strong></p>
 * <ul>
 *   <li>返回值为小写十六进制字符串（基于 Apache Commons Codec）。</li>
 *   <li>输入字符串不可为 {@code null}，否则可能抛出 {@link NullPointerException}。</li>
 *   <li>涉及安全场景时优先选择 {@link #SHA256} 或 {@link #SHA512}。</li>
 * </ul>
 *
 * @author pangju666
 * @see Signature
 * @since 1.0.0
 */
public enum SignatureAlgorithm {
	/**
	 * SHA-1 哈希算法。
	 * <p>安全性较低，已逐渐被弃用。</p>
	 *
	 * @since 1.0.0
	 */
	SHA1(DigestUtils::sha1Hex),
	/**
	 * SHA-256 哈希算法。
	 * <p>兼顾安全性与性能的主流算法，适用于大多数应用场景。</p>
	 *
	 * @since 1.0.0
	 */
	SHA256(DigestUtils::sha256Hex),
	/**
	 * SHA-512 哈希算法。
	 * <p>提供更高的安全性，适合高安全性需求场景。</p>
	 *
	 * @since 1.0.0
	 */
	SHA512(DigestUtils::sha512Hex),
	/**
	 * MD5 哈希算法。
	 * <p>计算速度快，但安全性较弱，已不推荐用于高安全性需求场景。</p>
	 *
	 * @since 1.0.0
	 */
	MD5(DigestUtils::md5Hex);

	private final Function<String, String> digestFunction;

	SignatureAlgorithm(Function<String, String> digestFunction) {
		this.digestFunction = digestFunction;
	}

	/**
	 * 计算字符串的摘要（哈希）。
	 *
	 * <p>
	 * 使用当前枚举所代表的算法计算输入字符串的摘要，并以小写十六进制形式返回。
	 * </p>
	 *
	 * @param rawStr 原始字符串（不可为 {@code null}）
	 * @return 摘要值（小写十六进制字符串）
	 * @since 1.0.0
	 */
	public String computeDigest(String rawStr) {
		return digestFunction.apply(rawStr);
	}
}