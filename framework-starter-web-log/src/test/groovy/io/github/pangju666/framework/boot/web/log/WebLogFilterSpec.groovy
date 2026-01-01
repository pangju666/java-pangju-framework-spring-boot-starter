package io.github.pangju666.framework.boot.web.log

import io.github.pangju666.framework.boot.web.log.configuration.WebLogConfiguration
import io.github.pangju666.framework.boot.web.log.filter.WebLogFilter
import io.github.pangju666.framework.boot.web.log.handler.MediaTypeBodyHandler
import io.github.pangju666.framework.boot.web.log.handler.impl.JsonBodyHandler
import io.github.pangju666.framework.boot.web.log.handler.impl.TextBodyHandler
import io.github.pangju666.framework.boot.web.log.model.WebLog
import io.github.pangju666.framework.boot.web.log.sender.WebLogSender
import io.github.pangju666.framework.web.model.Result
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification

class WebLogFilterSpec extends Specification {

    private WebLogConfiguration configuration
    private List<MediaTypeBodyHandler> bodyHandlers

    def setup() {
        configuration = new WebLogConfiguration()
        configuration.request.body = true
        configuration.request.headers = true
        configuration.request.queryParams = true
        configuration.request.acceptableMediaTypes = [MediaType.APPLICATION_JSON]

        configuration.response.body = true
        configuration.response.headers = true
        configuration.response.resultData = true
        configuration.response.acceptableMediaTypes = [MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN]

        bodyHandlers = [new JsonBodyHandler(), new TextBodyHandler()]
    }

    def "should capture JSON request and Result JSON response"() {
        given:
        def sender = Mock(WebLogSender)
        def filter = new WebLogFilter(configuration, sender, Set.of(), bodyHandlers, List.of())
        def request = new MockHttpServletRequest("POST", "/api/test")
        request.setCharacterEncoding("UTF-8")
        request.setContentType(MediaType.APPLICATION_JSON_VALUE)
        request.setContent('{"a":1}'.getBytes("UTF-8"))
        def response = new MockHttpServletResponse()
        def chain = new FilterChain() {
            @Override
            void doFilter(ServletRequest req, ServletResponse res) {
                // 模拟业务读取请求体以触发 ContentCachingRequestWrapper 缓存
                req.getInputStream().readAllBytes()
                // 写入符合 Result 结构的 JSON 响应
                res.setContentType(MediaType.APPLICATION_JSON_VALUE)
                def json = '{"code":200,"message":"OK","data":{"x":1}}'
                res.getOutputStream().write(json.getBytes("UTF-8"))
            }
        }

        when:
        filter.doFilter(request, response, chain)

        then:
        1 * sender.send({ WebLog wl ->
            assert wl != null
            assert wl.method == "POST"
            assert wl.url == "/api/test"
            assert wl.costMillis >= 0
            assert wl.request != null
            assert wl.request.contentType == MediaType.APPLICATION_JSON_VALUE
            assert wl.request.body != null
            assert wl.response != null
            assert wl.response.contentType == MediaType.APPLICATION_JSON_VALUE
            assert wl.response.status == 200
            assert wl.response.body instanceof Result
            assert ((Result) wl.response.body).code() == 200
            assert ((Result) wl.response.body).message() == "OK"
            // data 字段应包含我们写入的内容
            assert ((Result) wl.response.body).data() instanceof Map
            assert ((Map) ((Result) wl.response.body).data()).get("x") == 1
            true
        })
    }

    def "should capture text/plain response via TextBodyHandler"() {
        given:
        def sender = Mock(WebLogSender)
        def filter = new WebLogFilter(configuration, sender, Set.of(), bodyHandlers, List.of())
        def request = new MockHttpServletRequest("GET", "/ping")
        def response = new MockHttpServletResponse()
        def chain = new FilterChain() {
            @Override
            void doFilter(ServletRequest req, ServletResponse res) {
                res.setContentType(MediaType.TEXT_PLAIN_VALUE)
                res.getOutputStream().write("pong".getBytes("UTF-8"))
            }
        }

        when:
        filter.doFilter(request, response, chain)

        then:
        1 * sender.send({ WebLog wl ->
            assert wl != null
            assert wl.response.contentType == MediaType.TEXT_PLAIN_VALUE
            assert wl.response.body == "pong"
            true
        })
    }

    def "should ignore request body when content type not acceptable"() {
        given:
        def sender = Mock(WebLogSender)
        def filter = new WebLogFilter(configuration, sender, Set.of(), bodyHandlers, List.of())
        def request = new MockHttpServletRequest("POST", "/api/xml")
        request.setContentType(MediaType.APPLICATION_XML_VALUE)
        request.setContent("<a>1</a>".getBytes("UTF-8"))
        def response = new MockHttpServletResponse()
        def chain = new FilterChain() {
            @Override
            void doFilter(ServletRequest req, ServletResponse res) {
                res.setContentType(MediaType.TEXT_PLAIN_VALUE)
                res.getOutputStream().write("ok".getBytes("UTF-8"))
            }
        }

        when:
        filter.doFilter(request, response, chain)

        then:
        1 * sender.send({ WebLog wl ->
            assert wl.request.body == null
            assert wl.response.body == "ok"
            true
        })
    }
}

