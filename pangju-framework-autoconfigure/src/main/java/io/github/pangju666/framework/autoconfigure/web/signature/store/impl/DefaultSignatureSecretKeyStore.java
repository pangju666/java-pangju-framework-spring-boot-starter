package io.github.pangju666.framework.autoconfigure.web.signature.store.impl;

import io.github.pangju666.framework.autoconfigure.web.signature.RequestSignatureProperties;
import io.github.pangju666.framework.autoconfigure.web.signature.store.SignatureSecretKeyStore;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

public class DefaultSignatureSecretKeyStore implements SignatureSecretKeyStore {
	private final Map<String, String> secretKeyMap;

	public DefaultSignatureSecretKeyStore(RequestSignatureProperties properties) {
		this.secretKeyMap = MapUtils.emptyIfNull(properties.getSecretKeys());
	}

	@Override
	public String loadSecretKey(String appId) {
		return secretKeyMap.get(appId);
	}
}
