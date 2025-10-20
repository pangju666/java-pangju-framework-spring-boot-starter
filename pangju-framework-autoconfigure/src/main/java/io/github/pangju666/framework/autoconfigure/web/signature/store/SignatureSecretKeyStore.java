package io.github.pangju666.framework.autoconfigure.web.signature.store;

public interface SignatureSecretKeyStore {
	String loadSecretKey(String appId);
}
