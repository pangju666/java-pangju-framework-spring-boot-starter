package io.github.pangju666.framework.autoconfigure.web.security.filter;

import io.github.pangju666.framework.autoconfigure.web.security.authentication.Authenticator;
import io.github.pangju666.framework.autoconfigure.web.security.context.SecurityRequestContextHolder;
import io.github.pangju666.framework.autoconfigure.web.security.model.AccessToken;
import io.github.pangju666.framework.autoconfigure.web.security.model.AuthenticatedUser;
import io.github.pangju666.framework.autoconfigure.web.security.store.AccessTokenStore;
import io.github.pangju666.framework.core.exception.authentication.AuthenticationException;
import io.github.pangju666.framework.core.exception.base.BaseRuntimeException;
import io.github.pangju666.framework.core.exception.base.ServiceException;
import io.github.pangju666.framework.web.filter.BaseRequestFilter;
import io.github.pangju666.framework.web.utils.ResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.*;

public class AuthenticateLoginFilter extends BaseRequestFilter {
	private final Map<String, Authenticator> authenticatorMap;
	private final AccessTokenStore accessTokenStore;

	public AuthenticateLoginFilter(AccessTokenStore accessTokenStore,
								   Set<String> excludePathPattern, List<Authenticator> authenticators) {
		super(excludePathPattern);
		this.accessTokenStore = accessTokenStore;
		this.authenticatorMap = new HashMap<>(authenticators.size());
		for (Authenticator authenticator : authenticators) {
			authenticatorMap.put(authenticator.getRequestUrl(), authenticator);
		}
	}

	@Override
	protected void handle(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
		try {
			Authenticator authenticator = authenticatorMap.get(request.getServletPath());
			if (Objects.isNull(authenticator)) {
				SecurityRequestContextHolder.removeToken();
				ResponseUtils.writeExceptionToResponse(
					new ServiceException("登录接口:" + request.getServletPath() + "不存在"), response);
			}

			AuthenticatedUser authenticatedUser = authenticator.authenticate(request);
			if (Objects.isNull(authenticatedUser)) {
				throw new AuthenticationException("账户登录失败，请检查登录凭证是否正确");
			}
			AccessToken accessToken = accessTokenStore.generateToken(authenticatedUser);
			ResponseUtils.writeBeanToResponse(accessToken, response);
		} catch (BaseRuntimeException e) {
			SecurityRequestContextHolder.removeToken();
			ResponseUtils.writeExceptionToResponse(e, response);
		}
	}
}
