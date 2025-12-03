package io.github.pangju666.framework.boot.autoconfigure.crypto

import io.github.pangju666.framework.boot.crypto.factory.impl.StrongCryptoFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

@ContextConfiguration(classes = CryptoAutoConfiguration.class, loader = SpringBootContextLoader.class)
class StrongCryptoFactorySpec extends Specification {
	@Autowired
	StrongCryptoFactory factory

	def "文本加解密"() {
		when:
		def enc = factory.getTextEncryptor("pwd").encrypt("hello")
		def dec = factory.getTextDecryptor("pwd").decrypt(enc)
		then:
		dec == "hello"
	}

	def "字节加解密"() {
		given:
		def src = "hello".bytes
		when:
		def enc = factory.getBinaryEncryptor("pwd").encrypt(src)
		def dec = factory.getBinaryDecryptor("pwd").decrypt(enc)
		then:
		new String(dec) == "hello"
	}

	def "整数加解密"() {
		when:
		def enc = factory.getIntegerNumberEncryptor("pwd").encrypt(BigInteger.valueOf(123))
		def dec = factory.getIntegerNumberDecryptor("pwd").decrypt(enc)
		then:
		dec == 123
	}

	def "小数加解密"() {
		when:
		def enc = factory.getDecimalNumberEncryptor("pwd").encrypt(12.34G)
		def dec = factory.getDecimalNumberDecryptor("pwd").decrypt(enc)
		then:
		dec == 12.34G
	}

	def "同口令缓存同实例"() {
		expect:
		factory.getTextEncryptor("pwd").is(factory.getTextEncryptor("pwd"))
		factory.getBinaryEncryptor("pwd").is(factory.getBinaryEncryptor("pwd"))
		factory.getIntegerNumberEncryptor("pwd").is(factory.getIntegerNumberEncryptor("pwd"))
		factory.getDecimalNumberEncryptor("pwd").is(factory.getDecimalNumberEncryptor("pwd"))
	}

	@Unroll
	def "非法参数抛异常 - #caseName（TextEncryptor）"() {
		when:
		factory.getTextEncryptor(input)

		then:
		thrown(IllegalArgumentException)

		where:
		caseName   | input
		"空口令"    | ""
		"null口令"  | null
	}

	@Unroll
	def "非法参数抛异常 - #caseName（BinaryEncryptor）"() {
		when:
		factory.getBinaryEncryptor(input)

		then:
		thrown(IllegalArgumentException)

		where:
		caseName   | input
		"空口令"    | ""
		"null口令"  | null
	}

	@Unroll
	def "非法参数抛异常 - #caseName（IntegerNumberEncryptor）"() {
		when:
		factory.getIntegerNumberEncryptor(input)

		then:
		thrown(IllegalArgumentException)

		where:
		caseName   | input
		"空口令"    | ""
		"null口令"  | null
	}

	@Unroll
	def "非法参数抛异常 - #caseName（DecimalNumberEncryptor）"() {
		when:
		factory.getDecimalNumberEncryptor(input)

		then:
		thrown(IllegalArgumentException)

		where:
		caseName   | input
		"空口令"    | ""
		"null口令"  | null
	}
}
