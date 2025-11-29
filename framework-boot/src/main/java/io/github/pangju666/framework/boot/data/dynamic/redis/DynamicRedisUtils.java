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

package io.github.pangju666.framework.boot.data.dynamic.redis;

import io.github.pangju666.framework.boot.spring.StaticSpringContext;
import io.github.pangju666.framework.data.redis.core.ScanRedisTemplate;
import io.github.pangju666.framework.data.redis.core.StringScanRedisTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 动态 Redis 工具。
 *
 * <p><b>概述</b>：生成并获取动态 Redis 相关 Bean 的名称与实例，支持在运行时访问多个数据源。</p>
 *
 * <p><b>支持的 Bean 类型</b></p>
 * <ul>
 *   <li>{@link RedisConnectionFactory}：连接工厂。</li>
 *   <li>{@link RedisTemplate}：对象键值模板。</li>
 *   <li>{@link StringRedisTemplate}：字符串键值模板。</li>
 *   <li>{@link ScanRedisTemplate}：支持游标扫描的模板。</li>
 *   <li>{@link StringScanRedisTemplate}：支持游标扫描的字符串模板。</li>
 * </ul>
 *
 * <p><b>命名规则</b>：{name}{BeanType}</p>
 * <ul>
 *   <li>{name}RedisConnectionFactory</li>
 *   <li>{name}RedisTemplate</li>
 *   <li>{name}ScanRedisTemplate</li>
 *   <li>{name}StringRedisTemplate</li>
 *   <li>{name}StringScanRedisTemplate</li>
 * </ul>
 *
 * <p><b>使用方式</b>：构造 Bean 名称 -> 通过 {@link StaticSpringContext} 获取实例 -> 执行 Redis 操作。</p>
 * <p><b>约束</b>：名称必须对应已注册 Bean；未注册时通过 Spring 容器获取会抛出异常。</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class DynamicRedisUtils {
    /**
     * {@link RedisConnectionFactory} Bean 名称模板（{name}RedisConnectionFactory）。
     *
     * @since 1.0.0
     */
    private static final String CONNECTION_FACTORY_BEAN_NAME_TEMPLATE = "%sRedisConnectionFactory";
    /**
     * {@link RedisTemplate} Bean 名称模板（{name}RedisTemplate）。
     *
     * @since 1.0.0
     */
    private static final String TEMPLATE_BEAN_NAME_TEMPLATE = "%sRedisTemplate";
    /**
     * {@link ScanRedisTemplate} Bean 名称模板（{name}ScanRedisTemplate）。
     *
     * @since 1.0.0
     */
    private static final String SCAN_TEMPLATE_BEAN_NAME_TEMPLATE = "%sScanRedisTemplate";
    /**
     * {@link StringRedisTemplate} Bean 名称模板（{name}StringRedisTemplate）。
     *
     * @since 1.0.0
     */
    private static final String STRING_TEMPLATE_BEAN_NAME_TEMPLATE = "%sStringRedisTemplate";
    /**
     * {@link StringScanRedisTemplate} Bean 名称模板（{name}StringScanRedisTemplate）。
     *
     * @since 1.0.0
     */
    private static final String STRING_SCAN_TEMPLATE_BEAN_NAME_TEMPLATE = "%sStringScanRedisTemplate";

	protected DynamicRedisUtils() {
	}

    /**
     * 根据数据源名称获取 {@link RedisConnectionFactory} Bean 名称。
     *
     * <p><b>流程</b>：套用模板 -> 返回名称。</p>
     *
     * @param name 数据源名称
     * @return Bean 名称（{name}RedisConnectionFactory）
     * @since 1.0.0
     */
    public static String getRedisConnectionFactoryBeanName(String name) {
        return CONNECTION_FACTORY_BEAN_NAME_TEMPLATE.formatted(name);
    }

    /**
     * 根据数据源名称获取 {@link RedisTemplate} Bean 名称。
     *
     * <p><b>流程</b>：套用模板 -> 返回名称。</p>
     *
     * @param name 数据源名称
     * @return Bean 名称（{name}RedisTemplate）
     * @since 1.0.0
     */
    public static String getRedisTemplateBeanName(String name) {
        return TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name);
    }

    /**
     * 根据数据源名称获取 {@link ScanRedisTemplate} Bean 名称。
     *
     * <p><b>流程</b>：套用模板 -> 返回名称。</p>
     *
     * @param name 数据源名称
     * @return Bean 名称（{name}ScanRedisTemplate）
     * @since 1.0.0
     */
    public static String getScanRedisTemplateBeanName(String name) {
        return SCAN_TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name);
    }

    /**
     * 根据数据源名称获取 {@link StringRedisTemplate} Bean 名称。
     *
     * <p><b>流程</b>：套用模板 -> 返回名称。</p>
     *
     * @param name 数据源名称
     * @return Bean 名称（{name}StringRedisTemplate）
     * @since 1.0.0
     */
    public static String getStringRedisTemplateBeanName(String name) {
        return STRING_TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name);
    }

    /**
     * 根据数据源名称获取 {@link StringScanRedisTemplate} Bean 名称。
     *
     * <p><b>流程</b>：套用模板 -> 返回名称。</p>
     *
     * @param name 数据源名称
     * @return Bean 名称（{name}StringScanRedisTemplate）
     * @since 1.0.0
     */
    public static String getStringRedisScanTemplateBeanName(String name) {
        return STRING_SCAN_TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name);
    }

    /**
     * 获取 {@link RedisConnectionFactory} 实例。
     *
     * <p><b>流程</b>：构造名称 -> 通过 {@link StaticSpringContext} 获取 Bean -> 返回。</p>
     * <p><b>约束</b>：名称需对应已注册 Bean；否则 Spring 容器将抛出异常。</p>
     *
     * @param name 数据源名称
     * @return 连接工厂实例
     * @since 1.0.0
     */
    public static RedisConnectionFactory getRedisConnectionFactory(String name) {
        return StaticSpringContext.getBeanFactory().getBean(CONNECTION_FACTORY_BEAN_NAME_TEMPLATE.formatted(name),
            RedisConnectionFactory.class);
    }

    /**
     * 获取 {@link RedisTemplate} 实例。
     *
     * <p><b>流程</b>：构造名称 -> 通过 {@link StaticSpringContext} 获取 Bean -> 返回。</p>
     * <p><b>约束</b>：名称需对应已注册 Bean；返回未经泛型擦除的模板实例。</p>
     *
     * @param name 数据源名称
     * @return Redis 模板实例
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public static RedisTemplate<Object, Object> getRedisTemplate(String name) {
        return StaticSpringContext.getBeanFactory().getBean(TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name),
            RedisTemplate.class);
    }

    /**
     * 获取 {@link StringRedisTemplate} 实例。
     *
     * <p><b>流程</b>：构造名称 -> 通过 {@link StaticSpringContext} 获取 Bean -> 返回。</p>
     *
     * @param name 数据源名称
     * @return 字符串模板实例
     * @since 1.0.0
     */
    public static StringRedisTemplate getStringRedisTemplate(String name) {
        return StaticSpringContext.getBeanFactory().getBean(STRING_TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name),
            StringRedisTemplate.class);
    }

    /**
     * 获取 {@link ScanRedisTemplate} 实例。
     *
     * <p><b>流程</b>：构造名称 -> 通过 {@link StaticSpringContext} 获取 Bean -> 返回。</p>
     *
     * @param name 数据源名称
     * @return 支持游标扫描的模板实例
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public static ScanRedisTemplate<Object> getScanRedisTemplate(String name) {
        return StaticSpringContext.getBeanFactory().getBean(SCAN_TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name),
            ScanRedisTemplate.class);
    }

    /**
     * 获取 {@link StringScanRedisTemplate} 实例。
     *
     * <p><b>流程</b>：构造名称 -> 通过 {@link StaticSpringContext} 获取 Bean -> 返回。</p>
     *
     * @param name 数据源名称
     * @return 支持游标扫描的字符串模板实例
     * @since 1.0.0
     */
    public static StringScanRedisTemplate getStringScanRedisTemplate(String name) {
        return StaticSpringContext.getBeanFactory().getBean(STRING_SCAN_TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name),
            StringScanRedisTemplate.class);
    }
}
