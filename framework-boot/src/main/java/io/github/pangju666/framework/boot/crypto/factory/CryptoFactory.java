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

package io.github.pangju666.framework.boot.crypto.factory;

import org.jasypt.util.binary.BinaryEncryptor;
import org.jasypt.util.numeric.DecimalNumberEncryptor;
import org.jasypt.util.numeric.IntegerNumberEncryptor;

import java.security.spec.InvalidKeySpecException;

/**
 * 加/解密器工厂接口。
 * <p>
 * 为二进制、整型与高精度小数三类数据提供加密器与解密器的获取方法。
 * 具体算法由各实现类决定（如 RSA、AES‑256、PBE 等），密钥的含义也由实现确定：
 * 例如 RSA 工厂中加密器使用公钥、解密器使用私钥；PBE/AES 工厂中使用口令派生密钥。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see BinaryEncryptor
 * @see DecimalNumberEncryptor
 * @see IntegerNumberEncryptor
 * @see IntegerNumberEncryptor
 */
public interface CryptoFactory {
    /**
     * 获取字节数组加密器。
     *
     * @param key 密钥或口令；也可为占位符，具体由实现类解析
     * @return 字节数组加密器
     * @throws InvalidKeySpecException 密钥不可用或格式不合法时抛出
     * @since 1.0.0
     */
    BinaryEncryptor getBinaryEncryptor(String key) throws InvalidKeySpecException;

    /**
     * 获取大整数加密器。
     *
     * @param key 密钥或口令；也可为占位符，具体由实现类解析
     * @return 整型数字加密器
     * @throws InvalidKeySpecException 密钥不可用或格式不合法时抛出
     * @since 1.0.0
     */
    IntegerNumberEncryptor getIntegerNumberEncryptor(String key) throws InvalidKeySpecException;

    /**
     * 获取高精度小数加密器。
     *
     * @param key 密钥或口令；也可为占位符，具体由实现类解析
     * @return 高精度小数加密器
     * @throws InvalidKeySpecException 密钥不可用或格式不合法时抛出
     * @since 1.0.0
     */
    DecimalNumberEncryptor getDecimalNumberEncryptor(String key) throws InvalidKeySpecException;

    /**
     * 获取字节数组解密器。
     * <p>
     * 默认返回与加密器相同的实例；具体实现可覆盖以使用不同密钥或参数（如 RSA 私钥）。
     * </p>
     *
     * @param key 密钥或口令；也可为占位符，具体由实现类解析
     * @return 字节数组解密器
     * @throws InvalidKeySpecException 密钥不可用或格式不合法时抛出
     * @since 1.0.0
     */
    default BinaryEncryptor getBinaryDecryptor(String key) throws InvalidKeySpecException {
        return getBinaryEncryptor(key);
    }

    /**
     * 获取大整数解密器。
     * <p>
     * 默认返回与加密器相同的实例；具体实现可覆盖以使用不同密钥或参数（如 RSA 私钥）。
     * </p>
     *
     * @param key 密钥或口令；也可为占位符，具体由实现类解析
     * @return 整型数字解密器
     * @throws InvalidKeySpecException 密钥不可用或格式不合法时抛出
     * @since 1.0.0
     */
    default IntegerNumberEncryptor getIntegerNumberDecryptor(String key) throws InvalidKeySpecException {
        return getIntegerNumberEncryptor(key);
    }

    /**
     * 获取高精度小数解密器。
     * <p>
     * 默认返回与加密器相同的实例；具体实现可覆盖以使用不同密钥或参数。
     * </p>
     *
     * @param key 密钥或口令；也可为占位符，具体由实现类解析
     * @return 高精度小数解密器
     * @throws InvalidKeySpecException 密钥不可用或格式不合法时抛出
     * @since 1.0.0
     */
    default DecimalNumberEncryptor getDecimalNumberDecryptor(String key) throws InvalidKeySpecException {
        return getDecimalNumberEncryptor(key);
    }
}
