package io.github.pangju666.framework.autoconfigure.web.aspect;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RateLimit;
import io.github.pangju666.framework.autoconfigure.web.enums.RateLimitMethod;
import io.github.pangju666.framework.autoconfigure.web.exception.RequestLimitException;
import io.github.pangju666.framework.autoconfigure.web.limiter.RequestRateLimiter;
import io.github.pangju666.framework.core.exception.base.ServerException;
import io.github.pangju666.framework.core.utils.SpELUtils;
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

	@Before("@annotation(io.github.pangju666.framework.autoconfigure.web.annotation.validation.RateLimit) || " +
		"@within(io.github.pangju666.framework.autoconfigure.web.annotation.validation.RateLimit)")
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

		EvaluationContext context = SpELUtils.initEvaluationContext(method, point.getArgs(), discoverer);
		Expression expression = parser.parseExpression(annotation.key());
		String key = expression.getValue(context, String.class);

		try {
			if (!requestRateLimiter.tryAcquire(key, annotation, RequestUtils.getCurrentRequest())) {
				throw new RequestLimitException(annotation);
			}
		} catch (Exception e) {
			throw new ServerException(e);
		}
	}
}
