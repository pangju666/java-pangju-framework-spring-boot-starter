package io.github.pangju666.framework.boot.autoconfigure.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.pangju666.framework.boot.jackson.annotation.DesensitizeFormat
import io.github.pangju666.framework.boot.jackson.enums.DesensitizedType

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = JacksonAutoConfiguration.class, loader = SpringBootContextLoader.class)
class DesensitizeFormatSpec extends Specification {
	@Autowired
	ObjectMapper mapper

	static class UserDto {
		@DesensitizeFormat(type = DesensitizedType.EMAIL)
		String email

		@DesensitizeFormat(type = DesensitizedType.PHONE_NUMBER)
		String phone

		@DesensitizeFormat(type = DesensitizedType.CUSTOM, prefix = 2, suffix = 2)
		String customKeepBoth

		@DesensitizeFormat(type = DesensitizedType.CUSTOM, prefix = -1, suffix = 3)
		String customKeepRight

		@DesensitizeFormat(type = DesensitizedType.CUSTOM, prefix = 3, suffix = -1)
		String customKeepLeft

		@DesensitizeFormat(type = DesensitizedType.CUSTOM, prefix = -1, suffix = -1)
		String customHideAll

		@DesensitizeFormat(type = DesensitizedType.PASSWORD)
		String blankText

		@DesensitizeFormat(type = DesensitizedType.PASSWORD)
		String nullText

		@DesensitizeFormat(type = DesensitizedType.PASSWORD)
		CharSequence usernameAsCharSeq

		// 非字符串字段标注：应走默认序列化器，不作脱敏
		@DesensitizeFormat(type = DesensitizedType.PASSWORD)
		BigInteger bigInteger
	}

	def "EMAIL 内置类型：保留域名部分且内容被改变"() {
		given:
		def dto = new UserDto(email: "user.name@example.com")
		when:
		def json = mapper.writeValueAsString(dto)
		def node = (ObjectNode) mapper.readTree(json)
		def masked = node.get("email").asText()
		then:
		masked != dto.email
		masked.contains("@")
		masked.endsWith("@example.com")
	}

	def "PHONE_NUMBER 内置类型：长度不变，前3后2保留"() {
		given:
		def dto = new UserDto(phone: "13812345678")
		when:
		def masked = mapper.readTree(mapper.writeValueAsString(dto)).get("phone").asText()
		then:
		masked.length() == dto.phone.length()
		masked.startsWith(dto.phone.substring(0, 3))
		masked.endsWith(dto.phone.substring(dto.phone.length() - 2))
		masked != dto.phone
	}

	def "CUSTOM 前后缀保留：prefix=2,suffix=2 保留两端且长度一致"() {
		given:
		def dto = new UserDto(customKeepBoth: "abcdef")
		when:
		def masked = mapper.readTree(mapper.writeValueAsString(dto)).get("customKeepBoth").asText()
		then:
		masked.length() == dto.customKeepBoth.length()
		masked.startsWith("ab")
		masked.endsWith("ef")
		masked != dto.customKeepBoth
	}

	def "CUSTOM 仅保留右侧：prefix=-1,suffix=3 保留末尾三位"() {
		given:
		def dto = new UserDto(customKeepRight: "abcdefghi")
		when:
		def masked = mapper.readTree(mapper.writeValueAsString(dto)).get("customKeepRight").asText()
		then:
		masked.length() == dto.customKeepRight.length()
		masked.endsWith("ghi")
		masked != dto.customKeepRight
	}

	def "CUSTOM 仅保留左侧：prefix=3,suffix=-1 保留开头三位"() {
		given:
		def dto = new UserDto(customKeepLeft: "abcdefghi")
		when:
		def masked = mapper.readTree(mapper.writeValueAsString(dto)).get("customKeepLeft").asText()
		then:
		masked.length() == dto.customKeepLeft.length()
		masked.startsWith("abc")
		masked != dto.customKeepLeft
	}

	def "CUSTOM 全隐藏：prefix=-1,suffix=-1 不保留任意位置"() {
		given:
		def dto = new UserDto(customHideAll: "topsecret")
		when:
		def masked = mapper.readTree(mapper.writeValueAsString(dto)).get("customHideAll").asText()
		then:
		masked != dto.customHideAll
		masked.trim().length() > 0
	}

	def "空白字符串按原样输出（不触发转换器）"() {
		given:
		def dto = new UserDto(blankText: "   ")
		when:
		def masked = mapper.readTree(mapper.writeValueAsString(dto)).get("blankText").asText()
		then:
		masked == "   "
	}

	def "null 字段序列化为 JSON null"() {
		given:
		def dto = new UserDto(nullText: null)
		when:
		def node = mapper.readTree(mapper.writeValueAsString(dto))
		then:
		node.get("nullText").isNull()
	}

	def "CharSequence 也参与脱敏（同 String）"() {
		given:
		def dto = new UserDto(usernameAsCharSeq: "pangju666")
		when:
		def masked = mapper.readTree(mapper.writeValueAsString(dto)).get("usernameAsCharSeq").asText()
		then:
		masked != "pangju666"
	}

	def "非字符串字段标注：走默认序列化器，不脱敏"() {
		given:
		def val = new BigInteger("123456789012345678901234567890")
		def dto = new UserDto(bigInteger: val)
		when:
		def node = mapper.readTree(mapper.writeValueAsString(dto))
		then:
		node.get("bigInteger").asText() == val.toString()
	}

	def "未标注字段不受影响（控制样例）"() {
		given:
		def dto = new PlainDto(content: "HelloWorld")
		when:
		def node = mapper.readTree(mapper.writeValueAsString(dto))
		then:
		node.get("content").asText() == "HelloWorld"
	}

	class PlainDto {
		String content
	}
}
