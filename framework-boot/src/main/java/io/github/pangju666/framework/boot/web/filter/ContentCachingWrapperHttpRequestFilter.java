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

package io.github.pangju666.framework.boot.web.filter;

import io.github.pangju666.framework.web.servlet.filter.BaseHttpRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Set;

/**
 * 请求响应内容缓存包装过滤器
 * <p>
 * 该过滤器主要功能：
 * <ul>
 *     <li>包装请求和响应对象，使其支持内容缓存</li>
 *     <li>允许多次读取请求体和响应体内容</li>
 *     <li>支持配置排除路径，避免对特定请求进行包装</li>
 * </ul>
 * </p>
 *
 * <p>
 * 使用场景：
 * <ul>
 *     <li>需要在过滤器链中多次读取请求/响应内容</li>
 *     <li>日志记录、审计、调试等需要访问请求/响应内容的场景</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see ContentCachingRequestWrapper
 * @see ContentCachingResponseWrapper
 * @since 1.0.0
 */
public class ContentCachingWrapperHttpRequestFilter extends BaseHttpRequestFilter {
	/**
	 * 创建过滤器实例
	 *
	 * @param excludePathPatterns 排除路径模式集合，匹配的请求将不会被包装
	 * @since 1.0.0
	 */
	public ContentCachingWrapperHttpRequestFilter(Set<String> excludePathPatterns) {
		super(excludePathPatterns);
	}

	/**
	 * 处理HTTP请求
	 * <p>
	 * 处理流程：
	 * <ol>
	 *     <li>包装请求对象，使其支持内容缓存</li>
	 *     <li>包装响应对象，使其支持内容缓存</li>
	 *     <li>使用包装后的对象继续过滤器链处理</li>
	 *     <li>将缓存的响应内容写回到原始响应流</li>
	 * </ol>
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
		ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

		filterChain.doFilter(requestWrapper, responseWrapper);

		response.setContentType(responseWrapper.getContentType());
		response.setCharacterEncoding(responseWrapper.getCharacterEncoding());
		responseWrapper.copyBodyToResponse();
	}
}