package io.github.pangju666.framework.boot.autoconfigure.jackson

import io.github.pangju666.framework.boot.autoconfigure.crypto.CryptoAutoConfiguration
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory
import io.github.pangju666.framework.boot.crypto.factory.impl.AES256CryptoFactory
import io.github.pangju666.framework.boot.crypto.factory.impl.BasicCryptoFactory
import io.github.pangju666.framework.boot.crypto.factory.impl.RSACryptoFactory
import io.github.pangju666.framework.boot.crypto.factory.impl.StrongCryptoFactory
import io.github.pangju666.framework.boot.jackson.utils.CryptoFactoryRegistry
import io.github.pangju666.framework.boot.spring.StaticSpringContext

import org.jasypt.util.binary.BinaryEncryptor
import org.jasypt.util.numeric.DecimalNumberEncryptor
import org.jasypt.util.numeric.IntegerNumberEncryptor
import org.jasypt.util.text.TextEncryptor
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@ContextConfiguration(classes = [CryptoAutoConfiguration.class], loader = SpringBootContextLoader.class)
class CryptoFactoryRegistrySpec extends Specification {
	def setup() {
		clearRegistry()
		setBeanFactory(null)
	}

	def cleanup() {
		setBeanFactory(null)
		clearRegistry()
	}

	def "BeanFactory可用：优先从Spring获取并缓存同实例（Spring原生）"() {
		given:
		def bf = new DefaultListableBeanFactory()
		def aes = new AES256CryptoFactory(16)
		bf.registerSingleton("aes256CryptoFactory", aes)
		setBeanFactory(bf)

		when:
		def first = CryptoFactoryRegistry.getOrCreate(AES256CryptoFactory)

		then:
		first.is(aes)

		when:
		setBeanFactory(null)
		def second = CryptoFactoryRegistry.getOrCreate(AES256CryptoFactory)

		then:
		second.is(aes)
	}

	def "BeanFactory为null：回退直接构造并缓存"() {
		when:
		def first = CryptoFactoryRegistry.getOrCreate(AES256CryptoFactory)
		def second = CryptoFactoryRegistry.getOrCreate(AES256CryptoFactory)

		then:
		first instanceof AES256CryptoFactory
		second.is(first)
	}

	def "BeanFactory抛异常：回退直接构造"() {
		given:
		def bf = new DefaultListableBeanFactory() // 未注册 BasicCryptoFactory
		setBeanFactory(bf)

		when:
		def got = CryptoFactoryRegistry.getOrCreate(BasicCryptoFactory)

		then:
		got instanceof BasicCryptoFactory
	}

	def "显式构造内置实现类型正确"() {
		expect:
		CryptoFactoryRegistry.getOrCreate(AES256CryptoFactory) instanceof AES256CryptoFactory
		CryptoFactoryRegistry.getOrCreate(StrongCryptoFactory) instanceof StrongCryptoFactory
		CryptoFactoryRegistry.getOrCreate(RSACryptoFactory) instanceof RSACryptoFactory
		CryptoFactoryRegistry.getOrCreate(BasicCryptoFactory) instanceof BasicCryptoFactory
	}

	def "反射构造其他实现：无参构造可用"() {
		when:
		def got = CryptoFactoryRegistry.getOrCreate(TestCryptoFactory)

		then:
		got instanceof TestCryptoFactory
	}

	def "register后复用缓存实例优先于容器"() {
		given:
		def bf = new DefaultListableBeanFactory()
		def aesFromContainer = new AES256CryptoFactory(16)
		bf.registerSingleton("aes256CryptoFactory", aesFromContainer)
		setBeanFactory(bf)

		def aesRegistered = new AES256CryptoFactory(16)
		CryptoFactoryRegistry.register(aesRegistered)

		when:
		def got = CryptoFactoryRegistry.getOrCreate(AES256CryptoFactory)

		then:
		got.is(aesRegistered)

		when:
		setBeanFactory(null)
		def got2 = CryptoFactoryRegistry.getOrCreate(AES256CryptoFactory)

		then:
		got2.is(aesRegistered)
	}

	def "并发获取同类：返回同一实例"() {
		given:
		def pool = Executors.newFixedThreadPool(16)
		def start = new CountDownLatch(1)
		def tasks = (0..<32).collect {
			(Callable<CryptoFactory>) {
				start.await()
				CryptoFactoryRegistry.getOrCreate(StrongCryptoFactory)
			}
		}

		when:
		def futures = tasks.collect { pool.submit(it) }
		start.countDown()
		def results = futures.collect { it.get(5, TimeUnit.SECONDS) }
		pool.shutdown()
		pool.awaitTermination(5, TimeUnit.SECONDS)

		then:
		results.every { it.is(results[0]) }
	}

	private static void setBeanFactory(BeanFactory bf) {
		def f = StaticSpringContext.class.getDeclaredField('BEAN_FACTORY')
		f.setAccessible(true)
		f.set(null, bf)
	}

	private static void clearRegistry() {
		def f = CryptoFactoryRegistry.class.getDeclaredField('CRYPTO_FACTORY_MAP')
		f.setAccessible(true)
		((Map) f.get(null)).clear()
	}

	static class TestCryptoFactory implements CryptoFactory {
		TestCryptoFactory() {}
		@Override BinaryEncryptor getBinaryEncryptor(String key) { null }
		@Override TextEncryptor getTextEncryptor(String key) { null }
		@Override IntegerNumberEncryptor getIntegerNumberEncryptor(String key) { null }
		@Override DecimalNumberEncryptor getDecimalNumberEncryptor(String key) { null }
	}
}
