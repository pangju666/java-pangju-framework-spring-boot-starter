package io.github.pangju666.framework.autoconfigure.web.security.store;

import io.github.pangju666.framework.autoconfigure.web.security.model.AccessToken;
import io.github.pangju666.framework.autoconfigure.web.security.model.AuthenticatedUser;
import io.github.pangju666.framework.core.lang.pool.Constants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public interface AccessTokenStore {
    AccessToken generateToken(AuthenticatedUser authenticatedUser);

    AuthenticatedUser getAuthenticatedUser(String accessToken);

	Set<AccessToken> getAccessTokens(Serializable userId);

	AccessToken refreshToken(String refreshToken);

	default void removeToken(String accessToken) {
		accessToken = StringUtils.substringAfter(accessToken, Constants.TOKEN_PREFIX);
		AuthenticatedUser authenticatedUser = getAuthenticatedUser(accessToken);
		if (Objects.nonNull(authenticatedUser)) {
			removeToken(authenticatedUser.getId());
		}
	}

	default <U extends AuthenticatedUser> void removeToken(U authenticatedUser) {
		removeToken(authenticatedUser.getId());
	}

	void removeToken(Serializable userId);

	default void removeTokens(Collection<? extends Serializable> userIds) {
		if (CollectionUtils.isEmpty(userIds)) {
			return;
		}
		for (Serializable userId : userIds) {
			removeToken(userId);
		}
	}
}
