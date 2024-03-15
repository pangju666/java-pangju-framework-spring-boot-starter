package io.github.pangju666.framework.autoconfigure.web.authenticate.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.pangju666.commons.lang.pool.ConstantPool;
import io.github.pangju666.framework.autoconfigure.web.authenticate.annotation.Authenticated;
import io.github.pangju666.framework.autoconfigure.web.authenticate.properties.AuthenticatedProperties;
import io.github.pangju666.framework.web.exception.AuthenticationException;
import io.github.pangju666.framework.web.exception.authentication.AuthenticationExpireException;
import io.github.pangju666.framework.web.exception.authentication.NoRoleException;
import io.github.pangju666.framework.web.interceptor.BaseRequestInterceptor;
import io.github.pangju666.framework.web.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class AuthenticatedInterceptor extends BaseRequestInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(AuthenticatedInterceptor.class);
	private final Map<String, AuthenticatedProperties.User> userMap;

	public AuthenticatedInterceptor(AuthenticatedProperties properties) {
		this.userMap = properties.getUsers()
			.stream()
			.collect(Collectors.toMap(AuthenticatedProperties.User::getUsername, user -> user));
	}

	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
		if (handler instanceof HandlerMethod handlerMethod) {
			Class<?> targetClass = handlerMethod.getBeanType();
			Method targetMethod = handlerMethod.getMethod();

			Authenticated annotation = targetMethod.getAnnotation(Authenticated.class);
			if (Objects.isNull(annotation)) {
				annotation = targetClass.getAnnotation(Authenticated.class);
			}
			if (Objects.isNull(annotation) || annotation.anonymous()) {
				return true;
			}

			String token = request.getHeader(HttpHeaders.AUTHORIZATION);
			if (StringUtils.isBlank(token)) {
				ResponseUtils.writeExceptionToResponse(new AuthenticationException("token不可为空"), response);
				return false;
			}
			if (!token.startsWith(ConstantPool.TOKEN_PREFIX)) {
				ResponseUtils.writeExceptionToResponse(new AuthenticationException("token格式不正确"), response);
				return false;
			}
			token = StringUtils.substringAfter(token, ConstantPool.TOKEN_PREFIX);

			try {
				DecodedJWT decode = JWT.decode(token);
				Claim usernameClaim = decode.getClaims().get("username");
				if (Objects.isNull(usernameClaim)) {
					ResponseUtils.writeExceptionToResponse(new AuthenticationException("无效的token"), response);
					return false;
				}
				AuthenticatedProperties.User user = userMap.get(usernameClaim.asString());
				if (Objects.isNull(user)) {
					ResponseUtils.writeExceptionToResponse(new AuthenticationException("无效的token"), response);
					return false;
				}

				Date requestTime = new Date();
				Algorithm algorithm = Algorithm.HMAC256(user.getPassword());
				JWTVerifier jwt = JWT.require(algorithm).build();
				jwt.verify(token);
				Date expireDate = decode.getExpiresAt();
				if (requestTime.compareTo(expireDate) > 0) {
					ResponseUtils.writeExceptionToResponse(new AuthenticationExpireException("token已过期"), response, HttpStatus.UNAUTHORIZED.value());
					return false;
				}

				if (annotation.roles().length > 0) {
					Claim rolesClaim = decode.getClaim("roles");
					if (rolesClaim.isNull()) {
						ResponseUtils.writeExceptionToResponse(new NoRoleException("用户没有对应的角色"), response, HttpStatus.FORBIDDEN.value());
						return false;
					}
					List<String> userRoles = rolesClaim.asList(String.class);
					if (userRoles.isEmpty()) {
						ResponseUtils.writeExceptionToResponse(new NoRoleException("用户没有对应的角色"), response, HttpStatus.FORBIDDEN.value());
						return false;
					}

					if (annotation.matchAnyRole()) {
						boolean hasRole = Arrays.stream(annotation.roles()).anyMatch(userRoles::contains);
						if (!hasRole) {
							ResponseUtils.writeExceptionToResponse(new NoRoleException("用户没有对应的角色"), response, HttpStatus.FORBIDDEN.value());
							return false;
						}
					} else {
						List<String> exceptRoles = new ArrayList<>(Arrays.asList(annotation.roles()));
						exceptRoles.removeAll(userRoles);
						if (!exceptRoles.isEmpty()) {
							ResponseUtils.writeExceptionToResponse(new NoRoleException("用户没有对应的角色"), response, HttpStatus.FORBIDDEN.value());
							return false;
						}
					}
				}
			} catch (JWTDecodeException e) {
				ResponseUtils.writeExceptionToResponse(new AuthenticationException("token格式不正确"), response);
				return false;
			} catch (IllegalArgumentException | JWTVerificationException e) {
				logger.error("身份认证失败", e);
				ResponseUtils.writeExceptionToResponse(new AuthenticationException("无效的token"), response);
				return false;
			}
		}
		return true;
	}
}
