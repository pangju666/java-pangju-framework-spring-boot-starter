package io.github.pangju666.framework.autoconfigure.web.validation.store;

public interface SignatureSecretKeyStore {
	String loadSecretKey(String appId);
}
