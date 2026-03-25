package io.github.pangju666.framework.boot.web.log.autoconfigure

import io.github.pangju666.framework.boot.web.log.WebLog
import io.github.pangju666.framework.boot.web.log.receiver.WebLogReceiver
import io.github.pangju666.framework.boot.web.log.sender.impl.disruptor.DisruptorWebLogEventHandler
import io.github.pangju666.framework.boot.web.log.sender.impl.disruptor.DisruptorWebLogSender
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.atomic.AtomicReference

class DisruptorWebLogSenderSpec extends Specification {
    def "send publishes event to receiver"() {
        given:
        def received = new AtomicReference<WebLog>()
        def receiver = new WebLogReceiver() {
            @Override
            void receive(WebLog webLog) {
                received.set(webLog)
            }
        }
        def handler = new DisruptorWebLogEventHandler(receiver)
        def sender = new DisruptorWebLogSender(16, handler)
        def log = new WebLog()
        def conditions = new PollingConditions(timeout: 2, initialDelay: 0.05, delay: 0.05)

        when:
        sender.send(log)

        then:
        conditions.eventually {
            assert received.get() == log
        }
    }
}

