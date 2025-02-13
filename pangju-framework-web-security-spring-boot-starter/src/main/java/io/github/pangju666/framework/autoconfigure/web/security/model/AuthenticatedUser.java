package io.github.pangju666.framework.autoconfigure.web.security.model;

import java.io.Serializable;
import java.util.Set;

public abstract class AuthenticatedUser {
	private Serializable id;
	private String username;
	private Set<String> roles;
	private Set<String> permissions;
	private boolean disabled;

	public AuthenticatedUser() {
	}

	public Serializable getId() {
		return id;
	}

	public void setId(Serializable id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public Set<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<String> permissions) {
		this.permissions = permissions;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}
