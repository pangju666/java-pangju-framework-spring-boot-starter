package io.github.pangju666.framework.boot.autoconfigure.web

import io.github.pangju666.framework.boot.crypto.enums.Encoding
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory
import io.github.pangju666.framework.boot.web.annotation.EncryptRequestParam
import io.github.pangju666.framework.boot.web.exception.RequestDataDecryptFailureException
import io.github.pangju666.framework.boot.web.resolver.EncryptRequestParamArgumentResolver
import io.github.pangju666.framework.web.exception.base.ServerException
import io.github.pangju666.framework.web.exception.base.ValidationException
import org.apache.commons.codec.binary.Base64
import org.jasypt.exceptions.EncryptionOperationNotPossibleException
import org.jasypt.util.binary.BinaryEncryptor
import org.jasypt.util.numeric.DecimalNumberEncryptor
import org.jasypt.util.numeric.IntegerNumberEncryptor
import org.jasypt.util.text.TextEncryptor
import org.springframework.core.MethodParameter
import org.springframework.core.ParameterNameDiscoverer
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.context.request.NativeWebRequest
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Constructor
import java.lang.reflect.Method

class EncryptRequestParamSpec extends Specification {
	static class TestBinaryEncryptor implements BinaryEncryptor {
		@Override
		byte[] encrypt(byte[] bytes) { throw new UnsupportedOperationException() }
		@Override
		byte[] decrypt(byte[] bytes) {
			def s = new String(bytes)
			return (s.startsWith("X-") ? s.substring(2) : s).bytes
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
		void requiredNamed(@EncryptRequestParam(value = "data", key = "k", factory = [TestCryptoFactory], encoding = Encoding.BASE64) String data) {}
		void optionalNamed(@EncryptRequestParam(value = "data", key = "k", factory = [TestCryptoFactory], required = false, defaultValue = "DEF", encoding = Encoding.BASE64) String data) {}
		void fallbackName(@EncryptRequestParam(key = "k", factory = [TestCryptoFactory], encoding = Encoding.BASE64) String data) {}
		void hex(@EncryptRequestParam(value = "data", key = "k", factory = [TestCryptoFactory], encoding = Encoding.HEX) String data) {}
		void badKey(@EncryptRequestParam(value = "data", key = "", factory = [TestCryptoFactory], encoding = Encoding.BASE64) String data) {}
		void throwing(@EncryptRequestParam(value = "data", key = "k", factory = [ThrowingCryptoFactory], encoding = Encoding.BASE64) String data) {}
		void nonString(@EncryptRequestParam("data") Integer n) {}
	}
	static class D { void noAnno(String s) {} }

	static MethodParameter p(String name, Class<?> type) { new MethodParameter(C.getDeclaredMethod(name, type), 0) }

	def resolver = new EncryptRequestParamArgumentResolver([new TestCryptoFactory()])

	@Unroll
	def "supportsParameter works for #caseName"() {
		expect:
		resolver.supportsParameter(parameter) == expected

		where:
		caseName          | parameter                                              | expected
		"annotated String"| p("requiredNamed", String)                             | true
		"non-String type" | new MethodParameter(C.getDeclaredMethod("nonString", Integer), 0) | false
		"missing annotation" | new MethodParameter(D.getDeclaredMethod("noAnno", String), 0)  | false
	}

	def "resolveArgument decrypts BASE64 string when present"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("data") >> Base64.encodeBase64URLSafeString("X-abc".bytes)
		}

		expect:
		resolver.resolveArgument(p("requiredNamed", String), null, web, null) == "abc"
	}

	def "resolveArgument returns default when optional missing"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("data") >> null
		}

		expect:
		resolver.resolveArgument(p("optionalNamed", String), null, web, null) == "DEF"
	}

	def "resolveArgument throws MissingServletRequestParameterException when required missing"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("data") >> null
		}

		when:
		resolver.resolveArgument(p("requiredNamed", String), null, web, null)

		then:
		thrown(MissingServletRequestParameterException)
	}

	def "resolveArgument uses fallback parameter name when annotation value empty"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("data") >> Base64.encodeBase64URLSafeString("X-xyz".bytes)
		}
		def mp = p("fallbackName", String)
		mp.initParameterNameDiscovery(new ParameterNameDiscoverer() {
			@Override String[] getParameterNames(Method m) { ["data"] as String[] }
			@Override String[] getParameterNames(Constructor c) { null }
		})

		expect:
		resolver.resolveArgument(mp, null, web, null) == "xyz"
	}

	def "resolveArgument decrypts HEX encoded string"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("data") >> "582d616263" // "X-abc" -> hex
		}

		expect:
		resolver.resolveArgument(p("hex", String), null, web, null) == "abc"
	}

	def "resolveArgument throws ValidationException for invalid HEX"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("data") >> "ZZZ"
		}

		when:
		resolver.resolveArgument(p("hex", String), null, web, null)

		then:
		def ex = thrown(ValidationException)
		ex.message.contains("格式错误")
	}

	def "resolveArgument throws ServerException for blank key"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("data") >> Base64.encodeBase64URLSafeString("X-abc".bytes)
		}

		when:
		resolver.resolveArgument(p("badKey", String), null, web, null)

		then:
		thrown(ServerException)
	}

	def "resolveArgument throws RequestDataDecryptFailureException when decryption fails"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("data") >> Base64.encodeBase64URLSafeString("X-abc".bytes)
		}
		def throwingResolver = new EncryptRequestParamArgumentResolver([new ThrowingCryptoFactory()])

		when:
		throwingResolver.resolveArgument(p("throwing", String), null, web, null)

		then:
		def ex = thrown(RequestDataDecryptFailureException)
		ex.message.contains("加密请求参数")
	}

	def "resolveArgument throws ServerException when crypto factory missing"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("data") >> Base64.encodeBase64URLSafeString("X-abc".bytes)
		}
		def emptyResolver = new EncryptRequestParamArgumentResolver([])

		when:
		emptyResolver.resolveArgument(p("requiredNamed", String), null, web, null)

		then:
		thrown(ServerException)
	}
}
