package io.github.pangju666.framework.autoconfigure.web.utils;

import io.github.pangju666.commons.crypto.encryption.binary.RSABinaryEncryptor;
import io.github.pangju666.commons.crypto.key.RSAKey;
import io.github.pangju666.framework.autoconfigure.web.enums.Algorithm;
import io.github.pangju666.framework.autoconfigure.web.enums.Encoding;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.binary.AES256BinaryEncryptor;

import java.security.spec.InvalidKeySpecException;

public class CryptoUtils {
	protected CryptoUtils() {
	}

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

	public static byte[] encrypt(byte[] rawContent, String key, Algorithm algorithm, Encoding encoding) throws InvalidKeySpecException {
		byte[] result = encrypt(rawContent, key, algorithm);
		return switch (encoding) {
			case BASE64 -> Base64.encodeBase64(result);
			case HEX -> Hex.encodeHexString(result).getBytes();
		};
	}

	public static String decryptToString(String rawContent, String key, Algorithm algorithm, Encoding encoding) throws DecoderException, InvalidKeySpecException {
		byte[] result = decrypt(rawContent, key, algorithm, encoding);
		return new String(result);
	}

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

	private static byte[] decode(String content, Encoding encoding) throws DecoderException {
		return switch (encoding) {
			case BASE64 -> Base64.decodeBase64(content);
			case HEX -> Hex.decodeHex(content);
		};
	}
}