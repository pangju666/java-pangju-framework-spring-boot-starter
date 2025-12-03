package io.github.pangju666.framework.boot.autoconfigure.jackson


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.pangju666.framework.boot.autoconfigure.crypto.CryptoAutoConfiguration
import io.github.pangju666.framework.boot.crypto.enums.CryptoAlgorithm
import io.github.pangju666.framework.boot.crypto.enums.Encoding
import io.github.pangju666.framework.boot.crypto.factory.impl.AES256CryptoFactory
import io.github.pangju666.framework.boot.crypto.factory.impl.StrongCryptoFactory
import io.github.pangju666.framework.boot.crypto.utils.CryptoUtils
import io.github.pangju666.framework.boot.jackson.annotation.EncryptFormat
import io.github.pangju666.framework.boot.jackson.utils.CryptoFactoryRegistry
import io.github.pangju666.framework.boot.spring.StaticSpringContext

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = [JacksonAutoConfiguration.class, CryptoAutoConfiguration.class], loader = SpringBootContextLoader.class)
class EncryptFormatSpec extends Specification {
	@Autowired
	ObjectMapper mapper

	static class Dto {
		@EncryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256, encoding = Encoding.BASE64)
		String textBase64

		@EncryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256, encoding = Encoding.HEX)
		String textHex

		@EncryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256)
		byte[] bytesVal

		@EncryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256)
		BigInteger bigIntegerVal

		@EncryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256)
		BigDecimal bigDecimalVal

		@EncryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256, encoding = Encoding.BASE64)
		List<String> listText

		@EncryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256)
		Set<BigInteger> setBigInt

		@EncryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256)
		Map<String, Object> mapMixed

		@EncryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256)
		String blankText

		@EncryptFormat(key = "pwd", algorithm = CryptoAlgorithm.AES256)
		String nullText

		@EncryptFormat(key = "\${crypto.key}", algorithm = CryptoAlgorithm.AES256, encoding = Encoding.BASE64)
		String placeholderText

		@EncryptFormat(key = "\${missing.key}", algorithm = CryptoAlgorithm.AES256, encoding = Encoding.BASE64)
		String placeholderMissing

		@EncryptFormat(key = "pwd", factory = [StrongCryptoFactory], encoding = Encoding.BASE64)
		String strongText
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

	def "字符串加密-BASE64可解密"() {
		given:
		def dto = new Dto(textBase64: "hello")
		when:
		def json = mapper.writeValueAsString(dto)
		def masked = ((ObjectNode) mapper.readTree(json)).get("textBase64").asText()
		def plain = CryptoUtils.decryptString(new AES256CryptoFactory(16), masked, "pwd", Encoding.BASE64)
		then:
		plain == "hello"
	}

	def "字符串加密-HEX可解密"() {
		given:
		def dto = new Dto(textHex: "hello")
		when:
		def masked = ((ObjectNode) mapper.readTree(mapper.writeValueAsString(dto))).get("textHex").asText()
		def plain = CryptoUtils.decryptString(new AES256CryptoFactory(16), masked, "pwd", Encoding.HEX)
		then:
		plain == "hello"
	}

	def "字节数组加密可解密"() {
		given:
		def src = "hello".bytes
		def dto = new Dto(bytesVal: src)
		when:
		def node = (ObjectNode) mapper.readTree(mapper.writeValueAsString(dto))
		def masked = node.get("bytesVal").binaryValue()
		def plain = CryptoUtils.decrypt(new AES256CryptoFactory(16), masked, "pwd")
		then:
		new String(plain) == "hello"
	}

	def "BigInteger加密可解密"() {
		given:
		def bi = new BigInteger("12345678901234567890")
		def dto = new Dto(bigIntegerVal: bi)
		when:
		def masked = new BigInteger(((ObjectNode) mapper.readTree(mapper.writeValueAsString(dto))).get("bigIntegerVal").asText())
		def plain = CryptoUtils.decryptBigInteger(new AES256CryptoFactory(16), masked, "pwd")
		then:
		plain == bi
	}

	def "BigDecimal加密可解密"() {
		given:
		def bd = new BigDecimal("12345.6789")
		def dto = new Dto(bigDecimalVal: bd)
		when:
		def masked = new BigDecimal(((ObjectNode) mapper.readTree(mapper.writeValueAsString(dto))).get("bigDecimalVal").asText())
		def plain = CryptoUtils.decryptBigDecimal(new AES256CryptoFactory(16), masked, "pwd")
		then:
		plain == bd
	}

	def "集合加密可解密"() {
		given:
		def dto = new Dto(
			listText: ["a", "b", "c"],
			setBigInt: [new BigInteger("1"), new BigInteger("2")] as Set
		)
		when:
		def node = (ObjectNode) mapper.readTree(mapper.writeValueAsString(dto))
		def l0 = node.get("listText").get(0).asText()
		def l1 = node.get("listText").get(1).asText()
		def l2 = node.get("listText").get(2).asText()
		def s0 = new BigInteger(node.get("setBigInt").get(0).asText())
		def s1 = new BigInteger(node.get("setBigInt").get(1).asText())
		then:
		CryptoUtils.decryptString(new AES256CryptoFactory(16), l0, "pwd", Encoding.BASE64) == "a"
		CryptoUtils.decryptString(new AES256CryptoFactory(16), l1, "pwd", Encoding.BASE64) == "b"
		CryptoUtils.decryptString(new AES256CryptoFactory(16), l2, "pwd", Encoding.BASE64) == "c"
		CryptoUtils.decryptBigInteger(new AES256CryptoFactory(16), s0, "pwd") == new BigInteger("1")
		CryptoUtils.decryptBigInteger(new AES256CryptoFactory(16), s1, "pwd") == new BigInteger("2")
	}

	def "映射加密可解密"() {
		given:
		def dto = new Dto(mapMixed: [
			s : "xyz",
			b : "xyz".bytes,
			bi: new BigInteger("3"),
			bd: new BigDecimal("7.89")
		])

		when:
		def node = (ObjectNode) mapper.readTree(mapper.writeValueAsString(dto)).get("mapMixed")

		then:
		CryptoUtils.decryptString(new AES256CryptoFactory(16), node.get("s").asText(), "pwd", Encoding.BASE64) == "xyz"

		def bCipher = node.get("b").binaryValue()
		new String(CryptoUtils.decrypt(new AES256CryptoFactory(16), bCipher, "pwd")) == "xyz"

		def biCipher = mapper.treeToValue(node.get("bi"), BigInteger.class)
		CryptoUtils.decryptBigInteger(new AES256CryptoFactory(16), biCipher, "pwd") == new BigInteger("3")

		def bdCipherText = node.get("bd").asText()
		def bdCipher = new BigDecimal(bdCipherText)
		def bdPlain = CryptoUtils.decryptBigDecimal(new AES256CryptoFactory(16), bdCipher, "pwd")
		bdPlain == new BigDecimal("7.89")
	}

	def "空白字符串原样输出"() {
		given:
		def dto = new Dto(blankText: "   ")
		when:
		def masked = ((ObjectNode) mapper.readTree(mapper.writeValueAsString(dto))).get("blankText").asText()
		then:
		masked == "   "
	}

	def "null值输出JSON null"() {
		given:
		def dto = new Dto(nullText: null)
		when:
		def node = mapper.readTree(mapper.writeValueAsString(dto))
		then:
		node.get("nullText").isNull()
	}

	def "占位符密钥解析成功"() {
		given:
		def dto = new Dto(placeholderText: "hello")
		when:
		def masked = ((ObjectNode) mapper.readTree(mapper.writeValueAsString(dto))).get("placeholderText").asText()
		def plain = CryptoUtils.decryptString(new AES256CryptoFactory(16), masked, "pwd", Encoding.BASE64)
		then:
		plain == "hello"
	}

	def "占位符密钥解析失败返回null"() {
		given:
		def dto = new Dto(placeholderMissing: "hello")
		when:
		def node = mapper.readTree(mapper.writeValueAsString(dto))
		then:
		node.get("placeholderMissing").isNull()
	}

	def "自定义工厂优先：Strong工厂加密可解密"() {
		given:
		def dto = new Dto(strongText: "hello")
		when:
		def masked = ((ObjectNode) mapper.readTree(mapper.writeValueAsString(dto))).get("strongText").asText()
		def plain = CryptoUtils.decryptString(new StrongCryptoFactory(16), masked, "pwd", Encoding.BASE64)
		then:
		plain == "hello"
	}
}
