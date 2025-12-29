package io.github.pangju666.framework.boot.autoconfigure.web.log


import io.github.pangju666.framework.boot.web.log.receiver.WebLogReceiver
import io.github.pangju666.framework.boot.web.log.sender.WebLogSender
import io.github.pangju666.framework.boot.web.log.sender.impl.kafka.KafkaWebLogSender
import io.github.pangju666.framework.boot.web.log.sender.impl.kafka.WebLogKafkaListener
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.kafka.core.KafkaTemplate
import spock.lang.Specification

class KafkaSenderConfigurationSpec extends Specification {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KafkaSenderConfiguration))

    def "should register Kafka components when enabled and topic configured"() {
        expect:
        contextRunner
                .withPropertyValues(
                        "pangju.web.log.sender-type=KAFKA",
                        "pangju.web.log.kafka.topic=test-topic"
                )
                .withBean(KafkaTemplate, { -> Mock(KafkaTemplate) })
                .withBean(WebLogReceiver, { -> Mock(WebLogReceiver) })
                .withBean(WebLogProperties, {
                    def p = new WebLogProperties()
                    p.kafka.topic = "test-topic"
                    return p
                })
                .run { context ->
                    assert context.containsBean("kafkaWebLogSender")
                    assert context.containsBean("webLogKafkaListener")
                    assert context.getBean(KafkaWebLogSender)
                    assert context.getBean(WebLogKafkaListener)
                }
    }

    def "should use specific KafkaTemplate if configured"() {
        given:
        def customKafkaTemplate = Mock(KafkaTemplate)

        expect:
        contextRunner
                .withPropertyValues(
                        "pangju.web.log.sender-type=KAFKA",
                        "pangju.web.log.kafka.topic=test-topic",
                        "pangju.web.log.kafka.kafka-template-ref=customKafkaTemplate"
                )
                .withBean("customKafkaTemplate", KafkaTemplate, { -> customKafkaTemplate })
                .withBean(WebLogProperties, {
                    def p = new WebLogProperties()
                    p.kafka.topic = "test-topic"
                    p.kafka.kafkaTemplateRef = "customKafkaTemplate"
                    return p
                })
                .run { context ->
                    assert context.containsBean("kafkaWebLogSender")
                }
    }

    def "should not register Kafka components if sender-type is not KAFKA"() {
        expect:
        contextRunner
                .withPropertyValues(
                        "pangju.web.log.sender-type=DISRUPTOR",
                        "pangju.web.log.kafka.topic=test-topic"
                )
                .withBean(KafkaTemplate, { -> Mock(KafkaTemplate) })
                .run { context ->
                    assert !context.containsBean("kafkaWebLogSender")
                    assert !context.containsBean("webLogKafkaListener")
                }
    }

    def "should not register Kafka components if topic is missing"() {
        expect:
        contextRunner
                .withPropertyValues("pangju.web.log.sender-type=KAFKA")
                .withBean(KafkaTemplate, { -> Mock(KafkaTemplate) })
                .run { context ->
                    assert !context.containsBean("kafkaWebLogSender")
                    assert !context.containsBean("webLogKafkaListener")
                }
    }

    def "should back off if WebLogSender already exists"() {
        expect:
        contextRunner
                .withPropertyValues(
                        "pangju.web.log.sender-type=KAFKA",
                        "pangju.web.log.kafka.topic=test-topic"
                )
                .withBean(KafkaTemplate, { -> Mock(KafkaTemplate) })
                .withBean(WebLogSender, { -> Mock(WebLogSender) })
                .run { context ->
                    assert !context.containsBean("kafkaWebLogSender")
                }
    }

    def "should not register WebLogKafkaListener if WebLogReceiver is missing"() {
        expect:
        contextRunner
                .withPropertyValues(
                        "pangju.web.log.sender-type=KAFKA",
                        "pangju.web.log.kafka.topic=test-topic"
                )
                .withBean(KafkaTemplate, { -> Mock(KafkaTemplate) })
                .withBean(WebLogProperties, {
                    def p = new WebLogProperties()
                    p.kafka.topic = "test-topic"
                    return p
                })
                .run { context ->
                    assert context.containsBean("kafkaWebLogSender")
                    assert !context.containsBean("webLogKafkaListener")
                }
    }
}
