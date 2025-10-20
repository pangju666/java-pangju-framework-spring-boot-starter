package io.github.pangju666.framework.autoconfigure.web.advice;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pangju.web.advice")
public class AdviceProperties {
	private boolean binding = true;
	private boolean exception = true;

	public boolean isBinding() {
		return binding;
	}

	public void setBinding(boolean binding) {
		this.binding = binding;
	}

	public boolean isException() {
		return exception;
	}

	public void setException(boolean exception) {
		this.exception = exception;
	}
}
