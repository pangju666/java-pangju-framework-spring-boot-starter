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

package io.github.pangju666.framework.boot.utils;

import io.github.pangju666.commons.crypto.encryption.binary.RSABinaryEncryptor;
import io.github.pangju666.commons.crypto.key.RSAKey;
import io.github.pangju666.framework.boot.enums.Algorithm;
import io.github.pangju666.framework.boot.enums.Encoding;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.binary.AES256BinaryEncryptor;

import java.security.spec.InvalidKeySpecException;

/**
 * 加密解密工具类
 * <p>
 * 该工具类提供对称和非对称加密、解密功能，支持多种加密算法和编码方式。
 * 主要功能包括：
 * <ul>
 *     <li>Base64编码/解码</li>
 *     <li>十六进制编码/解码</li>
 *     <li>RSA非对称加密/解密</li>
 *     <li>AES-256对称加密/解密</li>
 * </ul>
 * </p>
 * <p>
 * 支持的编码方式：
 * <ul>
 *     <li>{@link Encoding#BASE64} - Base64编码</li>
 *     <li>{@link Encoding#HEX} - 十六进制编码</li>
 * </ul>
 * </p>
 * <p>
 * 支持的加密算法：
 * <ul>
 *     <li>{@link Algorithm#BASE64} - Base64编码（不加密）</li>
 *     <li>{@link Algorithm#HEX} - 十六进制编码（不加密）</li>
 *     <li>{@link Algorithm#RSA} - RSA非对称加密，需要密钥</li>
 *     <li>{@link Algorithm#AES256} - AES-256对称加密，需要密钥</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * // 加密为Base64字符串
 * String encrypted = CryptoUtils.encryptToString(
 *     "Hello World".getBytes(),
 *     "secretKey",
 *     Algorithm.AES256,
 *     Encoding.BASE64
 * );
 *
 * // 解密字符串
 * String decrypted = CryptoUtils.decryptToString(
 *     encrypted,
 *     "secretKey",
 *     Algorithm.AES256,
 *     Encoding.BASE64
 * );
 * }
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see Algorithm
 * @see Encoding
 * @see RSABinaryEncryptor
 * @see AES256BinaryEncryptor
 * @since 1.0.0
 */
public class CryptoUtils {
	protected CryptoUtils() {
	}

	/**
	 * 将字节数组加密为字符串
	 * <p>
	 * 根据指定的加密算法和编码方式，将原始内容加密并编码为字符串。
	 * </p>
	 * <p>
	 * 处理流程：
	 * <ul>
	 *     <li>若算法为BASE64，直接进行Base64编码并返回</li>
	 *     <li>若算法为HEX，直接进行十六进制编码并返回</li>
	 *     <li>其他算法（RSA、AES256）先加密后按指定编码方式编码为字符串</li>
	 * </ul>
	 * </p>
	 *
	 * @param rawContent 要加密的原始字节内容
	 * @param key        加密密钥。对于BASE64和HEX算法不需要；对于RSA和AES256算法必需
	 * @param algorithm  加密算法
	 * @param encoding   编码方式
	 * @return 加密后编码为字符串的结果
	 * @throws InvalidKeySpecException 当RSA密钥格式无效时抛出
	 * @see Algorithm
	 * @see Encoding
	 * @since 1.0.0
	 */
	public static String encryptToString(byte[] rawContent, String key, Algorithm algorithm, Encoding encoding) throws InvalidKeySpecException {
		if (algorithm == Algorithm.BASE64) {
			return Base64.encodeBase64String(rawContent);
		} else if (algorithm == Algorithm.HEX) {
			return Hex.encodeHexString(rawContent);
		}
		byte[] result = encrypt(rawContent, key, algorithm);
		return switch (encoding) {
			case BASE64 -> Base64.encodeBase64String(result);
			case HEX -> Hex.encodeHexString(result);
		};
	}

	/**
	 * 将字节数组加密为字节数组
	 * <p>
	 * 根据指定的加密算法和编码方式，将原始内容加密并编码为字节数组。
	 * 先进行加密操作，然后将密文按指定的编码方式编码为字节数组。
	 * </p>
	 *
	 * @param rawContent 要加密的原始字节内容
	 * @param key        加密密钥。对于BASE64和HEX算法不需要；对于RSA和AES256算法必需
	 * @param algorithm  加密算法
	 * @param encoding   编码方式
	 * @return 加密后编码为字节数组的结果
	 * @throws InvalidKeySpecException 当RSA密钥格式无效时抛出
	 * @see Algorithm
	 * @see Encoding
	 * @since 1.0.0
	 */
	public static byte[] encrypt(byte[] rawContent, String key, Algorithm algorithm, Encoding encoding) throws InvalidKeySpecException {
		byte[] result = encrypt(rawContent, key, algorithm);
		return switch (encoding) {
			case BASE64 -> Base64.encodeBase64(result);
			case HEX -> Hex.encodeHexString(result).getBytes();
		};
	}

