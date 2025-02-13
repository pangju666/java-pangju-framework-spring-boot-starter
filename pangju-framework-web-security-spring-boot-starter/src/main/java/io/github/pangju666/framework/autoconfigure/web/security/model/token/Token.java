package io.github.pangju666.framework.autoconfigure.web.security.model.token;

import java.io.Serializable;
import java.util.Date;

public class Token implements Serializable {
	private final String token;
	private final Serializable userId;
	private Date expireTime;

	public Token(String token, Serializable userId) {
		this.token = token;
		this.userId = userId;
	}

	public String getToken() {
		return token;
	}

	public Date getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	public Serializable getUserId() {
		return userId;
	}
}
