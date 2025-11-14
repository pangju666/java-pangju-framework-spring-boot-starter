package io.github.pangju666.framework.boot.autoconfigure.image;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties(ImageProperties.class)
@Import({GMConfiguration.class, ImageIOConfiguration.class})
public class ImageAutoConfiguration {
}
