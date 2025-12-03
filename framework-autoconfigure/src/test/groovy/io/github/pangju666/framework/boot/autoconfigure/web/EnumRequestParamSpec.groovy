package io.github.pangju666.framework.boot.autoconfigure.web

import io.github.pangju666.framework.boot.web.annotation.EnumRequestParam
import io.github.pangju666.framework.boot.web.resolver.EnumRequestParamArgumentResolver
import io.github.pangju666.framework.web.exception.base.ValidationException
import org.springframework.core.MethodParameter
import org.springframework.core.ParameterNameDiscoverer
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.context.request.NativeWebRequest
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Constructor
import java.lang.reflect.Method

enum UserStatus { ACTIVE, INACTIVE }

class EnumRequestParamSpec extends Specification {
	def resolver = new EnumRequestParamArgumentResolver()

	static class TestController {
		void requiredNamed(@EnumRequestParam("status") UserStatus status) {}
		void optionalNamed(@EnumRequestParam(value = "status", required = false) UserStatus status) {}
		void defaultNamed(@EnumRequestParam(value = "status", defaultValue = "ACTIVE") UserStatus status) {}
		void requiredFallback(@EnumRequestParam UserStatus status) {}
	}

	static class NonEnumController { void nonEnum(@EnumRequestParam("status") String status) {} }
	static class MissingAnnoController { void noAnno(UserStatus status) {} }

	MethodParameter param(String name) {
		new MethodParameter(TestController.getDeclaredMethod(name, UserStatus), 0)
	}

	@Unroll
	def "supportsParameter works for #caseName"() {
		expect:
		resolver.supportsParameter(parameter) == expected

		where:
		caseName              | parameter                                                                                                  | expected
		"annotated enum"     | param("requiredNamed")                                                                                     | true
		"optional enum"      | param("optionalNamed")                                                                                     | true
		"default enum"       | param("defaultNamed")                                                                                      | true
		"non-enum"           | new MethodParameter(NonEnumController.getDeclaredMethod("nonEnum", String), 0)                             | false
		"missing annotation" | new MethodParameter(MissingAnnoController.getDeclaredMethod("noAnno", UserStatus), 0)                      | false
	}

	def "resolveArgument returns enum when present"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("status") >> "ACTIVE"
		}

		expect:
		resolver.resolveArgument(param("requiredNamed"), null, web, null) == UserStatus.ACTIVE
	}

	def "resolveArgument is case-insensitive"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("status") >> "aCtIvE"
		}

		expect:
		resolver.resolveArgument(param("requiredNamed"), null, web, null) == UserStatus.ACTIVE
	}

	def "resolveArgument uses default when missing"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("status") >> null
		}

		expect:
		resolver.resolveArgument(param("defaultNamed"), null, web, null) == UserStatus.ACTIVE
	}

	def "resolveArgument returns null when optional missing"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("status") >> null
		}

		expect:
		resolver.resolveArgument(param("optionalNamed"), null, web, null) == null
	}

	def "resolveArgument throws MissingServletRequestParameterException when required missing"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("status") >> null
		}

		when:
		resolver.resolveArgument(param("requiredNamed"), null, web, null)

		then:
		thrown(MissingServletRequestParameterException)
	}

	def "resolveArgument throws ValidationException when invalid value"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("status") >> "UNKNOWN"
		}

		when:
		resolver.resolveArgument(param("requiredNamed"), null, web, null)

		then:
		def ex = thrown(ValidationException)
		ex.message.contains("无效的")
	}

	def "resolveArgument uses fallback parameter name when value empty"() {
		given:
		def web = Mock(NativeWebRequest) {
			getParameter("status") >> "INACTIVE"
		}
		def p = param("requiredFallback")
		p.initParameterNameDiscovery(new ParameterNameDiscoverer() {
			@Override
			String[] getParameterNames(Method m) { ["status"] as String[] }
			@Override
			String[] getParameterNames(Constructor c) { null }
		})

		expect:
		resolver.resolveArgument(p, null, web, null) == UserStatus.INACTIVE
	}
}
