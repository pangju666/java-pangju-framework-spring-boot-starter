package io.github.pangju666.framework.autoconfigure.web.security.store.impl;

import io.github.pangju666.framework.autoconfigure.web.security.model.AuthenticatedUser;
import io.github.pangju666.framework.autoconfigure.web.security.model.impl.PropertiesAuthenticatedUser;
import io.github.pangju666.framework.autoconfigure.web.security.properties.SecurityProperties;
import io.github.pangju666.framework.autoconfigure.web.security.store.AuthenticatedUserStore;

import java.util.Map;
import java.util.stream.Collectors;

public class PropertiesAuthenticatedUserStoreImpl implements AuthenticatedUserStore {
	private final Map<String, PropertiesAuthenticatedUser> userMap;

	public PropertiesAuthenticatedUserStoreImpl(SecurityProperties properties) {
		this.userMap = properties.getUsers()
			.stream()
			.collect(Collectors.toMap(SecurityProperties.User::getUsername, user -> {
				PropertiesAuthenticatedUser propertiesUser = new PropertiesAuthenticatedUser();
				propertiesUser.setUsername(user.getUsername());
				propertiesUser.setPassword(user.getPassword());
				propertiesUser.setRoles(user.getRoles());
				propertiesUser.setDisabled(user.isDisabled());
				propertiesUser.setLocked(user.isLocked());
				return propertiesUser;
			}));
	}

	@Override
	public AuthenticatedUser loadUserByCredentials(Object credentials) {
		return userMap.get((String) credentials);
	}
}
