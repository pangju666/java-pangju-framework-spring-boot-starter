package io.github.pangju666.framework.autoconfigure.web.authenticate.model;

import java.util.Set;

public class AuthenticatedUser {
	private String username;
	private Set<String> roles;
	private String token;

	public AuthenticatedUser() {
	}

	public AuthenticatedUser(String username, Set<String> roles, String token) {
		this.username = username;
		this.roles = roles;
		this.token = token;
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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
