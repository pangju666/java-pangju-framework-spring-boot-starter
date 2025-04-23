package io.github.pangju666.framework.autoconfigure.web.validation.aspect;

import io.github.pangju666.framework.autoconfigure.spring.context.utils.SpELUtils;
import io.github.pangju666.framework.autoconfigure.web.validation.annotation.Repeat;
import io.github.pangju666.framework.autoconfigure.web.validation.exception.RequestRepeatException;
import io.github.pangju666.framework.autoconfigure.web.validation.repeater.RequestRepeater;
import io.github.pangju666.framework.web.exception.base.ServerException;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Aspect
public class RequestRepeatAspect {
	private final SpelExpressionParser parser;
	private final ParameterNameDiscoverer discoverer;
	private final RequestRepeater requestRepeater;

	public RequestRepeatAspect(RequestRepeater requestRepeater) {
		this.parser = new SpelExpressionParser();
		this.discoverer = new DefaultParameterNameDiscoverer();
		this.requestRepeater = requestRepeater;
	}

	@Before("(@within(org.springframework.web.bind.annotation.RestController) || " +
		"@within(org.springframework.stereotype.Controller)) && " +
		"@annotation(io.github.pangju666.framework.autoconfigure.web.validation.annotation.Repeat)")
	public void doBefore(JoinPoint point) {
		MethodSignature methodSignature = (MethodSignature) point.getSignature();
		Method method = methodSignature.getMethod();
		Repeat annotation = method.getAnnotation(Repeat.class);

		boolean result;
		try {
			EvaluationContext context = SpELUtils.initEvaluationContext(method, point.getArgs(), discoverer);
			Expression expression = parser.parseExpression(annotation.key());
			String key = expression.getValue(context, String.class);
			result = requestRepeater.tryAcquire(key, annotation, ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest());
		} catch (Exception e) {
			throw new ServerException(e);
		}
		if (!result) {
			throw new RequestRepeatException(annotation);
		}
	}
}
