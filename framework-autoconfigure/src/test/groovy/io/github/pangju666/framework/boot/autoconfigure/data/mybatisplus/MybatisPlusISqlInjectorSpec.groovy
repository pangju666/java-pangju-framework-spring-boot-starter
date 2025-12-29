package io.github.pangju666.framework.boot.autoconfigure.data.mybatisplus

import com.baomidou.mybatisplus.core.injector.ISqlInjector
import io.github.pangju666.framework.boot.autoconfigure.Application
import io.github.pangju666.framework.boot.data.mybatisplus.injector.TableLogicFillSqlInjector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ActiveProfiles("mybatisplus")
@ContextConfiguration(classes = Application.class, loader = SpringBootContextLoader.class)
class MybatisPlusISqlInjectorSpec extends Specification {
    @Autowired
	ISqlInjector sqlInjector

    def "测试是否正确装配Bean"() {
        expect:
		sqlInjector != null

		sqlInjector instanceof TableLogicFillSqlInjector
    }
}
