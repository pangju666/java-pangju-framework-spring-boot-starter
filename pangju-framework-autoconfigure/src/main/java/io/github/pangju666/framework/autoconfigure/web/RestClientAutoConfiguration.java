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

package io.github.pangju666.framework.autoconfigure.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * RestClient自动配置类
 * <p>
 * 该类用于在Spring Boot应用启动时自动配置{@link RestClient}实例。
 * 通过Spring框架提供的{@link RestClient.Builder}构建一个可用的RestClient Bean。
 * </p>
 * <p>
 * 配置优先级：
 * <ul>
 *     <li>在{@code org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration}之后加载</li>
 *     <li>确保Spring Boot的RestClient自动配置先完成</li>
 * </ul>
 * </p>
 * <p>
 * 配置条件：
 * <ul>
 *     <li>Classpath中必须存在{@link RestClient}类（Spring Framework 6.1+）</li>
 *     <li>必须存在{@link RestClient.Builder} Bean</li>
 *     <li>容器中不存在已定义的{@link RestClient} Bean</li>
 * </ul>
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>自动创建并配置RestClient实例</li>
 *     <li>提供统一的HTTP客户端访问方式</li>
 *     <li>简化REST API调用</li>
 * </ul>
 * </p>
 * <p>
 * RestClient特点：
 * <ul>
 *     <li>基于Spring 6.1+的新型HTTP客户端</li>
 *     <li>提供流式API，易于使用</li>
 *     <li>支持异步和同步调用</li>
 *     <li>内置错误处理机制</li>
 *     <li>支持请求和响应拦截器</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * &#64;Service
 * public class UserService {
 *     &#64;Autowired
 *     private RestClient restClient;
 *
 *     public User getUserById(Long id) {
 *         return restClient.get()
 *             .uri("http://api.example.com/users/{id}", id)
 *             .retrieve()
 *             .body(User.class);
 *     }
 *
 *     public void createUser(User user) {
 *         restClient.post()
 *             .uri("http://api.example.com/users")
 *             .body(user)
 *             .retrieve()
 *             .toBodilessEntity();
 *     }
 * }
 * </pre>
 * </p>
 * <p>
 * 与RestTemplate的区别：
 * <ul>
 *     <li>RestClient是Spring 6.1+推出的新型HTTP客户端</li>
 *     <li>提供更现代的流式API设计</li>
 *     <li>相比RestTemplate更灵活易用</li>
 *     <li>RestTemplate仍然支持但不再是推荐方案</li>
 * </ul>
 * </p>
 * <p>
 * 自定义RestClient：
 * <p>
 * 如果需要自定义RestClient的配置，可以定义自己的Bean：
 * <pre>
 * &#64;Configuration
 * public class RestClientConfiguration {
 *     &#64;Bean
 *     public RestClient customRestClient(RestClient.Builder builder) {
 *         return builder
 *             .requestFactory(new SimpleClientHttpRequestFactory())
 *             .baseUrl("http://api.example.com")
 *             .build();
 *     }
 * }
 * </pre>
 * 定义后，该自动配置类将不会创建默认的RestClient Bean。
 * </p>
 * </p>
 *
 * @author pangju666
 * @see RestClient
 * @see RestClient.Builder
 * @see org.springframework.boot.autoconfigure.AutoConfiguration
 * @since 1.0.0
 */
@AutoConfiguration(after = org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class)
@ConditionalOnClass(RestClient.class)
public class RestClientAutoConfiguration {
	/**
	 * 创建RestClient实例
	 * <p>
	 * 该方法通过注入的{@link RestClient.Builder}构建一个可用的RestClient实例。
	 * RestClient.Builder通常由Spring Boot的RestClient自动配置提供。
	 * </p>
	 * <p>
	 * 方法执行条件：
	 * <ul>
	 *     <li>必须存在RestClient.Builder Bean</li>
	 *     <li>容器中不存在RestClient Bean</li>
	 * </ul>
	 * </p>
	 *
	 * @param builder Spring提供的RestClient构建器
	 * @return 配置完成的RestClient实例
	 * @see RestClient.Builder
	 * @since 1.0.0
	 */
	@ConditionalOnBean(RestClient.Builder.class)
	@ConditionalOnMissingBean(RestClient.class)
	@Bean
	public RestClient restClient(RestClient.Builder builder) {
		return builder.build();
	}
}
