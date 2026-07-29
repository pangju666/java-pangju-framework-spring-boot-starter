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

package io.github.pangju666.framework.boot.image.model.opeartions;

import org.jspecify.annotations.Nullable;

/**
 * 通用图像操作类。
 * <p>
 * 继承自{@link ImageOperations}，提供通用的图像操作配置。
 * </p>
 * <p>
 * 该类不绑定任何特定的图像处理引擎，仅作为配置容器使用。
 * </p>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class GenericImageOperations extends ImageOperations<GenericImageOperations> {
	/**
	 * 默认构造函数。
	 *
	 * @since 2.1.0
	 */
	public GenericImageOperations() {
	}

	/**
	 * 拷贝构造函数。
	 *
	 * @param operations 源操作对象
	 * @since 2.1.0
	 */
	public GenericImageOperations(@Nullable ImageOperations<?> operations) {
		super(operations);
	}
}
