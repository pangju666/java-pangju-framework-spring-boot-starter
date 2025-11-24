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

package io.github.pangju666.framework.boot.autoconfigure.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 加密模块配置属性。
 *
 * <p>前缀：{@code pangju.crypto}</p>
 *
 * <p>用于约束加密相关组件的缓存规模等行为，避免在长时间运行或高并发场景下出现内存膨胀。</p>
 *
 * @since 1.0.0
 * @author pangju666
 */
@ConfigurationProperties(prefix = "pangju.crypto")
public class CryptoProperties {
    /**
     * 缓存键最大数量。
     *
     * <p>用于加密工厂缓存的密钥数量。</p>
     * <p>对应属性：{@code pangju.crypto.max-cache-crypto-key-size}</p>
     * <p>默认值：{@code 16}</p>
     *
     * @since 1.0.0
     */
    private int maxCacheCryptoKeySize = 16;

	public int getMaxCacheCryptoKeySize() {
		return maxCacheCryptoKeySize;
	}

	public void setMaxCacheCryptoKeySize(int maxCacheCryptoKeySize) {
		this.maxCacheCryptoKeySize = maxCacheCryptoKeySize;
	}
}
