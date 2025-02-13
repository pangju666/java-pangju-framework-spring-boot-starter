package io.github.pangju666.framework.autoconfigure.web.store.impl;

import io.github.pangju666.framework.autoconfigure.web.properties.RequestSignatureProperties;
import io.github.pangju666.framework.autoconfigure.web.store.SignatureSecretKeyStore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapSignatureSecretKeyStore implements SignatureSecretKeyStore {
	Map<String, String> secretKeyMap;

	public MapSignatureSecretKeyStore(RequestSignatureProperties properties) {
		this.secretKeyMap = new ConcurrentHashMap<>();
	}

	@Override
	public String loadSecretKey(String appId) {
		return secretKeyMap.get(appId);
	}
}
