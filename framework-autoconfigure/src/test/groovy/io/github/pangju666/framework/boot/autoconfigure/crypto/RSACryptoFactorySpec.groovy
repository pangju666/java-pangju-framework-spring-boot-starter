package io.github.pangju666.framework.boot.autoconfigure.crypto

import io.github.pangju666.framework.boot.crypto.factory.impl.RSACryptoFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

@ContextConfiguration(classes = CryptoAutoConfiguration.class, loader = SpringBootContextLoader.class)
class RSACryptoFactorySpec extends Specification {
	static String pubBase64
	static String priBase64

	@Autowired
	RSACryptoFactory factory

	def setupSpec() {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA")
		kpg.initialize(2048)
		KeyPair kp = kpg.generateKeyPair()
		def pubSpec = new X509EncodedKeySpec(kp.public.encoded)
		def priSpec = new PKCS8EncodedKeySpec(kp.private.encoded)
		def pubBytes = pubSpec.getEncoded()
		def priBytes = priSpec.getEncoded()
		pubBase64 = Base64.encoder.encodeToString(pubBytes)
		priBase64 = Base64.encoder.encodeToString(priBytes)
	}

	def "文本加解密"() {
		when:
		def enc = factory.getTextEncryptor(pubBase64).encrypt("hello")
		def dec = factory.getTextDecryptor(priBase64).decrypt(enc)
		then:
		dec == "hello"
	}

	def "字节加解密"() {
		given:
		def src = "hello".bytes
		when:
		def enc = factory.getBinaryEncryptor(pubBase64).encrypt(src)
		def dec = factory.getBinaryDecryptor(priBase64).decrypt(enc)
		then:
		new String(dec) == "hello"
	}

	def "整数加解密"() {
		when:
		def enc = factory.getIntegerNumberEncryptor(pubBase64).encrypt(BigInteger.valueOf(123))
		def dec = factory.getIntegerNumberDecryptor(priBase64).decrypt(enc)
		then:
		dec == 123
	}

	def "小数加解密"() {
		when:
		def enc = factory.getDecimalNumberEncryptor(pubBase64).encrypt(12.34G)
		def dec = factory.getDecimalNumberDecryptor(priBase64).decrypt(enc)
		then:
		dec == 12.34G
	}

	def "同键缓存同实例"() {
		expect:
		factory.getTextEncryptor(pubBase64).is(factory.getTextEncryptor(pubBase64))
		factory.getBinaryEncryptor(pubBase64).is(factory.getBinaryEncryptor(pubBase64))
		factory.getIntegerNumberEncryptor(pubBase64).is(factory.getIntegerNumberEncryptor(pubBase64))
		factory.getDecimalNumberEncryptor(pubBase64).is(factory.getDecimalNumberEncryptor(pubBase64))
		factory.getTextDecryptor(priBase64).is(factory.getTextDecryptor(priBase64))
		factory.getBinaryDecryptor(priBase64).is(factory.getBinaryDecryptor(priBase64))
		factory.getIntegerNumberDecryptor(priBase64).is(factory.getIntegerNumberDecryptor(priBase64))
		factory.getDecimalNumberDecryptor(priBase64).is(factory.getDecimalNumberDecryptor(priBase64))
	}

	@Unroll
	def "非法公钥抛异常 - #caseName（RSA TextEncryptor）"() {
		when:
		factory.getTextEncryptor(publicKey)

		then:
		thrown(IllegalArgumentException)

		where:
		caseName     | publicKey
		"空公钥"      | ""
		"null公钥"    | null
	}

	@Unroll
	def "非法私钥抛异常 - #caseName（RSA TextDecryptor）"() {
		when:
		factory.getTextDecryptor(privateKey)

		then:
		thrown(IllegalArgumentException)

		where:
		caseName     | privateKey
		"空私钥"      | ""
		"null私钥"    | null
	}
}
