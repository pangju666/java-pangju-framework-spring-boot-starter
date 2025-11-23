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

import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.jasypt.util.binary.BinaryEncryptor;
import org.jasypt.util.binary.StrongBinaryEncryptor;
import org.jasypt.util.numeric.DecimalNumberEncryptor;
import org.jasypt.util.numeric.IntegerNumberEncryptor;
import org.jasypt.util.numeric.StrongDecimalNumberEncryptor;
import org.jasypt.util.numeric.StrongIntegerNumberEncryptor;
import org.jasypt.util.text.StrongTextEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 高强度加密工厂实现。
 * <p>
 * 通过口令派生密钥后执行DES对称加密，在同等口令强度下具备更高的安全性。
 * </p>
 * <p>
 * 对应算法：<code>PBEWithMD5AndTripleDES</code>
 * </p>
 *
 * @author pangju666
 * @see StrongBinaryEncryptor
 * @see StrongTextEncryptor
 * @see StrongIntegerNumberEncryptor
 * @see StrongDecimalNumberEncryptor
 * @since 1.0.0
 */
public class StrongCryptoFactory implements CryptoFactory {
	/**
	 * 口令到二进制加密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, StrongBinaryEncryptor> BINARY_ENCRYPTOR_MAP = new ConcurrentHashMap<>();
	/**
	 * 口令到文本加密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, StrongTextEncryptor> TEXT_ENCRYPTOR_MAP = new ConcurrentHashMap<>();
	/**
	 * 口令到整型数字加密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, StrongIntegerNumberEncryptor> INTEGER_ENCRYPTOR_MAP = new ConcurrentHashMap<>();
	/**
	 * 口令到高精度小数加密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, StrongDecimalNumberEncryptor> DECIMAL_ENCRYPTOR_MAP = new ConcurrentHashMap<>();

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

		return BINARY_ENCRYPTOR_MAP.computeIfAbsent(DigestUtils.sha256Hex(key), k -> {
			StrongBinaryEncryptor encryptor = new StrongBinaryEncryptor();
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

		return TEXT_ENCRYPTOR_MAP.computeIfAbsent(DigestUtils.sha256Hex(key), k -> {
			StrongTextEncryptor encryptor = new StrongTextEncryptor();
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

		return INTEGER_ENCRYPTOR_MAP.computeIfAbsent(DigestUtils.sha256Hex(key), k -> {
			StrongIntegerNumberEncryptor encryptor = new StrongIntegerNumberEncryptor();
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

		return DECIMAL_ENCRYPTOR_MAP.computeIfAbsent(DigestUtils.sha256Hex(key), k -> {
			StrongDecimalNumberEncryptor encryptor = new StrongDecimalNumberEncryptor();
			encryptor.setPassword(key);
			return encryptor;
		});
	}
}
