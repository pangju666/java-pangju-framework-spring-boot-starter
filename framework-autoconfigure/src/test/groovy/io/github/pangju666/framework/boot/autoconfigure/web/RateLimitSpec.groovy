package io.github.pangju666.framework.boot.autoconfigure.web

import io.github.pangju666.framework.boot.spring.StaticSpringContext
import io.github.pangju666.framework.boot.web.annotation.RateLimit
import io.github.pangju666.framework.boot.web.interceptor.RateLimitInterceptor
import io.github.pangju666.framework.boot.web.limit.RateLimitSourceExtractor
import io.github.pangju666.framework.boot.web.limit.RateLimiter
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.BeanFactory
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.method.HandlerMethod
import spock.lang.Specification

class RateLimitSpec extends Specification {
	def rateLimiter = Mock(RateLimiter)
	def interceptor = new RateLimitInterceptor(rateLimiter)
	def request = Mock(HttpServletRequest)
	def response = new MockHttpServletResponse()  // 改这里

	def setup() {
		request.getRequestURI() >> "/api/test"
		request.getMethod() >> "GET"
	}

	static class DummySourceExtractor implements RateLimitSourceExtractor {
		@Override
		String getSource(HttpServletRequest req) { "SRC" }
	}

	static class ControllerDefault {
		@RateLimit(rate = 1)
		void method() {}
		void none() {}
	}

	static class ControllerSpEL {
		@RateLimit(key = "#request.getParameter('x')", rate = 1)
		void spEl() {}
		@RateLimit(key = "#invalid(", rate = 1)
		void invalid() {}
	}

	@RateLimit(rate = 5)
	static class ControllerClassAnnotation {
		@RateLimit(rate = 1)
		void override() {}
		void inherit() {}
	}

	static class ControllerSource {
		@RateLimit(rate = 1, scope = RateLimit.RateLimitScope.SOURCE, source = DummySourceExtractor.class)
		void source() {}
	}

	static class ControllerNoAnnotation {
		void plain() {}
	}

	def "默认键生成 使用URI与方法"() {
		given:
		def controller = new ControllerDefault()
		def handler = new HandlerMethod(controller, controller.class.getMethod("method"))

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		1 * rateLimiter.tryAcquire({ it == "/api/test_GET" }, { it.rate() == 1 }, request) >> true
		result
	}

	def "SpEL键生成 使用请求参数"() {
		given:
		def controller = new ControllerSpEL()
		def handler = new HandlerMethod(controller, controller.class.getMethod("spEl"))
		request.getParameter("x") >> "abc"

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		1 * rateLimiter.tryAcquire({ it == "abc" }, { it.rate() == 1 }, request) >> true
		result
	}

	def "SpEL解析失败 回退原始表达式"() {
		given:
		def controller = new ControllerSpEL()
		def handler = new HandlerMethod(controller, controller.class.getMethod("invalid"))

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		1 * rateLimiter.tryAcquire({ it == "#invalid(" }, { it.rate() == 1 }, request) >> true
		result
	}

	def "方法注解优先于类注解"() {
		given:
		def controller = new ControllerClassAnnotation()
		def handler = new HandlerMethod(controller, controller.class.getMethod("override"))

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		1 * rateLimiter.tryAcquire({ it == "/api/test_GET" }, { it.rate() == 1 }, request) >> true
		result
	}

	def "类注解生效 当方法无注解"() {
		given:
		def controller = new ControllerClassAnnotation()
		def handler = new HandlerMethod(controller, controller.class.getMethod("inherit"))

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		1 * rateLimiter.tryAcquire({ it == "/api/test_GET" }, { it.rate() == 5 }, request) >> true
		result
	}

	def "SOURCE作用域 追加源标识"() {
		given:
		def beanFactory = Mock(BeanFactory)
		beanFactory.getBean(DummySourceExtractor.class) >> new DummySourceExtractor()
		def f = StaticSpringContext.class.getDeclaredField("BEAN_FACTORY")
		f.setAccessible(true)
		f.set(null, beanFactory)

		def controller = new ControllerSource()
		def handler = new HandlerMethod(controller, controller.class.getMethod("source"))

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		1 * rateLimiter.tryAcquire({ it == "/api/test_GET_SRC" }, { it.rate() == 1 && it.scope() == RateLimit.RateLimitScope.SOURCE }, request) >> true
		result
	}

	def "限流拒绝 返回false"() {
		given:
		def controller = new ControllerDefault()
		def handler = new HandlerMethod(controller, controller.class.getMethod("method"))

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		1 * rateLimiter.tryAcquire({ it == "/api/test_GET" }, { it.rate() == 1 }, request) >> false
		!result
	}

	def "限流器异常 返回false"() {
		given:
		def controller = new ControllerDefault()
		def handler = new HandlerMethod(controller, controller.class.getMethod("method"))

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		1 * rateLimiter.tryAcquire({ it == "/api/test_GET" }, { it.rate() == 1 }, request) >> { throw new RuntimeException("err") }
		!result
	}

	def "无注解 直接放行"() {
		given:
		def controller = new ControllerNoAnnotation()
		def handler = new HandlerMethod(controller, controller.class.getMethod("plain"))

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		0 * rateLimiter._
		result
	}
}
