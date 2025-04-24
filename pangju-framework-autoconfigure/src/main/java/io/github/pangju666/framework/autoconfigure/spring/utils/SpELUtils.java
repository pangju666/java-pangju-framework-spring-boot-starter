/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.framework.autoconfigure.spring.utils;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Spring表达式语言(SpEL)工具类
 * <p>
 * 该工具类提供了Spring表达式语言(SpEL)的常用操作，简化了SpEL的使用。
 * 主要功能包括：
 * <ul>
 *     <li>初始化表达式计算上下文</li>
 *     <li>解析表达式并获取结果</li>
 *     <li>解析表达式并转换为指定类型</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see org.springframework.expression.spel.standard.SpelExpressionParser
 * @see org.springframework.expression.EvaluationContext
 * @since 1.0.0
 */
public class SpELUtils {
	/**
	 * 默认的SpEL表达式解析器
	 * <p>
	 * 用于解析SpEL表达式字符串，线程安全，可在多个线程间共享。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	public static final SpelExpressionParser DEFAULT_EXPRESSION_PARSER = new SpelExpressionParser();
	/**
	 * 默认的表达式计算上下文
	 * <p>
	 * 用于提供表达式计算的上下文环境，包含变量、函数等信息。
	 * 注意：此上下文是共享的，如果需要隔离变量，应创建新的上下文。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	public static final StandardEvaluationContext DEFAULT_EVALUATION_CONTEXT = new StandardEvaluationContext();

	protected SpELUtils() {
	}

	/**
	 * 初始化基于方法的表达式计算上下文
	 * <p>
	 * 该方法创建一个基于方法的表达式计算上下文，并将方法参数作为变量添加到上下文中。
	 * 这对于在AOP等场景中获取方法参数并在表达式中使用非常有用。
	 * </p>
	 *
	 * <p>
	 * 示例:
	 * <pre>{@code
	 * Method method = MyClass.class.getMethod("myMethod", String.class, Integer.class);
	 * Object[] args = new Object[]{"test", 123};
	 * ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
	 * EvaluationContext context = SpELUtils.initEvaluationContext(method, args, discoverer);
	 * // 现在可以在表达式中使用方法参数，如 #paramName
	 * }</pre>
	 * </p>
	 *
	 * @param method     目标方法
	 * @param args       方法参数值数组
	 * @param discoverer 参数名称发现器，用于获取方法参数名
	 * @return 初始化后的表达式计算上下文
	 * @throws NullPointerException 如果参数名称发现器无法获取参数名
	 * @since 1.0.0
	 */
	public static EvaluationContext initEvaluationContext(final Method method, final Object[] args,
														  final ParameterNameDiscoverer discoverer) {
		EvaluationContext context = new MethodBasedEvaluationContext(method, method, args, discoverer);
		String[] parametersName = discoverer.getParameterNames(method);
		for (int i = 0; i < args.length; i++) {
			context.setVariable(Objects.requireNonNull(parametersName)[i], args[i]);
		}
		return context;
	}

	/**
	 * 解析SpEL表达式并返回结果
	 * <p>
	 * 该方法使用默认的表达式解析器和计算上下文解析表达式，并返回结果。
	 * 结果类型由表达式计算结果决定。
	 * </p>
	 *
	 * <p>
	 * 示例:
	 * <pre>{@code
	 * Object result = SpELUtils.parseExpression("'Hello ' + 'World'");
	 * // result = "Hello World"
	 *
	 * Object result2 = SpELUtils.parseExpression("2 * 3");
	 * // result2 = 6
	 * }</pre>
	 * </p>
	 *
	 * @param expressionString SpEL表达式字符串
	 * @return 表达式计算结果
	 * @throws org.springframework.expression.ParseException      如果表达式解析出错
	 * @throws org.springframework.expression.EvaluationException 如果表达式计算出错
	 * @since 1.0.0
	 */
	public static Object parseExpression(final String expressionString) {
		Expression expression = DEFAULT_EXPRESSION_PARSER.parseExpression(expressionString);
		return expression.getValue(DEFAULT_EVALUATION_CONTEXT);
	}

	/**
	 * 解析SpEL表达式并将结果转换为指定类型
	 * <p>
	 * 该方法使用默认的表达式解析器和计算上下文解析表达式，并将结果转换为指定的类型。
	 * 如果无法转换为指定类型，将抛出异常。
	 * </p>
	 *
	 * <p>
	 * 示例:
	 * <pre>{@code
	 * String result = SpELUtils.parseExpression("'Hello ' + 'World'", String.class);
	 * // result = "Hello World"
	 *
	 * Integer result2 = SpELUtils.parseExpression("2 * 3", Integer.class);
	 * // result2 = 6
	 *
	 * Boolean result3 = SpELUtils.parseExpression("2 > 1", Boolean.class);
	 * // result3 = true
	 * }</pre>
	 * </p>
	 *
	 * @param expressionString  SpEL表达式字符串
	 * @param desiredResultType 期望的结果类型
	 * @param <T>               结果类型
	 * @return 转换后的表达式计算结果
	 * @throws org.springframework.expression.ParseException      如果表达式解析出错
	 * @throws org.springframework.expression.EvaluationException 如果表达式计算出错或类型转换失败
	 * @since 1.0.0
	 */
	public static <T> T parseExpression(final String expressionString, final Class<T> desiredResultType) {
		Expression expression = DEFAULT_EXPRESSION_PARSER.parseExpression(expressionString);
		return expression.getValue(DEFAULT_EVALUATION_CONTEXT, desiredResultType);
	}
}
