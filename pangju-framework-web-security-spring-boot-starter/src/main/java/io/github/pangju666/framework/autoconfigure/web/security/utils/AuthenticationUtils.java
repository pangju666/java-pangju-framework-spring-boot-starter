package io.github.pangju666.framework.autoconfigure.web.security.utils;

import io.github.pangju666.framework.autoconfigure.web.security.model.AuthenticatedUser;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.Serializable;
import java.util.Objects;

public class AuthenticationUtils {
	public static final String AUTHENTICATION_ATTRIBUTE_NAME = "current_user";

	private AuthenticationUtils() {
	}

	public static Serializable getCurrentUserId() {
		AuthenticatedUser authenticatedUser = getCurrentUser();
		if (Objects.isNull(authenticatedUser)) {
			return null;
		}
		return authenticatedUser.getId();
	}

	public static AuthenticatedUser getCurrentUser() {
		return (AuthenticatedUser) RequestContextHolder.currentRequestAttributes()
			.getAttribute(AUTHENTICATION_ATTRIBUTE_NAME, RequestAttributes.SCOPE_REQUEST);
	}
}
