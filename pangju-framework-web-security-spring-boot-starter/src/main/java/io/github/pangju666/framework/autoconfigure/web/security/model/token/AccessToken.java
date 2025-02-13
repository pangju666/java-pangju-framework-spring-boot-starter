package io.github.pangju666.framework.autoconfigure.web.security.model.token;

import java.io.Serializable;

public class AccessToken extends Token {
	private final Token refreshToken;

	public AccessToken(String accessToken, String refreshToken, Serializable userId) {
		super(accessToken, userId);
		this.refreshToken = new Token(refreshToken, userId);
	}

	public AccessToken(String accessToken, Token refreshToken) {
		super(accessToken, refreshToken.getUserId());
		this.refreshToken = refreshToken;
	}

	public Token getRefreshToken() {
		return refreshToken;
	}
}