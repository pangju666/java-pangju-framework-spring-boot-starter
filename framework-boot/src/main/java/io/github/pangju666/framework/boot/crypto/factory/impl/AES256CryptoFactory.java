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

package io.github.pangju666.framework.boot.crypto.factory.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.jasypt.util.binary.AES256BinaryEncryptor;
import org.jasypt.util.binary.BinaryEncryptor;
import org.jasypt.util.numeric.AES256DecimalNumberEncryptor;
import org.jasypt.util.numeric.AES256IntegerNumberEncryptor;
import org.jasypt.util.numeric.DecimalNumberEncryptor;
import org.jasypt.util.numeric.IntegerNumberEncryptor;
import org.jasypt.util.text.AES256TextEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.util.Assert;

/**
 * AES‑256 加密工厂实现。
 * <p>
 * 通过口令派生密钥后执行AES对称加密，适用于通用业务数据的高效加解密。
 * </p>
 * <p>
 * 对应算法：<code>PBEWithHMACSHA512AndAES_256</code>
 * </p>
 *
 * @author pangju666
 * @see AES256BinaryEncryptor
 * @see AES256TextEncryptor
 * @see AES256IntegerNumberEncryptor
 * @see AES256DecimalNumberEncryptor
 * @since 1.0.0
 */
public class AES256CryptoFactory implements CryptoFactory {
	/**
	 * 口令到二进制加密器的缓存。
	 *
	 * @since 1.0.0
	 */
	private final Cache<String, AES256BinaryEncryptor> binaryEncryptorCache;
	/**
	 * 口令到文本加密器的缓存。
	 *
	 * @since 1.0.0
	 */
	private final Cache<String, AES256TextEncryptor> textEncryptorCache;
	/**
	 * 口令到整型数字加密器的缓存。
	 *
	 * @since 1.0.0
	 */
	private final Cache<String, AES256IntegerNumberEncryptor> integerEncryptorCache;
	/**
	 * 口令到高精度小数加密器的缓存。
	 *
	 * @since 1.0.0
	 */
	private final Cache<String, AES256DecimalNumberEncryptor> decimalEncryptorCache;

	/**
	 * 构造 AES‑256 加密工厂并初始化内部缓存。
	 *
	 * <p>缓存策略：为二进制、文本、整型与高精度小数四类加密器分别创建独立的 Caffeine 缓存，
	 * 每个缓存的最大条目数均受 {@code maxKeySize} 限制。</p>
	 * <p>参数校验：{@code maxKeySize} 必须大于 0。</p>
	 *
	 * @param maxKeySize 每个加密器缓存的最大条目数
	 * @since 1.0.0
	 */
	public AES256CryptoFactory(int maxKeySize) {
		Assert.isTrue(maxKeySize > 0, "maxKeySize 必须大于0");

		this.binaryEncryptorCache = Caffeine.newBuilder()
			.maximumSize(maxKeySize)
			.build();
		this.textEncryptorCache = Caffeine.newBuilder()
			.maximumSize(maxKeySize)
			.build();
		this.integerEncryptorCache = Caffeine.newBuilder()
			.maximumSize(maxKeySize)
			.build();
		this.decimalEncryptorCache = Caffeine.newBuilder()
			.maximumSize(maxKeySize)
			.build();
	}

	/**
	 * 获取并缓存字节数组加密器（按口令）。
	 *
	 * @param key 口令（Password）
	 * @return 字节数组加密器
	 * @since 1.0.0
	 */
	@Override
	public BinaryEncryptor getBinaryEncryptor(String key) {
		Assert.hasText(key, "key 不可为空");

		return binaryEncryptorCache.get(DigestUtils.sha256Hex(key), k -> {
			AES256BinaryEncryptor encryptor = new AES256BinaryEncryptor();
			encryptor.setPassword(key);
			return encryptor;
		});
	}

	/**
	 * 获取并缓存文本加密器（按口令）。
	 *
	 * @param key 口令（Password）
	 * @return 文本加密器
	 * @since 1.0.0
	 */
	@Override
	public TextEncryptor getTextEncryptor(String key) {
		Assert.hasText(key, "key 不可为空");

		return textEncryptorCache.get(DigestUtils.sha256Hex(key), k -> {
			AES256TextEncryptor encryptor = new AES256TextEncryptor();
			encryptor.setPassword(key);
			return encryptor;
		});
	}

	/**
	 * 获取并缓存整型数字加密器（按口令）。
	 *
	 * @param key 口令（Password）
	 * @return 整型数字加密器
	 * @since 1.0.0
	 */
	@Override
	public IntegerNumberEncryptor getIntegerNumberEncryptor(String key) {
		Assert.hasText(key, "key 不可为空");

		return integerEncryptorCache.get(DigestUtils.sha256Hex(key), k -> {
			AES256IntegerNumberEncryptor encryptor = new AES256IntegerNumberEncryptor();
			encryptor.setPassword(key);
			return encryptor;
		});
	}

	/**
	 * 获取并缓存高精度小数加密器（按口令）。
	 *
	 * @param key 口令（Password）
	 * @return 高精度小数加密器
	 * @since 1.0.0
	 */
	@Override
	public DecimalNumberEncryptor getDecimalNumberEncryptor(String key) {
		Assert.hasText(key, "key 不可为空");

		return decimalEncryptorCache.get(DigestUtils.sha256Hex(key), k -> {
			AES256DecimalNumberEncryptor encryptor = new AES256DecimalNumberEncryptor();
			encryptor.setPassword(key);
			return encryptor;
		});
	}
}
