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
import io.github.pangju666.commons.crypto.encryption.binary.RSABinaryEncryptor;
import io.github.pangju666.commons.crypto.encryption.numeric.RSADecimalNumberEncryptor;
import io.github.pangju666.commons.crypto.encryption.numeric.RSAIntegerNumberEncryptor;
import io.github.pangju666.commons.crypto.encryption.text.RSATextEncryptor;
import io.github.pangju666.commons.crypto.key.RSAKeyPair;
import io.github.pangju666.commons.crypto.transformation.RSATransformation;
import io.github.pangju666.commons.crypto.transformation.impl.RSAOEAPWithSHA256Transformation;
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.jasypt.util.binary.BinaryEncryptor;
import org.jasypt.util.numeric.DecimalNumberEncryptor;
import org.jasypt.util.numeric.IntegerNumberEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.util.Assert;

import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

/**
 * RSA 加密工厂实现。
 *
 * <p>
 * 通过密钥后执行RSA非对称加密；加密器使用公钥，解密器使用私钥；密钥以 Base64 形式传入
 * </p>
 * <p>
 * 默认算法：<code>RSA/ECB/OAEPWithSHA-256AndMGF1Padding</code>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see RSABinaryEncryptor
 * @see RSATextEncryptor
 * @see RSAIntegerNumberEncryptor
 * @see RSADecimalNumberEncryptor
 */
public class RSACryptoFactory implements CryptoFactory {
	/**
	 * 公钥到二进制加密器的缓存。
	 *
	 * @since 1.0.0
	 */
	protected final Cache<String, RSABinaryEncryptor> binaryEncryptEncryptorCache;
	/**
	 * 私钥到二进制解密器的缓存。
	 *
	 * @since 1.0.0
	 */
	protected final Cache<String, RSABinaryEncryptor> binaryDecryptEncryptorCache;
	/**
	 * 公钥到文本加密器的缓存。
	 *
	 * @since 1.0.0
	 */
	protected final Cache<String, RSATextEncryptor> textEncryptEncryptorCache;
	/**
	 * 私钥到文本解密器的缓存。
	 *
	 * @since 1.0.0
	 */
	protected final Cache<String, RSATextEncryptor> textDecryptEncryptorCache;
	/**
	 * 公钥到整型数字加密器的缓存。
	 *
	 * @since 1.0.0
	 */
	protected final Cache<String, RSAIntegerNumberEncryptor> integerEncryptEncryptorCache;
	/**
	 * 私钥到整型数字解密器的缓存。
	 *
	 * @since 1.0.0
	 */
	protected final Cache<String, RSAIntegerNumberEncryptor> integerDecryptEncryptorCache;
	/**
	 * 公钥到高精度小数加密器的缓存。
	 *
	 * @since 1.0.0
	 */
	protected final Cache<String, RSADecimalNumberEncryptor> decimalEncryptEncryptorCache;
	/**
	 * 私钥到高精度小数解密器的缓存。
	 *
	 * @since 1.0.0
	 */
	protected final Cache<String, RSADecimalNumberEncryptor> decimalDecryptEncryptorCache;

    /**
     * RSA 加密方案（填充/摘要等参数）。
     * 默认为 OAEPWithSHA-256AndMGF1Padding，可通过构造参数替换。
	 *
	 * @since 1.0.0
	 */
    protected final RSATransformation transformation;

    /**
     * 默认使用 OAEPWithSHA-256AndMGF1Padding 作为加密方案的构造方法。
     *
     * <p>缓存策略：为加/解密的二进制、文本、整型与高精度小数四类加密器分别创建独立的缓存，
     * 每个缓存的最大条目数受 {@code maxKeySize} 限制。</p>
     *
     * @param maxKeySize 每类加密器缓存的最大条目数（建议为正整数）
     * @since 1.0.0
     */
    public RSACryptoFactory(int maxKeySize) {
        this(maxKeySize, new RSAOEAPWithSHA256Transformation());
    }

    /**
     * 指定 RSA 加密方案的构造方法。
     *
     * <p>缓存策略：为加/解密的二进制、文本、整型与高精度小数四类加密器分别创建独立的缓存，
     * 每个缓存的最大条目数受 {@code maxKeySize} 限制。</p>
     *
     * @param maxKeySize     每类加密器缓存的最大条目数（建议为正整数）
     * @param transformation RSA 加密方案
     * @throws IllegalArgumentException 当 {@code transformation} 为 {@code null} 时抛出
     * @since 1.0.0
     */
    public RSACryptoFactory(int maxKeySize, RSATransformation transformation) {
        Assert.notNull(transformation, "transformation 不可为 null");

        this.transformation = transformation;
		
		this.binaryEncryptEncryptorCache = Caffeine.newBuilder()
			.maximumSize(maxKeySize)
			.build();
		this.binaryDecryptEncryptorCache = Caffeine.newBuilder()
			.maximumSize(maxKeySize)
			.build();
		this.textEncryptEncryptorCache = Caffeine.newBuilder()
			.maximumSize(maxKeySize)
			.build();
		this.textDecryptEncryptorCache = Caffeine.newBuilder()
			.maximumSize(maxKeySize)
			.build();
		this.integerEncryptEncryptorCache = Caffeine.newBuilder()
			.maximumSize(maxKeySize)
			.build();
		this.integerDecryptEncryptorCache = Caffeine.newBuilder()
			.maximumSize(maxKeySize)
			.build();
		this.decimalEncryptEncryptorCache = Caffeine.newBuilder()
			.maximumSize(maxKeySize)
			.build();
		this.decimalDecryptEncryptorCache = Caffeine.newBuilder()
			.maximumSize(maxKeySize)
			.build();
    }

