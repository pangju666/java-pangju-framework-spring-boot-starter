package io.github.pangju666.framework.boot.autoconfigure.web.log

import io.github.pangju666.framework.boot.web.log.model.WebLog
import io.github.pangju666.framework.boot.web.log.sender.impl.kafka.KafkaWebLogSender
import org.springframework.kafka.core.KafkaTemplate
import spock.lang.Specification

class KafkaWebLogSenderSpec extends Specification {
    def "send delegates to KafkaTemplate"() {
        given:
        def template = Mock(KafkaTemplate)
        def sender = new KafkaWebLogSender(template, "web-log-topic")
        def log = new WebLog()

        when:
        sender.send(log)

        then:
        1 * template.send("web-log-topic", log)
    }
}

