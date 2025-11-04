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

package io.github.pangju666.framework.boot.autoconfigure.web.exception;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 用于配置 HTTP 异常信息的属性类。
 * <p>
 * 该类定义了与 HTTP 异常统计相关的配置项，包括是否启用功能、请求路径的配置，以及需要统计的包范围等。
 * 同时支持通过嵌套类 {@link Path} 配置异常统计的路径类型和路径列表。
 * </p>
 * <p>
 * 配置前缀：{@code pangju.web.exception.info}
 * </p>
 *
 * <h3>主要配置项：</h3>
 * <ul>
 *     <li>{@code enabled}：是否启用异常统计功能（默认启用）。</li>
 *     <li>{@code packages}：要扫描的自定义异常所在包的路径列表（如果配置为空，则只扫描框架内置的异常）。</li>
 *     <li>{@code requestPath}：请求路径相关的子配置，由 {@link Path} 类描述。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "pangju.web.exception.info")
public class HttpExceptionInfoProperties {
	/**
	 * 是否启用 HTTP 异常统计功能，默认为 {@code true}。
	 *
	 * @since 1.0.0
	 */
	private boolean enabled = true;
	/**
	 * HTTP 请求路径相关配置，包括异常路径类型和值。
	 *
	 * @since 1.0.0
	 */
	private Path requestPath = new Path();
	/**
	 * 要扫描的自定义异常所在包的路径列表。
	 *
	 * @since 1.0.0
	 */
	private List<String> packages;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Path getRequestPath() {
		return requestPath;
	}

	public void setRequestPath(Path requestPath) {
		this.requestPath = requestPath;
	}

	public List<String> getPackages() {
		return packages;
	}

	public void setPackages(List<String> packages) {
		this.packages = packages;
	}

	/**
	 * HTTP 请求路径配置内部类。
	 * <p>
	 * 用于定义统计目标的路径类型和路径值列表。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	public static class Path {
		/**
		 * 需要统计的路径类型，默认为 {@code "/exception/types"}。
		 *
		 * @since 1.0.0
		 */
		private String types = "/exception/types";
		/**
		 * 需要统计的路径列表，默认为 {@code "/exception/list"}。
		 *
		 * @since 1.0.0
		 */
		private String list = "/exception/list";

		public String getTypes() {
			return types;
		}

		public void setTypes(String types) {
			this.types = types;
		}

		public String getList() {
			return list;
		}

		public void setList(String list) {
			this.list = list;
		}
	}
}
