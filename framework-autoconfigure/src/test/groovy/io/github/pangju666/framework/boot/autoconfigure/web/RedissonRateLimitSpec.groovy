package io.github.pangju666.framework.boot.autoconfigure.web

import io.github.pangju666.framework.boot.autoconfigure.web.limit.RateLimiterAutoConfiguration
import io.github.pangju666.framework.boot.web.annotation.RateLimit
import org.redisson.api.RedissonClient
import org.redisson.spring.starter.RedissonAutoConfigurationV2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [ServletWebServerFactoryAutoConfiguration, DispatcherServletAutoConfiguration,
	RedissonAutoConfigurationV2, RedisAutoConfiguration, RateLimiterAutoConfiguration,
	WebMvcConfigurerAutoConfiguration, WebMvcAutoConfiguration, TestController, TestController2],
	properties = [
		"pangju.web.rate-limit.type=REDISSON",
		"pangju.web.rate-limit.redisson.key-prefix=rate-limit",
	]
)
@AutoConfigureMockMvc
class RedissonRateLimitSpec extends Specification {
	@Autowired
	MockMvc mockMvc
	@Autowired
	RedissonClient redissonClient

	def cleanup() {
		redissonClient.keys.deleteByPattern("rate-limit:*")
	}

	def "默认键生成 使用URI与方法"() {
		expect:
		mockMvc.perform(get("/api/test/method"))
			.andExpect(status().isOk())

		mockMvc.perform(get("/api/test/method"))
			.andExpect(status().isTooManyRequests())
	}

	def "SpEL键生成 使用请求参数"() {
		expect:
		mockMvc.perform(get("/api/test/spel").param("x", "abc"))
			.andExpect(status().isOk())

		// 相同的参数应被限流
		mockMvc.perform(get("/api/test/spel").param("x", "abc"))
			.andExpect(status().isTooManyRequests())

		// 不同的参数应通过
		mockMvc.perform(get("/api/test/spel").param("x", "def"))
			.andExpect(status().isOk())
	}

	def "SpEL解析失败 回退原始表达式"() {
		expect:
		mockMvc.perform(get("/api/test/invalid"))
			.andExpect(status().isOk())

		mockMvc.perform(get("/api/test/invalid"))
			.andExpect(status().isTooManyRequests())
	}

	def "方法注解优先于类注解"() {
		expect:
		mockMvc.perform(get("/api/test/override"))
			.andExpect(status().isOk())

		// 方法注解 rate=1
		mockMvc.perform(get("/api/test/override"))
			.andExpect(status().isTooManyRequests())
	}

	def "类注解生效 当方法无注解"() {
		expect:
		// 类注解 rate=5
		5.times {
			mockMvc.perform(get("/api/test/inherit"))
				.andExpect(status().isOk())
		}

		mockMvc.perform(get("/api/test/inherit"))
			.andExpect(status().isTooManyRequests())
	}

	def "SOURCE作用域 追加源标识"() {
		expect:
		mockMvc.perform(get("/api/test/source"))
			.andExpect(status().isOk())

		mockMvc.perform(get("/api/test/source"))
			.andExpect(status().isTooManyRequests())
	}

	def "无注解 直接放行"() {
		expect:
		10.times {
			mockMvc.perform(get("/api/test2/plain"))
				.andExpect(status().isOk())
		}
	}

	@RequestMapping("/api/test")
	@RestController
	@RateLimit(rate = 5)
	static class TestController {
		@GetMapping("/method")
		@RateLimit(rate = 1)
		void method() {}

		@GetMapping("/spel")
		@RateLimit(key = "#request.getParameter('x')", rate = 1)
		void spEl() {}

		@GetMapping("/invalid")
		@RateLimit(key = "#invalid(", rate = 1)
		void invalid() {}

		@GetMapping("/override")
		@RateLimit(rate = 1)
		void override() {}

		@GetMapping("/inherit")
		void inherit() {}

		@GetMapping("/source")
		@RateLimit(rate = 1, scope = RateLimit.RateLimitScope.SOURCE)
		void source() {}
	}

	@RequestMapping("/api/test2")
	@RestController
	static class TestController2 {
		@GetMapping("/plain")
		void plain() {}
	}
}
