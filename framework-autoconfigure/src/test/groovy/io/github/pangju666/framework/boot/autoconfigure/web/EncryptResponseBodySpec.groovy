package io.github.pangju666.framework.boot.autoconfigure.web

import io.github.pangju666.framework.boot.autoconfigure.web.crypto.ResponseBodyEncryptAdvice
import io.github.pangju666.framework.boot.crypto.enums.Encoding
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory
import io.github.pangju666.framework.boot.web.annotation.EncryptResponseBody
import io.github.pangju666.framework.web.exception.base.ServerException
import io.github.pangju666.framework.web.model.Result
import org.apache.commons.codec.binary.Base64
import org.jasypt.util.binary.BinaryEncryptor
import org.jasypt.util.numeric.DecimalNumberEncryptor
import org.jasypt.util.numeric.IntegerNumberEncryptor
import org.jasypt.util.text.TextEncryptor
import org.springframework.core.MethodParameter
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import spock.lang.Specification
import spock.lang.Unroll

class EncryptResponseBodySpec extends Specification {
	static class TestBinaryEncryptor implements BinaryEncryptor {
		@Override
		byte[] encrypt(byte[] bytes) {
			return ("X-" + new String(bytes)).bytes
		}
		@Override
		byte[] decrypt(byte[] bytes) {
			return bytes
		}
	}

	static class TestCryptoFactory implements CryptoFactory {
		@Override
		BinaryEncryptor getBinaryEncryptor(String key) {
			if (key == null || key.trim().isEmpty()) throw new IllegalArgumentException("key 不可为空")
			return new TestBinaryEncryptor()
		}
		@Override
		TextEncryptor getTextEncryptor(String key) { throw new UnsupportedOperationException() }
		@Override
		IntegerNumberEncryptor getIntegerNumberEncryptor(String key) { throw new UnsupportedOperationException() }
		@Override
		DecimalNumberEncryptor getDecimalNumberEncryptor(String key) { throw new UnsupportedOperationException() }
		@Override
		IntegerNumberEncryptor getIntegerNumberDecryptor(String key) { throw new UnsupportedOperationException() }
		@Override
		DecimalNumberEncryptor getDecimalNumberDecryptor(String key) { throw new UnsupportedOperationException() }
		@Override
		TextEncryptor getTextDecryptor(String key) { throw new UnsupportedOperationException() }
		@Override
		BinaryEncryptor getBinaryDecryptor(String key) { return getBinaryEncryptor(key) }
	}

	@EncryptResponseBody(factory = [TestCryptoFactory], key = "k", encoding = Encoding.BASE64)
	static class MethodAnnoController {
		String s() { "" }
		byte[] b() { new byte[0] }
		Object o() { null }
		Result<String> r() { Result.ok("") }
	}

	@EncryptResponseBody(factory = [TestCryptoFactory], key = "k")
	static class ClassAnnoController {
		String s() { "" }
		Object o() { null }
	}

	static MethodParameter returnType(Class<?> c, String m) {
		new MethodParameter(c.getDeclaredMethod(m), -1)
	}

	def factories = [new TestCryptoFactory()]
	def advice = new ResponseBodyEncryptAdvice(factories)

	@Unroll
	def "supports true when annotation present and converter #conv"() {
		expect:
		advice.supports(returnType(MethodAnnoController, "s"), conv)

		where:
		conv << [StringHttpMessageConverter, ByteArrayHttpMessageConverter, MappingJackson2HttpMessageConverter]
	}

	def "supports true with class-level annotation"() {
		expect:
		advice.supports(returnType(ClassAnnoController, "s"), MappingJackson2HttpMessageConverter)
	}

	def "supports false without annotation"() {
		expect:
		!advice.supports(new MethodParameter(NoAnno.getDeclaredMethod("s"), -1), StringHttpMessageConverter)
	}

	def "beforeBodyWrite encrypts String with BASE64"() {
		given:
		def rt = returnType(MethodAnnoController, "s")
		def body = "abc"
		def expected = Base64.encodeBase64URLSafeString(("X-" + body).bytes)

		expect:
		advice.beforeBodyWrite(body, rt, null, StringHttpMessageConverter, Mock(ServerHttpRequest), Mock(ServerHttpResponse)) == expected
	}

