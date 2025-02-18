package io.github.pangju666.framework.autoconfigure.web.security.store.impl;

import io.github.pangju666.framework.autoconfigure.web.security.model.AccessToken;
import io.github.pangju666.framework.autoconfigure.web.security.model.AuthenticatedUser;
import io.github.pangju666.framework.autoconfigure.web.security.model.Token;
import io.github.pangju666.framework.autoconfigure.web.security.properties.SecurityProperties;
import io.github.pangju666.framework.autoconfigure.web.security.store.AccessTokenStore;
import io.github.pangju666.framework.core.exception.authentication.AuthenticationException;
import io.github.pangju666.framework.core.exception.authentication.AuthenticationExpireException;
import io.github.pangju666.framework.core.lang.pool.Constants;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MapAccessTokenStoreImpl implements AccessTokenStore {
	private final ExpiringMap<String, AuthenticatedUser> accessTokenUserExpiringMap;
	private final ExpiringMap<String, AuthenticatedUser> refreshTokenUserExpiringMap;
	private final Map<String, Set<AccessToken>> userAccessTokenMap;
	private final Set<String> tokenSet;
	private final SecurityProperties properties;

	public MapAccessTokenStoreImpl(SecurityProperties properties) {
		this.accessTokenUserExpiringMap = ExpiringMap.builder()
			.variableExpiration()
			.expiration(properties.getToken().getAccessTokenExpire().toMillis(), TimeUnit.MILLISECONDS)
			.build();
		this.refreshTokenUserExpiringMap = ExpiringMap.builder()
			.variableExpiration()
			.expiration(properties.getToken().getRefreshTokenExpire().toMillis(), TimeUnit.MILLISECONDS)
			.build();
		this.userAccessTokenMap = new ConcurrentHashMap<>();
		this.tokenSet = ConcurrentHashMap.newKeySet();
		this.properties = properties;
	}

	@Override
	public AccessToken generateToken(AuthenticatedUser authenticatedUser) {
		if (!properties.getToken().isConcurrent()) {
			removeToken(authenticatedUser.getId());
		}
		return createTokenByUser(authenticatedUser);
	}

	@Override
	public Set<AccessToken> getAccessTokens(Serializable userId) {
		return userAccessTokenMap.get(userId.toString());
	}

	@Override
	public AuthenticatedUser getAuthenticatedUser(String accessToken) {
		return accessTokenUserExpiringMap.get(accessToken);
	}

	@Override
	public void removeToken(Serializable userId) {
		Set<AccessToken> accessTokens = userAccessTokenMap.get(userId.toString());
		if (CollectionUtils.isNotEmpty(accessTokens)) {
			for (AccessToken accessToken : accessTokens) {
				refreshTokenUserExpiringMap.remove(accessToken.getRefreshToken().getToken());
				accessTokenUserExpiringMap.remove(accessToken.getToken());
				tokenSet.remove(accessToken.getToken());
				tokenSet.remove(accessToken.getRefreshToken().getToken());
			}
		}
		userAccessTokenMap.remove(userId.toString());
	}

	@Override
	public AccessToken refreshToken(String refreshToken) {
		refreshToken = StringUtils.substringAfter(refreshToken, Constants.TOKEN_PREFIX);

		AuthenticatedUser authenticatedUser = refreshTokenUserExpiringMap.get(refreshToken);
		if (Objects.isNull(authenticatedUser)) {
			throw new AuthenticationExpireException("无效的token");
		}

		refreshTokenUserExpiringMap.remove(refreshToken);
		tokenSet.remove(refreshToken);

		Set<AccessToken> accessTokens = SetUtils.emptyIfNull(userAccessTokenMap.get(authenticatedUser.getId().toString()));
		for (AccessToken accessToken : accessTokens) {
			if (accessToken.getRefreshToken().getToken().equals(refreshToken)) {
				accessTokens.remove(accessToken);
				accessTokenUserExpiringMap.remove(accessToken.getToken());
				tokenSet.remove(accessToken.getToken());
				break;
			}
		}

		return generateToken(authenticatedUser);
	}

	private AccessToken createTokenByUser(AuthenticatedUser authenticatedUser) {
		if (properties.getToken().isConcurrent() && properties.getToken().getMaxLoginCount() > -1) {
			Set<AccessToken> userAccessTokens = SetUtils.emptyIfNull(userAccessTokenMap.get(
				authenticatedUser.getId().toString()));
			if (userAccessTokens.size() >= properties.getToken().getMaxLoginCount()) {
				throw new AuthenticationException("已达到最大登录数量");
			}
		}

		AccessToken accessToken = createTokenByUserId(authenticatedUser.getId());
		accessTokenUserExpiringMap.put(accessToken.getToken(), authenticatedUser);
		refreshTokenUserExpiringMap.put(accessToken.getRefreshToken().getToken(), authenticatedUser);
		Set<AccessToken> userAccessTokens = userAccessTokenMap.get(authenticatedUser.getId().toString());
		if (Objects.isNull(userAccessTokens)) {
			userAccessTokens = ConcurrentHashMap.newKeySet();
			userAccessTokenMap.put(authenticatedUser.getId().toString(), userAccessTokens);
		}
		userAccessTokens.add(accessToken);
		return accessToken;
	}

	private AccessToken createTokenByUserId(Serializable userId) {
		String refreshTokenStr;
		do {
			refreshTokenStr = RandomStringUtils.secureStrong().nextAlphanumeric(properties.getToken().getTokenLength());
		} while (tokenSet.contains(refreshTokenStr));
		tokenSet.add(refreshTokenStr);

		Token refreshToken = new Token(refreshTokenStr, userId);

		Calendar refreshTokenCalendar = Calendar.getInstance();
		refreshTokenCalendar.add(Calendar.MILLISECOND,
			(int) properties.getToken().getRefreshTokenExpire().toMillis());
		refreshToken.setExpireTime(refreshTokenCalendar.getTime().getTime());

		return createTokenByRefreshToken(refreshToken);
	}

	private AccessToken createTokenByRefreshToken(Token refreshToken) {
		String accessTokenStr;
		do {
			accessTokenStr = RandomStringUtils.secureStrong().nextAlphanumeric(properties.getToken().getTokenLength());
		} while (tokenSet.contains(accessTokenStr));
		tokenSet.add(accessTokenStr);

		AccessToken accessToken = new AccessToken(accessTokenStr, refreshToken);

		Calendar accessTokenCalendar = Calendar.getInstance();
		accessTokenCalendar.add(Calendar.MILLISECOND,
			(int) properties.getToken().getAccessTokenExpire().toMillis());
		accessToken.setExpireTime(accessTokenCalendar.getTime().getTime());

		return accessToken;
	}
}
