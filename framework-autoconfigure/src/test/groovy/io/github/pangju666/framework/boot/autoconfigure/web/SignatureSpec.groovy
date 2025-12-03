package io.github.pangju666.framework.boot.autoconfigure.web

import io.github.pangju666.framework.boot.web.annotation.Signature
import io.github.pangju666.framework.boot.web.annotation.Signature.SignatureType
import io.github.pangju666.framework.boot.web.configuration.SignatureConfiguration
import io.github.pangju666.framework.boot.web.enums.SignatureAlgorithm
import io.github.pangju666.framework.boot.web.interceptor.SignatureInterceptor
import io.github.pangju666.framework.boot.web.signature.SecretKeyStorer
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.MissingRequestValueException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.method.HandlerMethod
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class SignatureSpec extends Specification {

	def config = new SignatureConfiguration()
	def secretKeyStorer = Mock(SecretKeyStorer)
	def interceptor = new SignatureInterceptor(config, secretKeyStorer)
	def request = Mock(HttpServletRequest)
	def response = Mock(HttpServletResponse)

	def buffer
	def sos
	def writer

	def setup() {
		config.setSignatureHeaderName("X-Signature")
		config.setAppIdHeaderName("X-App-Id")
		config.setTimestampHeaderName("X-Timestamp")
		config.setSignatureParamName("sign")
		config.setAppIdParamName("appId")

		buffer = new ByteArrayOutputStream()
		sos = new ServletOutputStream() {
			@Override
			void write(int b) { buffer.write(b) }

			@Override
			boolean isReady() { true }

			@Override
			void setWriteListener(WriteListener writeListener) {}
		}
		writer = new PrintWriter(buffer, true)
		response.getOutputStream() >> sos
		response.getWriter() >> writer

		request.getRequestURL() >> new StringBuffer("http://localhost/api/test")
	}

	static class ControllerParam {
		@Signature(type = SignatureType.PARAM, algorithm = SignatureAlgorithm.SHA256, timeout = 1)
		void method() {}
	}

	static class ControllerHeader {
		@Signature(type = SignatureType.HEADER, algorithm = SignatureAlgorithm.SHA256, timeout = 1)
		void method() {}
	}

	static class ControllerHeaderWhitelist {
		@Signature(type = SignatureType.HEADER, algorithm = SignatureAlgorithm.SHA256, timeout = 1, appId = ["ok"])
		void method() {}
	}

	static class ControllerAny {
		@Signature(type = SignatureType.ANY, algorithm = SignatureAlgorithm.SHA256, timeout = 1)
		void method() {}
	}

	@Signature(type = SignatureType.HEADER, algorithm = SignatureAlgorithm.SHA256, timeout = 1)
	static class ControllerClassAnnotated {
		@Signature(type = SignatureType.PARAM, algorithm = SignatureAlgorithm.SHA256, timeout = 1)
		void overrideMethod() {}

		void inheritMethod() {}
	}

	def "PARAM 成功校验（URL编码且移除签名相关参数）"() {
		given:
		def appId = "app-1"
		def secret = "s3cr3t"
		def handler = new HandlerMethod(new ControllerParam(), ControllerParam.class.getMethod("method"))
		request.getQueryString() >> "appId=${appId}&sign=xx&k=v"
		secretKeyStorer.loadSecretKey(appId) >> secret
		def encodedUrl = URLEncoder.encode("http://localhost/api/test?k=v", StandardCharsets.UTF_8)
		def expected = SignatureAlgorithm.SHA256.computeDigest([appId, secret, encodedUrl].join("&"))
		request.getParameter("appId") >> appId
		request.getParameter("sign") >> expected

		expect:
		interceptor.preHandle(request, response, handler)
	}

	def "PARAM 缺少appId抛出异常"() {
		given:
		def handler = new HandlerMethod(new ControllerParam(), ControllerParam.class.getMethod("method"))
		request.getParameter("appId") >> null

		when:
		interceptor.preHandle(request, response, handler)

		then:
		thrown(MissingServletRequestParameterException)
	}

	def "PARAM 缺少sign抛出异常"() {
		given:
		def handler = new HandlerMethod(new ControllerParam(), ControllerParam.class.getMethod("method"))
		request.getParameter("appId") >> "app-1"
		request.getParameter("sign") >> null

		when:
		interceptor.preHandle(request, response, handler)

		then:
		thrown(MissingServletRequestParameterException)
	}

	def "PARAM secretKey为空 返回false"() {
		given:
		def handler = new HandlerMethod(new ControllerParam(), ControllerParam.class.getMethod("method"))
		request.getParameter("appId") >> "app-1"
		request.getParameter("sign") >> "x"
		secretKeyStorer.loadSecretKey("app-1") >> ""

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		!result
	}

	def "PARAM 签名不匹配 返回false"() {
		given:
		def appId = "app-1"
		def handler = new HandlerMethod(new ControllerParam(), ControllerParam.class.getMethod("method"))
		request.getQueryString() >> "appId=${appId}&sign=xx&k=v"
		secretKeyStorer.loadSecretKey(appId) >> "s3cr3t"
		request.getParameter("appId") >> appId
		request.getParameter("sign") >> "wrong"

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		!result
	}

	def "HEADER 成功校验（URL不编码）"() {
		given:
		def appId = "app-1"
		def secret = "s3cr3t"
		def ts = System.currentTimeMillis().toString()
		def handler = new HandlerMethod(new ControllerHeader(), ControllerHeader.class.getMethod("method"))
		request.getHeader("X-App-Id") >> appId
		request.getHeader("X-Timestamp") >> ts
		secretKeyStorer.loadSecretKey(appId) >> secret
		def plainUrl = "http://localhost/api/test"
		def expected = SignatureAlgorithm.SHA256.computeDigest([appId, secret, plainUrl, ts].join("&"))
		request.getHeader("X-Signature") >> expected

		expect:
		interceptor.preHandle(request, response, handler)
	}

	def "HEADER 缺少appId抛出异常"() {
		given:
		def handler = new HandlerMethod(new ControllerHeader(), ControllerHeader.class.getMethod("method"))
		request.getHeader("X-App-Id") >> ""

		when:
		interceptor.preHandle(request, response, handler)

		then:
		thrown(MissingRequestValueException)
	}

	def "HEADER 缺少signature抛出异常"() {
		given:
		def handler = new HandlerMethod(new ControllerHeader(), ControllerHeader.class.getMethod("method"))
		request.getHeader("X-App-Id") >> "app-1"
		request.getHeader("X-Signature") >> ""
		request.getHeader("X-Timestamp") >> "123"

		when:
		interceptor.preHandle(request, response, handler)

		then:
		thrown(MissingRequestValueException)
	}

	def "HEADER 缺少timestamp抛出异常"() {
		given:
		def handler = new HandlerMethod(new ControllerHeader(), ControllerHeader.class.getMethod("method"))
		request.getHeader("X-App-Id") >> "app-1"
		request.getHeader("X-Signature") >> "x"
		request.getHeader("X-Timestamp") >> ""

		when:
		interceptor.preHandle(request, response, handler)

		then:
		thrown(MissingRequestValueException)
	}

	def "HEADER 非法timestamp 返回false"() {
		given:
		def handler = new HandlerMethod(new ControllerHeader(), ControllerHeader.class.getMethod("method"))
		request.getHeader("X-App-Id") >> "app-1"
		request.getHeader("X-Signature") >> "x"
		request.getHeader("X-Timestamp") >> "abc"
		secretKeyStorer.loadSecretKey("app-1") >> "s3cr3t"

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		!result
	}

	def "HEADER 过期timestamp 返回false"() {
		given:
		def handler = new HandlerMethod(new ControllerHeader(), ControllerHeader.class.getMethod("method"))
		def past = (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2)).toString()
		request.getHeader("X-App-Id") >> "app-1"
		request.getHeader("X-Signature") >> "x"
		request.getHeader("X-Timestamp") >> past
		secretKeyStorer.loadSecretKey("app-1") >> "s3cr3t"

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		!result
	}

	def "HEADER secretKey为空 返回false"() {
		given:
		def handler = new HandlerMethod(new ControllerHeader(), ControllerHeader.class.getMethod("method"))
		request.getHeader("X-App-Id") >> "app-1"
		request.getHeader("X-Timestamp") >> System.currentTimeMillis().toString()
		request.getHeader("X-Signature") >> "x"     // 关键：补齐签名请求头
		secretKeyStorer.loadSecretKey("app-1") >> ""

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		!result
	}

	def "HEADER 签名不匹配 返回false"() {
		given:
		def appId = "app-1"
		def ts = System.currentTimeMillis().toString()
		def handler = new HandlerMethod(new ControllerHeader(), ControllerHeader.class.getMethod("method"))
		request.getHeader("X-App-Id") >> appId
		request.getHeader("X-Timestamp") >> ts
		secretKeyStorer.loadSecretKey(appId) >> "s3cr3t"
		request.getHeader("X-Signature") >> "wrong"

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		!result
	}

	def "HEADER appId不在白名单 返回false"() {
		given:
		def handler = new HandlerMethod(new ControllerHeaderWhitelist(), ControllerHeaderWhitelist.class.getMethod("method"))
		request.getHeader("X-App-Id") >> "bad"
		request.getHeader("X-Timestamp") >> System.currentTimeMillis().toString()
		secretKeyStorer.loadSecretKey("bad") >> "s3cr3t"
		request.getHeader("X-Signature") >> "x"

		when:
		def result = interceptor.preHandle(request, response, handler)

		then:
		!result
	}

	def "ANY 头为空走参数校验（URL编码）"() {
		given:
		def appId = "app-1"
		def secret = "s3cr3t"
		def handler = new HandlerMethod(new ControllerAny(), ControllerAny.class.getMethod("method"))
		request.getHeader("X-Signature") >> ""
		request.getHeader("X-Timestamp") >> ""
		request.getQueryString() >> "appId=${appId}&sign=xx&k=v"
		secretKeyStorer.loadSecretKey(appId) >> secret
		def encodedUrl = java.net.URLEncoder.encode("http://localhost/api/test?k=v", java.nio.charset.StandardCharsets.UTF_8)
		def expected = SignatureAlgorithm.SHA256.computeDigest([appId, secret, encodedUrl].join("&"))
		request.getParameter("appId") >> appId
		request.getParameter("sign") >> expected

		expect:
		interceptor.preHandle(request, response, handler)
	}

	def "ANY 头存在走头校验（URL不编码）"() {
		given:
		def appId = "app-1"
		def secret = "s3cr3t"
		def ts = System.currentTimeMillis().toString()
		def handler = new HandlerMethod(new ControllerAny(), ControllerAny.class.getMethod("method"))

		// 请求头
		request.getHeader("X-App-Id") >> appId
		request.getHeader("X-Timestamp") >> ts

		// 未编码的URL
		def plainUrl = "http://localhost/api/test"
		// 你的拦截器会从 request.getRequestURL() 读取这个URL
		request.getRequestURL() >> new StringBuffer(plainUrl)

		// 密钥与期望签名
		secretKeyStorer.loadSecretKey(appId) >> secret
		def expected = SignatureAlgorithm.SHA256.computeDigest([appId, secret, plainUrl, ts].join("&"))
		request.getHeader("X-Signature") >> expected

		expect:
		interceptor.preHandle(request, response, handler)
	}

	def "方法注解优先于类注解"() {
		given:
		def appId = "app-1"
		def secret = "s3cr3t"
		def handler = new HandlerMethod(new ControllerClassAnnotated(), ControllerClassAnnotated.class.getMethod("overrideMethod"))
		request.getHeader("X-App-Id") >> appId
		request.getHeader("X-Timestamp") >> System.currentTimeMillis().toString()
		request.getHeader("X-Signature") >> "x"
		request.getQueryString() >> "appId=${appId}&sign=xx"
		secretKeyStorer.loadSecretKey(appId) >> secret
		def encodedUrl = URLEncoder.encode("http://localhost/api/test", StandardCharsets.UTF_8)
		def expected = SignatureAlgorithm.SHA256.computeDigest([appId, secret, encodedUrl].join("&"))
		request.getParameter("appId") >> appId
		request.getParameter("sign") >> expected

		expect:
		interceptor.preHandle(request, response, handler)
	}

	def "类注解生效当方法无注解（头方式且URL不编码）"() {
		given:
		def appId = "app-1"
		def secret = "s3cr3t"
		def ts = System.currentTimeMillis().toString()
		def handler = new HandlerMethod(new ControllerClassAnnotated(), ControllerClassAnnotated.class.getMethod("inheritMethod"))
		request.getHeader("X-App-Id") >> appId
		request.getHeader("X-Timestamp") >> ts
		secretKeyStorer.loadSecretKey(appId) >> secret
		def plainUrl = "http://localhost/api/test"
		def expected = SignatureAlgorithm.SHA256.computeDigest([appId, secret, plainUrl, ts].join("&"))
		request.getHeader("X-Signature") >> expected

		expect:
		interceptor.preHandle(request, response, handler)
	}
}