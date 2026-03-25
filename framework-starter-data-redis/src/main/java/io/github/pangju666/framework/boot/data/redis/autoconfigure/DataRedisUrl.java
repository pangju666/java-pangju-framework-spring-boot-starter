package io.github.pangju666.framework.boot.data.redis.autoconfigure;

import org.jspecify.annotations.Nullable;
import org.springframework.lang.Contract;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * <p>copy from org.springframework.boot.data.redis.autoconfigure.DataRedisUrl</p>
 */
record DataRedisUrl(URI uri, boolean useSsl, DataRedisUrl.Credentials credentials, int database) {

	@Contract("!null -> !null")
	static @Nullable DataRedisUrl of(@Nullable String url) {
		return (url != null) ? of(toUri(url)) : null;
	}

	private static DataRedisUrl of(URI uri) {
		boolean useSsl = ("rediss".equals(uri.getScheme()));
		Credentials credentials = Credentials.fromUserInfo(uri.getUserInfo());
		int database = getDatabase(uri);
		return new DataRedisUrl(uri, useSsl, credentials, database);
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
				throw new DataRedisUrlSyntaxException(url);
			}
			return uri;
		}
		catch (URISyntaxException ex) {
			throw new DataRedisUrlSyntaxException(url, ex);
		}
	}

	/**
	 * Redis connection credentials.
	 *
	 * @param username the username or {@code null}
	 * @param password the password
	 */
	record Credentials(@Nullable String username, @Nullable String password) {

		private static final DataRedisUrl.Credentials NONE = new DataRedisUrl.Credentials(null, null);

		private static DataRedisUrl.Credentials fromUserInfo(@Nullable String userInfo) {
			if (userInfo == null) {
				return NONE;
			}
			int index = userInfo.indexOf(':');
			if (index != -1) {
				return new DataRedisUrl.Credentials(userInfo.substring(0, index), userInfo.substring(index + 1));
			}
			return new DataRedisUrl.Credentials(null, userInfo);
		}

	}

}