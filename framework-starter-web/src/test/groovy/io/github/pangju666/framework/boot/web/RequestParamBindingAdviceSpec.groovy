package io.github.pangju666.framework.boot.web


import io.github.pangju666.framework.boot.web.autoconfigure.advice.bind.RequestParamBindingAdvice
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import spock.lang.Specification

import java.time.Instant

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class RequestParamBindingAdviceSpec extends Specification {
	MockMvc mockMvc

	def setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
			.setControllerAdvice(new RequestParamBindingAdvice())
			.build()
	}

	def "should bind timestamp to Date parameter"() {
		given:
		def timestamp = "1704067200000" // 2024-01-01 00:00:00 UTC

		expect:
		mockMvc.perform(get("/test/date").param("date", timestamp))
			.andExpect(status().isOk())
			.andExpect(content().string(timestamp))
	}

	def "should bind timestamp to Instant parameter"() {
		given:
		def timestamp = "1704067200000"

		expect:
		mockMvc.perform(get("/test/instant").param("instant", timestamp))
			.andExpect(status().isOk())
			.andExpect(content().string(timestamp))
	}

	def "should fail with 400 when timestamp is invalid"() {
		expect:
		mockMvc.perform(get("/test/date").param("date", "invalid"))
			.andExpect(status().isBadRequest())
	}

	@RestController
	static class TestController {
		@GetMapping("/test/date")
		String dateApi(@RequestParam("date") Date date) {
			return String.valueOf(date.time)
		}

		@GetMapping("/test/instant")
		String instantApi(@RequestParam("instant") Instant instant) {
			return String.valueOf(instant.toEpochMilli())
		}
	}
}
