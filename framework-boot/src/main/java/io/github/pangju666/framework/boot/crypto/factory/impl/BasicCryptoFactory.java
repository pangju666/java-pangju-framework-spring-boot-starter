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
import org.jasypt.util.binary.BasicBinaryEncryptor;
import org.jasypt.util.binary.BinaryEncryptor;
import org.jasypt.util.numeric.BasicDecimalNumberEncryptor;
import org.jasypt.util.numeric.BasicIntegerNumberEncryptor;
import org.jasypt.util.numeric.DecimalNumberEncryptor;
import org.jasypt.util.numeric.IntegerNumberEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基础强度加密工厂实现。
 * <p>
 * 通过口令派生密钥后执行DES对称加密，适用于对安全要求不高或默认场景。
 * </p>
 * <p>
 * 对应算法：<code>PBEWithMD5AndDES</code>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see BasicBinaryEncryptor
 * @see BasicTextEncryptor
 * @see BasicIntegerNumberEncryptor
 * @see BasicDecimalNumberEncryptor
 */
public class BasicCryptoFactory implements CryptoFactory {
    /**
     * 口令到二进制加密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
    private static final Map<String, BasicBinaryEncryptor> BINARY_ENCRYPTOR_MAP = new ConcurrentHashMap<>();
	/**
	 * 口令到文本加密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, BasicTextEncryptor> TEXT_ENCRYPTOR_MAP = new ConcurrentHashMap<>();
    /**
     * 口令到整型数字加密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
    private static final Map<String, BasicIntegerNumberEncryptor> INTEGER_ENCRYPTOR_MAP = new ConcurrentHashMap<>();
    /**
     * 口令到高精度小数加密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
    private static final Map<String, BasicDecimalNumberEncryptor> DECIMAL_ENCRYPTOR_MAP = new ConcurrentHashMap<>();

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
		return BINARY_ENCRYPTOR_MAP.computeIfAbsent(key, k -> {
			BasicBinaryEncryptor encryptor = new BasicBinaryEncryptor();
			encryptor.setPassword(k);
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
		return TEXT_ENCRYPTOR_MAP.computeIfAbsent(key, k -> {
			BasicTextEncryptor encryptor = new BasicTextEncryptor();
			encryptor.setPassword(k);
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
		return INTEGER_ENCRYPTOR_MAP.computeIfAbsent(key, k -> {
			BasicIntegerNumberEncryptor encryptor = new BasicIntegerNumberEncryptor();
			encryptor.setPassword(k);
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
		return DECIMAL_ENCRYPTOR_MAP.computeIfAbsent(key, k -> {
			BasicDecimalNumberEncryptor encryptor = new BasicDecimalNumberEncryptor();
			encryptor.setPassword(k);
			return encryptor;
		});
	}
}
