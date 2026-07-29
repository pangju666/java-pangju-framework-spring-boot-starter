package io.github.pangju666.framework.boot.ocr

import io.github.pangju666.commons.io.resource.IOResource
import io.github.pangju666.framework.boot.ocr.autoconfigure.OcrAutoConfiguration
import io.github.pangju666.framework.boot.ocr.core.OcrTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ActiveProfiles("tesseract_cli")
@ContextConfiguration(classes = [OcrAutoConfiguration.class], loader = SpringBootContextLoader.class)
class TesseractCliOcrTemplateSpec extends Specification {
	@Autowired
	OcrTemplate ocrTemplate

	def "test"() {
		setup:
		File file = new ClassPathResource("images/" + "test.png").getFile()
		println ocrTemplate.ocrImage(new IOResource(file))
	}
}