package io.github.pangju666.framework.boot.web.log

import com.lmax.disruptor.dsl.Disruptor
import io.github.pangju666.framework.boot.autoconfigure.web.log.WebLogProperties
import io.github.pangju666.framework.boot.web.log.receiver.WebLogReceiver
import io.github.pangju666.framework.boot.web.log.sender.WebLogSender
import io.github.pangju666.framework.boot.web.log.sender.impl.disruptor.DisruptorWebLogEventHandler
import io.github.pangju666.framework.boot.web.log.sender.impl.disruptor.DisruptorWebLogSender
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import spock.lang.Specification

class DisruptorSenderConfigurationSpec extends Specification {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DisruptorSenderConfiguration))

    def "should register Disruptor components when enabled and receiver exists"() {
        expect:
        contextRunner
                .withPropertyValues("pangju.web.log.sender-type=DISRUPTOR")
                .withBean(WebLogProperties, { -> new WebLogProperties() })
                .withBean(WebLogReceiver, { -> Mock(WebLogReceiver) })
                .run { context ->
                    assert context.containsBean("disruptorWebLogEventHandler")
                    assert context.containsBean("disruptorWebLogSender")
                    assert context.getBean(DisruptorWebLogEventHandler)
                    assert context.getBean(DisruptorWebLogSender)
                }
    }

    def "should register Disruptor components by default when receiver exists"() {
        expect:
        contextRunner
                .withBean(WebLogProperties, { -> new WebLogProperties() })
                .withBean(WebLogReceiver, { -> Mock(WebLogReceiver) })
                .run { context ->
                    assert context.containsBean("disruptorWebLogEventHandler")
                    assert context.containsBean("disruptorWebLogSender")
                }
    }

    def "should not register Disruptor components if sender-type is not DISRUPTOR"() {
        expect:
        contextRunner
                .withPropertyValues("pangju.web.log.sender-type=KAFKA")
                .withBean(WebLogReceiver, { -> Mock(WebLogReceiver) })
                .run { context ->
                    assert !context.containsBean("disruptorWebLogEventHandler")
                    assert !context.containsBean("disruptorWebLogSender")
                }
    }

    def "should not register Disruptor components if WebLogReceiver is missing"() {
        expect:
        contextRunner
                .withPropertyValues("pangju.web.log.sender-type=DISRUPTOR")
                .run { context ->
                    assert !context.containsBean("disruptorWebLogEventHandler")
                    // disruptorWebLogSender depends on disruptorWebLogEventHandler
                    assert !context.containsBean("disruptorWebLogSender")
                }
    }

    def "should back off if WebLogSender already exists"() {
        expect:
        contextRunner
                .withPropertyValues("pangju.web.log.sender-type=DISRUPTOR")
                .withBean(WebLogReceiver, { -> Mock(WebLogReceiver) })
                .withBean(WebLogSender, { -> Mock(WebLogSender) })
                .run { context ->
                    // Event handler might still be registered as it's not conditional on WebLogSender missing
                    assert context.containsBean("disruptorWebLogEventHandler")
                    // Sender should not be registered
                    assert !context.containsBean("disruptorWebLogSender")
                }
    }
}
