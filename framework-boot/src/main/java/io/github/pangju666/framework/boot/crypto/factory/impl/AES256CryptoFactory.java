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
import org.jasypt.util.binary.AES256BinaryEncryptor;
import org.jasypt.util.binary.BinaryEncryptor;
import org.jasypt.util.numeric.AES256DecimalNumberEncryptor;
import org.jasypt.util.numeric.AES256IntegerNumberEncryptor;
import org.jasypt.util.numeric.DecimalNumberEncryptor;
import org.jasypt.util.numeric.IntegerNumberEncryptor;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AES‑256 加密工厂实现。
 * <p>
 * 基于 Jasypt 的 AES‑256 加密器，通过口令派生密钥后执行对称加密，
 * 适用于通用业务数据的高效加解密。与枚举 {@code Algorithm.AES256}
 * 的说明一致，通常对应 {@code PBEWithHMACSHA512AndAES_256} 变体。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see AES256BinaryEncryptor
 * @see AES256IntegerNumberEncryptor
 * @see AES256DecimalNumberEncryptor
 */
public class AES256CryptoFactory implements CryptoFactory {
    /**
     * 口令到二进制加密器的缓存映射。
	 *
	 * @since 1.0.0
     */
    private static final Map<String, AES256BinaryEncryptor> BINARY_ENCRYPTOR_MAP = new ConcurrentHashMap<>();
    /**
     * 口令到整型数字加密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
    private static final Map<String, AES256IntegerNumberEncryptor> INTEGER_ENCRYPTOR_MAP = new ConcurrentHashMap<>();
    /**
     * 口令到高精度小数加密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
    private static final Map<String, AES256DecimalNumberEncryptor> DECIMAL_ENCRYPTOR_MAP = new ConcurrentHashMap<>();

    /**
     * 获取并缓存字节数组加密器（按口令）。
     *
     * @param key 口令（Password）
     * @return 字节数组加密器
     * @since 1.0.0
     */
    @Override
    public BinaryEncryptor getBinaryEncryptor(String key) {
        AES256BinaryEncryptor encryptor = BINARY_ENCRYPTOR_MAP.get(key);
        if (Objects.isNull(encryptor)) {
            encryptor = new AES256BinaryEncryptor();
            encryptor.setPassword(key);
            BINARY_ENCRYPTOR_MAP.put(key, encryptor);
        }
        return encryptor;
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
        AES256IntegerNumberEncryptor encryptor = INTEGER_ENCRYPTOR_MAP.get(key);
        if (Objects.isNull(encryptor)) {
            encryptor = new AES256IntegerNumberEncryptor();
            encryptor.setPassword(key);
            INTEGER_ENCRYPTOR_MAP.put(key, encryptor);
        }
        return encryptor;
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
        AES256DecimalNumberEncryptor encryptor = DECIMAL_ENCRYPTOR_MAP.get(key);
        if (Objects.isNull(encryptor)) {
            encryptor = new AES256DecimalNumberEncryptor();
            encryptor.setPassword(key);
            DECIMAL_ENCRYPTOR_MAP.put(key, encryptor);
        }
        return encryptor;
    }
}
