package io.github.pangju666.framework.autoconfigure.web.security.config;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;

public interface FormLoginConfigurerCustomizer extends Customizer<FormLoginConfigurer<HttpSecurity>> {
}
