package io.github.pangju666.framework.boot.compress.autoconfigure;

import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tukaani.xz.LZMA2Options;

import java.util.zip.Deflater;

/**
 * 压缩归档配置属性。
 * <p>
 * 配置压缩和归档操作的相关参数，包括压缩类型、归档类型、加密归档类型
 * 以及各种压缩算法的具体配置参数。
 * </p>
 *
 * <p><strong>配置前缀</strong></p>
 * <p>配置组前缀：{@code pangju.compress}</p>
 *
 * <p><strong>主要配置项</strong></p>
 * <ul>
 *   <li>{@code compression-type}：压缩类型（GZIP、ZSTD、XZ）</li>
 *   <li>{@code archive-type}：归档类型（TAR、ZIP、SEVEN_Z、TAR_GZIP、TAR_ZSTD、TAR_XZ）</li>
 *   <li>{@code archive-encrypt-type}：加密归档类型（ZIP、SEVEN_Z）</li>
 *   <li>各压缩算法的具体配置参数（压缩级别、策略等）</li>
 * </ul>
 *
 * @since 2.1.0
 */
@ConfigurationProperties(prefix = "pangju.compress")
public class CompressProperties {
	/**
	 * 压缩类型。
	 * <p>默认值：{@code GZIP}</p>
	 * <p>对应属性：{@code pangju.compress.compression-type}</p>
	 *
	 * @since 2.1.0
	 */
	private CompressionType compressionType = CompressionType.GZIP;
	/**
	 * 归档类型。
	 * <p>默认值：{@code ZIP}</p>
	 * <p>对应属性：{@code pangju.compress.archive-type}</p>
	 *
	 * @since 2.1.0
	 */
	private ArchiveType archiveType = ArchiveType.ZIP;
	/**
	 * 加密归档类型。
	 * <p>默认值：{@code ZIP}</p>
	 * <p>对应属性：{@code pangju.compress.archive-encrypt-type}</p>
	 *
	 * @since 2.1.0
	 */
	private ArchiveEncryptType archiveEncryptType = ArchiveEncryptType.ZIP;
	/**
	 * GZIP压缩配置。
	 *
	 * @since 2.1.0
	 */
	private Gzip gzip = new Gzip();
	/**
	 * Zstd压缩配置。
	 *
	 * @since 2.1.0
	 */
	private Zstd zstd = new Zstd();
	/**
	 * XZ压缩配置。
	 *
	 * @since 2.1.0
	 */
	private XZ xz = new XZ();
	/**
	 * 7-Zip归档配置。
	 *
	 * @since 2.1.0
	 */
	private SevenZ sevenZ = new SevenZ();
	/**
	 * ZIP归档配置。
	 *
	 * @since 2.1.0
	 */
	private Zip zip = new Zip();

	/**
	 * 获取ZIP归档配置。
	 *
	 * @return ZIP归档配置
	 * @since 2.1.0
	 */
	public Zip getZip() {
		return zip;
	}

	/**
	 * 设置ZIP归档配置。
	 *
	 * @param zip ZIP归档配置
	 * @since 2.1.0
	 */
	public void setZip(Zip zip) {
		this.zip = zip;
	}

	/**
	 * 获取7-Zip归档配置。
	 *
	 * @return 7-Zip归档配置
	 * @since 2.1.0
	 */
	public SevenZ getSevenZ() {
		return sevenZ;
	}

	/**
	 * 设置7-Zip归档配置。
	 *
	 * @param sevenZ 7-Zip归档配置
	 * @since 2.1.0
	 */
	public void setSevenZ(SevenZ sevenZ) {
		this.sevenZ = sevenZ;
	}

	/**
	 * 获取XZ压缩配置。
	 *
	 * @return XZ压缩配置
	 * @since 2.1.0
	 */
	public XZ getXz() {
		return xz;
	}

	/**
	 * 设置XZ压缩配置。
	 *
	 * @param xz XZ压缩配置
	 * @since 2.1.0
	 */
	public void setXz(XZ xz) {
		this.xz = xz;
	}

