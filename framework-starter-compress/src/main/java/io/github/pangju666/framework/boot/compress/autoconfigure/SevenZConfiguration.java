package io.github.pangju666.framework.boot.compress.autoconfigure;

import io.github.pangju666.framework.boot.compress.core.ArchiveEncryptTemplate;
import io.github.pangju666.framework.boot.compress.core.ArchiveTemplate;
import io.github.pangju666.framework.boot.compress.core.impl.SevenZArchiveTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tukaani.xz.XZ;

/**
 * 7-Zip归档自动配置类。
 * <p>
 * 配置7-Zip归档模板和加密归档模板的Bean注册。
 * </p>
 *
 * <p><strong>生效条件</strong></p>
 * <ul>
 *   <li>类路径中存在{@link org.tukaani.xz.XZ}类</li>
 *   <li>7-Zip归档模板：配置{@code pangju.compress.archive-type}为{@code SEVEN_Z}</li>
 *   <li>7-Zip加密归档模板：配置{@code pangju.compress.archive-encrypt-type}为{@code SEVEN_Z}</li>
 *   <li>容器中不存在对应的Bean实例</li>
 * </ul>
 *
 * @since 2.1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(XZ.class)
class SevenZConfiguration {
	/**
	 * 配置7-Zip归档模板Bean。
	 * <p>
	 * 当配置{@code pangju.compress.archive-type}为{@code SEVEN_Z}时生效，
	 * 且容器中不存在{@link ArchiveTemplate}类型的Bean时注册。
	 * </p>
	 *
	 * @param properties 压缩配置属性
	 * @return 7-Zip归档模板实例
	 * @since 2.1.0
	 */
	@ConditionalOnProperty(prefix = "pangju.compress", name = "archive-type", havingValue = "SEVEN_Z")
	@ConditionalOnMissingBean(ArchiveTemplate.class)
	@Bean
	public ArchiveTemplate archiveTemplate(CompressProperties properties) {
		return new SevenZArchiveTemplate(properties);
	}

	/**
	 * 配置7-Zip加密归档模板Bean。
	 * <p>
	 * 当配置{@code pangju.compress.archive-encrypt-type}为{@code SEVEN_Z}时生效，
	 * 且容器中不存在{@link ArchiveEncryptTemplate}类型的Bean时注册。
	 * </p>
	 *
	 * @param properties 压缩配置属性
	 * @return 7-Zip加密归档模板实例
	 * @since 2.1.0
	 */
	@ConditionalOnProperty(prefix = "pangju.compress", name = "archive-encrypt-type", havingValue = "SEVEN_Z")
	@ConditionalOnMissingBean(ArchiveEncryptTemplate.class)
	@Bean
	public ArchiveEncryptTemplate archiveEncryptTemplate(CompressProperties properties) {
		return new SevenZArchiveTemplate(properties);
	}
}
