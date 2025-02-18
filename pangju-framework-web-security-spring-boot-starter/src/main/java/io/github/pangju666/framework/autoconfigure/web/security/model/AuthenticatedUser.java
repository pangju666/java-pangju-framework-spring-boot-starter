package io.github.pangju666.framework.autoconfigure.web.security.model;

import java.io.Serializable;
import java.util.Set;

public interface AuthenticatedUser {
	Object getPrincipal();

	Object getCredentials();

	Set<String> getRoles();

	Set<String> getPermissions();

	Serializable getId();

	boolean isLocked();

	boolean isDisabled();
}
