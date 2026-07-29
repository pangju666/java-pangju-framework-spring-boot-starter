package io.github.pangju666.framework.boot.compress.autoconfigure;

import io.github.pangju666.framework.boot.compress.core.ArchiveEncryptTemplate;
import io.github.pangju666.framework.boot.compress.core.ArchiveTemplate;
import io.github.pangju666.framework.boot.compress.core.impl.ZipArchiveTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ZIP归档自动配置类。
 * <p>
 * 配置ZIP归档模板和加密归档模板的Bean注册。
 * </p>
 *
 * <p><strong>生效条件</strong></p>
 * <ul>
 *   <li>ZIP归档模板：配置{@code pangju.compress.archive-type}为{@code ZIP}（默认值）</li>
 *   <li>ZIP加密归档模板：配置{@code pangju.compress.archive-encrypt-type}为{@code ZIP}（默认值）</li>
 *   <li>容器中不存在对应的Bean实例</li>
 * </ul>
 *
 * @since 2.1.0
 */
@Configuration(proxyBeanMethods = false)
class ZipConfiguration {
	/**
	 * 配置ZIP归档模板Bean。
	 * <p>
	 * 当配置{@code pangju.compress.archive-type}为{@code ZIP}时生效，
	 * 且容器中不存在{@link ArchiveTemplate}类型的Bean时注册。
	 * </p>
	 *
	 * @param properties 压缩配置属性
	 * @return ZIP归档模板实例
	 * @since 2.1.0
	 */
	@ConditionalOnProperty(prefix = "pangju.compress", name = "archive-type", havingValue = "ZIP", matchIfMissing = true)
	@ConditionalOnMissingBean(ArchiveTemplate.class)
	@Bean
	public ArchiveTemplate archiveTemplate(CompressProperties properties) {
		return new ZipArchiveTemplate(properties);
	}

	/**
	 * 配置ZIP加密归档模板Bean。
	 * <p>
	 * 当配置{@code pangju.compress.archive-encrypt-type}为{@code ZIP}时生效，
	 * 且容器中不存在{@link ArchiveEncryptTemplate}类型的Bean时注册。
	 * </p>
	 *
	 * @param properties 压缩配置属性
	 * @return ZIP加密归档模板实例
	 * @since 2.1.0
	 */
	@ConditionalOnProperty(prefix = "pangju.compress", name = "archive-encrypt-type", havingValue = "ZIP", matchIfMissing = true)
	@ConditionalOnMissingBean(ArchiveEncryptTemplate.class)
	@Bean
	public ArchiveEncryptTemplate archiveEncryptTemplate(CompressProperties properties) {
		return new ZipArchiveTemplate(properties);
	}
}
