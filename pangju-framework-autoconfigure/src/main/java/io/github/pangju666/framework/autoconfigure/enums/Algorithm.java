/*
 *   Copyright 2025 pangju666
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

package io.github.pangju666.framework.autoconfigure.enums;

/**
 * 加密算法枚举类
 * <p>
 * 定义了支持的各种加密算法类型，并标识每种算法是否需要密钥
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public enum Algorithm {
	/**
	 * BASE64编码算法
	 * <p>
	 * 一种基本的编码方案，用于将二进制数据转换为ASCII字符串格式
	 * 不需要密钥
	 * </p>
	 *
	 * @since 1.0.0
	 */
	BASE64(false),

	/**
	 * RSA非对称加密算法
	 * <p>
	 * 一种使用公钥和私钥的非对称加密算法
	 * 需要密钥
	 * </p>
	 *
	 * @since 1.0.0
	 */
	RSA(true),

	/**
	 * AES256对称加密算法
	 * <p>
	 * 一种使用256位密钥的高级加密标准算法
	 * 需要密钥
	 * </p>
	 *
	 * @since 1.0.0
	 */
	AES256(true),

	/**
	 * 十六进制编码
	 * <p>
	 * 将二进制数据转换为十六进制字符串表示
	 * 不需要密钥
	 * </p>
	 *
	 * @since 1.0.0
	 */
	HEX(false);

	/**
	 * 是否需要密钥
	 *
	 * @since 1.0.0
	 */
	private final boolean needKey;

	/**
	 * 构造函数
	 *
	 * @param needKey 是否需要密钥
	 * @since 1.0.0
	 */
	Algorithm(boolean needKey) {
		this.needKey = needKey;
	}

	/**
	 * 判断该算法是否需要密钥
	 *
	 * @return 如果需要密钥返回true，否则返回false
	 * @since 1.0.0
	 */
	public boolean needKey() {
		return needKey;
	}
}