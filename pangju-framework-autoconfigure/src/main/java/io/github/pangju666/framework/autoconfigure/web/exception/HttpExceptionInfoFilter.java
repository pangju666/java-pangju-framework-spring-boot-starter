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

package io.github.pangju666.framework.autoconfigure.web.exception;

import io.github.pangju666.framework.web.annotation.HttpException;
import io.github.pangju666.framework.web.enums.HttpExceptionType;
import io.github.pangju666.framework.web.exception.base.BaseHttpException;
import io.github.pangju666.framework.web.model.vo.EnumVO;
import io.github.pangju666.framework.web.model.vo.HttpExceptionVO;
import io.github.pangju666.framework.web.pool.WebConstants;
import io.github.pangju666.framework.web.utils.ServletResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * HTTP异常信息过滤器
 * <p>
 * 提供两个端点用于获取系统中的异常信息：
 * <ul>
 *     <li>异常类型列表：返回所有已定义的异常类型</li>
 *     <li>异常信息列表：返回所有使用 {@link HttpException} 注解标注的异常类信息</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see HttpException
 * @see BaseHttpException
 * @since 1.0.0
 */
public class HttpExceptionInfoFilter extends OncePerRequestFilter {
	/**
	 * 框架包路径
	 */
	protected final static String FRAMEWORK_PACKAGE = "io.github.pangju666.framework";

	/**
	 * 异常列表请求路径
	 *
	 * @since 1.0.0
	 */
	private final String listRequestPath;
	/**
	 * 异常类型列表请求路径
	 *
	 * @since 1.0.0
	 */
	private final String typesRequestPath;
	/**
	 * 异常类型列表缓存
	 *
	 * @since 1.0.0
	 */
	private final List<EnumVO> httpExceptionTypeList;
	/**
	 * 异常信息列表缓存
	 *
	 * @since 1.0.0
	 */
	private final List<HttpExceptionVO> httpExceptionList;

	/**
	 * 创建过滤器实例
	 * <p>
	 * 初始化过滤器，设置异常信息访问的请求路径和需要扫描的包路径。
	 * 通过该构造函数，可以自定义异常类型列表和异常列表的请求路径，
	 * 以及指定需要扫描的包路径来限定异常扫描范围。
	 * </p>
	 *
	 * @param typesRequestPath 异常类型列表请求路径，不能为空
	 * @param listRequestPath  异常列表请求路径，不能为空
	 * @param packages         需要扫描的包路径，可选参数，为空时仅扫描框架包
	 * @throws IllegalArgumentException 当请求路径参数为空时抛出
	 * @since 1.0.0
	 */
	public HttpExceptionInfoFilter(String typesRequestPath, String listRequestPath, Collection<String> packages) {
		Assert.hasText(typesRequestPath, "typesRequestPath must not be null");
		Assert.hasText(listRequestPath, "listRequestPath must not be null");

		this.typesRequestPath = typesRequestPath;
		this.listRequestPath = listRequestPath;
		this.httpExceptionTypeList = Arrays.stream(HttpExceptionType.values())
			.map(type -> new EnumVO(type.getLabel(), type.name()))
			.toList();
		this.httpExceptionList = scanHttpExceptions(packages);
	}

	/**
	 * 执行过滤器内部处理逻辑
	 * <p>
	 * 根据请求路径分发到不同的处理方法：
	 * <ul>
	 *     <li>typesRequestPath: 返回异常类型列表</li>
	 *     <li>listRequestPath: 返回异常信息列表</li>
	 *     <li>其他路径: 继续过滤器链处理</li>
	 * </ul>
	 * </p>
	 *
	 * @param request     HTTP请求对象
	 * @param response    HTTP响应对象
	 * @param filterChain 过滤器链
	 * @throws ServletException Servlet异常
	 * @throws IOException      IO异常
	 * @since 1.0.0
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {
		String servletPath = request.getServletPath();
		if (servletPath.equals(typesRequestPath)) {
			ServletResponseUtils.writeBeanToResponse(this.httpExceptionTypeList, response, false);
		} else if (servletPath.equals(listRequestPath)) {
			ServletResponseUtils.writeBeanToResponse(httpExceptionList, response, true);
		} else {
			filterChain.doFilter(request, response);
		}
	}

	/**
	 * 扫描HTTP异常类
	 * <p>
	 * 扫描流程：
	 * <ol>
	 *     <li>配置扫描器，设置扫描范围为框架包和用户指定的包</li>
	 *     <li>获取所有 {@link BaseHttpException} 的子类</li>
	 *     <li>添加默认的未知异常类型</li>
	 *     <li>处理所有带有 {@link HttpException} 注解的异常类</li>
	 * </ol>
	 * </p>
	 *
	 * @return 异常信息列表
	 * @since 1.0.0
	 */
	protected List<HttpExceptionVO> scanHttpExceptions(Collection<String> packages) {
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
			.setScanners(Scanners.TypesAnnotated, Scanners.SubTypes)
			.forPackage(FRAMEWORK_PACKAGE);
		if (CollectionUtils.isNotEmpty(packages)) {
			configurationBuilder.forPackages(packages.toArray(String[]::new));
		}

		Reflections reflections = new Reflections(configurationBuilder);
		Set<Class<? extends BaseHttpException>> classes = reflections.getSubTypesOf(BaseHttpException.class);

		List<HttpExceptionVO> exceptionList = new ArrayList<>(classes.size() + 1);
		// 添加未知异常类型
		exceptionList.add(new HttpExceptionVO(HttpExceptionType.UNKNOWN, WebConstants.BASE_ERROR_CODE, null));

		// 扫描并添加所有标注了HttpException注解的异常类
		classes.stream()
			.map(clazz -> clazz.getAnnotation(HttpException.class))
			.filter(Objects::nonNull)
			.map(annotation -> new HttpExceptionVO(
				annotation.type(),
				annotation.type().computeCode(annotation.code()),
				StringUtils.defaultIfBlank(annotation.description(), annotation.type().getLabel())
			))
			.forEach(exceptionList::add);

		return exceptionList;
	}
}