package io.github.pangju666.framework.boot.compress.autoconfigure;

import io.github.pangju666.framework.boot.compress.core.ArchiveTemplate;
import io.github.pangju666.framework.boot.compress.core.CompressTemplate;
import io.github.pangju666.framework.boot.compress.core.impl.GzipCompressTemplate;
import io.github.pangju666.framework.boot.compress.core.impl.TarGzipArchiveTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * GZIP压缩自动配置类。
 * <p>
 * 配置GZIP压缩模板和TAR.GZIP归档模板的Bean注册。
 * </p>
 *
 * <p><strong>生效条件</strong></p>
 * <ul>
 *   <li>GZIP压缩模板：配置{@code pangju.compress.compression-type}为{@code GZIP}（默认值）</li>
 *   <li>TAR.GZIP归档模板：配置{@code pangju.compress.archive-type}为{@code TAR_GZIP}</li>
 *   <li>容器中不存在对应的Bean实例</li>
 * </ul>
 *
 * @since 2.1.0
 */
@Configuration(proxyBeanMethods = false)
class GzipConfiguration {
	/**
	 * 配置GZIP压缩模板Bean。
	 * <p>
	 * 当配置{@code pangju.compress.compression-type}为{@code GZIP}时生效，
	 * 且容器中不存在{@link CompressTemplate}类型的Bean时注册。
	 * </p>
	 *
	 * @param properties 压缩配置属性
	 * @return GZIP压缩模板实例
	 * @since 2.1.0
	 */
	@ConditionalOnProperty(prefix = "pangju.compress", name = "compression-type", havingValue = "GZIP", matchIfMissing = true)
	@ConditionalOnMissingBean(CompressTemplate.class)
	@Bean
	public CompressTemplate compressTemplate(CompressProperties properties) {
		return new GzipCompressTemplate(properties);
	}

	/**
	 * 配置TAR.GZIP归档模板Bean。
	 * <p>
	 * 当配置{@code pangju.compress.archive-type}为{@code TAR_GZIP}时生效，
	 * 且容器中不存在{@link ArchiveTemplate}类型的Bean时注册。
	 * </p>
	 *
	 * @param properties 压缩配置属性
	 * @return TAR.GZIP归档模板实例
	 * @since 2.1.0
	 */
	@ConditionalOnProperty(prefix = "pangju.compress", name = "archive-type", havingValue = "TAR_GZIP")
	@ConditionalOnMissingBean(ArchiveTemplate.class)
	@Bean
	public ArchiveTemplate archiveTemplate(CompressProperties properties) {
		return new TarGzipArchiveTemplate(properties);
	}
}
