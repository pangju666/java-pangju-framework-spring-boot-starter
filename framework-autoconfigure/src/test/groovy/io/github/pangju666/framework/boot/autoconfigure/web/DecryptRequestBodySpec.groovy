package io.github.pangju666.framework.boot.autoconfigure.web

import io.github.pangju666.framework.boot.autoconfigure.web.crypto.RequestBodyDecryptAdvice
import io.github.pangju666.framework.boot.crypto.enums.Encoding
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory
import io.github.pangju666.framework.boot.web.annotation.DecryptRequestBody
import io.github.pangju666.framework.boot.web.exception.RequestDataDecryptFailureException
import io.github.pangju666.framework.web.exception.base.ServerException
import io.github.pangju666.framework.web.exception.base.ValidationException
import org.apache.commons.codec.binary.Base64
import org.jasypt.exceptions.EncryptionOperationNotPossibleException
import org.jasypt.util.binary.BinaryEncryptor
import org.jasypt.util.numeric.DecimalNumberEncryptor
import org.jasypt.util.numeric.IntegerNumberEncryptor
import org.jasypt.util.text.TextEncryptor
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpInputMessage
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class DecryptRequestBodySpec extends Specification {
	static class SimpleInputMessage implements HttpInputMessage {
		final HttpHeaders headers = new HttpHeaders()
		final InputStream body
		SimpleInputMessage(String content) { this.body = new ByteArrayInputStream(content?.bytes ?: new byte[0]) }
		@Override HttpHeaders getHeaders() { headers }
		@Override InputStream getBody() { body }
	}

	static class TestBinaryEncryptor implements BinaryEncryptor {
		@Override
		byte[] encrypt(byte[] bytes) { throw new UnsupportedOperationException() }
		@Override
		byte[] decrypt(byte[] bytes) {
			def s = new String(bytes, StandardCharsets.UTF_8)
			return (s.startsWith("X-") ? s.substring(2) : s).getBytes(StandardCharsets.UTF_8)
		}
	}

	static class TestCryptoFactory implements CryptoFactory {
		@Override BinaryEncryptor getBinaryEncryptor(String key) { throw new UnsupportedOperationException() }
		@Override BinaryEncryptor getBinaryDecryptor(String key) {
			if (key == null || key.trim().isEmpty()) throw new IllegalArgumentException("key 不可为空")
			return new TestBinaryEncryptor()
		}
		@Override TextEncryptor getTextEncryptor(String key) { throw new UnsupportedOperationException() }
		@Override TextEncryptor getTextDecryptor(String key) { throw new UnsupportedOperationException() }
		@Override IntegerNumberEncryptor getIntegerNumberEncryptor(String key) { throw new UnsupportedOperationException() }
		@Override IntegerNumberEncryptor getIntegerNumberDecryptor(String key) { throw new UnsupportedOperationException() }
		@Override DecimalNumberEncryptor getDecimalNumberEncryptor(String key) { throw new UnsupportedOperationException() }
		@Override DecimalNumberEncryptor getDecimalNumberDecryptor(String key) { throw new UnsupportedOperationException() }
	}

	static class ThrowingCryptoFactory implements CryptoFactory {
		@Override BinaryEncryptor getBinaryEncryptor(String key) { throw new UnsupportedOperationException() }
		@Override BinaryEncryptor getBinaryDecryptor(String key) {
			return new BinaryEncryptor() {
				@Override byte[] encrypt(byte[] bytes) { throw new UnsupportedOperationException() }
				@Override byte[] decrypt(byte[] bytes) { throw new EncryptionOperationNotPossibleException() }
			}
		}
		@Override TextEncryptor getTextEncryptor(String key) { throw new UnsupportedOperationException() }
		@Override TextEncryptor getTextDecryptor(String key) { throw new UnsupportedOperationException() }
		@Override IntegerNumberEncryptor getIntegerNumberEncryptor(String key) { throw new UnsupportedOperationException() }
		@Override IntegerNumberEncryptor getIntegerNumberDecryptor(String key) { throw new UnsupportedOperationException() }
		@Override DecimalNumberEncryptor getDecimalNumberEncryptor(String key) { throw new UnsupportedOperationException() }
		@Override DecimalNumberEncryptor getDecimalNumberDecryptor(String key) { throw new UnsupportedOperationException() }
	}

	static class C {
		void json(@DecryptRequestBody(factory = [TestCryptoFactory], key = "k", encoding = Encoding.BASE64) Object o) {}
		void str(@DecryptRequestBody(factory = [TestCryptoFactory], key = "k", encoding = Encoding.BASE64) String s) {}
		void hexStr(@DecryptRequestBody(factory = [TestCryptoFactory], key = "k", encoding = Encoding.HEX) String s) {}
		void badKey(@DecryptRequestBody(factory = [TestCryptoFactory], key = "", encoding = Encoding.BASE64) String s) {}
		void throwing(@DecryptRequestBody(factory = [ThrowingCryptoFactory], key = "k", encoding = Encoding.BASE64) String s) {}
	}

	static MethodParameter param(String name, Class<?> type) {
		new MethodParameter(C.getDeclaredMethod(name, type), 0)
	}

	def factories = [new TestCryptoFactory()]
	def advice = new RequestBodyDecryptAdvice(factories)

	@Unroll
	def "supports true for annotated parameter with converter #conv"() {
		expect:
		advice.supports(param("json", Object), Object, conv)

		where:
		conv << [MappingJackson2HttpMessageConverter, StringHttpMessageConverter]
	}

	def "supports false without annotation"() {
		def p = new MethodParameter(D.getDeclaredMethod("noAnno", String), 0)

		expect:
		!advice.supports(p, String, MappingJackson2HttpMessageConverter)
	}

	def "beforeBodyRead decrypts JSON payload to plaintext"() {
		given:
		def plaintext = '{"a":1}'
		def encrypted = Base64.encodeBase64URLSafeString(("X-" + plaintext).getBytes(StandardCharsets.UTF_8))
		def input = new SimpleInputMessage(encrypted)
		def p = param("json", Object)

		when:
		def outMsg = advice.beforeBodyRead(input, p, Object, MappingJackson2HttpMessageConverter)

		then:
		new String(outMsg.getBody().readAllBytes(), StandardCharsets.UTF_8) == plaintext
	}

	def "beforeBodyRead replaces blank body with empty JSON object"() {
		given:
		def input = new SimpleInputMessage("")
		def p = param("json", Object)

		when:
		def outMsg = advice.beforeBodyRead(input, p, Object, MappingJackson2HttpMessageConverter)

		then:
		new String(outMsg.getBody().readAllBytes(), StandardCharsets.UTF_8) == "{}"
	}

	def "beforeBodyRead leaves String converter untouched"() {
		given:
		def input = new SimpleInputMessage("anything")
		def p = param("str", String)

		expect:
		advice.beforeBodyRead(input, p, String, StringHttpMessageConverter).is(input)
	}

	def "afterBodyRead decrypts string for String converter"() {
		given:
		def p = param("str", String)
		def encrypted = Base64.encodeBase64URLSafeString("X-abc".bytes)

		expect:
		advice.afterBodyRead(encrypted, null, p, String, StringHttpMessageConverter) == "abc"
	}

	def "afterBodyRead returns original when blank string"() {
		given:
		def p = param("str", String)

		expect:
		advice.afterBodyRead("", null, p, String, StringHttpMessageConverter) == ""
	}

	def "beforeBodyRead throws ServerException when factory missing"() {
		given:
		def emptyAdvice = new RequestBodyDecryptAdvice([])
		def p = param("json", Object)
		def encrypted = Base64.encodeBase64URLSafeString("X-{}".bytes)
		def input = new SimpleInputMessage(encrypted)

		when:
		emptyAdvice.beforeBodyRead(input, p, Object, MappingJackson2HttpMessageConverter)

		then:
		thrown(ServerException)
	}

	def "beforeBodyRead throws ServerException for blank key"() {
		given:
		def p = param("badKey", String)
		def input = new SimpleInputMessage(Base64.encodeBase64URLSafeString("X-abc".bytes))

		when:
		advice.beforeBodyRead(input, p, String, MappingJackson2HttpMessageConverter)

		then:
		thrown(ServerException)
	}

	def "beforeBodyRead throws ValidationException for invalid HEX"() {
		given:
		def p = param("hexStr", String)
		def input = new SimpleInputMessage("ZZZ") // 非法 HEX

		when:
		advice.beforeBodyRead(input, p, String, MappingJackson2HttpMessageConverter)

		then:
		def ex = thrown(ValidationException)
		ex.message.contains("加密请求体格式错误")
	}

	def "afterBodyRead throws ValidationException for invalid HEX"() {
		given:
		def p = param("hexStr", String)

		when:
		advice.afterBodyRead("ZZZ", null, p, String, StringHttpMessageConverter)

		then:
		def ex = thrown(ValidationException)
		ex.message.contains("加密请求数据格式错误")
	}

	def "afterBodyRead throws RequestDataDecryptFailureException when decryption fails"() {
		given:
		def throwingAdvice = new RequestBodyDecryptAdvice([new ThrowingCryptoFactory()])
		def p = param("throwing", String)
		def encrypted = Base64.encodeBase64URLSafeString("X-abc".bytes)

		when:
		throwingAdvice.afterBodyRead(encrypted, null, p, String, StringHttpMessageConverter)

		then:
		def ex = thrown(RequestDataDecryptFailureException)
		ex.message.contains("无效的加密请求数据")
	}

	class D { void noAnno(String s) {} }
}
