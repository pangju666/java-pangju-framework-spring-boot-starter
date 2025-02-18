package io.github.pangju666.framework.autoconfigure.web.security.authentication;

import io.github.pangju666.framework.autoconfigure.web.security.model.AuthenticatedUser;
import io.github.pangju666.framework.core.exception.authentication.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;

public interface Authenticator {
	String getRequestUrl();

	AuthenticatedUser authenticate(HttpServletRequest request) throws AuthenticationException;
}
