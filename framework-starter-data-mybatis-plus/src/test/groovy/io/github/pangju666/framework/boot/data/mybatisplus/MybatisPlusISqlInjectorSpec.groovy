package io.github.pangju666.framework.boot.data.mybatisplus

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties
import com.baomidou.mybatisplus.core.injector.ISqlInjector
import io.github.pangju666.framework.boot.data.mybatisplus.injector.TableLogicFillSqlInjector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ActiveProfiles("datasource")
@ContextConfiguration(classes = [
	DataSourceAutoConfiguration.class,
	MybatisPlusLanguageDriverAutoConfiguration.class,
	MybatisPlusISqlInjectorAutoConfiguration.class,
	MybatisPlusAutoConfiguration.class
], loader = SpringBootContextLoader.class)
class MybatisPlusISqlInjectorSpec extends Specification {
    @Autowired
	ISqlInjector sqlInjector
	@Autowired
	MybatisPlusProperties mybatisPlusProperties

    def "测试是否正确装配Bean"() {
        expect:
		sqlInjector != null
		sqlInjector instanceof TableLogicFillSqlInjector;

		mybatisPlusProperties != null
		mybatisPlusProperties.getGlobalConfig().getSqlInjector() instanceof TableLogicFillSqlInjector
    }
}