	def "beforeBodyWrite encrypts byte array"() {
		given:
		def rt = returnType(MethodAnnoController, "b")
		def body = "abc".bytes
		def res = advice.beforeBodyWrite(body, rt, null, ByteArrayHttpMessageConverter, Mock(ServerHttpRequest), Mock(ServerHttpResponse)) as byte[]

		expect:
		new String(res) == "X-abc"
	}

	def "beforeBodyWrite encrypts Result data string"() {
		given:
		def rt = returnType(MethodAnnoController, "r")
		def body = Result.ok("abc")
		def out = advice.beforeBodyWrite(body, rt, null, MappingJackson2HttpMessageConverter, Mock(ServerHttpRequest), Mock(ServerHttpResponse)) as Result

		expect:
		out.code() == body.code()
		out.message() == body.message()
		new String(Base64.decodeBase64(out.data() as String)).startsWith("X-")
		new String(Base64.decodeBase64(out.data() as String)).substring(2) == "abc"
	}

	def "beforeBodyWrite leaves Result with null data unchanged"() {
		given:
		def rt = returnType(MethodAnnoController, "r")
		def body = Result.ok(null)
		def out = advice.beforeBodyWrite(body, rt, null, MappingJackson2HttpMessageConverter, Mock(ServerHttpRequest), Mock(ServerHttpResponse))

		expect:
		out.is(body)
	}

	def "beforeBodyWrite encrypts object to JSON then BASE64"() {
		given:
		def rt = returnType(MethodAnnoController, "o")
		def body = [a: 1]
		def out = advice.beforeBodyWrite(body, rt, null, MappingJackson2HttpMessageConverter, Mock(ServerHttpRequest), Mock(ServerHttpResponse)) as Result

		expect:
		new String(Base64.decodeBase64(out.data() as String)).startsWith("X-")
	}

	def "factory missing throws ServerException"() {
		given:
		def emptyAdvice = new ResponseBodyEncryptAdvice([])
		def rt = returnType(MethodAnnoController, "s")

		when:
		emptyAdvice.beforeBodyWrite("abc", rt, null, StringHttpMessageConverter, Mock(ServerHttpRequest), Mock(ServerHttpResponse))

		then:
		thrown(ServerException)
	}

	def "blank key throws ServerException"() {
		def rt = returnType(BadKeyCtrl, "s")

		when:
		advice.beforeBodyWrite("abc", rt, null, StringHttpMessageConverter, Mock(ServerHttpRequest), Mock(ServerHttpResponse))

		then:
		thrown(ServerException)
	}

	def "supports false for unsupported converter type"() {
		expect:
		!advice.supports(returnType(MethodAnnoController, "s"), DummyConverter)
	}

	def "class-level annotation encrypts JSON object"() {
		given:
		def rt = returnType(ClassAnnoController, "o")
		def body = [x: "y"]
		def out = advice.beforeBodyWrite(body, rt, null, MappingJackson2HttpMessageConverter, Mock(ServerHttpRequest), Mock(ServerHttpResponse)) as Result

		expect:
		new String(Base64.decodeBase64(out.data() as String)).startsWith("X-")
	}

	class NoAnno { String s() { "" } }

	@EncryptResponseBody(factory = [TestCryptoFactory], key = "", encoding = Encoding.BASE64)
	class BadKeyCtrl { String s() { "" } }

	class DummyConverter implements HttpMessageConverter<Object> {
		@Override boolean canRead(Class<?> clazz, MediaType mediaType) { false }
		@Override boolean canWrite(Class<?> clazz, MediaType mediaType) { false }
		@Override List<MediaType> getSupportedMediaTypes() { [] }
		@Override Object read(Class<? extends Object> clazz, HttpInputMessage inputMessage) { null }
		@Override void write(Object o, MediaType contentType, HttpOutputMessage outputMessage) {}
	}
}
