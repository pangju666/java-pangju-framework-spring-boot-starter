package io.github.pangju666.framework.autoconfigure.web.security.utils;

import io.github.pangju666.framework.autoconfigure.web.security.context.SecurityRequestContextHolder;
import io.github.pangju666.framework.autoconfigure.web.security.model.AuthenticatedUser;

import java.io.Serializable;

public class AuthenticationUtils {
	private AuthenticationUtils() {
	}

	public static Serializable getCurrentUserId() {
		return SecurityRequestContextHolder.getAuthenticatedUser().getId();
	}

	public static AuthenticatedUser getCurrentUser() {
		return SecurityRequestContextHolder.getAuthenticatedUser();
	}
}
