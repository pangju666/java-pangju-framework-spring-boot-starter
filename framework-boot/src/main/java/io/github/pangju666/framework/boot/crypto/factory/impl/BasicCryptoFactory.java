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
import org.jasypt.util.numeric.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基础强度加密工厂实现。
 * <p>
 * 基于 Jasypt 的基础强度加密器（BasicEncryptor），通常映射到
 * <code>PBEWithMD5AndDES</code> 的口令派生加密（PBE），适用于对安全
 * 要求不高或默认场景。建议提升口令强度并配置更合理的参数。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see BasicBinaryEncryptor
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
        BasicBinaryEncryptor encryptor = BINARY_ENCRYPTOR_MAP.get(key);
        if (Objects.isNull(encryptor)) {
            encryptor = new BasicBinaryEncryptor();
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
        BasicIntegerNumberEncryptor encryptor = INTEGER_ENCRYPTOR_MAP.get(key);
        if (Objects.isNull(encryptor)) {
            encryptor = new BasicIntegerNumberEncryptor();
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
        BasicDecimalNumberEncryptor encryptor = DECIMAL_ENCRYPTOR_MAP.get(key);
        if (Objects.isNull(encryptor)) {
            encryptor = new BasicDecimalNumberEncryptor();
            encryptor.setPassword(key);
            DECIMAL_ENCRYPTOR_MAP.put(key, encryptor);
        }
        return encryptor;
    }
}
