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

import io.github.pangju666.commons.crypto.encryption.binary.RSABinaryEncryptor;
import io.github.pangju666.commons.crypto.encryption.numeric.RSADecimalNumberEncryptor;
import io.github.pangju666.commons.crypto.encryption.numeric.RSAIntegerNumberEncryptor;
import io.github.pangju666.commons.crypto.key.RSAKey;
import io.github.pangju666.commons.crypto.transformation.RSATransformation;
import io.github.pangju666.commons.crypto.transformation.impl.RSAOEAPWithSHA256Transformation;
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import org.jasypt.util.binary.BinaryEncryptor;
import org.jasypt.util.numeric.DecimalNumberEncryptor;
import org.jasypt.util.numeric.IntegerNumberEncryptor;

import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RSA 加密工厂实现。
 * <p>
 * 基于 RSA（默认使用{@code RSA/ECB/OAEPWithSHA-256AndMGF1Padding}）的非对称加密与数字处理器，分别为二进制、整型与高精度小数提供适配的加密器。
 * 加密器使用公钥，解密器使用私钥；密钥以 Base64 形式传入。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see RSABinaryEncryptor
 * @see RSAIntegerNumberEncryptor
 * @see RSADecimalNumberEncryptor
 */
public class RSACryptoFactory implements CryptoFactory {
	/**
	 * 公钥到二进制加密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, RSABinaryEncryptor> RSA_BINARY_ENCRYPT_ENCRYPTOR_MAP = new ConcurrentHashMap<>();
	/**
	 * 私钥到二进制解密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, RSABinaryEncryptor> RSA_BINARY_DECRYPT_ENCRYPTOR_MAP = new ConcurrentHashMap<>();
	/**
	 * 公钥到整型数字加密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, RSAIntegerNumberEncryptor> RSA_INTEGER_ENCRYPT_ENCRYPTOR_MAP = new ConcurrentHashMap<>();
	/**
	 * 私钥到整型数字解密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, RSAIntegerNumberEncryptor> RSA_INTEGER_DECRYPT_ENCRYPTOR_MAP = new ConcurrentHashMap<>();
	/**
	 * 公钥到高精度小数加密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, RSADecimalNumberEncryptor> RSA_BIGDECIMAL_ENCRYPT_ENCRYPTOR_MAP = new ConcurrentHashMap<>();
	/**
	 * 私钥到高精度小数解密器的缓存映射。
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, RSADecimalNumberEncryptor> RSA_BIGDECIMAL_DECRYPT_ENCRYPTOR_MAP = new ConcurrentHashMap<>();

    /**
     * RSA 变换（填充/摘要等参数）。
     * 默认为 OAEPWithSHA-256AndMGF1Padding，可通过构造参数替换。
	 *
	 * @since 1.0.0
	 */
    private final RSATransformation transformation;

    /**
     * 使用默认 OAEPWithSHA-256AndMGF1Padding 变换的构造方法。
     *
     * @since 1.0.0
     */
    public RSACryptoFactory() {
        this.transformation = new RSAOEAPWithSHA256Transformation();
    }

    /**
     * 指定 RSA 变换的构造方法。
     *
     * @param transformation RSA 变换策略
     * @since 1.0.0
     */
    public RSACryptoFactory(RSATransformation transformation) {
        this.transformation = transformation;
    }

    /**
     * 获取二进制加密器（使用公钥）。
     *
     * @param key Base64 编码的公钥字符串
     * @return 二进制加密器
     * @throws InvalidKeySpecException 公钥格式不合法时抛出
     * @since 1.0.0
     */
    @Override
    public BinaryEncryptor getBinaryEncryptor(String key) throws InvalidKeySpecException {
        RSABinaryEncryptor encryptor = RSA_BINARY_ENCRYPT_ENCRYPTOR_MAP.get(key);
        if (Objects.isNull(encryptor)) {
            encryptor = new RSABinaryEncryptor(transformation);
            encryptor.setKey(RSAKey.fromBase64String(key, null));
            RSA_BINARY_ENCRYPT_ENCRYPTOR_MAP.put(key, encryptor);
        }
        return encryptor;
    }