	/**
	 * 获取Zstd压缩配置。
	 *
	 * @return Zstd压缩配置
	 * @since 2.1.0
	 */
	public Zstd getZstd() {
		return zstd;
	}

	/**
	 * 设置Zstd压缩配置。
	 *
	 * @param zstd Zstd压缩配置
	 * @since 2.1.0
	 */
	public void setZstd(Zstd zstd) {
		this.zstd = zstd;
	}

	/**
	 * 获取GZIP压缩配置。
	 *
	 * @return GZIP压缩配置
	 * @since 2.1.0
	 */
	public Gzip getGzip() {
		return gzip;
	}

	/**
	 * 设置GZIP压缩配置。
	 *
	 * @param gzip GZIP压缩配置
	 * @since 2.1.0
	 */
	public void setGzip(Gzip gzip) {
		this.gzip = gzip;
	}

	/**
	 * 获取压缩类型。
	 *
	 * @return 压缩类型
	 * @since 2.1.0
	 */
	public CompressionType getCompressionType() {
		return compressionType;
	}

	/**
	 * 设置压缩类型。
	 *
	 * @param compressionType 压缩类型
	 * @since 2.1.0
	 */
	public void setCompressionType(CompressionType compressionType) {
		this.compressionType = compressionType;
	}

	/**
	 * 获取归档类型。
	 *
	 * @return 归档类型
	 * @since 2.1.0
	 */
	public ArchiveType getArchiveType() {
		return archiveType;
	}

	/**
	 * 设置归档类型。
	 *
	 * @param archiveType 归档类型
	 * @since 2.1.0
	 */
	public void setArchiveType(ArchiveType archiveType) {
		this.archiveType = archiveType;
	}

	/**
	 * 获取加密归档类型。
	 *
	 * @return 加密归档类型
	 * @since 2.1.0
	 */
	public ArchiveEncryptType getArchiveEncryptType() {
		return archiveEncryptType;
	}

	/**
	 * 设置加密归档类型。
	 *
	 * @param archiveEncryptType 加密归档类型
	 * @since 2.1.0
	 */
	public void setArchiveEncryptType(ArchiveEncryptType archiveEncryptType) {
		this.archiveEncryptType = archiveEncryptType;
	}

	/**
	 * Deflater压缩级别枚举。
	 * <p>
	 * 定义GZIP和ZIP压缩的压缩级别，对应Java {@link java.util.zip.Deflater} 的压缩级别常量。
	 * </p>
	 *
	 * <p><strong>可选值</strong></p>
	 * <ul>
	 *   <li>{@code NO_COMPRESSION}：无压缩（值为0）</li>
	 *   <li>{@code BEST_SPEED}：最快压缩速度（值为1）</li>
	 *   <li>{@code BEST_COMPRESSION}：最佳压缩率（值为9）</li>
	 *   <li>{@code DEFAULT_COMPRESSION}：默认压缩级别（值为-1）</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	public enum DeflaterCompressionLevel {
		/**
		 * 无压缩
		 *
		 * @since 2.1.0
		 */
		NO_COMPRESSION(Deflater.DEFAULT_COMPRESSION),
		/**
		 * 最快压缩速度
		 *
		 * @since 2.1.0
		 */
		BEST_SPEED(Deflater.BEST_SPEED),
		/**
		 * 最佳压缩率
		 *
		 * @since 2.1.0
		 */
		BEST_COMPRESSION(Deflater.BEST_COMPRESSION),
		/**
		 * 默认压缩级别
		 *
		 * @since 2.1.0
		 */
		DEFAULT_COMPRESSION(Deflater.DEFAULT_COMPRESSION);

		/**
		 * 压缩级别值。
		 *
		 * @since 2.1.0
		 */
		private final int value;

		/**
		 * 构造函数。
		 *
		 * @param value 压缩级别值
		 * @since 2.1.0
		 */
		DeflaterCompressionLevel(int value) {
			this.value = value;
		}

