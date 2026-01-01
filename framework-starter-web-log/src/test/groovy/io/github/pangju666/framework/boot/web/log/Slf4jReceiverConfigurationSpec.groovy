package io.github.pangju666.framework.boot.web.log

import io.github.pangju666.framework.boot.web.log.receiver.impl.slf4j.Slf4jWebLogReceiver
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import spock.lang.Specification

class Slf4jReceiverConfigurationSpec extends Specification {
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(Slf4jReceiverConfiguration))

	def "should register Slf4jWebLogReceiver when enabled and logger configured"() {
		expect:
		contextRunner
			.withPropertyValues(
				"pangju.web.log.receiver-type=SLF4J",
				"pangju.web.log.slf4j.logger=test-logger"
			)
			.withBean(WebLogProperties, {
				def p = new WebLogProperties()
				p.slf4j.logger = "test-logger"
				return p
			})
			.run { context ->
				assert context.containsBean("slf4jWebLogReceiver")
				assert context.getBean(Slf4jWebLogReceiver)
			}
	}

	def "should register Slf4jWebLogReceiver by default when logger configured"() {
		expect:
		contextRunner
			.withPropertyValues("pangju.web.log.slf4j.logger=test-logger")
			.withBean(WebLogProperties, {
				def p = new WebLogProperties()
				p.slf4j.logger = "test-logger"
				return p
			})
			.run { context ->
				assert context.containsBean("slf4jWebLogReceiver")
			}
	}

	def "should not register Slf4jWebLogReceiver if logger not configured"() {
		expect:
		contextRunner
			.withPropertyValues("pangju.web.log.receiver-type=SLF4J")
			.run { context ->
				assert !context.containsBean("slf4jWebLogReceiver")
			}
	}

	def "should not register Slf4jWebLogReceiver if receiver-type is not SLF4J"() {
		expect:
		contextRunner
			.withPropertyValues(
				"pangju.web.log.receiver-type=MONGODB",
				"pangju.web.log.slf4j.logger=test-logger"
			)
			.run { context ->
				assert !context.containsBean("slf4jWebLogReceiver")
			}
	}
}
