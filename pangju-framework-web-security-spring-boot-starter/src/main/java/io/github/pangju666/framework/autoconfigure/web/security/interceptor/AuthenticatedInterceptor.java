package io.github.pangju666.framework.autoconfigure.web.security.interceptor;

import io.github.pangju666.commons.lang.pool.Constants;
import io.github.pangju666.commons.lang.utils.RegExUtils;
import io.github.pangju666.framework.autoconfigure.web.security.annotation.Anonymous;
import io.github.pangju666.framework.autoconfigure.web.security.annotation.Authenticated;
import io.github.pangju666.framework.autoconfigure.web.security.context.SecurityRequestContextHolder;
import io.github.pangju666.framework.autoconfigure.web.security.model.AuthenticatedUser;
import io.github.pangju666.framework.autoconfigure.web.security.properties.SecurityProperties;
import io.github.pangju666.framework.autoconfigure.web.security.store.AccessTokenStore;
import io.github.pangju666.framework.core.exception.authentication.AuthenticationException;
import io.github.pangju666.framework.core.exception.base.BaseRuntimeException;
import io.github.pangju666.framework.core.exception.base.ValidationException;
import io.github.pangju666.framework.web.interceptor.BaseRequestInterceptor;
import io.github.pangju666.framework.web.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.regex.Pattern;

public class AuthenticatedInterceptor extends BaseRequestInterceptor {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatedInterceptor.class);

	private final AccessTokenStore accessTokenStore;
	private final Pattern tokenPattern;

	public AuthenticatedInterceptor(AccessTokenStore accessTokenStore, SecurityProperties securityProperties) {
		this.accessTokenStore = accessTokenStore;
		this.tokenPattern = Pattern.compile("Bearer [a-zA-Z0-9]{%d}".formatted(
			securityProperties.getToken().getTokenLength()));
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (handler instanceof HandlerMethod handlerMethod) {
			Method targetMethod = handlerMethod.getMethod();
			Authenticated authenticated = targetMethod.getAnnotation(Authenticated.class);
			if (Objects.isNull(authenticated)) {
				Class<?> targetClass = handlerMethod.getBeanType();
				authenticated = targetClass.getAnnotation(Authenticated.class);
				if (Objects.isNull(authenticated)) {
					return true;
				}
			}
			Anonymous anonymous = targetMethod.getAnnotation(Anonymous.class);
			if (Objects.nonNull(anonymous)) {
				return true;
			}

			String token = request.getHeader(HttpHeaders.AUTHORIZATION);
			if (StringUtils.isBlank(token)) {
				ResponseUtils.writeExceptionToResponse(new ValidationException("token不可为空"), response,
					HttpStatus.UNAUTHORIZED);
				return false;
			}
			if (!RegExUtils.matches(tokenPattern, token)) {
				ResponseUtils.writeExceptionToResponse(new ValidationException("token格式不正确"), response,
					HttpStatus.UNAUTHORIZED);
				return false;
			}
			token = StringUtils.substringAfter(token, Constants.TOKEN_PREFIX);

			try {
				SecurityRequestContextHolder.setToken(token);
				AuthenticatedUser authenticatedUser = accessTokenStore.getAuthenticatedUser(token);
				if (Objects.isNull(authenticatedUser)) {
					SecurityRequestContextHolder.removeToken();
					ResponseUtils.writeExceptionToResponse(new AuthenticationException("无效的token"), response);
					return false;
				}
				SecurityRequestContextHolder.setAuthenticatedUser(authenticatedUser);
			} catch (BaseRuntimeException e) {
				SecurityRequestContextHolder.removeToken();
				ResponseUtils.writeExceptionToResponse(e, response);
				return false;
			}
		}
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		SecurityRequestContextHolder.removeAuthenticatedUser();
		SecurityRequestContextHolder.removeToken();
	}
}
