package io.github.pangju666.framework.autoconfigure.web.security.model.impl;

import io.github.pangju666.framework.autoconfigure.web.security.model.AuthenticatedUser;

import java.io.Serializable;
import java.util.Set;

public class PropertiesAuthenticatedUser implements AuthenticatedUser {
	private String username;
	private String password;
	private Set<String> roles;
	private boolean disabled;
	private boolean locked;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public Object getPrincipal() {
		return username;
	}

	@Override
	public Object getCredentials() {
		return password;
	}

	@Override
	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	@Override
	public Set<String> getPermissions() {
		return roles;
	}

	@Override
	public Serializable getId() {
		return username;
	}

	@Override
	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}
