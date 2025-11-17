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

package io.github.pangju666.framework.boot.crypto.utils;

import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.enums.Encoding;
import io.github.pangju666.framework.boot.spring.StaticSpringContext;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

/**
 * 加解密工具类（内部使用）。
 * <p>
 * 提供对字节数组、字符串、{@link BigInteger}、{@link BigDecimal} 等类型的通用加/解密能力，
 * 并封装了常见编码（Base64、Hex）及密钥占位符解析（如 <code>${...}</code>）的辅助逻辑。
 * 工具类为无状态设计，所有方法均为静态方法，线程安全。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see CryptoFactory
 */
public class CryptoUtils {
    /**
     * 日志记录器。
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(CryptoUtils.class);

    protected CryptoUtils() {
    }

    /**
     * 加密字节数组。
     *
     * @param factory 加解密工厂，负责创建具体的加密器
     * @param rawData 原始字节数据；为空时直接返回
     * @param key     密钥或占位符（支持 <code>${property.name}</code> 形式）
     * @return 加密后的字节数组；若输入为空则返回原值
     * @throws InvalidKeySpecException 当密钥格式不合法或不可用时抛出
	 * @since 1.0.0
     */
    public static byte[] encrypt(final CryptoFactory factory, final byte[] rawData, final String key) throws InvalidKeySpecException {
        if (ArrayUtils.isEmpty(rawData)) {
            return rawData;
        }
        return factory.getBinaryEncryptor(key).encrypt(rawData);
    }

    /**
     * 解密字节数组。
     *
     * @param factory 加解密工厂，负责创建具体的解密器
     * @param rawData 密文字节数据；为空时直接返回
     * @param key     密钥或占位符（支持 <code>${property.name}</code> 形式）
     * @return 解密后的字节数组；若输入为空则返回原值
     * @throws InvalidKeySpecException 当密钥格式不合法或不可用时抛出
	 * @since 1.0.0
	 */
    public static byte[] decrypt(final CryptoFactory factory, final byte[] rawData, final String key) throws InvalidKeySpecException {
        if (ArrayUtils.isEmpty(rawData)) {
            return rawData;
        }
        return factory.getBinaryDecryptor(key).decrypt(rawData);
    }

    /**
     * 加密字符串并按指定编码输出。
     *
     * @param factory  加解密工厂
     * @param rawData  原始字符串；为空白时直接返回
     * @param key      密钥或占位符（支持 <code>${property.name}</code> 形式）
     * @param encoding 输出编码方式（Base64 或 Hex）
     * @return 编码后的密文字符串；若输入为空白则返回原值
     * @throws InvalidKeySpecException 当密钥格式不合法或不可用时抛出
	 * @since 1.0.0
	 */
    public static String encryptString(final CryptoFactory factory, final String rawData, final String key,
                                       final Encoding encoding) throws InvalidKeySpecException {
        if (StringUtils.isBlank(rawData)) {
            return rawData;
        }
        byte[] result = encrypt(factory, rawData.getBytes(), key);
        return switch (encoding) {
            case BASE64 -> Base64.encodeBase64String(result);
            case HEX -> Hex.encodeHexString(result);
        };
    }

    /**
     * 解密字符串（先按指定编码解码，再进行解密）。
     *
     * @param factory  加解密工厂
     * @param rawData  编码后的密文字符串；为空白时直接返回
     * @param key      密钥或占位符（支持 <code>${property.name}</code> 形式）
     * @param encoding 输入的编码方式（Base64 或 Hex）
     * @return 解密后的明文字符串；若输入为空白则返回原值
     * @throws InvalidKeySpecException 当密钥格式不合法或不可用时抛出
     * @throws DecoderException        当编码内容解析失败时抛出（如 Hex 非法）
	 * @since 1.0.0
	 */
    public static String decryptString(final CryptoFactory factory, final String rawData, final String key,
                                       final Encoding encoding) throws InvalidKeySpecException, DecoderException {
        if (StringUtils.isBlank(rawData)) {
            return rawData;
        }
        byte[] result = switch (encoding) {
            case BASE64 -> Base64.decodeBase64(rawData);
            case HEX -> Hex.decodeHex(rawData);
        };
        return new String(decrypt(factory, result, key));
    }

    /**
     * 加密大整数。
     *
     * @param factory 加解密工厂
     * @param rawData 原始大整数；为 null 时返回 null
     * @param key     密钥或占位符
     * @return 加密后的大整数；为 null 时返回 null
     * @throws InvalidKeySpecException 当密钥格式不合法或不可用时抛出
	 * @since 1.0.0
	 */
    public static BigInteger encryptBigInteger(final CryptoFactory factory, final BigInteger rawData, final String key) throws InvalidKeySpecException {
        if (Objects.isNull(rawData)) {
            return null;
        }
        return factory.getIntegerNumberEncryptor(key).encrypt(rawData);
    }

