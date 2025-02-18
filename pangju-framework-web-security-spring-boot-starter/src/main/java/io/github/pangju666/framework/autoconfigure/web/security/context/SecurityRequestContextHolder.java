package io.github.pangju666.framework.autoconfigure.web.security.context;

import io.github.pangju666.framework.autoconfigure.web.security.model.AuthenticatedUser;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public final class SecurityRequestContextHolder {
	private SecurityRequestContextHolder() {
	}

	public static String getToken() {
		return (String) RequestContextHolder.currentRequestAttributes().getAttribute("token", RequestAttributes.SCOPE_REQUEST);
	}

	public static void setToken(String token) {
		RequestContextHolder.currentRequestAttributes().setAttribute("token", token, RequestAttributes.SCOPE_REQUEST);
	}

	public static void removeToken() {
		RequestContextHolder.currentRequestAttributes().removeAttribute("token", RequestAttributes.SCOPE_REQUEST);
	}

	public static AuthenticatedUser getAuthenticatedUser() {
		return (AuthenticatedUser) RequestContextHolder.currentRequestAttributes()
			.getAttribute("authenticated_user", RequestAttributes.SCOPE_REQUEST);
	}

	public static void setAuthenticatedUser(AuthenticatedUser authenticatedUser) {
		RequestContextHolder.currentRequestAttributes()
			.setAttribute("authenticated_user", authenticatedUser, RequestAttributes.SCOPE_REQUEST);
	}

	public static void removeAuthenticatedUser() {
		RequestContextHolder.currentRequestAttributes()
			.removeAttribute("authenticated_user", RequestAttributes.SCOPE_REQUEST);
	}
}
