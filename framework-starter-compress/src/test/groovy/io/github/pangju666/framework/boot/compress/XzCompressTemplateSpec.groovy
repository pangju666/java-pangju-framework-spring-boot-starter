package io.github.pangju666.framework.boot.compress

import io.github.pangju666.framework.boot.compress.autoconfigure.CompressAutoConfiguration
import io.github.pangju666.framework.boot.compress.core.CompressTemplate
import io.github.pangju666.framework.boot.compress.core.impl.GzipCompressTemplate
import io.github.pangju666.framework.boot.compress.core.impl.XZCompressTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ActiveProfiles("xz")
@ContextConfiguration(classes = [CompressAutoConfiguration.class], loader = SpringBootContextLoader.class)
class XzCompressTemplateSpec extends Specification {
	@Autowired
	CompressTemplate compressTemplate

	def "test"() {
		setup:
		compressTemplate instanceof XZCompressTemplate
	}
}