    /**
     * 解密大整数。
     *
     * @param factory 加解密工厂
     * @param rawData 密文大整数；为 null 时返回 null
     * @param key     密钥或占位符
     * @return 解密后的大整数；为 null 时返回 null
     * @throws InvalidKeySpecException 当密钥格式不合法或不可用时抛出
	 * @since 1.0.0
	 */
    public static BigInteger decryptBigInteger(final CryptoFactory factory, final BigInteger rawData, final String key) throws InvalidKeySpecException {
        if (Objects.isNull(rawData)) {
            return null;
        }
        return factory.getIntegerNumberDecryptor(key).decrypt(rawData);
    }

    /**
     * 加密高精度小数。
     *
     * @param factory 加解密工厂
     * @param rawData 原始小数；为 null 时返回 null
     * @param key     密钥或占位符
     * @return 加密后的高精度小数；为 null 时返回 null
     * @throws InvalidKeySpecException 当密钥格式不合法或不可用时抛出
	 * @since 1.0.0
	 */
    public static BigDecimal encryptBigDecimal(final CryptoFactory factory, final BigDecimal rawData, final String key) throws InvalidKeySpecException {
        if (Objects.isNull(rawData)) {
            return null;
        }
        return factory.getDecimalNumberEncryptor(key).encrypt(rawData);
    }

    /**
     * 解密高精度小数。
     *
     * @param factory 加解密工厂
     * @param rawData 密文小数；为 null 时返回 null
     * @param key     密钥或占位符
     * @return 解密后的高精度小数；为 null 时返回 null
     * @throws InvalidKeySpecException 当密钥格式不合法或不可用时抛出
	 * @since 1.0.0
	 */
    public static BigDecimal decryptBigDecimal(final CryptoFactory factory, final BigDecimal rawData, final String key) throws InvalidKeySpecException {
        if (Objects.isNull(rawData)) {
            return null;
        }
        return factory.getDecimalNumberDecryptor(key).decrypt(rawData);
    }

    /**
     * 解析密钥字符串。
     * <p>
     * 支持以下两种形式：
     * <ul>
     *     <li>明文密钥：直接返回入参。</li>
     *     <li>占位符密钥：形如 <code>${crypto.key}</code>，从 Spring 环境解析实际值。</li>
     * </ul>
     * </p>
     * <p>
     * 行为说明：
     * <ul>
     *     <li>入参为空白：<code>throwsException == true</code> 时抛出 {@link InvalidKeySpecException}，否则记录错误日志并返回 {@code null}。</li>
     *     <li>占位符未解析到值：<code>throwsException == true</code> 时抛出 {@link InvalidKeySpecException}，否则记录错误日志并返回 {@code null}。</li>
     * </ul>
     * </p>
     * <p>
     * 注意事项：
     * <ul>
     *     <li>占位符解析通过 {@link StaticSpringContext#getProperty(String)}。</li>
     *     <li>调用方可通过 <code>throwsException</code> 控制失败时的处理策略。</li>
     * </ul>
     * </p>
     *
     * @param key 明文密钥或占位符（如 <code>${crypto.key}</code>）
     * @param throwsException 解析失败时是否抛出异常；true 抛出异常，false 返回 {@code null}
     * @return 解析后的明文密钥；当 <code>throwsException == false</code> 且失败时返回 {@code null}
	 * @throws InvalidKeySpecException 当 <code>throwsException == true</code> 且入参为空或未解析到值时抛出
	 * @since 1.0.0
	 */
    public static String getKey(final String key, boolean throwsException) throws InvalidKeySpecException {
        if (StringUtils.isBlank(key)) {
            if (throwsException) {
				throw new InvalidKeySpecException("密钥属性为空");
			}
			LOGGER.error("密钥属性不可为空");
            return null;
        }

        if (Strings.CS.startsWith(key, "${") && Strings.CS.endsWith(key, "}")) {
            String cryptoKey = StaticSpringContext.getProperty(key);
            if (StringUtils.isBlank(cryptoKey)) {
				if (throwsException) {
					throw new InvalidKeySpecException("未找到密钥，属性：" + key);
				}
                LOGGER.error("未找到密钥，属性：{}", key);
                return null;
            }
            return cryptoKey;
        }
        return key;
    }
}