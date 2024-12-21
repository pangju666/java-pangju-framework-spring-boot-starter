package io.github.pangju666.framework.autoconfigure.web.authenticate.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.github.pangju666.commons.codec.encryption.text.RSATextEncryptor;
import io.github.pangju666.commons.codec.key.RSAKey;
import io.github.pangju666.framework.autoconfigure.web.authenticate.enums.PasswordAlgorithm;
import io.github.pangju666.framework.autoconfigure.web.authenticate.model.AuthenticatedUser;
import io.github.pangju666.framework.autoconfigure.web.authenticate.properties.AuthenticatedProperties;
import io.github.pangju666.framework.core.exception.base.ServiceException;
import io.github.pangju666.framework.core.exception.base.ValidationException;
import io.github.pangju666.framework.web.filter.BaseRequestFilter;
import io.github.pangju666.framework.web.model.Result;
import io.github.pangju666.framework.web.utils.ResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class AuthenticateLoginFilter extends BaseRequestFilter {
	private final AuthenticatedProperties properties;
	private final Map<String, AuthenticatedProperties.User> userMap;
	private AES256TextEncryptor aes256TextEncryptor;
	private RSATextEncryptor rsaTextEncryptor;

	public AuthenticateLoginFilter(AuthenticatedProperties properties, Set<String> excludePathPattern) {
		super(excludePathPattern);
		this.properties = properties;
		this.userMap = properties.getUsers()
			.stream()
			.collect(Collectors.toMap(AuthenticatedProperties.User::getUsername, user -> user));
		if (properties.getPasswordAlgorithm() == PasswordAlgorithm.AES256) {
			this.aes256TextEncryptor = new AES256TextEncryptor();
			this.aes256TextEncryptor.setPassword(properties.getAes256().getKey());
		} else {
			RSAKey rsaKey = RSAKey.fromBase64(properties.getRsa().getPublicKey(), properties.getRsa().getPrivateKey());
			this.rsaTextEncryptor = new RSATextEncryptor(rsaKey);
		}
	}

	@Override
	protected void handle(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
		if (!StringUtils.equalsIgnoreCase(request.getMethod(), properties.getRequest().getLoginMethod())) {
			ResponseUtils.writeResultToResponse(Result.failByMessage("预期的请求方法类型：" + properties.getRequest().getLoginMethod()), response, HttpStatus.METHOD_NOT_ALLOWED);
			return;
		}
		String username = request.getParameter(properties.getRequest().getUsernameParameter());
		if (StringUtils.isBlank(username)) {
			ResponseUtils.writeExceptionToResponse(new ValidationException("用户名不可为空"), response);
			return;
		}
		if (!userMap.containsKey(username)) {
			ResponseUtils.writeExceptionToResponse(new ValidationException("用户名不存在"), response);
			return;
		}
		try {
			String password = request.getParameter(properties.getRequest().getPasswordParameter());
			if (StringUtils.isBlank(password)) {
				ResponseUtils.writeExceptionToResponse(new ValidationException("密码不可为空"), response);
				return;
			}
			password = switch (properties.getPasswordAlgorithm()) {
				case HEX -> new String(Hex.decodeHex(password), StandardCharsets.UTF_8);
				case BASE64 -> new String(Base64.decodeBase64(password), StandardCharsets.UTF_8);
				case AES256 -> aes256TextEncryptor.decrypt(password);
				case RSA -> rsaTextEncryptor.decrypt(password);
			};
			AuthenticatedProperties.User user = userMap.get(username);
			if (!user.getPassword().equals(password)) {
				ResponseUtils.writeExceptionToResponse(new ValidationException("密码错误"), response);
				return;
			}

			Calendar instance = Calendar.getInstance();
			Date issuedDate = instance.getTime();
			instance.add(Calendar.MILLISECOND, (int) properties.getDuration().toMillis());
			String token = JWT.create()
				.withClaim("username", username)
				.withClaim("roles", new ArrayList<>(user.getRoles()))
				.withIssuedAt(issuedDate)
				.withExpiresAt(instance.getTime())
				.sign(Algorithm.HMAC256(password));
			AuthenticatedUser authenticatedUser = new AuthenticatedUser(username, user.getRoles(), token);
			ResponseUtils.writeBeanToResponse(authenticatedUser, response);
		} catch (EncryptionOperationNotPossibleException e) {
			ResponseUtils.writeExceptionToResponse(new ServiceException("无效的密码", "密码解密失败", e), response);
		} catch (DecoderException e) {
			ResponseUtils.writeExceptionToResponse(new ServiceException("无效的密码", "密码解码失败", e), response);
		}
	}
}
