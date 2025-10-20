package io.github.pangju666.framework.autoconfigure.enums;

public enum Algorithm {
	BASE64(false),
	RSA(true),
	AES256(true),
	HEX(false);

	private final boolean needKey;

	Algorithm(boolean needKey) {
		this.needKey = needKey;
	}

	public boolean needKey() {
		return needKey;
	}
}