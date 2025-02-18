package io.github.pangju666.framework.autoconfigure.web.security.authentication.impl;

import io.github.pangju666.commons.codec.encryption.text.RSATextEncryptor;
import io.github.pangju666.commons.codec.key.RSAKey;
import io.github.pangju666.framework.autoconfigure.web.security.authentication.Authenticator;
import io.github.pangju666.framework.autoconfigure.web.security.enums.PasswordAlgorithm;
import io.github.pangju666.framework.autoconfigure.web.security.model.AuthenticatedUser;
import io.github.pangju666.framework.autoconfigure.web.security.properties.SecurityProperties;
import io.github.pangju666.framework.autoconfigure.web.security.store.AuthenticatedUserStore;
import io.github.pangju666.framework.core.exception.authentication.AuthenticationException;
import io.github.pangju666.framework.core.exception.base.ServiceException;
import io.github.pangju666.framework.core.exception.base.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class UsernamePasswordAuthenticator implements Authenticator {
	private final SecurityProperties securityProperties;
	private final AuthenticatedUserStore userStore;
	private final PasswordEncoder passwordEncoder;

	private AES256TextEncryptor aes256TextEncryptor;
	private RSATextEncryptor rsaTextEncryptor;

	public UsernamePasswordAuthenticator(SecurityProperties securityProperties,
										 PasswordEncoder passwordEncoder,
										 AuthenticatedUserStore userStore) {
		this.securityProperties = securityProperties;
		this.userStore = userStore;
		this.passwordEncoder = passwordEncoder;

		if (securityProperties.getPassword().getAlgorithm() == PasswordAlgorithm.AES256) {
			this.aes256TextEncryptor = new AES256TextEncryptor();
			this.aes256TextEncryptor.setPassword(securityProperties.getPassword().getAes256().getPassword());
		} else if (securityProperties.getPassword().getAlgorithm() == PasswordAlgorithm.RSA) {
			RSAKey rsaKey = RSAKey.fromBase64(securityProperties.getPassword().getRsa().getPublicKey(),
				securityProperties.getPassword().getRsa().getPrivateKey());
			this.rsaTextEncryptor = new RSATextEncryptor(rsaKey);
		}
	}

	@Override
	public String getRequestUrl() {
		return "/login/password";
	}

	@Override
	public AuthenticatedUser authenticate(HttpServletRequest request) throws AuthenticationException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if (StringUtils.isBlank(username)) {
			throw new ValidationException("用户名不可为空");
		}
		if (StringUtils.isBlank(username)) {
			throw new ValidationException("密码不可为空");
		}
		try {
			password = switch (securityProperties.getPassword().getAlgorithm()) {
				case PLAIN -> password;
				case HEX -> new String(Hex.decodeHex(password), StandardCharsets.UTF_8);
				case BASE64 -> new String(Base64.decodeBase64(password), StandardCharsets.UTF_8);
				case AES256 -> aes256TextEncryptor.decrypt(password);
				case RSA -> rsaTextEncryptor.decrypt(password);
			};
		} catch (DecoderException e) {
			throw new ServiceException("密码解码失败");
		}
		AuthenticatedUser authenticatedUser = userStore.loadUserByCredentials(username);
		if (Objects.isNull(authenticatedUser)) {
			throw new AuthenticationException("用户名不正确");
		}
		if (!passwordEncoder.matches(password, (String) authenticatedUser.getCredentials())) {
			throw new AuthenticationException("密码错误");
		}
		return authenticatedUser;
	}
}
