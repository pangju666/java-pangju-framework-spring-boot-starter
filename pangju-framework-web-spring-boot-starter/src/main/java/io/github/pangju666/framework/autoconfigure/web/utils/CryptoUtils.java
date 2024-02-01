package io.github.pangju666.framework.autoconfigure.web.utils;

import io.github.pangju666.commons.codec.utils.AesUtils;
import io.github.pangju666.commons.codec.utils.RsaUtils;
import io.github.pangju666.framework.autoconfigure.web.enums.Algorithm;
import io.github.pangju666.framework.autoconfigure.web.enums.Encoding;
import io.github.pangju666.framework.core.lang.pool.ConstantPool;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class CryptoUtils {
	protected CryptoUtils() {
	}

	public static String encryptToString(byte[] rawContent, String key, Algorithm algorithm, Encoding encoding, String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		if (algorithm == Algorithm.BASE64) {
			return Base64.encodeBase64String(rawContent);
		} else if (algorithm == Algorithm.HEX) {
			return Hex.encodeHexString(rawContent);
		}
		byte[] result = encrypt(rawContent, key, algorithm, transformation);
		return switch (encoding) {
			case BASE64 -> Base64.encodeBase64String(result);
			case HEX -> Hex.encodeHexString(result);
		};
	}

	public static byte[] encrypt(byte[] rawContent, String key, Algorithm algorithm, Encoding encoding, String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		byte[] result = encrypt(rawContent, key, algorithm, transformation);
		return switch (encoding) {
			case BASE64 -> Base64.encodeBase64(result);
			case HEX -> Hex.encodeHexString(result).getBytes();
		};
	}

	public static String decryptToString(String rawContent, String key, Algorithm algorithm, Encoding encoding, String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, DecoderException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		byte[] result = decrypt(rawContent, key, algorithm, encoding, transformation);
		return new String(result);
	}

	public static byte[] decrypt(String rawContent, String key, Algorithm algorithm, Encoding encoding, String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, DecoderException, InvalidAlgorithmParameterException {
		if (StringUtils.isBlank(rawContent)) {
			return rawContent.getBytes();
		}
		return switch (algorithm) {
			case BASE64 -> Base64.decodeBase64(rawContent);
			case HEX -> Hex.decodeHex(rawContent);
			case RSA_PRIVATE -> {
				KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(key));
				PrivateKey privateKey = RsaUtils.getKeyFactory().generatePrivate(keySpec);
				if (StringUtils.isNotBlank(transformation)) {
					yield RsaUtils.decrypt(decode(rawContent, encoding), privateKey, transformation);
				}
				yield RsaUtils.decrypt(decode(rawContent, encoding), privateKey);
			}
			case RSA_PUBLIC -> {
				KeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(key));
				PublicKey publicKey = RsaUtils.getKeyFactory().generatePublic(keySpec);
				if (StringUtils.isNotBlank(transformation)) {
					yield RsaUtils.decrypt(decode(rawContent, encoding), publicKey, transformation);
				}
				yield RsaUtils.decrypt(decode(rawContent, encoding), publicKey);
			}
			case AES -> {
				SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), AesUtils.ALGORITHM);
				if (StringUtils.isNotBlank(transformation)) {
					yield AesUtils.decrypt(decode(rawContent, encoding), keySpec, transformation);
				}
				yield AesUtils.decrypt(decode(rawContent, encoding), keySpec, ConstantPool.DEFAULT_AES_TRANSFORMATION);
			}
		};
	}

	private static byte[] encrypt(byte[] rawContent, String key, Algorithm algorithm, String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		return switch (algorithm) {
			case BASE64 -> Base64.encodeBase64(rawContent);
			case HEX -> Hex.encodeHexString(rawContent).getBytes();
			case RSA_PRIVATE -> {
				KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(key));
				PrivateKey privateKey = RsaUtils.getKeyFactory().generatePrivate(keySpec);
				if (StringUtils.isNotBlank(transformation)) {
					yield RsaUtils.encrypt(rawContent, privateKey, transformation);
				}
				yield RsaUtils.encrypt(rawContent, privateKey);
			}
			case RSA_PUBLIC -> {
				KeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(key));
				PublicKey publicKey = RsaUtils.getKeyFactory().generatePublic(keySpec);
				if (StringUtils.isNotBlank(transformation)) {
					yield RsaUtils.encrypt(rawContent, publicKey, transformation);
				}
				yield RsaUtils.encrypt(rawContent, publicKey);
			}
			case AES -> {
				SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), AesUtils.ALGORITHM);
				if (StringUtils.isNotBlank(transformation)) {
					yield AesUtils.encrypt(rawContent, keySpec, transformation);
				}
				yield AesUtils.encrypt(rawContent, keySpec, ConstantPool.DEFAULT_AES_TRANSFORMATION);
			}
		};
	}

	private static byte[] decode(String content, Encoding encoding) throws DecoderException {
		return switch (encoding) {
			case BASE64 -> Base64.decodeBase64(content);
			case HEX -> Hex.decodeHex(content);
		};
	}
}
