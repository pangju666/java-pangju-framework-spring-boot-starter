package io.github.pangju666.framework.boot.autoconfigure.data.dynamic.redis;

/**
 * Exception thrown when a Redis URL is malformed or invalid.
 *
 * <p>copy from org.springframework.boot.autoconfigure.data.redis.RedisUrlSyntaxException</p>
 *
 * @author Scott Frederick
 */
class RedisUrlSyntaxException extends RuntimeException {

	private final String url;

	RedisUrlSyntaxException(String url, Exception cause) {
		super(buildMessage(url), cause);
		this.url = url;
	}

	RedisUrlSyntaxException(String url) {
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
