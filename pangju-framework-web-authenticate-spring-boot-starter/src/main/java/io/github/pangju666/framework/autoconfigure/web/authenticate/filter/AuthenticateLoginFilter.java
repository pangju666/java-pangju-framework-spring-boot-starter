package io.github.pangju666.framework.autoconfigure.web.authenticate.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.github.pangju666.commons.codec.utils.AesUtils;
import io.github.pangju666.commons.codec.utils.RsaUtils;
import io.github.pangju666.framework.autoconfigure.web.authenticate.model.AuthenticatedUser;
import io.github.pangju666.framework.autoconfigure.web.authenticate.properties.AuthenticatedProperties;
import io.github.pangju666.framework.core.exception.base.ServiceException;
import io.github.pangju666.framework.core.exception.validation.ValidationException;
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
import org.springframework.http.HttpStatus;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

public class AuthenticateLoginFilter extends BaseRequestFilter {
	private final AuthenticatedProperties properties;
	private final Key key;
	private final Map<String, AuthenticatedProperties.User> userMap;

	public AuthenticateLoginFilter(AuthenticatedProperties properties, Set<String> excludePathPattern) throws NoSuchAlgorithmException, InvalidKeySpecException {
		super(excludePathPattern);
		this.properties = properties;
		this.userMap = properties.getUsers()
			.stream()
			.collect(Collectors.toMap(AuthenticatedProperties.User::getUsername, user -> user));
		key = switch (properties.getAlgorithm()) {
			case AES -> new SecretKeySpec(properties.getAes().getKey().getBytes(), AesUtils.ALGORITHM);
			case RSA -> {
				KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(properties.getRsa().getPrivateKey()));
				yield RsaUtils.getKeyFactory().generatePrivate(keySpec);
			}
			default -> null;
		};
	}

	@Override
	protected void handle(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
		if (!StringUtils.equalsIgnoreCase(request.getMethod(), properties.getRequest().getLoginMethod())) {
			ResponseUtils.writeResponse(Result.failByMessage("预期的请求方法类型：" + properties.getRequest().getLoginMethod()), response, HttpStatus.METHOD_NOT_ALLOWED);
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
			password = switch (properties.getAlgorithm()) {
				case HEX -> new String(Hex.decodeHex(password));
				case BASE64 -> new String(Base64.decodeBase64(password));
				case AES ->
					AesUtils.decryptToString(Base64.decodeBase64(password), (SecretKey) key, properties.getAes().getTransformation());
				case RSA -> RsaUtils.decryptToString(Base64.decodeBase64(password), (PrivateKey) key);
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
		} catch (IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException |
				 NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException |
				 InvalidAlgorithmParameterException e) {
			logger.error("密码解密失败", e);
			ResponseUtils.writeExceptionToResponse(new ServiceException("密码解密失败"), response);
		} catch (DecoderException e) {
			logger.error("十六进制解码失败", e);
			ResponseUtils.writeExceptionToResponse(new ServiceException("请求数据十六进制解码失败"), response);
		}
	}
}