    /**
     * 获取整型数字加密器（使用公钥）。
     *
     * @param key Base64 编码的公钥字符串
     * @return 整型数字加密器
     * @throws InvalidKeySpecException 公钥格式不合法时抛出
     * @since 1.0.0
     */
    @Override
    public IntegerNumberEncryptor getIntegerNumberEncryptor(String key) throws InvalidKeySpecException {
        RSAIntegerNumberEncryptor encryptor = RSA_INTEGER_ENCRYPT_ENCRYPTOR_MAP.get(key);
        if (Objects.isNull(encryptor)) {
            encryptor = new RSAIntegerNumberEncryptor(transformation);
            encryptor.setKey(RSAKey.fromBase64String(key, null));
            RSA_INTEGER_ENCRYPT_ENCRYPTOR_MAP.put(key, encryptor);
        }
        return encryptor;
    }

    /**
     * 获取高精度小数加密器（使用公钥）。
     *
     * @param key Base64 编码的公钥字符串
     * @return 高精度小数加密器
     * @throws InvalidKeySpecException 公钥格式不合法时抛出
     * @since 1.0.0
     */
    @Override
    public DecimalNumberEncryptor getDecimalNumberEncryptor(String key) throws InvalidKeySpecException {
        RSADecimalNumberEncryptor encryptor = RSA_BIGDECIMAL_ENCRYPT_ENCRYPTOR_MAP.get(key);
        if (Objects.isNull(encryptor)) {
            encryptor = new RSADecimalNumberEncryptor(transformation);
            encryptor.setKey(RSAKey.fromBase64String(key, null));
            RSA_BIGDECIMAL_ENCRYPT_ENCRYPTOR_MAP.put(key, encryptor);
        }
        return encryptor;
    }

    /**
     * 获取二进制解密器（使用私钥）。
     *
     * @param key Base64 编码的私钥字符串
     * @return 二进制解密器
     * @throws InvalidKeySpecException 私钥格式不合法时抛出
     * @since 1.0.0
     */
    @Override
    public BinaryEncryptor getBinaryDecryptor(String key) throws InvalidKeySpecException {
        RSABinaryEncryptor encryptor = RSA_BINARY_DECRYPT_ENCRYPTOR_MAP.get(key);
        if (Objects.isNull(encryptor)) {
            encryptor = new RSABinaryEncryptor(transformation);
            encryptor.setKey(RSAKey.fromBase64String(null, key));
            RSA_BINARY_DECRYPT_ENCRYPTOR_MAP.put(key, encryptor);
        }
        return encryptor;
    }

    /**
     * 获取整型数字解密器（使用私钥）。
     *
     * @param key Base64 编码的私钥字符串
     * @return 整型数字解密器
     * @throws InvalidKeySpecException 私钥格式不合法时抛出
     * @since 1.0.0
     */
    @Override
    public IntegerNumberEncryptor getIntegerNumberDecryptor(String key) throws InvalidKeySpecException {
        RSAIntegerNumberEncryptor encryptor = RSA_INTEGER_DECRYPT_ENCRYPTOR_MAP.get(key);
        if (Objects.isNull(encryptor)) {
            encryptor = new RSAIntegerNumberEncryptor(transformation);
            encryptor.setKey(RSAKey.fromBase64String(null, key));
            RSA_INTEGER_DECRYPT_ENCRYPTOR_MAP.put(key, encryptor);
        }
        return encryptor;
    }

    /**
     * 获取高精度小数解密器（使用私钥）。
     *
     * @param key Base64 编码的私钥字符串
     * @return 高精度小数解密器
     * @throws InvalidKeySpecException 私钥格式不合法时抛出
     * @since 1.0.0
     */
    @Override
    public DecimalNumberEncryptor getDecimalNumberDecryptor(String key) throws InvalidKeySpecException {
        RSADecimalNumberEncryptor encryptor = RSA_BIGDECIMAL_DECRYPT_ENCRYPTOR_MAP.get(key);
        if (Objects.isNull(encryptor)) {
            encryptor = new RSADecimalNumberEncryptor(transformation);
            encryptor.setKey(RSAKey.fromBase64String(null, key));
            RSA_BIGDECIMAL_DECRYPT_ENCRYPTOR_MAP.put(key, encryptor);
        }
        return encryptor;
    }
}
