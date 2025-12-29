package io.github.pangju666.framework.boot.autoconfigure.web.log

import com.jayway.jsonpath.JsonPath
import io.github.pangju666.framework.boot.autoconfigure.web.WebMvcConfigurerAutoConfiguration
import io.github.pangju666.framework.web.model.Result
import io.restassured.RestAssured
import org.apache.commons.io.FileUtils
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@ActiveProfiles("log-kafka-slf4j")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	classes = [ServletWebServerFactoryAutoConfiguration, DispatcherServletAutoConfiguration,
		JacksonAutoConfiguration, KafkaAutoConfiguration,
		WebLogAutoConfiguration, WebMvcConfigurerAutoConfiguration, WebMvcAutoConfiguration, TestController]
)
@TestPropertySource(properties = [
	"server.port=0"
])
class WebLogKafkaSlf4jSpec extends Specification {
	private File logFile = new File("E:/logs/web/web.log")

	@LocalServerPort
	int port

	def setup() {
		RestAssured.port = port
		RestAssured.baseURI = "http://localhost"

		if (logFile.exists()) {
			logFile.delete()
		}
		logFile.parentFile.mkdirs()
	}

	def cleanup() {
		FileUtils.forceDeleteOnExit(logFile)
	}

	def "端到端：使用SLF4J接收器与Kafka发送器写入日志文件"() {
		given:
		def conditions = new PollingConditions(timeout: 10, delay: 0.5)

		when:
		def response = RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
			.body("{\"message\": \"hello\"}")
			.post("/api/echo")

		then:
		response.statusCode() == 200

		and:
		conditions.eventually {
			assert logFile.exists()
			def text = logFile.getText("UTF-8")
			assert JsonPath.read(text, "\$.method") == "POST"
			assert JsonPath.read(text, "\$.url") == "/api/echo"
			assert JsonPath.read(text, "\$.response.body.code") == 0
			assert JsonPath.read(text, "\$.response.body.message") == "请求成功"
			assert JsonPath.read(text, "\$.response.status") == 200
		}
	}

	@RestController
	static class TestController {
		@PostMapping("/api/echo")
		Result<Map<String, Object>> echo(@RequestBody Map<String, Object> payload) {
			return Result.ok([message: payload.get("message")])
		}
	}
}
