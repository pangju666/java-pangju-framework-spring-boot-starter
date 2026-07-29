package io.github.pangju666.framework.boot.compress.autoconfigure;

import io.github.pangju666.framework.boot.compress.core.ArchiveTemplate;
import io.github.pangju666.framework.boot.compress.core.impl.TarArchiveTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TAR归档自动配置类。
 * <p>
 * 配置TAR归档模板的Bean注册。
 * </p>
 *
 * <p><strong>生效条件</strong></p>
 * <ul>
 *   <li>配置{@code pangju.compress.archive-type}为{@code TAR}</li>
 *   <li>容器中不存在{@link ArchiveTemplate}类型的Bean</li>
 * </ul>
 *
 * @since 2.1.0
 */
@Configuration(proxyBeanMethods = false)
class TarConfiguration {
	/**
	 * 配置TAR归档模板Bean。
	 * <p>
	 * 当配置{@code pangju.compress.archive-type}为{@code TAR}时生效，
	 * 且容器中不存在{@link ArchiveTemplate}类型的Bean时注册。
	 * </p>
	 *
	 * @return TAR归档模板实例
	 * @since 2.1.0
	 */
	@ConditionalOnProperty(prefix = "pangju.compress", name = "archive-type", havingValue = "TAR")
	@ConditionalOnMissingBean(ArchiveTemplate.class)
	@Bean
	public ArchiveTemplate archiveTemplate() {
		return new TarArchiveTemplate();
	}
}