    /**
     * 获取并缓存二进制加密器（使用公钥）。
     *
     * @param publicKey Base64 编码的 X.509 格式公钥字符串
     * @return 二进制加密器
     * @since 1.0.0
     */
    @Override
    public BinaryEncryptor getBinaryEncryptor(String publicKey) {
		Assert.hasText(publicKey, "key 不可为空");

		String mapKey = DigestUtils.sha256Hex(publicKey);
		return binaryEncryptEncryptorCache.get(mapKey, k -> {
			try {
				RSABinaryEncryptor encryptor = new RSABinaryEncryptor(transformation);
				encryptor.setPublicKey(RSAKeyPair.fromBase64String(publicKey, null).getPublicKey());
				encryptor.initialize();
				return encryptor;
			} catch (InvalidKeySpecException e) {
				throw new IllegalArgumentException("无效的 RSA 公钥", e);
			}
		});
    }

	/**
	 * 获取并缓存文本加密器（使用公钥）。
	 *
	 * @param publicKey Base64 编码的 X.509 格式公钥字符串
	 * @return 文本加密器
	 * @since 1.0.0
	 */
	@Override
	public TextEncryptor getTextEncryptor(String publicKey) {
		Assert.hasText(publicKey, "key 不可为空");

		String mapKey = DigestUtils.sha256Hex(publicKey);
		return textEncryptEncryptorCache.get(mapKey, k -> {
			try {
				RSABinaryEncryptor binaryEncryptor = binaryEncryptEncryptorCache.getIfPresent(publicKey);
				if (Objects.nonNull(binaryEncryptor)) {
					return new RSATextEncryptor(binaryEncryptor);
				}

				RSATextEncryptor encryptor = new RSATextEncryptor(transformation);
				encryptor.setPublicKey(RSAKeyPair.fromBase64String(publicKey, null).getPublicKey());
				encryptor.initialize();
				return encryptor;
			} catch (InvalidKeySpecException e) {
				throw new IllegalArgumentException("无效的 RSA 公钥", e);
			}
		});
	}

    /**
     * 获取并缓存整型数字加密器（使用公钥）。
     *
     * @param publicKey Base64 编码的 X.509 格式公钥字符串
     * @return 整型数字加密器
     * @since 1.0.0
     */
    @Override
    public IntegerNumberEncryptor getIntegerNumberEncryptor(String publicKey) {
		Assert.hasText(publicKey, "publicKey 不可为空");

		String mapKey = DigestUtils.sha256Hex(publicKey);
		return integerEncryptEncryptorCache.get(mapKey, k -> {
			try {
				RSABinaryEncryptor binaryEncryptor = binaryEncryptEncryptorCache.getIfPresent(publicKey);
				if (Objects.nonNull(binaryEncryptor)) {
					return new RSAIntegerNumberEncryptor(binaryEncryptor);
				}

				RSAIntegerNumberEncryptor encryptor = new RSAIntegerNumberEncryptor(transformation);
				encryptor.setPublicKey(RSAKeyPair.fromBase64String(publicKey, null).getPublicKey());
				encryptor.initialize();
				return encryptor;
			} catch (InvalidKeySpecException e) {
				throw new IllegalArgumentException("无效的 RSA 公钥", e);
			}
		});
    }

    /**
     * 获取并缓存高精度小数加密器（使用公钥）。
     *
     * @param publicKey Base64 编码的 X.509 格式公钥字符串
     * @return 高精度小数加密器
     * @since 1.0.0
     */
    @Override
    public DecimalNumberEncryptor getDecimalNumberEncryptor(String publicKey) {
		Assert.hasText(publicKey, "publicKey 不可为空");

		String mapKey = DigestUtils.sha256Hex(publicKey);
		return decimalEncryptEncryptorCache.get(mapKey, k -> {
			try {
				RSABinaryEncryptor binaryEncryptor = binaryEncryptEncryptorCache.getIfPresent(publicKey);
				if (Objects.nonNull(binaryEncryptor)) {
					return new RSADecimalNumberEncryptor(binaryEncryptor);
				}

				RSADecimalNumberEncryptor encryptor = new RSADecimalNumberEncryptor(transformation);
				encryptor.setPublicKey(RSAKeyPair.fromBase64String(publicKey, null).getPublicKey());
				encryptor.initialize();
				return encryptor;
			} catch (InvalidKeySpecException e) {
				throw new IllegalArgumentException("无效的 RSA 公钥", e);
			}
		});
    }

