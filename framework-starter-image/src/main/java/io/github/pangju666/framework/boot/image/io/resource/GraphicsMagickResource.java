/*
 *   Copyright 2026 pangju666
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

package io.github.pangju666.framework.boot.image.io.resource;

import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.framework.boot.image.exception.ImageParsingException;
import io.github.pangju666.framework.boot.image.model.gm.IdentifyResult;
import io.github.pangju666.framework.boot.image.utils.GraphicsMagickUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMException;
import org.gm4java.engine.GMServiceException;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * GraphicsMagick图像资源类。
 * <p>
 * 继承自{@link IOResource}，在基础资源功能之上增加了通过GraphicsMagick引擎解析图像信息的能力。
 * </p>
 * <p>
 * 该类在构造时会自动调用GraphicsMagick的identify命令解析图像的详细信息，包括尺寸、格式、是否包含透明通道等。
 * 如果传入的资源已经是GraphicsMagickResource实例，则复用其解析结果，避免重复解析。
 * </p>
 * <p>
 * 支持从多种数据源创建：文件路径、File对象、字节数组、输入流或已有的{@link IOResource}对象。
 * </p>
 *
 * @author pangju666
 * @see IOResource
 * @since 2.1.0
 */
public class GraphicsMagickResource extends IOResource {
	/**
	 * GraphicsMagick识别结果，包含图像的尺寸、格式、透明通道等信息
	 *
	 * @see IdentifyResult
	 * @since 2.1.0
	 */
	protected final IdentifyResult identifyResult;

	/**
	 * 基于IOResource构造GraphicsMagick资源。
	 * <p>
	 * 如果传入的resource已经是GraphicsMagickResource实例，则复用其解析结果。
	 * 否则使用GraphicsMagick引擎解析图像信息。
	 * </p>
	 *
	 * @param resource   基础资源对象
	 * @param connection GraphicsMagick连接对象
	 * @throws IOException                  IO异常
	 * @throws GMServiceException           GraphicsMagick服务异常
	 * @throws UnsupportedResourceException 不支持的资源类型异常
	 * @since 2.1.0
	 */
	public GraphicsMagickResource(IOResource resource, GMConnection connection) throws IOException, GMServiceException {
		super(resource, false);

		if (resource instanceof GraphicsMagickResource graphicsMagickResource) {
			this.identifyResult = graphicsMagickResource.identifyResult;
		} else {
			Assert.notNull(connection, "connection 不可为 null");
			if (!isImage()) {
				throw new UnsupportedResourceException("resource 不是图像类型资源");
			}

			try {
				this.identifyResult = GraphicsMagickUtils.identify(getFile(), connection);
				if (Objects.isNull(identifyResult.imageSize())) {
					throw new ImageParsingException(file, "尺寸解析失败");
				}
			} catch (GMException e) {
				if (StringUtils.isNotBlank(this.format)) {
					throw new UnsupportedResourceException("不支持读取 " + this.format + " 格式图像");
				} else {
					throw new UnsupportedResourceException("不支持读取 " + this.mimeType + " 类型图像");
				}
			}
		}
	}

	/**
	 * 基于文件路径构造GraphicsMagick资源。
	 *
	 * @param filePath   图像文件路径
	 * @param connection GraphicsMagick连接对象
	 * @throws IOException                  IO异常
	 * @throws GMServiceException           GraphicsMagick服务异常
	 * @throws UnsupportedResourceException 不支持的资源类型异常
	 * @since 2.1.0
	 */
	public GraphicsMagickResource(String filePath, GMConnection connection) throws IOException, GMServiceException {
		super(filePath, false);

		Assert.notNull(connection, "connection 不可为 null");
		if (!isImage()) {
			throw new UnsupportedResourceException("filePath 不是图像文件路径");
		}

		try {
			this.identifyResult = GraphicsMagickUtils.identify(getFile(), connection);
			if (Objects.isNull(identifyResult.imageSize())) {
				throw new ImageParsingException(file, "尺寸解析失败");
			}
		} catch (GMException e) {
			if (StringUtils.isNotBlank(this.format)) {
				throw new UnsupportedResourceException("不支持读取 " + this.format + " 格式图像");
			} else {
				throw new UnsupportedResourceException("不支持读取 " + this.mimeType + " 类型图像");
			}
		}
	}

