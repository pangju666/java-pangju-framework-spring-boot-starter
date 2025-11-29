package io.github.pangju666.framework.boot.autoconfigure.data.dynamic.redis;

import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A parsed URL used to connect to Redis.
 *
 * <p>copy from org.springframework.boot.autoconfigure.data.redis.RedisUrl</p>
 *
 * @param uri         the source URI
 * @param useSsl      if SSL is used to connect
 * @param credentials the connection credentials
 * @param database    the database index
 * @author Mark Paluch
 * @author Stephane Nicoll
 * @author Alen Turkovic
 * @author Scott Frederick
 * @author Eddú Meléndez
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @author Yanming Zhou
 * @author Phillip Webb
 */
record RedisUrl(URI uri, boolean useSsl, RedisUrl.Credentials credentials, int database) {

	static RedisUrl of(String url) {
		return (url != null) ? of(toUri(url)) : null;
	}

	private static RedisUrl of(URI uri) {
		boolean useSsl = ("rediss".equals(uri.getScheme()));
		Credentials credentials = RedisUrl.Credentials.fromUserInfo(uri.getUserInfo());
		int database = getDatabase(uri);
		return new RedisUrl(uri, useSsl, credentials, database);
	}

	private static int getDatabase(URI uri) {
		String path = uri.getPath();
		String[] split = (!StringUtils.hasText(path)) ? new String[0] : path.split("/", 2);
		return (split.length > 1 && !split[1].isEmpty()) ? Integer.parseInt(split[1]) : 0;
	}

	private static URI toUri(String url) {
		try {
			URI uri = new URI(url);
			String scheme = uri.getScheme();
			if (!"redis".equals(scheme) && !"rediss".equals(scheme)) {
				throw new RedisUrlSyntaxException(url);
			}
			return uri;
		}
		catch (URISyntaxException ex) {
			throw new RedisUrlSyntaxException(url, ex);
		}
	}

	/**
	 * Redis connection credentials.
	 *
	 * @param username the username or {@code null}
	 * @param password the password
	 */
	record Credentials(String username, String password) {

		private static final RedisUrl.Credentials NONE = new RedisUrl.Credentials(null, null);

		private static RedisUrl.Credentials fromUserInfo(String userInfo) {
			if (userInfo == null) {
				return NONE;
			}
			int index = userInfo.indexOf(':');
			if (index != -1) {
				return new RedisUrl.Credentials(userInfo.substring(0, index), userInfo.substring(index + 1));
			}
			return new RedisUrl.Credentials(null, userInfo);
		}

	}

}