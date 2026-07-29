package io.github.pangju666.framework.boot.compress.autoconfigure;

import io.github.pangju666.framework.boot.compress.core.ArchiveTemplate;
import io.github.pangju666.framework.boot.compress.core.CompressTemplate;
import io.github.pangju666.framework.boot.compress.core.impl.TarXZArchiveTemplate;
import io.github.pangju666.framework.boot.compress.core.impl.XZCompressTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tukaani.xz.XZ;

/**
 * XZ压缩自动配置类。
 * <p>
 * 配置XZ压缩模板和TAR.XZ归档模板的Bean注册。
 * </p>
 *
 * <p><strong>生效条件</strong></p>
 * <ul>
 *   <li>类路径中存在{@link org.tukaani.xz.XZ}类</li>
 *   <li>XZ压缩模板：配置{@code pangju.compress.compression-type}为{@code XZ}</li>
 *   <li>TAR.XZ归档模板：配置{@code pangju.compress.archive-type}为{@code TAR_XZ}</li>
 *   <li>容器中不存在对应的Bean实例</li>
 * </ul>
 *
 * @since 2.1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(XZ.class)
class XZConfiguration {
	/**
	 * 配置XZ压缩模板Bean。
	 * <p>
	 * 当配置{@code pangju.compress.compression-type}为{@code XZ}时生效，
	 * 且容器中不存在{@link CompressTemplate}类型的Bean时注册。
	 * </p>
	 *
	 * @param properties 压缩配置属性
	 * @return XZ压缩模板实例
	 * @since 2.1.0
	 */
	@ConditionalOnProperty(prefix = "pangju.compress", name = "compression-type", havingValue = "XZ")
	@ConditionalOnMissingBean(CompressTemplate.class)
	@Bean
	public CompressTemplate compressTemplate(CompressProperties properties) {
		return new XZCompressTemplate(properties);
	}

	/**
	 * 配置TAR.XZ归档模板Bean。
	 * <p>
	 * 当配置{@code pangju.compress.archive-type}为{@code TAR_XZ}时生效，
	 * 且容器中不存在{@link ArchiveTemplate}类型的Bean时注册。
	 * </p>
	 *
	 * @param properties 压缩配置属性
	 * @return TAR.XZ归档模板实例
	 * @since 2.1.0
	 */
	@ConditionalOnProperty(prefix = "pangju.compress", name = "archive-type", havingValue = "TAR_XZ")
	@ConditionalOnMissingBean(ArchiveTemplate.class)
	@Bean
	public ArchiveTemplate archiveTemplate(CompressProperties properties) {
		return new TarXZArchiveTemplate(properties);
	}
}
