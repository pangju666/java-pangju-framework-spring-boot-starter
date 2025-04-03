package io.github.pangju666.framework.autoconfigure.web.validation.aspect;

import io.github.pangju666.framework.autoconfigure.web.validation.annotation.RateLimit;
import io.github.pangju666.framework.autoconfigure.web.validation.enums.RateLimitMethod;
import io.github.pangju666.framework.autoconfigure.web.validation.exception.RequestLimitException;
import io.github.pangju666.framework.autoconfigure.web.validation.limiter.RequestRateLimiter;
import io.github.pangju666.framework.spring.utils.SpELUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.utils.RequestUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.Objects;

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Aspect
public class RequestRateLimitAspect {
	private final SpelExpressionParser parser;
	private final ParameterNameDiscoverer discoverer;
	private final RequestRateLimiter requestRateLimiter;

	public RequestRateLimitAspect(RequestRateLimiter requestRateLimiter) {
		this.parser = new SpelExpressionParser();
		this.discoverer = new DefaultParameterNameDiscoverer();
		this.requestRateLimiter = requestRateLimiter;
	}

	@Before("(@within(org.springframework.web.bind.annotation.RestController) || " +
		"@within(org.springframework.stereotype.Controller)) && " +
		"(@annotation(io.github.pangju666.framework.autoconfigure.web.validation.annotation.RateLimit) || " +
		"@within(io.github.pangju666.framework.autoconfigure.web.validation.annotation.RateLimit))")
	public void doBefore(JoinPoint point) {
		MethodSignature methodSignature = (MethodSignature) point.getSignature();
		Method method = methodSignature.getMethod();
		RateLimit annotation = method.getAnnotation(RateLimit.class);
		if (Objects.isNull(annotation)) {
			Class<?> targetClass = method.getDeclaringClass();
			annotation = targetClass.getAnnotation(RateLimit.class);
		}
		if (annotation.method() != RateLimitMethod.AOP) {
			return;
		}

		boolean result;
		try {
			EvaluationContext context = SpELUtils.initEvaluationContext(method, point.getArgs(), discoverer);
			Expression expression = parser.parseExpression(annotation.key());
			String key = expression.getValue(context, String.class);
			result = requestRateLimiter.tryAcquire(key, annotation, RequestUtils.getCurrentRequest());
		} catch (Exception e) {
			throw new ServerException(e);
		}
		if (!result) {
			throw new RequestLimitException(annotation);
		}
	}
}
