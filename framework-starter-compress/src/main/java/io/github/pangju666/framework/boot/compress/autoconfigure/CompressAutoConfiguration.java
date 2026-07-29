package io.github.pangju666.framework.boot.compress.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 压缩归档自动配置类。
 * <p>
 * 压缩归档功能的主自动配置入口，负责导入所有压缩和归档相关的配置类，
 * 并启用压缩归档配置属性。
 * </p>
 *
 * <p><strong>导入的配置类</strong></p>
 * <ul>
 *   <li>{@link GzipConfiguration}：GZIP压缩和TAR.GZIP归档配置</li>
 *   <li>{@link XZConfiguration}：XZ压缩和TAR.XZ归档配置</li>
 *   <li>{@link ZstdConfiguration}：Zstd压缩和TAR.Zstd归档配置</li>
 *   <li>{@link TarConfiguration}：TAR归档配置</li>
 *   <li>{@link SevenZConfiguration}：7-Zip归档和加密归档配置</li>
 *   <li>{@link ZipConfiguration}：ZIP归档和加密归档配置</li>
 * </ul>
 *
 * <p><strong>配置属性</strong></p>
 * <p>启用{@link CompressProperties}配置属性绑定，配置前缀为{@code pangju.compress}。</p>
 *
 * @since 2.1.0
 */
@AutoConfiguration
@Import({GzipConfiguration.class, XZConfiguration.class, ZstdConfiguration.class, TarConfiguration.class,
	SevenZConfiguration.class, ZipConfiguration.class})
@EnableConfigurationProperties(CompressProperties.class)
public class CompressAutoConfiguration {
}
