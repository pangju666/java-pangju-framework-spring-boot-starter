package io.github.pangju666.framework.boot.compress.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 压缩归档自动配置类。
 * <p>
 * 压缩归档功能的主自动配置入口，负责导入所有压缩和归档相关的配置类，并启用压缩归档配置属性。
 * 支持多种压缩算法和归档格式，包括GZIP、XZ、Zstd、TAR、7-Zip和ZIP等。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>支持多种压缩算法：GZIP、XZ、Zstd</li>
 *   <li>支持多种归档格式：TAR、7-Zip、ZIP</li>
 *   <li>支持组合格式：TAR.GZIP、TAR.XZ、TAR.Zstd</li>
 *   <li>支持加密归档：7-Zip加密、ZIP加密</li>
 *   <li>支持自定义压缩级别和策略</li>
 *   <li>提供统一的压缩模板接口，简化使用</li>
 * </ul>
 *
 * <p><strong>支持的压缩格式</strong></p>
 * <ul>
 *   <li>{@code GZIP}：GZIP压缩，基于Deflater算法，兼容性好</li>
 *   <li>{@code XZ}：XZ压缩，基于LZMA2算法，压缩率高</li>
 *   <li>{@code Zstd}：Zstandard压缩，压缩率和速度均优于GZIP</li>
 * </ul>
 *
 * <p><strong>支持的归档格式</strong></p>
 * <ul>
 *   <li>{@code TAR}：TAR归档，保留文件元数据</li>
 *   <li>{@code TAR_GZIP}：TAR+GZIP组合格式，Linux常用</li>
 *   <li>{@code TAR_XZ}：TAR+XZ组合格式，开源软件常用</li>
 *   <li>{@code TAR_ZSTD}：TAR+Zstd组合格式，性能优化</li>
 *   <li>{@code SEVEN_Z}：7-Zip归档，支持高压缩率和加密</li>
 *   <li>{@code ZIP}：ZIP归档，通用性强，支持加密</li>
 * </ul>
 *
 * <p><strong>依赖要求</strong></p>
 * <ul>
 *   <li>{@code XZ}压缩：需要添加依赖{@code org.tukaani:xz}</li>
 *   <li>{@code 7-Zip}归档：需要添加依赖{@code org.tukaani:xz}</li>
 *   <li>{@code Zstd}压缩：需要添加依赖{@code com.github.luben:zstd-jni}</li>
 * </ul>
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
 * <ul>
 *   <li>{@code pangju.compress.compression-type}：压缩类型（默认：GZIP）</li>
 *   <li>{@code pangju.compress.archive-type}：归档类型（默认：ZIP）</li>
 *   <li>{@code pangju.compress.archive-encrypt-type}：加密归档类型（默认：ZIP）</li>
 *   <li>{@code pangju.compress.gzip}：GZIP压缩配置</li>
 *   <li>{@code pangju.compress.xz}：XZ压缩配置</li>
 *   <li>{@code pangju.compress.zstd}：Zstd压缩配置</li>
 *   <li>{@code pangju.compress.seven-z}：7-Zip归档配置</li>
 *   <li>{@code pangju.compress.zip}：ZIP归档配置</li>
 * </ul>
 *
 * @since 2.1.0
 */
@AutoConfiguration
@Import({GzipConfiguration.class, XZConfiguration.class, ZstdConfiguration.class, TarConfiguration.class,
	SevenZConfiguration.class, ZipConfiguration.class})
@EnableConfigurationProperties(CompressProperties.class)
public class CompressAutoConfiguration {
}
