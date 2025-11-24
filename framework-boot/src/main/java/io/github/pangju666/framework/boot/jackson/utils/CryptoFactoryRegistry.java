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

package io.github.pangju666.framework.boot.jackson.utils;

import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.crypto.factory.impl.AES256CryptoFactory;
import io.github.pangju666.framework.boot.crypto.factory.impl.BasicCryptoFactory;
import io.github.pangju666.framework.boot.crypto.factory.impl.RSACryptoFactory;
import io.github.pangju666.framework.boot.crypto.factory.impl.StrongCryptoFactory;
import io.github.pangju666.framework.boot.spring.StaticSpringContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加密工厂注册与缓存工具。
 * <p>
 * 提供统一的 {@link CryptoFactory} 获取入口：优先从 Spring {@link BeanFactory} 获取 Bean；
 * 当容器不可用或获取失败时，回退到直接构造工厂实例。为提升性能与复用，内部使用并发映射按工厂类名缓存实例。
 * </p>
 *
 * <p>特点：线程安全、惰性创建；对常见实现（AES、RSA、Strong、Basic）使用显式构造，其他实现通过反射调用无参构造。</p>
 *
 * @author pangju666
 * @see CryptoFactory
 * @see AES256CryptoFactory
 * @see RSACryptoFactory
 * @see StrongCryptoFactory
 * @see BasicCryptoFactory
 * @see StaticSpringContext
 * @see BeanFactory
 * @since 1.0.0
 */
public final class CryptoFactoryRegistry {
    /**
     * 工厂实例缓存
     * <p>
     * 键为工厂类的完全限定名，值为对应的 {@link CryptoFactory} 实例；使用并发映射确保并发环境下的安全与可见性。
     * </p>
     *
     * @since 1.0.0
     */
    private static final Map<String, CryptoFactory> CRYPTO_FACTORY_MAP = new ConcurrentHashMap<>(4);
	
    private CryptoFactoryRegistry() {}

    /**
     * 获取或创建指定类型的加密工厂实例。
     * <p>
     * 先尝试通过 Spring 容器获取 Bean；当容器为空或抛出 {@link BeansException} 时，回退到直接构造。
     * 结果按工厂类名缓存，后续重复请求将复用已存在实例。
     * </p>
     *
     * @param factoryClass 工厂实现类
     * @return 对应的 {@link CryptoFactory} 实例（来自容器或直接构造）
	 * @see #getCryptoFactory(Class) 
     * @since 1.0.0
     */
    public static CryptoFactory getOrCreate(Class<? extends CryptoFactory> factoryClass) {
        return CRYPTO_FACTORY_MAP.computeIfAbsent(factoryClass.getName(), k -> {
            try {
                BeanFactory beanFactory = StaticSpringContext.getBeanFactory();
                if (Objects.nonNull(beanFactory)) {
                    return beanFactory.getBean(factoryClass);
                } else {
                    return getCryptoFactory(factoryClass);
                }
            } catch (BeansException e) {
                return getCryptoFactory(factoryClass);
            }
        });
    }

    /**
     * 直接构造加密工厂实例。
     * <p>
     * 对常见实现使用显式构造以避免反射开销；其余实现通过无参构造反射创建。
     * 当构造失败时抛出 {@link IllegalStateException} 封装原始异常。
     * </p>
     *
     * @param factoryClass 工厂实现类
     * @return 新创建的 {@link CryptoFactory} 实例
     * @since 1.0.0
     */
    private static CryptoFactory getCryptoFactory(Class<? extends CryptoFactory> factoryClass) {
        if (factoryClass == AES256CryptoFactory.class) {
            return new AES256CryptoFactory();
        } else if (factoryClass == RSACryptoFactory.class) {
            return new RSACryptoFactory();
        } else if (factoryClass == StrongCryptoFactory.class) {
            return new StrongCryptoFactory();
        } else if (factoryClass == BasicCryptoFactory.class) {
            return new BasicCryptoFactory();
        } else {
            try {
                return factoryClass.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
