package io.github.pangju666.framework.boot.autoconfigure.image;

import io.github.pangju666.commons.image.utils.ImageEditor;
import io.github.pangju666.framework.boot.image.core.ImageTemplate;
import io.github.pangju666.framework.boot.image.core.impl.BufferedImageTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ImageEditor.class})
@ConditionalOnProperty(prefix = "pangju.image", name = "type", havingValue = "IMAGEIO", matchIfMissing = true)
class ImageIOConfiguration {
	@ConditionalOnMissingBean(ImageTemplate.class)
	@Bean
	public BufferedImageTemplate bufferImageTemplate() {
		return new BufferedImageTemplate();
	}
}
