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

package io.github.pangju666.framework.boot.enums;

import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.crypto.factory.impl.AES256CryptoFactory;
import io.github.pangju666.framework.boot.crypto.factory.impl.BasicCryptoFactory;
import io.github.pangju666.framework.boot.crypto.factory.impl.RSACryptoFactory;
import io.github.pangju666.framework.boot.crypto.factory.impl.StrongCryptoFactory;

/**
 * 加密算法枚举。
 *
 * @author pangju666
 * @since 1.0.0
 */
public enum CryptoAlgorithm {
	/**
	 * RSA 非对称加密算法。
	 * <p>
	 * 基于公钥/私钥的非对称加密与签名算法，安全性高，适用于密钥交换、
	 * 短消息加密或签名校验等场景。
	 * </p>
	 * <p>
	 * 对应算法：<code>RSA/ECB/OAEPWithSHA-256AndMGF1Padding</code>
	 * </p>
	 * <p>
	 * 关联工厂：{@link RSACryptoFactory}
	 * </p>
	 */
	RSA(RSACryptoFactory.class),
	/**
	 * AES‑256 对称加密算法。
	 * <p>
	 * 使用 256 位密钥的高级加密标准算法，性能与安全性兼顾，适用于大多数
	 * 业务数据的高效加解密。
	 * </p>
	 * <p>
	 * 对应算法：<code>PBEWithMD5AndDES</code>（基于口令的加密，PBE）。
	 * </p>
	 * <p>
	 * 关联工厂：{@link AES256CryptoFactory}
	 * </p>
	 *
	 * @since 1.0.0
	 */
	AES256(AES256CryptoFactory.class),
	/**
	 * 普通强度 DES 对称加密算法。
	 * <p>
	 * 对应算法：<code>PBEWithMD5AndDES</code>（基于口令的加密，PBE）。
	 * </p>
	 * <p>
	 * 关联工厂：{@link BasicCryptoFactory}
	 * </p>
	 *
	 * @since 1.0.0
	 */
	BASIC(BasicCryptoFactory.class),
	/**
	 * 自定义加密算法。
	 * <p>
	 * 允许用户扩展并提供自定义 {@link CryptoFactory} 的实现。
	 * </p>
	 * <p>
	 * 默认关联为接口类型，只是作为占位使用。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	CUSTOM(CryptoFactory.class),
	/**
	 * 高强度 DES 对称加密算法。
	 * <p>
	 * 提供更强的安全策略或多算法组合能力，适合对安全性要求更高的场景。
	 * </p>
	 * <p>
	 * 对应算法：<code>PBEWithMD5AndTripleDES</code>（基于口令的加密，PBE）。
	 * </p>
	 * <p>
	 * 关联工厂：{@link StrongCryptoFactory}
	 * </p>
	 *
	 * @since 1.0.0
	 */
	STRONG(StrongCryptoFactory.class);

	/**
	 * 与算法枚举关联的工厂类型。
	 * 用于实例化具体的加/解密器。
	 *
	 * @since 1.0.0
	 */
	private final Class<? extends CryptoFactory> factoryClass;

	/**
	 * 枚举构造函数，关联算法与工厂类型。
	 *
	 * @param cryptoFactoryClass 对应的工厂实现类
	 * @since 1.0.0
	 */
	CryptoAlgorithm(Class<? extends CryptoFactory> cryptoFactoryClass) {
		this.factoryClass = cryptoFactoryClass;
	}

	/**
	 * 获取与当前算法关联的工厂类型。
	 *
	 * @return 工厂实现类类型
	 * @since 1.0.0
	 */
	public Class<? extends CryptoFactory> getFactoryClass() {
		return factoryClass;
	}
}