package io.github.pangju666.framework.autoconfigure.web.security.properties;

import io.github.pangju666.framework.autoconfigure.web.security.enums.TokenType;

import java.time.Duration;

public class TokenProperties {
	private TokenType type = TokenType.MAP;
	private JwtToken jwtToken = new JwtToken();
	private AccessToken accessToken = new AccessToken();

	public JwtToken getJwtToken() {
		return jwtToken;
	}

	public void setJwtToken(JwtToken jwtToken) {
		this.jwtToken = jwtToken;
	}

	public AccessToken getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(AccessToken accessToken) {
		this.accessToken = accessToken;
	}

	public TokenType getType() {
		return type;
	}

	public void setType(TokenType type) {
		this.type = type;
	}

	public static class JwtToken {
		private Duration tokenExpire = Duration.ofDays(7);

		public Duration getTokenExpire() {
			return tokenExpire;
		}

		public void setTokenExpire(Duration tokenExpire) {
			this.tokenExpire = tokenExpire;
		}
	}

	public static class AccessToken {
		private int tokenLength = 128;
		private Duration accessTokenExpire = Duration.ofHours(1);
		private Duration refreshTokenExpire = Duration.ofDays(30);
		private boolean isConcurrent = true;
		private int maxLoginCount = 12;
		private Redis redis = new Redis();

		public boolean isConcurrent() {
			return isConcurrent;
		}

		public void setConcurrent(boolean concurrent) {
			isConcurrent = concurrent;
		}

		public int getMaxLoginCount() {
			return maxLoginCount;
		}

		public void setMaxLoginCount(int maxLoginCount) {
			this.maxLoginCount = maxLoginCount;
		}

		public int getTokenLength() {
			return tokenLength;
		}

		public void setTokenLength(int tokenLength) {
			this.tokenLength = tokenLength;
		}

		public Duration getAccessTokenExpire() {
			return accessTokenExpire;
		}

		public void setAccessTokenExpire(Duration accessTokenExpire) {
			this.accessTokenExpire = accessTokenExpire;
		}

		public Duration getRefreshTokenExpire() {
			return refreshTokenExpire;
		}

		public void setRefreshTokenExpire(Duration refreshTokenExpire) {
			this.refreshTokenExpire = refreshTokenExpire;
		}

		public Redis getRedis() {
			return redis;
		}

		public void setRedis(Redis redis) {
			this.redis = redis;
		}
	}

	public static class Redis {
		private String accessTokenUserKeyPrefix = "access_token_user";
		private String refreshTokenUserKeyPrefix = "refresh_token_user";
		private String userAccessTokenSetKeyPrefix = "user_access_token";
		private String tokenSetKey = "token_set";

		public String getAccessTokenUserKeyPrefix() {
			return accessTokenUserKeyPrefix;
		}

		public void setAccessTokenUserKeyPrefix(String accessTokenUserKeyPrefix) {
			this.accessTokenUserKeyPrefix = accessTokenUserKeyPrefix;
		}

		public String getRefreshTokenUserKeyPrefix() {
			return refreshTokenUserKeyPrefix;
		}

		public void setRefreshTokenUserKeyPrefix(String refreshTokenUserKeyPrefix) {
			this.refreshTokenUserKeyPrefix = refreshTokenUserKeyPrefix;
		}

		public String getUserAccessTokenSetKeyPrefix() {
			return userAccessTokenSetKeyPrefix;
		}

		public void setUserAccessTokenSetKeyPrefix(String userAccessTokenSetKeyPrefix) {
			this.userAccessTokenSetKeyPrefix = userAccessTokenSetKeyPrefix;
		}

		public String getTokenSetKey() {
			return tokenSetKey;
		}

		public void setTokenSetKey(String tokenSetKey) {
			this.tokenSetKey = tokenSetKey;
		}
	}
}