    /**
     * 获取并缓存二进制解密器（使用私钥）。
     *
     * @param protectedKey Base64 编码的 PKCS#8 格式私钥字符串
     * @return 二进制解密器
     * @since 1.0.0
     */
    @Override
    public BinaryEncryptor getBinaryDecryptor(String protectedKey) {
		Assert.hasText(protectedKey, "protectedKey 不可为空");

		String mapKey = DigestUtils.sha256Hex(protectedKey);
		return binaryDecryptEncryptorCache.get(mapKey, k -> {
			try {
				RSABinaryEncryptor encryptor = new RSABinaryEncryptor(transformation);
				encryptor.setPrivateKey(RSAKeyPair.fromBase64String( null, protectedKey).getPrivateKey());
				encryptor.initialize();
				return encryptor;
			} catch (InvalidKeySpecException e) {
				throw new IllegalArgumentException("无效的 RSA 私钥", e);
			}
		});
    }

	/**
	 * 获取并缓存文本解密器（使用私钥）。
	 *
	 * @param protectedKey Base64 编码的 PKCS#8 格式私钥字符串
	 * @return 二进制解密器
	 * @since 1.0.0
	 */
	@Override
	public TextEncryptor getTextDecryptor(String protectedKey) {
		Assert.hasText(protectedKey, "protectedKey 不可为空");

		String mapKey = DigestUtils.sha256Hex(protectedKey);
		return textDecryptEncryptorCache.get(mapKey, k -> {
			try {
				RSABinaryEncryptor binaryEncryptor = binaryDecryptEncryptorCache.getIfPresent(protectedKey);
				if (Objects.nonNull(binaryEncryptor)) {
					return new RSATextEncryptor(binaryEncryptor);
				}

				RSATextEncryptor encryptor = new RSATextEncryptor(transformation);
				encryptor.setPrivateKey(RSAKeyPair.fromBase64String( null, protectedKey).getPrivateKey());
				encryptor.initialize();
				return encryptor;
			} catch (InvalidKeySpecException e) {
				throw new IllegalArgumentException("无效的 RSA 私钥", e);
			}
		});
	}

    /**
     * 获取并缓存整型数字解密器（使用私钥）。
     *
     * @param protectedKey Base64 编码的 PKCS#8 格式私钥字符串
     * @return 整型数字解密器
     * @since 1.0.0
     */
    @Override
    public IntegerNumberEncryptor getIntegerNumberDecryptor(String protectedKey) {
		Assert.hasText(protectedKey, "protectedKey 不可为空");

		String mapKey = DigestUtils.sha256Hex(protectedKey);
		return integerDecryptEncryptorCache.get(mapKey, k -> {
			try {
				RSABinaryEncryptor binaryEncryptor = binaryDecryptEncryptorCache.getIfPresent(protectedKey);
				if (Objects.nonNull(binaryEncryptor)) {
					return new RSAIntegerNumberEncryptor(binaryEncryptor);
				}

				RSAIntegerNumberEncryptor encryptor = new RSAIntegerNumberEncryptor(transformation);
				encryptor.setPrivateKey(RSAKeyPair.fromBase64String( null, protectedKey).getPrivateKey());
				encryptor.initialize();
				return encryptor;
			} catch (InvalidKeySpecException e) {
				throw new IllegalArgumentException("无效的 RSA 私钥", e);
			}
		});
    }

    /**
     * 获取并缓存高精度小数解密器（使用私钥）。
     *
     * @param protectedKey Base64 编码的 PKCS#8 格式私钥字符串
     * @return 高精度小数解密器
     * @since 1.0.0
     */
    @Override
    public DecimalNumberEncryptor getDecimalNumberDecryptor(String protectedKey) {
		Assert.hasText(protectedKey, "protectedKey 不可为空");

		String mapKey = DigestUtils.sha256Hex(protectedKey);
		return decimalDecryptEncryptorCache.get(mapKey, k -> {
			try {
				RSABinaryEncryptor binaryEncryptor = binaryDecryptEncryptorCache.getIfPresent(protectedKey);
				if (Objects.nonNull(binaryEncryptor)) {
					return new RSADecimalNumberEncryptor(binaryEncryptor);
				}

				RSADecimalNumberEncryptor encryptor = new RSADecimalNumberEncryptor(transformation);
				encryptor.setPrivateKey(RSAKeyPair.fromBase64String(null, protectedKey).getPrivateKey());
				encryptor.initialize();
				return encryptor;
			} catch (InvalidKeySpecException e) {
				throw new IllegalArgumentException("无效的 RSA 私钥", e);
			}
		});
	}
}