	/**
	 * 将加密的字符串解密为字符串
	 * <p>
	 * 根据指定的加密算法和编码方式，将加密的字符串解密并转换为原始字符串。
	 * 支持自动处理空值情况。
	 * </p>
	 *
	 * @param rawContent 要解密的加密内容字符串
	 * @param key        解密密钥。对于BASE64和HEX算法不需要；对于RSA和AES256算法必需
	 * @param algorithm  解密算法
	 * @param encoding   原始编码方式（用于解码密文）
	 * @return 解密后的字符串
	 * @throws DecoderException 当十六进制解码失败时抛出
	 * @throws InvalidKeySpecException 当RSA密钥格式无效时抛出
	 * @see Algorithm
	 * @see Encoding
	 * @since 1.0.0
	 */
	public static String decryptToString(String rawContent, String key, Algorithm algorithm, Encoding encoding) throws DecoderException, InvalidKeySpecException {
		byte[] result = decrypt(rawContent, key, algorithm, encoding);
		return new String(result);
	}

	/**
	 * 将加密的字符串解密为字节数组
	 * <p>
	 * 根据指定的加密算法和编码方式，将加密的字符串解密为原始字节数组。
	 * 处理流程：
	 * <ul>
	 *     <li>首先根据编码方式将字符串解码为字节数组（用于密文）</li>
	 *     <li>然后根据算法进行解密操作</li>
	 *     <li>空或空白字符串会被返回为其字节形式</li>
	 * </ul>
	 * </p>
	 *
	 * @param rawContent 要解密的加密内容字符串
	 * @param key        解密密钥。对于BASE64和HEX算法不需要；对于RSA和AES256算法必需
	 * @param algorithm  解密算法
	 * @param encoding   原始编码方式（用于解码密文）
	 * @return 解密后的字节数组
	 * @throws DecoderException 当十六进制解码失败时抛出
	 * @throws InvalidKeySpecException 当RSA密钥格式无效时抛出
	 * @see Algorithm
	 * @see Encoding
	 * @since 1.0.0
	 */
	public static byte[] decrypt(String rawContent, String key, Algorithm algorithm, Encoding encoding) throws DecoderException, InvalidKeySpecException {
		if (StringUtils.isBlank(rawContent)) {
			return rawContent.getBytes();
		}
		return switch (algorithm) {
			case BASE64 -> Base64.decodeBase64(rawContent);
			case HEX -> Hex.decodeHex(rawContent);
			case RSA -> {
				RSAKey rsaKey = RSAKey.fromBase64String(null, key);
				RSABinaryEncryptor encryptor = new RSABinaryEncryptor(rsaKey);
				yield encryptor.decrypt(decode(rawContent, encoding));
			}
			case AES256 -> {
				AES256BinaryEncryptor encryptor = new AES256BinaryEncryptor();
				encryptor.setPassword(key);
				yield encryptor.decrypt(decode(rawContent, encoding));
			}
		};
	}

	/**
	 * 将字节数组加密为字节数组（内部方法）
	 * <p>
	 * 根据指定的加密算法，将原始内容加密为字节数组。
	 * 这是一个内部方法，供公开方法调用。
	 * </p>
	 *
	 * @param rawContent 要加密的原始字节内容
	 * @param key        加密密钥。对于BASE64和HEX算法不需要；对于RSA和AES256算法必需
	 * @param algorithm  加密算法
	 * @return 加密后的字节数组
	 * @throws InvalidKeySpecException 当RSA密钥格式无效时抛出
	 * @since 1.0.0
	 */
	private static byte[] encrypt(byte[] rawContent, String key, Algorithm algorithm) throws InvalidKeySpecException {
		return switch (algorithm) {
			case BASE64 -> Base64.encodeBase64(rawContent);
			case HEX -> Hex.encodeHexString(rawContent).getBytes();
			case RSA -> {
				RSAKey rsaKey = RSAKey.fromBase64String(key, null);
				RSABinaryEncryptor encryptor = new RSABinaryEncryptor(rsaKey);
				yield encryptor.encrypt(rawContent);
			}
			case AES256 -> {
				AES256BinaryEncryptor encryptor = new AES256BinaryEncryptor();
				encryptor.setPassword(key);
				yield encryptor.encrypt(rawContent);
			}
		};
	}

	/**
	 * 根据编码方式对字符串进行解码（内部方法）
	 * <p>
	 * 将以指定编码方式编码的字符串解码为字节数组。
	 * 这是一个内部辅助方法，用于在解密前对密文进行解码。
	 * </p>
	 *
	 * @param content  要解码的内容字符串
	 * @param encoding 编码方式
	 * @return 解码后的字节数组
	 * @throws DecoderException 当十六进制解码失败时抛出
	 * @since 1.0.0
	 */
	private static byte[] decode(String content, Encoding encoding) throws DecoderException {
		return switch (encoding) {
			case BASE64 -> Base64.decodeBase64(content);
			case HEX -> Hex.decodeHex(content);
		};
	}
}