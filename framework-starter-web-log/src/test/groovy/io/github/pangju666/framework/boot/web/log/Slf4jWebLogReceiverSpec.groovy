package io.github.pangju666.framework.boot.web.log

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.github.pangju666.framework.boot.web.log.model.WebLog
import io.github.pangju666.framework.boot.web.log.receiver.impl.slf4j.Slf4jWebLogReceiver
import org.slf4j.LoggerFactory
import spock.lang.Specification

class Slf4jWebLogReceiverSpec extends Specification {
    def "receive writes JSON to specified logger"() {
        given:
        def loggerName = "TestWebLogLogger"
        def logger = (Logger) LoggerFactory.getLogger(loggerName)
        def listAppender = new ListAppender<ILoggingEvent>()
        listAppender.start()
        logger.addAppender(listAppender)
        def receiver = new Slf4jWebLogReceiver(loggerName)
        def log = new WebLog()

        when:
        receiver.receive(log)

        then:
        listAppender.list.size() == 1
        listAppender.list.get(0).formattedMessage.contains("{")

        cleanup:
        logger.detachAppender(listAppender)
    }

    def "receive ignores null webLog"() {
        given:
        def loggerName = "TestWebLogLogger2"
        def logger = (Logger) LoggerFactory.getLogger(loggerName)
        def listAppender = new ListAppender<ILoggingEvent>()
        listAppender.start()
        logger.addAppender(listAppender)
        def receiver = new Slf4jWebLogReceiver(loggerName)

        when:
        receiver.receive(null)

        then:
        listAppender.list.isEmpty()

        cleanup:
        logger.detachAppender(listAppender)
    }
}

