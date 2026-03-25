package io.github.pangju666.framework.boot.web.log.autoconfigure

import io.github.pangju666.framework.boot.web.log.WebLog
import io.github.pangju666.framework.boot.web.log.receiver.impl.mongo.MongoWebLogReceiver
import io.github.pangju666.framework.boot.web.log.receiver.impl.mongo.WebLogDocument
import org.springframework.data.mongodb.core.MongoTemplate
import spock.lang.Specification

class MongoWebLogReceiverSpec extends Specification {
    def "receive creates collection and saves document with base prefix"() {
        given:
        def template = Mock(MongoTemplate)
        def receiver = new MongoWebLogReceiver(template, "web-log")
        def log = new WebLog()
        template.collectionExists(_ as String) >> false

        when:
        receiver.receive(log)

        then:
        1 * template.createCollection({ it.startsWith("web-log-") })
        1 * template.save({ it instanceof WebLogDocument }, { it.startsWith("web-log-") })
    }

    def "receive does not save when webLog is null"() {
        given:
        def template = Mock(MongoTemplate)
        def receiver = new MongoWebLogReceiver(template, null)
        template.collectionExists(_ as String) >> true

        when:
        receiver.receive(null)

        then:
        0 * template.save(_, _)
    }
}

