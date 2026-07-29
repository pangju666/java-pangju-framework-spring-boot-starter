package io.github.pangju666.framework.boot.compress

import io.github.pangju666.framework.boot.compress.autoconfigure.CompressAutoConfiguration
import io.github.pangju666.framework.boot.compress.core.ArchiveTemplate
import io.github.pangju666.framework.boot.compress.core.impl.TarArchiveTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ActiveProfiles("tar")
@ContextConfiguration(classes = [CompressAutoConfiguration.class], loader = SpringBootContextLoader.class)
class TarCompressTemplateSpec extends Specification {
	@Autowired
	ArchiveTemplate archiveTemplate

	def "test"() {
		setup:
		archiveTemplate instanceof TarArchiveTemplate
	}
}