		/**
		 * 获取压缩级别值。
		 *
		 * @return 压缩级别值
		 * @since 2.1.0
		 */
		public int getValue() {
			return value;
		}
	}

	/**
	 * Deflater压缩策略枚举。
	 * <p>
	 * 定义GZIP压缩的压缩策略，对应Java {@link java.util.zip.Deflater} 的压缩策略常量。
	 * </p>
	 *
	 * <p><strong>可选值</strong></p>
	 * <ul>
	 *   <li>{@code FILTERED}：过滤压缩策略，适用于已过滤的数据</li>
	 *   <li>{@code HUFFMAN_ONLY}：仅使用霍夫曼编码压缩</li>
	 *   <li>{@code DEFAULT_STRATEGY}：默认压缩策略</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	public enum DeflaterDeflateStrategy {
		/**
		 * 过滤压缩策略。
		 *
		 * @since 2.1.0
		 */
		FILTERED(Deflater.FILTERED),
		/**
		 * 仅霍夫曼编码压缩。
		 *
		 * @since 2.1.0
		 */
		HUFFMAN_ONLY(Deflater.HUFFMAN_ONLY),
		/**
		 * 默认压缩策略。
		 *
		 * @since 2.1.0
		 */
		DEFAULT_STRATEGY(Deflater.DEFAULT_STRATEGY);

		/**
		 * 压缩策略值。
		 *
		 * @since 2.1.0
		 */
		private final int value;

		/**
		 * 构造函数。
		 *
		 * @param value 压缩策略值
		 * @since 2.1.0
		 */
		DeflaterDeflateStrategy(int value) {
			this.value = value;
		}

		/**
		 * 获取压缩策略值。
		 *
		 * @return 压缩策略值
		 * @since 2.1.0
		 */
		public int getValue() {
			return value;
		}
	}

	/**
	 * XZ压缩模式枚举。
	 * <p>
	 * 定义XZ压缩的压缩模式，对应 {@link org.tukaani.xz.LZMA2Options} 的压缩模式常量。
	 * </p>
	 *
	 * <p><strong>可选值</strong></p>
	 * <ul>
	 *   <li>{@code UNCOMPRESSED}：不压缩模式</li>
	 *   <li>{@code FAST}：快速压缩模式</li>
	 *   <li>{@code NORMAL}：普通压缩模式</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	public enum XZCompressionMode {
		/**
		 * 不压缩模式。
		 *
		 * @since 2.1.0
		 */
		UNCOMPRESSED(LZMA2Options.MODE_UNCOMPRESSED),
		/**
		 * 快速压缩模式。
		 *
		 * @since 2.1.0
		 */
		FAST(LZMA2Options.MODE_FAST),
		/**
		 * 普通压缩模式。
		 *
		 * @since 2.1.0
		 */
		NORMAL(LZMA2Options.MODE_NORMAL);

		/**
		 * 压缩模式值。
		 *
		 * @since 2.1.0
		 */
		private final int value;

		/**
		 * 构造函数。
		 *
		 * @param value 压缩模式值
		 * @since 2.1.0
		 */
		XZCompressionMode(int value) {
			this.value = value;
		}

		/**
		 * 获取压缩模式值。
		 *
		 * @return 压缩模式值
		 * @since 2.1.0
		 */
		public int getValue() {
			return value;
		}
	}

	/**
	 * 压缩类型枚举。
	 * <p>
	 * 定义支持的压缩算法类型。
	 * </p>
	 *
	 * <p><strong>可选值</strong></p>
	 * <ul>
	 *   <li>{@code GZIP}：GZIP压缩格式</li>
	 *   <li>{@code ZSTD}：Zstandard压缩格式</li>
	 *   <li>{@code XZ}：XZ压缩格式</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	public enum CompressionType {
		/**
		 * GZIP压缩格式。
		 *
		 * @since 2.1.0
		 */
		GZIP,
		/**
		 * Zstandard压缩格式。
		 *
		 * @since 2.1.0
		 */
		ZSTD,
		/**
		 * XZ压缩格式。
		 *
		 * @since 2.1.0
		 */
		XZ
	}

	/**
	 * 归档类型枚举。
	 * <p>
	 * 定义支持的归档格式类型。
	 * </p>
	 *
	 * <p><strong>可选值</strong></p>
	 * <ul>
	 *   <li>{@code TAR}：TAR归档格式</li>
	 *   <li>{@code ZIP}：ZIP归档格式</li>
	 *   <li>{@code SEVEN_Z}：7-Zip归档格式</li>
	 *   <li>{@code TAR_GZIP}：TAR+GZIP组合格式</li>
	 *   <li>{@code TAR_ZSTD}：TAR+Zstd组合格式</li>
	 *   <li>{@code TAR_XZ}：TAR+XZ组合格式</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	public enum ArchiveType {
		/**
		 * TAR归档格式。
		 *
		 * @since 2.1.0
		 */
		TAR,
		/**
		 * ZIP归档格式。
		 *
		 * @since 2.1.0
		 */
		ZIP,
		/**
		 * 7-Zip归档格式。
		 *
		 * @since 2.1.0
		 */
		SEVEN_Z,
		/**
		 * TAR+GZIP组合格式。
		 *
		 * @since 2.1.0
		 */
		TAR_GZIP,
		/**
		 * TAR+Zstd组合格式。
		 *
		 * @since 2.1.0
		 */
		TAR_ZSTD,
		/**
		 * TAR+XZ组合格式。
		 *
		 * @since 2.1.0
		 */
		TAR_XZ
	}

	/**
	 * 加密归档类型枚举。
	 * <p>
	 * 定义支持的加密归档格式类型。
	 * </p>
	 *
	 * <p><strong>可选值</strong></p>
	 * <ul>
	 *   <li>{@code ZIP}：ZIP加密归档格式</li>
	 *   <li>{@code SEVEN_Z}：7-Zip加密归档格式</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	public enum ArchiveEncryptType {
		/**
		 * ZIP加密归档格式。
		 *
		 * @since 2.1.0
		 */
		ZIP,
		/**
		 * 7-Zip加密归档格式。
		 *
		 * @since 2.1.0
		 */
		SEVEN_Z
	}

	/**
	 * GZIP压缩配置。
	 * <p>
	 * 配置GZIP压缩的压缩级别和压缩策略。
	 * </p>
	 *
	 * <p><strong>配置属性</strong></p>
	 * <ul>
	 *   <li>{@code pangju.compress.gzip.compression-level}：压缩级别</li>
	 *   <li>{@code pangju.compress.gzip.deflate-strategy}：压缩策略</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	public static class Gzip {
		/**
		 * 压缩级别。
		 *
		 * @since 2.1.0
		 */
		private DeflaterCompressionLevel compressionLevel;
		/**
		 * 压缩策略。
		 *
		 * @since 2.1.0
		 */
		private DeflaterDeflateStrategy deflateStrategy;

		/**
		 * 获取压缩级别。
		 *
		 * @return 压缩级别
		 * @since 2.1.0
		 */
		public DeflaterCompressionLevel getCompressionLevel() {
			return compressionLevel;
		}

		/**
		 * 设置压缩级别。
		 *
		 * @param compressionLevel 压缩级别
		 * @since 2.1.0
		 */
		public void setCompressionLevel(DeflaterCompressionLevel compressionLevel) {
			this.compressionLevel = compressionLevel;
		}

		/**
		 * 获取压缩策略。
		 *
		 * @return 压缩策略
		 * @since 2.1.0
		 */
		public DeflaterDeflateStrategy getDeflateStrategy() {
			return deflateStrategy;
		}

		/**
		 * 设置压缩策略。
		 *
		 * @param deflateStrategy 压缩策略
		 * @since 2.1.0
		 */
		public void setDeflateStrategy(DeflaterDeflateStrategy deflateStrategy) {
			this.deflateStrategy = deflateStrategy;
		}
	}

	/**
	 * Zstd压缩配置。
	 * <p>
	 * 配置Zstandard压缩的压缩级别。
	 * </p>
	 *
	 * <p><strong>配置属性</strong></p>
	 * <ul>
	 *   <li>{@code pangju.compress.zstd.compression-level}：压缩级别（负数到22）</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	public static class Zstd {
		/**
		 * 压缩级别。
		 *
		 * @since 2.1.0
		 */
		private Integer compressionLevel;

		/**
		 * 获取压缩级别。
		 *
		 * @return 压缩级别
		 * @since 2.1.0
		 */
		public Integer getCompressionLevel() {
			return compressionLevel;
		}

		/**
		 * 设置压缩级别。
		 *
		 * @param compressionLevel 压缩级别
		 * @since 2.1.0
		 */
		public void setCompressionLevel(Integer compressionLevel) {
			this.compressionLevel = compressionLevel;
		}
	}

	/**
	 * XZ压缩配置。
	 * <p>
	 * 配置XZ压缩的预设级别和压缩模式。
	 * </p>
	 *
	 * <p><strong>配置属性</strong></p>
	 * <ul>
	 *   <li>{@code pangju.compress.xz.preset}：预设级别（0-9）</li>
	 *   <li>{@code pangju.compress.xz.compression-mode}：压缩模式</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	public static class XZ {
		/**
		 * 预设级别。
		 *
		 * @since 2.1.0
		 */
		private Integer preset;
		/**
		 * 压缩模式。
		 *
		 * @since 2.1.0
		 */
		private XZCompressionMode compressionMode;

		/**
		 * 获取预设级别。
		 *
		 * @return 预设级别
		 * @since 2.1.0
		 */
		public Integer getPreset() {
			return preset;
		}

		/**
		 * 设置预设级别。
		 *
		 * @param preset 预设级别
		 * @since 2.1.0
		 */
		public void setPreset(Integer preset) {
			this.preset = preset;
		}

		/**
		 * 获取压缩模式。
		 *
		 * @return 压缩模式
		 * @since 2.1.0
		 */
		public XZCompressionMode getCompressionMode() {
			return compressionMode;
		}

		/**
		 * 设置压缩模式。
		 *
		 * @param compressionMode 压缩模式
		 * @since 2.1.0
		 */
		public void setCompressionMode(XZCompressionMode compressionMode) {
			this.compressionMode = compressionMode;
		}
	}

	/**
	 * 7-Zip归档配置。
	 * <p>
	 * 配置7-Zip归档的压缩方法。
	 * </p>
	 *
	 * <p><strong>配置属性</strong></p>
	 * <ul>
	 *   <li>{@code pangju.compress.seven-z.method}：压缩方法</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	public static class SevenZ {
		/**
		 * 压缩方法（目前仅支持 COPY、LZMA2、BZIP2 和 DEFLATE）。
		 *
		 * @since 2.1.0
		 */
		private SevenZMethod method;

		/**
		 * 获取压缩方法。
		 *
		 * @return 压缩方法
		 * @since 2.1.0
		 */
		public SevenZMethod getMethod() {
			return method;
		}

		/**
		 * 设置压缩方法。
		 *
		 * @param method 压缩方法
		 * @since 2.1.0
		 */
		public void setMethod(SevenZMethod method) {
			this.method = method;
		}
	}

	/**
	 * ZIP归档配置。
	 * <p>
	 * 配置ZIP归档的压缩级别。
	 * </p>
	 *
	 * <p><strong>配置属性</strong></p>
	 * <ul>
	 *   <li>{@code pangju.compress.zip.compression-level}：压缩级别</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	public static class Zip {
		/**
		 * 压缩级别。
		 *
		 * @since 2.1.0
		 */
		private DeflaterCompressionLevel compressionLevel;

		/**
		 * 获取压缩级别。
		 *
		 * @return 压缩级别
		 * @since 2.1.0
		 */
		public DeflaterCompressionLevel getCompressionLevel() {
			return compressionLevel;
		}

		/**
		 * 设置压缩级别。
		 *
		 * @param compressionLevel 压缩级别
		 * @since 2.1.0
		 */
		public void setCompressionLevel(DeflaterCompressionLevel compressionLevel) {
			this.compressionLevel = compressionLevel;
		}
	}
}
