package io.github.pangju666.framework.boot.autoconfigure.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pangju666.framework.boot.autoconfigure.crypto.CryptoAutoConfiguration
import io.github.pangju666.framework.boot.crypto.enums.CryptoAlgorithm
import io.github.pangju666.framework.boot.crypto.enums.Encoding
import io.github.pangju666.framework.boot.crypto.factory.impl.AES256CryptoFactory
import io.github.pangju666.framework.boot.crypto.factory.impl.StrongCryptoFactory
import io.github.pangju666.framework.boot.crypto.utils.CryptoUtils
import io.github.pangju666.framework.boot.jackson.annotation.DecryptFormat
import io.github.pangju666.framework.boot.jackson.utils.CryptoFactoryRegistry
import io.github.pangju666.framework.boot.spring.StaticSpringContext

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = [JacksonAutoConfiguration.class, CryptoAutoConfiguration.class], loader = SpringBootContextLoader.class)
class DecryptFormatSpec extends Specification {
	@Autowired
	ObjectMapper mapper

	static class Dto {
		@DecryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256, encoding = Encoding.BASE64)
		String textBase64

		@DecryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256, encoding = Encoding.HEX)
		String textHex

		@DecryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256)
		byte[] bytesVal

		@DecryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256)
		BigInteger bigIntegerVal

		@DecryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256)
		BigDecimal bigDecimalVal

		@DecryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256, encoding = Encoding.BASE64)
		List<String> listText

		@DecryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256)
		Set<BigInteger> setBigInt

		@DecryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256, encoding = Encoding.BASE64)
		Map<String, String> mapString

		@DecryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256)
		Map<String, byte[]> mapBytes

		@DecryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256)
		Map<String, BigInteger> mapBigInt

		@DecryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256)
		Map<String, BigDecimal> mapBigDec

		@DecryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256, encoding = Encoding.HEX)
		String invalidHex

		@DecryptFormat(key = "\${crypto.key}", algorithm = CryptoAlgorithm.AES256, encoding = Encoding.BASE64)
		String placeholderOk

		@DecryptFormat(key = "\${missing.key}", algorithm = CryptoAlgorithm.AES256, encoding = Encoding.BASE64)
		String placeholderMissing

		@DecryptFormat(key = "pwd", factory = [StrongCryptoFactory], encoding = Encoding.BASE64)
		String strongText

		@DecryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256, encoding = Encoding.BASE64)
		String nullText
	}

	def setupSpec() {
		def env = Mock(Environment)
		env.resolvePlaceholders('${crypto.key}') >> 'pwd'
		env.resolvePlaceholders('${missing.key}') >> '${missing.key}'
		def fe = StaticSpringContext.class.getDeclaredField('ENVIRONMENT')
		fe.setAccessible(true)
		fe.set(null, env)
		def fm = CryptoFactoryRegistry.class.getDeclaredField('CRYPTO_FACTORY_MAP')
		fm.setAccessible(true)
		((Map) fm.get(null)).clear()
	}

	def "字符串解密-BASE64"() {
		given:
		def enc = CryptoUtils.encryptString(new AES256CryptoFactory(16), 'hello', 'pwd', Encoding.BASE64)
		def json = mapper.writeValueAsString([textBase64: enc])

		when:
		def dto = mapper.readValue(json, Dto)

		then:
		dto.textBase64 == 'hello'
	}

	def "字符串解密-HEX"() {
		given:
		def enc = CryptoUtils.encryptString(new AES256CryptoFactory(16), 'hello', 'pwd', Encoding.HEX)
		def json = mapper.writeValueAsString([textHex: enc])

		when:
		def dto = mapper.readValue(json, Dto)

		then:
		dto.textHex == 'hello'
	}

	def "字节数组解密"() {
		given:
		def enc = CryptoUtils.encrypt(new AES256CryptoFactory(16), 'hello'.bytes, 'pwd')
		def json = mapper.writeValueAsString([bytesVal: enc])

		when:
		def dto = mapper.readValue(json, Dto)

		then:
		new String(dto.bytesVal) == 'hello'
	}

	def "BigInteger解密"() {
		given:
		def bi = new BigInteger('12345678901234567890')
		def enc = CryptoUtils.encryptBigInteger(new AES256CryptoFactory(16), bi, 'pwd')
		def json = mapper.writeValueAsString([bigIntegerVal: enc])

		when:
		def dto = mapper.readValue(json, Dto)

		then:
		dto.bigIntegerVal == bi
	}

	def "BigDecimal解密"() {
		given:
		def bd = new BigDecimal('12345.6789')
		def enc = CryptoUtils.encryptBigDecimal(new AES256CryptoFactory(16), bd, 'pwd')
		def json = mapper.writeValueAsString([bigDecimalVal: enc])

		when:
		def dto = mapper.readValue(json, Dto)

		then:
		dto.bigDecimalVal.compareTo(bd) == 0
	}

	def "列表与集合解密"() {
		given:
		def l0 = CryptoUtils.encryptString(new AES256CryptoFactory(16), 'a', 'pwd', Encoding.BASE64)
		def l1 = null
		def l2 = CryptoUtils.encryptString(new AES256CryptoFactory(16), 'c', 'pwd', Encoding.BASE64)
		def s0 = CryptoUtils.encryptBigInteger(new AES256CryptoFactory(16), new BigInteger('1'), 'pwd')
		def s1 = CryptoUtils.encryptBigInteger(new AES256CryptoFactory(16), new BigInteger('2'), 'pwd')
		def json = mapper.writeValueAsString([listText: [l0, l1, l2], setBigInt: [s0, s1] as Set])

		when:
		def dto = mapper.readValue(json, Dto)

		then:
		dto.listText[0] == 'a'
		dto.listText[1] == null
		dto.listText[2] == 'c'
		dto.setBigInt.containsAll([new BigInteger('1'), new BigInteger('2')])
	}

	def "映射解密-字符串/字节/大整数/小数"() {
		given:
		String s = CryptoUtils.encryptString(new AES256CryptoFactory(16), 'xyz', 'pwd', Encoding.BASE64)
		byte[] b = CryptoUtils.encrypt(new AES256CryptoFactory(16), 'xyz'.bytes, 'pwd')
		BigInteger bi = CryptoUtils.encryptBigInteger(new AES256CryptoFactory(16), new BigInteger('3'), 'pwd')
		BigDecimal bd = CryptoUtils.encryptBigDecimal(new AES256CryptoFactory(16), new BigDecimal('7.89'), 'pwd')
		def json = mapper.writeValueAsString([
			mapString: [a: s, b: s],
			mapBytes : [x: b, y: b],
			mapBigInt: [i: bi.toString(), j: bi.toString()],
			mapBigDec: [p: bd.toPlainString(), q: bd.toPlainString()]
		])

		when:
		Dto dto = mapper.readValue(json, Dto)

		then:
		dto.mapString.values().every { it == 'xyz' }
		dto.mapBytes.values().every { new String(it) == 'xyz' }
		dto.mapBigInt.values().every { it == new BigInteger('3') }
		dto.mapBigDec.values().every { (it == new BigDecimal('7.89')) }
	}

	def "非法HEX输入返回null"() {
		given:
		def json = mapper.writeValueAsString([invalidHex: 'zz'])

		when:
		def dto = mapper.readValue(json, Dto)

		then:
		dto.invalidHex == null
	}

	def "占位符密钥解析成功"() {
		given:
		def enc = CryptoUtils.encryptString(new AES256CryptoFactory(16), 'hello', 'pwd', Encoding.BASE64)
		def json = mapper.writeValueAsString([placeholderOk: enc])

		when:
		def dto = mapper.readValue(json, Dto)

		then:
		dto.placeholderOk == 'hello'
	}

	def "占位符密钥解析失败输出null"() {
		given:
		def enc = CryptoUtils.encryptString(new AES256CryptoFactory(16), 'hello', 'pwd', Encoding.BASE64)
		def json = mapper.writeValueAsString([placeholderMissing: enc])

		when:
		def dto = mapper.readValue(json, Dto)

		then:
		dto.placeholderMissing == null
	}

	def "自定义工厂优先-Strong工厂"() {
		given:
		def enc = CryptoUtils.encryptString(new StrongCryptoFactory(16), 'hello', 'pwd', Encoding.BASE64)
		def json = mapper.writeValueAsString([strongText: enc])

		when:
		def dto = mapper.readValue(json, Dto)

		then:
		dto.strongText == 'hello'
	}

	def "null值反序列化为null"() {
		given:
		def json = mapper.writeValueAsString([nullText: null])

		when:
		def dto = mapper.readValue(json, Dto)

		then:
		dto.nullText == null
	}
}
