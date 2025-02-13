package io.github.pangju666.framework.autoconfigure.web.store;

public interface SignatureSecretKeyStore {
	String loadSecretKey(String appId);
}
