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
 * HTTP 异常信息属性配置。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>定义异常统计/查询相关的开关、接口路径与自定义异常扫描包。</li>
 *   <li>配合异常信息组件，提供“异常类型汇总”和“异常列表”接口地址配置。</li>
 * </ul>
 *
 * <p><b>前缀</b></p>
 * <ul>
 *   <li>配置前缀：{@code pangju.web.exception.info}。</li>
 *   <li>属性键采用 kebab-case（如 {@code request-path.types}）。</li>
 * </ul>
 *
 * <p><b>字段</b></p>
 * <ul>
 *   <li>{@link #enabled} 是否启用异常统计功能。</li>
 *   <li>{@link #endpoints} 异常统计接口路径配置（类型汇总/列表）。</li>
 *   <li>{@link #scanPackages} 自定义异常类的扫描包列表，用于扩展统计范围。</li>
 * </ul>
 *
 * <p><b>示例（application.yml）</b></p>
 * <pre>
 * pangju:
 *   web:
 *     exception:
 *       info:
 *         enabled: true
 *         endpoints:
 *           types: /exception/types
 *           list: /exception/list
 *         scan-packages:
 *           - com.example.app.common.exception
 *           - com.example.app.feature.exception
 * </pre>
 *
 * <p><b>备注</b></p>
 * <ul>
 *   <li>当 {@link #scanPackages} 为空或未配置时，默认仅统计框架内置异常类型。</li>
 *   <li>{@link #endpoints} 的路径值应避免与现有接口冲突，建议置于统一命名空间（如 {@code /exception/**}）。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "pangju.web.exception.statistics")
public class HttpExceptionInfoProperties {
	/**
	 * 是否启用异常统计功能。
	 *
	 * <p>默认值为 {@code true}。关闭后相关异常统计接口将不工作或不暴露。</p>
	 *
	 * @since 1.0.0
	 */
	private boolean enabled = true;
	/**
	 * 异常统计接口路径配置。
	 *
	 * <p>包含异常类型汇总与异常列表查询的路径设置，详见 {@link Path}。</p>
	 *
	 * @since 1.0.0
	 */
	private Path endpoints = new Path();
	/**
	 * 自定义异常类的扫描包列表。
	 *
	 * <p>用于扩展统计范围到业务自定义异常。当为空时，仅统计框架内置异常。</p>
	 *
	 * @since 1.0.0
	 */
	private List<String> scanPackages;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Path getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(Path endpoints) {
		this.endpoints = endpoints;
	}

	public List<String> getScanPackages() {
		return scanPackages;
	}

	public void setScanPackages(List<String> scanPackages) {
		this.scanPackages = scanPackages;
	}

	/**
     * 异常统计接口路径配置。
     *
     * <p><b>概述</b></p>
     * <ul>
     *   <li>配置两个 HTTP 接口路径：异常类型汇总与异常列表查询。</li>
     *   <li>默认路径分别为 {@code /exception/types} 与 {@code /exception/list}。</li>
     * </ul>
     *
     * <p><b>示例（application.yml）</b></p>
     * <pre>
     * pangju:
     *   web:
     *     exception:
     *       info:
     *         request-path:
     *           types: /exception/types
     *           list: /exception/list
     * </pre>
     *
     * @since 1.0.0
     */
    public static class Path {
        /**
         * 异常类型汇总接口路径。
         *
         * <p>默认值：{@code "/exception/types"}。</p>
         *
         * @since 1.0.0
         */
        private String types = "/exception/types";
        /**
         * 异常列表查询接口路径。
         *
         * <p>默认值：{@code "/exception/list"}。</p>
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
