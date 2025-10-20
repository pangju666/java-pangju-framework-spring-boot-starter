package io.github.pangju666.framework.autoconfigure.web.advice;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties({AdviceProperties.class})
public class AdviceAutoConfiguration {
}
