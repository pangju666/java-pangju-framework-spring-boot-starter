package io.github.pangju666.framework.boot.autoconfigure.crypto

import io.github.pangju666.framework.boot.crypto.enums.Encoding
import io.github.pangju666.framework.boot.crypto.factory.impl.AES256CryptoFactory
import io.github.pangju666.framework.boot.crypto.utils.CryptoUtils
import io.github.pangju666.framework.boot.spring.StaticSpringContext

import org.apache.commons.codec.DecoderException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

@ContextConfiguration(classes = CryptoAutoConfiguration.class, loader = SpringBootContextLoader.class)
class CryptoUtilsSpec extends Specification {
	@Autowired
	AES256CryptoFactory factory

	def setupSpec() {
		def env = Mock(Environment)
		env.resolvePlaceholders('${crypto.key}') >> 'resolved-secret'
		env.resolvePlaceholders('${missing.key}') >> '${missing.key}'
		def f = StaticSpringContext.class.getDeclaredField('ENVIRONMENT')
		f.setAccessible(true)
		f.set(null, env)
	}

	def "加密/解密字节数组非空"() {
		given:
		def raw = "hello".bytes

		when:
		def enc = CryptoUtils.encrypt(factory, raw, 'pwd')
		def dec = CryptoUtils.decrypt(factory, enc, 'pwd')

		then:
		new String(dec) == 'hello'
	}

	def "加密/解密字节数组为空直接返回原值"() {
		given:
		byte[] empty = new byte[0]

		expect:
		CryptoUtils.encrypt(factory, empty, 'pwd').is(empty)
		CryptoUtils.decrypt(factory, empty, 'pwd').is(empty)
	}

	def "加密字符串-Base64与解密"() {
		when:
		def enc = CryptoUtils.encryptString(factory, 'hello', 'pwd', Encoding.BASE64)
		def dec = CryptoUtils.decryptString(factory, enc, 'pwd', Encoding.BASE64)

		then:
		dec == 'hello'
	}

	def "加密字符串-Hex与解密"() {
		when:
		def enc = CryptoUtils.encryptString(factory, 'hello', 'pwd', Encoding.HEX)
		def dec = CryptoUtils.decryptString(factory, enc, 'pwd', Encoding.HEX)

		then:
		dec == 'hello'
	}

	def "解密字符串-Hex非法抛出DecoderException"() {
		when:
		CryptoUtils.decryptString(factory, 'zz', 'pwd', Encoding.HEX)

		then:
		thrown(DecoderException)
	}

	def "加密/解密字符串为空白原样返回"() {
		expect:
		CryptoUtils.encryptString(factory, '', 'pwd', Encoding.BASE64) == ''
		CryptoUtils.decryptString(factory, '', 'pwd', Encoding.BASE64) == ''
		CryptoUtils.encryptString(factory, '   ', 'pwd', Encoding.HEX) == '   '
		CryptoUtils.decryptString(factory, '   ', 'pwd', Encoding.HEX) == '   '
	}

	def "加密/解密BigInteger非空"() {
		given:
		def bi = new BigInteger('12345678901234567890')

		when:
		def enc = CryptoUtils.encryptBigInteger(factory, bi, 'pwd')
		def dec = CryptoUtils.decryptBigInteger(factory, enc, 'pwd')

		then:
		dec == bi
	}

	def "加密/解密BigInteger为空返回null"() {
		expect:
		CryptoUtils.encryptBigInteger(factory, null, 'pwd') == null
		CryptoUtils.decryptBigInteger(factory, null, 'pwd') == null
	}

	def "加密/解密BigDecimal非空"() {
		given:
		def bd = new BigDecimal('12345.6789')

		when:
		def enc = CryptoUtils.encryptBigDecimal(factory, bd, 'pwd')
		def dec = CryptoUtils.decryptBigDecimal(factory, enc, 'pwd')

		then:
		dec == bd
	}

	def "加密/解密BigDecimal为空返回null"() {
		expect:
		CryptoUtils.encryptBigDecimal(factory, null, 'pwd') == null
		CryptoUtils.decryptBigDecimal(factory, null, 'pwd') == null
	}

	def "getKey明文密钥原样返回"() {
		expect:
		CryptoUtils.getKey('plain-secret') == 'plain-secret'
	}

	def "getKey空密钥抛出IllegalArgumentException"() {
		when:
		CryptoUtils.getKey('')

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message.contains('密钥属性为空')
	}

	def "getKey占位符解析成功返回解析结果"() {
		expect:
		CryptoUtils.getKey('${crypto.key}') == 'resolved-secret'
	}

	def "getKey占位符未解析到值抛出IllegalArgumentException"() {
		when:
		CryptoUtils.getKey('${missing.key}')

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message.contains('未找到密钥，属性：${missing.key}')
	}

	@Unroll
	def "encryptString空白输入原样返回 - '#input'"(String input) {
		expect:
		CryptoUtils.encryptString(factory, input, 'pwd', Encoding.BASE64) == input
		CryptoUtils.encryptString(factory, input, 'pwd', Encoding.HEX) == input

		where:
		input << ['', '   ']
	}

	@Unroll
	def "decryptString空白输入原样返回 - '#input'"(String input) {
		expect:
		CryptoUtils.decryptString(factory, input, 'pwd', Encoding.BASE64) == input
		CryptoUtils.decryptString(factory, input, 'pwd', Encoding.HEX) == input

		where:
		input << ['', '   ']
	}
}