	/**
	 * 基于File对象构造GraphicsMagick资源。
	 *
	 * @param file       图像文件对象
	 * @param connection GraphicsMagick连接对象
	 * @throws IOException                  IO异常
	 * @throws GMServiceException           GraphicsMagick服务异常
	 * @throws UnsupportedResourceException 不支持的资源类型异常
	 * @since 2.1.0
	 */
	public GraphicsMagickResource(File file, GMConnection connection) throws IOException, GMServiceException {

		super(file, false);

		Assert.notNull(connection, "connection 不可为 null");
		if (!isImage()) {
			throw new UnsupportedResourceException("file 不是图像文件");
		}

		try {
			this.identifyResult = GraphicsMagickUtils.identify(getFile(), connection);
			if (Objects.isNull(identifyResult.imageSize())) {
				throw new ImageParsingException(file, "尺寸解析失败");
			}
		} catch (GMException e) {
			if (StringUtils.isNotBlank(this.format)) {
				throw new UnsupportedResourceException("不支持读取 " + this.format + " 格式图像");
			} else {
				throw new UnsupportedResourceException("不支持读取 " + this.mimeType + " 类型图像");
			}
		}
	}

	/**
	 * 基于字节数组构造GraphicsMagick资源。
	 *
	 * @param bytes      图像字节数组
	 * @param connection GraphicsMagick连接对象
	 * @throws IOException                  IO异常
	 * @throws GMServiceException           GraphicsMagick服务异常
	 * @throws UnsupportedResourceException 不支持的资源类型异常
	 * @since 2.1.0
	 */
	public GraphicsMagickResource(byte[] bytes, GMConnection connection) throws IOException, GMServiceException {

		super(bytes);

		Assert.notNull(connection, "connection 不可为 null");
		if (!isImage()) {
			throw new UnsupportedResourceException("bytes 不是图像数据");
		}

		try {
			this.identifyResult = GraphicsMagickUtils.identify(getFile(), connection);
			if (Objects.isNull(identifyResult.imageSize())) {
				throw new ImageParsingException(file, "尺寸解析失败");
			}
		} catch (GMException e) {
			if (StringUtils.isNotBlank(this.format)) {
				throw new UnsupportedResourceException("不支持读取 " + this.format + " 格式图像");
			} else {
				throw new UnsupportedResourceException("不支持读取 " + this.mimeType + " 类型图像");
			}
		}
	}

	/**
	 * 基于输入流构造GraphicsMagick资源。
	 *
	 * @param inputStream 图像数据输入流
	 * @param connection  GraphicsMagick连接对象
	 * @throws IOException                  IO异常
	 * @throws GMServiceException           GraphicsMagick服务异常
	 * @throws UnsupportedResourceException 不支持的资源类型异常
	 * @since 2.1.0
	 */
	public GraphicsMagickResource(InputStream inputStream, GMConnection connection) throws IOException, GMServiceException {

		super(inputStream);

		Assert.notNull(connection, "connection 不可为 null");
		if (!isImage()) {
			throw new UnsupportedResourceException("inputStream 不是图像数据输入流");
		}

		try {
			this.identifyResult = GraphicsMagickUtils.identify(getFile(), connection);
			if (Objects.isNull(identifyResult.imageSize())) {
				throw new ImageParsingException(file, "尺寸解析失败");
			}
		} catch (GMException e) {
			if (StringUtils.isNotBlank(this.format)) {
				throw new UnsupportedResourceException("不支持读取 " + this.format + " 格式图像");
			} else {
				throw new UnsupportedResourceException("不支持读取 " + this.mimeType + " 类型图像");
			}
		}
	}

	/**
	 * 获取图像尺寸。
	 *
	 * @return 图像尺寸对象
	 * @since 2.1.0
	 */
	public ImageSize getImageSize() {
		checkClosed();

		return identifyResult.imageSize();
	}

	/**
	 * 获取图像格式。
	 *
	 * @return 图像格式，如"jpg"、"png"等，可能为null
	 * @since 2.1.0
	 */
	public @Nullable String getImageFormat() {
		checkClosed();

		return identifyResult.format();
	}

	/**
	 * 判断图像是否包含透明通道。
	 *
	 * @return 如果包含透明通道返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean hasAlpha() {
		checkClosed();

		return BooleanUtils.isTrue(identifyResult.hasAlpha());
	}

	/**
	 * 获取GraphicsMagick识别结果。
	 *
	 * @return 识别结果对象，包含图像的完整信息
	 * @since 2.1.0
	 */
	public IdentifyResult getIdentifyResult() {
		checkClosed();

		return identifyResult;
	}
}
