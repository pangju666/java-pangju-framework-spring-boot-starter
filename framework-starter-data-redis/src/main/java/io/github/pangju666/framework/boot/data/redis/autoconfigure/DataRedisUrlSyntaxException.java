package io.github.pangju666.framework.boot.data.redis.autoconfigure;

/**
 * <p>copy from org.springframework.boot.data.redis.autoconfigure.DataRedisUrlSyntaxException</p>
 */
class DataRedisUrlSyntaxException extends RuntimeException {

	private final String url;

	DataRedisUrlSyntaxException(String url, Exception cause) {
		super(buildMessage(url), cause);
		this.url = url;
	}

	DataRedisUrlSyntaxException(String url) {
		super(buildMessage(url));
		this.url = url;
	}

	String getUrl() {
		return this.url;
	}

	private static String buildMessage(String url) {
		return "Invalid Redis URL '" + url + "'";
	}

}
