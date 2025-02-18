package io.github.pangju666.framework.autoconfigure.web.security.model;

import java.io.Serializable;

public class Token implements Serializable {
	private final String token;
	private final Serializable userId;
	private Long expireTime;

	public Token(String token, Serializable userId) {
		this.token = token;
		this.userId = userId;
	}

	public String getToken() {
		return token;
	}

	public Long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Long expireTime) {
		this.expireTime = expireTime;
	}

	public Serializable getUserId() {
		return userId;
	}
}
