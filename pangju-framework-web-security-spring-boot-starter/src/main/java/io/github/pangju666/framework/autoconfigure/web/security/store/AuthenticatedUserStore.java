package io.github.pangju666.framework.autoconfigure.web.security.store;

import io.github.pangju666.framework.autoconfigure.web.security.model.AuthenticatedUser;

public interface AuthenticatedUserStore {
	AuthenticatedUser loadUserByCredentials(Object principal);
}
