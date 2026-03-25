package io.github.pangju666.framework.boot.web.log.autoconfigure

import com.jayway.jsonpath.JsonPath
import io.github.pangju666.framework.boot.web.autoconfigure.WebMvcConfigurerAutoConfiguration
import io.github.pangju666.framework.boot.web.log.annotation.WebLogOperation
import io.github.pangju666.framework.web.model.Result
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletAutoConfiguration
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ActiveProfiles("log-kafka-mongodb")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	classes = [DispatcherServletAutoConfiguration,
		JacksonAutoConfiguration, KafkaAutoConfiguration, MongoAutoConfiguration, DataMongoAutoConfiguration,
		WebLogAutoConfiguration, WebMvcConfigurerAutoConfiguration, WebMvcAutoConfiguration, TestController]
)
@TestPropertySource(properties = [
	"server.port=0",
	"pangju.web.log.mongo.base-collection-name=web-log"
])
class WebLogKafkaMongoSpec extends Specification {
	@LocalServerPort
	int port

	@Autowired
	MongoTemplate mongoTemplate

	def setup() {
		RestAssured.port = port
		RestAssured.baseURI = "http://localhost"
	}

	def "端到端：使用Kafka接收器与MongoDB发送器写入日志文件"() {
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
			def date = LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT))
			def logs = mongoTemplate.findAll(WebLogDocument.class, "web-log-" + date)
			def log = logs.last()
			assert log != null
			def text = JsonUtils.toString(log)
			assert JsonPath.read(text, "\$.operation") == "测试接口"
			assert JsonPath.read(text, "\$.method") == "POST"
			assert JsonPath.read(text, "\$.url") == "/api/echo"
			assert JsonPath.read(text, "\$.response.body.code") == 0
			assert JsonPath.read(text, "\$.response.body.message") == "请求成功"
			assert JsonPath.read(text, "\$.response.status") == 200
		}
	}

	@RestController
	static class TestController {
		@WebLogOperation("测试接口")
		@PostMapping("/api/echo")
		Result<Map<String, Object>> echo(@RequestBody Map<String, Object> payload) {
			return Result.ok([message: payload.get("message")])
		}
	}
}
