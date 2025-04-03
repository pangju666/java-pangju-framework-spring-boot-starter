package io.github.pangju666.framework.autoconfigure.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.executable.ExecutableValidator;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration.class)
@ConditionalOnClass(ExecutableValidator.class)
@ConditionalOnResource(resources = "classpath:META-INF/services/jakarta.validation.spi.ValidationProvider")
public class ValidationAutoConfiguration {
	@ConditionalOnMissingBean(Validator.class)
	@Bean
	public Validator validator() {
		HibernateValidatorConfiguration configuration = Validation.byProvider(HibernateValidator.class)
			.configure()
			.failFast(true);
		try (ValidatorFactory validatorFactory = configuration.buildValidatorFactory()) {
			return validatorFactory.getValidator();
		}
	}
}
