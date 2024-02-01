package io.github.pangju666.framework.autoconfigure.web.authenticate.filter;


import io.github.pangju666.framework.autoconfigure.web.authenticate.properties.AuthenticatedProperties;
import io.github.pangju666.framework.web.filter.BaseRequestFilter;
import io.github.pangju666.framework.web.model.Result;
import io.github.pangju666.framework.web.utils.ResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.util.Set;

public class AuthenticateKeyFilter extends BaseRequestFilter {
	private final AuthenticatedProperties properties;

	public AuthenticateKeyFilter(AuthenticatedProperties properties, Set<String> excludePathPattern) {
		super(excludePathPattern);
		this.properties = properties;
	}

	@Override
	protected void handle(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
		if (!StringUtils.equalsIgnoreCase(request.getMethod(), properties.getRequest().getKeyMethod())) {
			ResponseUtils.writeResponse(Result.failByMessage("预期的请求方法类型：" + properties.getRequest().getLoginMethod()), response, HttpStatus.METHOD_NOT_ALLOWED);
			return;
		}
		switch (properties.getAlgorithm()) {
			case AES -> ResponseUtils.writeBeanToResponse(properties.getAes().getKey(), response);
			case RSA -> ResponseUtils.writeBeanToResponse(properties.getRsa().getPublicKey(), response);
			default -> ResponseUtils.writeBeanToResponse(null, response);
		}
	}
}
