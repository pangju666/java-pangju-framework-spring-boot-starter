package io.github.pangju666.framework.autoconfigure.web.advice.bind;

import io.github.pangju666.commons.lang.utils.DateUtils;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "binding", matchIfMissing = true)
@RestControllerAdvice
public class RequestParamBindingAdvice {
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				try {
					setValue(DateUtils.toDate(Long.valueOf(text)));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			}
		});

		binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				try {
					setValue(DateUtils.toLocalDate(Long.valueOf(text)));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			}
		});

		binder.registerCustomEditor(LocalDateTime.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				try {
					setValue(DateUtils.toLocalDateTime(Long.valueOf(text)));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			}
		});
	}
}
