package io.github.pangju666.framework.autoconfigure.web.security.store.impl;

import io.github.pangju666.commons.lang.utils.RegExUtils;
import io.github.pangju666.framework.autoconfigure.web.security.model.AuthenticatedUser;
import io.github.pangju666.framework.autoconfigure.web.security.model.token.AccessToken;
import io.github.pangju666.framework.autoconfigure.web.security.model.token.Token;
import io.github.pangju666.framework.autoconfigure.web.security.properties.TokenProperties;
import io.github.pangju666.framework.autoconfigure.web.security.store.AccessTokenStore;
import io.github.pangju666.framework.core.exception.authentication.AuthenticationException;
import io.github.pangju666.framework.core.exception.authentication.AuthenticationExpireException;
import io.github.pangju666.framework.core.lang.pool.Constants;
import io.github.pangju666.framework.data.redis.utils.RedisUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RedisAccessTokenStoreImpl implements AccessTokenStore {
	private static final Integer REDIS_DELETE_RETRY_TIMES = 3;

	private final RedisTemplate<String, Object> redisTemplate;
	private final TokenProperties properties;
	private final Pattern tokenPattern;

	public RedisAccessTokenStoreImpl(RedisTemplate<String, Object> redisTemplate, TokenProperties properties) {
		this.redisTemplate = redisTemplate;
		this.properties = properties;
		this.tokenPattern = Pattern.compile("Bearer [a-zA-Z0-9]{%d}".formatted(
			properties.getAccessToken().getTokenLength()));
	}

	@Override
	public <U extends AuthenticatedUser> AccessToken generateToken(U authenticatedUser) {
		if (!properties.getAccessToken().isConcurrent()) {
			removeToken(authenticatedUser.getId());
		}
		return createTokenByUser(authenticatedUser);
	}

	@Override
	public Set<AccessToken> getAccessTokens(Serializable userId) {
		String userAccessTokenKey = RedisUtils.computeKey(
			properties.getAccessToken().getRedis().getUserAccessTokenSetKeyPrefix(), userId.toString());
		return SetUtils.emptyIfNull(redisTemplate.opsForSet().members(userAccessTokenKey))
			.stream()
			.map(object -> (AccessToken) object)
			.collect(Collectors.toSet());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends AuthenticatedUser> U getAuthenticatedUser(String accessToken) {
		String accessTokenUserKey = RedisUtils.computeKey(
			properties.getAccessToken().getRedis().getAccessTokenUserKeyPrefix(), accessToken);
		return (U) redisTemplate.opsForValue().get(accessTokenUserKey);
	}

	@Override
	public void removeToken(Serializable userId) {
		Pair<Set<String>, Set<String>> keysTokensPair = getKeysAndTokens(userId);
		deleteRedisKeys(keysTokensPair.getLeft());
		redisTemplate.opsForSet().remove(properties.getAccessToken().getRedis().getTokenSetKey(),
			keysTokensPair.getRight().toArray(Object[]::new));
	}

	@Override
	public void removeTokens(Collection<? extends Serializable> userIds) {
		if (CollectionUtils.isEmpty(userIds)) {
			return;
		}

		Set<String> keys = new HashSet<>();
		Set<String> tokens = new HashSet<>();

		for (Serializable userId : userIds) {
			Pair<Set<String>, Set<String>> keysTokensPair = getKeysAndTokens(userId);
			keys.addAll(keysTokensPair.getLeft());
			tokens.addAll(keysTokensPair.getRight());
		}

		deleteRedisKeys(keys);
		redisTemplate.opsForSet().remove(properties.getAccessToken().getRedis().getTokenSetKey(),
			tokens.toArray(Object[]::new));
	}

	@Override
	public AccessToken refreshToken(String refreshToken) {
		if (!RegExUtils.matches(tokenPattern, refreshToken)) {
			throw new AuthenticationExpireException("无效的token");
		}
		refreshToken = StringUtils.substringAfter(refreshToken, Constants.TOKEN_PREFIX);

		Set<String> deleteKeys = new HashSet<>(2);
		AccessToken oldAccessToken = null;

		String refreshTokenUserKey = RedisUtils.computeKey(
			properties.getAccessToken().getRedis().getRefreshTokenUserKeyPrefix(), refreshToken);
		deleteKeys.add(refreshTokenUserKey);

		AuthenticatedUser authenticatedUser = (AuthenticatedUser) redisTemplate.opsForValue().get(refreshTokenUserKey);
		if (Objects.isNull(authenticatedUser)) {
			throw new AuthenticationExpireException("无效的token");
		}

		String userAccessTokenKey = RedisUtils.computeKey(
			properties.getAccessToken().getRedis().getUserAccessTokenSetKeyPrefix(),
			authenticatedUser.getId().toString());
		Set<Object> accessTokens = SetUtils.emptyIfNull(redisTemplate.opsForSet().members(userAccessTokenKey));
		for (Object accessToken : accessTokens) {
			if (((AccessToken) accessToken).getRefreshToken().getToken().equals(refreshToken)) {
				oldAccessToken = (AccessToken) accessToken;
				break;
			}
		}

		if (Objects.nonNull(oldAccessToken)) {
			String accessTokenUserKey = RedisUtils.computeKey(
				properties.getAccessToken().getRedis().getAccessTokenUserKeyPrefix(), oldAccessToken.getToken());
			deleteKeys.add(accessTokenUserKey);

			redisTemplate.opsForSet().remove(userAccessTokenKey, oldAccessToken);
			redisTemplate.opsForSet().remove(properties.getAccessToken().getRedis().getTokenSetKey(),
				oldAccessToken.getToken(), oldAccessToken.getRefreshToken().getToken());
		}
		redisTemplate.delete(deleteKeys);

		return generateToken(authenticatedUser);
	}

	private void storeAccessToken(AccessToken accessToken, AuthenticatedUser authenticatedUser) {
		String accessTokenUserKey = RedisUtils.computeKey(
			properties.getAccessToken().getRedis().getAccessTokenUserKeyPrefix(), accessToken.getToken());
		redisTemplate.opsForValue().set(accessTokenUserKey, authenticatedUser,
			properties.getAccessToken().getAccessTokenExpire());

		String userAccessTokenKey = RedisUtils.computeKey(
			properties.getAccessToken().getRedis().getUserAccessTokenSetKeyPrefix(),
			authenticatedUser.getId().toString());
		redisTemplate.opsForSet().add(userAccessTokenKey, accessToken);
	}

	private void storeRefreshToken(Token refreshToken, AuthenticatedUser authenticatedUser) {
		String refreshTokenUserKey = RedisUtils.computeKey(
			properties.getAccessToken().getRedis().getRefreshTokenUserKeyPrefix(), refreshToken.getToken());
		redisTemplate.opsForValue().set(refreshTokenUserKey, authenticatedUser,
			properties.getAccessToken().getRefreshTokenExpire());
	}

	private Pair<Set<String>, Set<String>> getKeysAndTokens(Serializable userId) {
		Set<String> keys = new HashSet<>();
		Set<String> tokens = new HashSet<>();

		String userAccessTokenKey = RedisUtils.computeKey(
			properties.getAccessToken().getRedis().getUserAccessTokenSetKeyPrefix(), userId.toString());
		keys.add(userAccessTokenKey);

		for (Object accessToken : SetUtils.emptyIfNull(redisTemplate.opsForSet().members(userAccessTokenKey))) {
			String accessTokenUserKey = RedisUtils.computeKey(
				properties.getAccessToken().getRedis().getAccessTokenUserKeyPrefix(),
				((AccessToken) accessToken).getToken());
			keys.add(accessTokenUserKey);
			tokens.add(((AccessToken) accessToken).getToken());

			String refreshTokenUserKey = RedisUtils.computeKey(
				properties.getAccessToken().getRedis().getRefreshTokenUserKeyPrefix(),
				((AccessToken) accessToken).getRefreshToken().getToken());
			keys.add(refreshTokenUserKey);
			tokens.add(((AccessToken) accessToken).getRefreshToken().getToken());
		}

		return Pair.of(keys, tokens);
	}

	private void deleteRedisKeys(Collection<String> keys) {
		if (CollectionUtils.isNotEmpty(keys)) {
			long deleteCount = ObjectUtils.defaultIfNull(redisTemplate.delete(keys), 0L);
			if (deleteCount < keys.size()) {
				long count = keys.size() - deleteCount;
				long times = 0;
				while (times <= REDIS_DELETE_RETRY_TIMES && count > 0) {
					long tmpCount = redisTemplate.delete(keys);
					++times;
					count -= tmpCount;
					deleteCount += tmpCount;
				}
			}
		}
	}

	private <U extends AuthenticatedUser> AccessToken createTokenByUser(U authenticatedUser) {
		if (properties.getAccessToken().isConcurrent() && properties.getAccessToken().getMaxLoginCount() > -1) {
			String userAccessTokenKey = RedisUtils.computeKey(
				properties.getAccessToken().getRedis().getUserAccessTokenSetKeyPrefix(),
				authenticatedUser.getId().toString());
			Set<Object> accessTokens = SetUtils.emptyIfNull(redisTemplate.opsForSet().members(userAccessTokenKey));
			if (accessTokens.size() >= properties.getAccessToken().getMaxLoginCount()) {
				throw new AuthenticationException("已达到最大登录数量");
			}
		}

		AccessToken accessToken = createTokenByUserId(authenticatedUser.getId());
		storeAccessToken(accessToken, authenticatedUser);
		storeRefreshToken(accessToken.getRefreshToken(), authenticatedUser);
		return accessToken;
	}

	private AccessToken createTokenByUserId(Serializable userId) {
		String refreshTokenStr;
		do {
			refreshTokenStr = RandomStringUtils.secureStrong().nextAlphanumeric(properties.getAccessToken().getTokenLength());
		} while (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(
			properties.getAccessToken().getRedis().getTokenSetKey(), refreshTokenStr)));
		redisTemplate.opsForSet().add(properties.getAccessToken().getRedis().getTokenSetKey(), refreshTokenStr);

		Token refreshToken = new Token(refreshTokenStr, userId);

		Calendar refreshTokenCalendar = Calendar.getInstance();
		refreshTokenCalendar.add(Calendar.MILLISECOND,
			(int) properties.getAccessToken().getRefreshTokenExpire().toMillis());
		refreshToken.setExpireTime(refreshTokenCalendar.getTime());

		return createTokenByRefreshToken(refreshToken);
	}

	private AccessToken createTokenByRefreshToken(Token refreshToken) {
		String accessTokenStr;
		do {
			accessTokenStr = RandomStringUtils.secureStrong().nextAlphanumeric(properties.getAccessToken().getTokenLength());
		} while (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(
			properties.getAccessToken().getRedis().getTokenSetKey(), accessTokenStr)));
		redisTemplate.opsForSet().add(properties.getAccessToken().getRedis().getTokenSetKey(), accessTokenStr);

		AccessToken accessToken = new AccessToken(accessTokenStr, refreshToken);

		Calendar accessTokenCalendar = Calendar.getInstance();
		accessTokenCalendar.add(Calendar.MILLISECOND,
			(int) properties.getAccessToken().getAccessTokenExpire().toMillis());
		accessToken.setExpireTime(accessTokenCalendar.getTime());

		return accessToken;
	}
}
