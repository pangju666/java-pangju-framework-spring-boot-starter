package io.github.pangju666.framework.boot.autoconfigure.crypto

import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory
import io.github.pangju666.framework.boot.crypto.factory.impl.AES256CryptoFactory
import io.github.pangju666.framework.boot.crypto.factory.impl.BasicCryptoFactory
import io.github.pangju666.framework.boot.crypto.factory.impl.RSACryptoFactory
import io.github.pangju666.framework.boot.crypto.factory.impl.StrongCryptoFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = CryptoAutoConfiguration.class, loader = SpringBootContextLoader.class)
class CryptoFactorySpec extends Specification {
	@Autowired
	List<CryptoFactory> factories

	def "从Spring中获取多个实现"() {
		expect:
		factories.size() == 4
		factories*.class.toSet() == [
			AES256CryptoFactory,
			BasicCryptoFactory,
			StrongCryptoFactory,
			RSACryptoFactory
		] as Set
	}

	def "集合中AES实现可用"() {
		given:
		def f = factories.find { it instanceof AES256CryptoFactory }

		when:
		def enc = f.getTextEncryptor("pwd").encrypt("abc")
		def dec = f.getTextDecryptor("pwd").decrypt(enc)

		then:
		dec == "abc"
	}
}
