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

package io.github.pangju666.framework.boot.autoconfigure.image;

import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.framework.boot.autoconfigure.task.OnceTaskExecutorAutoConfiguration;
import io.github.pangju666.framework.boot.image.core.ImageTaskExecutor;
import io.github.pangju666.framework.boot.image.core.ImageTemplate;
import io.github.pangju666.framework.boot.task.OnceTaskExecutor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.AsyncTaskExecutor;

/**
 * 图像处理自动配置入口。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>启用 {@link ImageProperties} 作为配置载体（{@link EnableConfigurationProperties}）。</li>
 *   <li>按条件导入 {@link GMConfiguration} 与 {@link ImageIOConfiguration} 两个子配置（{@link Import}）。</li>
 *   <li>在满足条件时注册默认的 {@link ImageTaskExecutor} Bean，用于调度图像任务（支持同步/异步与超时）。</li>
 * </ul>
 *
 * <p><strong>生效条件</strong></p>
 * <ul>
 *   <li>GM：配置 {@code pangju.image.gm.path} 且类型为 {@code GM}。</li>
 *   <li>IMAGEIO：类路径存在 {@code ImageEditor} 且类型为 {@code IMAGEIO}（默认）。</li>
 * </ul>
 *
 * <p><strong>顺序</strong></p>
 * <ul>
 *   <li>在 {@link OnceTaskExecutorAutoConfiguration} 之后执行，以确保依赖的 {@link OnceTaskExecutor} 已注册。</li>
 *   <li>在 {@link TaskExecutionAutoConfiguration} 之后执行，以确保默认异步执行器可用。</li>
 * </ul>
 *
 * <p><strong>属性映射</strong></p>
 * <ul>
 *   <li>{@code pangju.image.async-task-executor-ref}：异步任务执行器 Bean 名称；未配置时使用
 *   {@link TaskExecutionAutoConfiguration#APPLICATION_TASK_EXECUTOR_BEAN_NAME}。</li>
 * </ul>
 *
 * @since 1.0.0
 * @author pangju666
 */
@AutoConfiguration(after = {OnceTaskExecutorAutoConfiguration.class, TaskExecutionAutoConfiguration.class})
@ConditionalOnClass({ImageSize.class})
@EnableConfigurationProperties(ImageProperties.class)
@Import({GMConfiguration.class, ImageIOConfiguration.class})
public class ImageAutoConfiguration {
	/**
	 * 注册图像任务执行器 Bean。
	 *
	 * <p>条件：</p>
	 * <ul>
	 *   <li>容器中不存在其它 {@link ImageTaskExecutor} Bean（{@link ConditionalOnMissingBean}）。</li>
	 *   <li>容器已存在 {@link ImageTemplate}、{@link OnceTaskExecutor} 与 {@link AsyncTaskExecutor}（{@link ConditionalOnBean}）。</li>
	 * </ul>
	 *
	 * <p>行为：通过 {@link BeanFactory} 使用配置的 Bean 名称（来自 {@link ImageProperties#getAsyncTaskExecutorRef()}）
	 * 获取 {@link AsyncTaskExecutor}，并与模板及一次性任务执行器一起构造 {@link ImageTaskExecutor}，统一调度图像处理任务。</p>
	 *
	 * @param template 图像处理模板
	 * @param executor 一次性任务执行器
	 * @param beanFactory Bean 工厂，用于按名称获取 {@link AsyncTaskExecutor}
	 * @param properties 图像配置属性，提供异步执行器引用名
	 * @return 图像任务执行器
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(ImageTaskExecutor.class)
	@ConditionalOnBean({ImageTemplate.class, OnceTaskExecutor.class, AsyncTaskExecutor.class})
	@Bean
	public ImageTaskExecutor imageTaskExecutor(ImageTemplate template, OnceTaskExecutor executor,
											   BeanFactory beanFactory, ImageProperties properties) {
		return new ImageTaskExecutor(template, executor, beanFactory.getBean(
			properties.getAsyncTaskExecutorRef(), AsyncTaskExecutor.class));
	}
}
