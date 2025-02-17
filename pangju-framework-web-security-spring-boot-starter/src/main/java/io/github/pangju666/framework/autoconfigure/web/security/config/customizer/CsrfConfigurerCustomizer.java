package io.github.pangju666.framework.autoconfigure.web.security.config.customizer;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;

public interface CsrfConfigurerCustomizer extends Customizer<CsrfConfigurer<HttpSecurity>> {
}
