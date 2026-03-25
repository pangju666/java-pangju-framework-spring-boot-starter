package io.github.pangju666.framework.boot.web.log.autoconfigure

import io.github.pangju666.framework.boot.web.log.filter.WebLogFilter
import io.github.pangju666.framework.boot.web.log.handler.impl.JsonBodyHandler
import io.github.pangju666.framework.boot.web.log.handler.impl.TextBodyHandler
import io.github.pangju666.framework.boot.web.log.sender.WebLogSender
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import org.springframework.boot.web.servlet.FilterRegistrationBean
import spock.lang.Specification

class WebLogAutoConfigurationSpec extends Specification {
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebLogAutoConfiguration))

    def "should not register beans when disabled"() {
        expect:
        contextRunner.run { context ->
            assert !context.containsBean("webLogFilterRegistrationBean")
            assert !context.containsBean("jsonBodyHandler")
            assert !context.containsBean("textBodyHandler")
            assert !context.containsBean("webLogInterceptor")
        }
    }

    def "should register default beans when enabled"() {
        expect:
        contextRunner
                .withPropertyValues("pangju.web.log.enabled=true")
                .run { context ->
                    assert context.containsBean("jsonBodyHandler")
                    assert context.containsBean("textBodyHandler")
                    assert context.containsBean("webLogInterceptor")
                }
    }

    def "should register WebLogFilter when enabled and WebLogSender exists"() {
        expect:
        contextRunner
                .withPropertyValues("pangju.web.log.enabled=true")
                .withBean(WebLogSender, { -> Mock(WebLogSender) })
                .run { context ->
                    assert context.containsBean("webLogFilterRegistrationBean")
                    def registrationBean = context.getBean("webLogFilterRegistrationBean", FilterRegistrationBean)
                    assert registrationBean.filter instanceof WebLogFilter
                    assert registrationBean.urlPatterns.contains("/*")
                    assert registrationBean.order == Integer.MIN_VALUE + 2
                }
    }

    def "should back off if beans already exist"() {
        expect:
        contextRunner
                .withPropertyValues("pangju.web.log.enabled=true")
                .withBean("jsonBodyHandler", JsonBodyHandler, { -> new JsonBodyHandler() })
                .withBean("textBodyHandler", TextBodyHandler, { -> new TextBodyHandler() })
                .run { context ->
                    assert context.containsBean("jsonBodyHandler")
                    assert context.containsBean("textBodyHandler")
                }
    }
}
