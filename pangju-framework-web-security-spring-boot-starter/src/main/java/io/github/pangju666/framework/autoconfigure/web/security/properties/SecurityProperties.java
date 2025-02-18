package io.github.pangju666.framework.autoconfigure.web.security.properties;

import io.github.pangju666.commons.codec.utils.RSAKeyUtils;
import io.github.pangju666.framework.autoconfigure.web.security.enums.PasswordAlgorithm;
import io.github.pangju666.framework.autoconfigure.web.security.enums.TokenStoreType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@ConfigurationProperties(prefix = "pangju.web.security")
public class SecurityProperties {
	private List<User> users = Collections.emptyList();
    private Duration duration = Duration.ofDays(7);
    private Token token = new Token();
    private Password password = new Password();

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

    public Password getPassword() {
        return password;
    }

    public void setPassword(Password password) {
        this.password = password;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public static class Password {
        private PasswordAlgorithm algorithm = PasswordAlgorithm.PLAIN;
        private Aes256 aes256 = new Aes256();
        private Rsa rsa = new Rsa();

        public PasswordAlgorithm getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(PasswordAlgorithm algorithm) {
            this.algorithm = algorithm;
        }

        public Aes256 getAes256() {
            return aes256;
        }

        public void setAes256(Aes256 aes256) {
            this.aes256 = aes256;
        }

        public Rsa getRsa() {
            return rsa;
        }

        public void setRsa(Rsa rsa) {
            this.rsa = rsa;
        }
    }

    public static class User {
		private String username;
		private String password;
        private Set<String> roles = Collections.emptySet();
		private boolean disabled = false;
        private boolean locked = false;

        public boolean isLocked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public boolean isDisabled() {
			return disabled;
		}

		public void setDisabled(boolean disabled) {
			this.disabled = disabled;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

        public Set<String> getRoles() {
			return roles;
		}

        public void setRoles(Set<String> roles) {
			this.roles = roles;
		}
	}

	public static class Aes256 {
        private String password;

        public String getPassword() {
            return password;
		}

        public void setPassword(String password) {
            this.password = password;
		}
	}

	public static class Rsa {
        private String privateKey;
        private String publicKey;
        private String algorithm = RSAKeyUtils.DEFAULT_CIPHER_ALGORITHM;

		public String getPrivateKey() {
			return privateKey;
		}

		public void setPrivateKey(String privateKey) {
			this.privateKey = privateKey;
		}

		public String getPublicKey() {
			return publicKey;
		}

		public void setPublicKey(String publicKey) {
			this.publicKey = publicKey;
		}

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }
    }

    public static class Token {
        private TokenStoreType type = TokenStoreType.MAP;
        private int tokenLength = 128;
        private Duration accessTokenExpire = Duration.ofHours(1);
        private Duration refreshTokenExpire = Duration.ofDays(30);
        private boolean isConcurrent = true;
        private int maxLoginCount = 12;
        private Redis redis = new Redis();

        public TokenStoreType getType() {
            return type;
        }

        public void setType(TokenStoreType type) {
            this.type = type;
        }

        public int getTokenLength() {
            return tokenLength;
        }

        public void setTokenLength(int tokenLength) {
            this.tokenLength = tokenLength;
        }

        public Duration getAccessTokenExpire() {
            return accessTokenExpire;
        }

        public void setAccessTokenExpire(Duration accessTokenExpire) {
            this.accessTokenExpire = accessTokenExpire;
        }

        public Duration getRefreshTokenExpire() {
            return refreshTokenExpire;
        }

        public void setRefreshTokenExpire(Duration refreshTokenExpire) {
            this.refreshTokenExpire = refreshTokenExpire;
        }

        public boolean isConcurrent() {
            return isConcurrent;
        }

        public void setConcurrent(boolean concurrent) {
            isConcurrent = concurrent;
        }

        public int getMaxLoginCount() {
            return maxLoginCount;
        }

        public void setMaxLoginCount(int maxLoginCount) {
            this.maxLoginCount = maxLoginCount;
        }

        public Redis getRedis() {
            return redis;
        }

        public void setRedis(Redis redis) {
            this.redis = redis;
        }
    }

    public static class Redis {
        private String accessTokenUserKeyPrefix = "access_token_user";
        private String refreshTokenUserKeyPrefix = "refresh_token_user";
        private String userAccessTokenSetKeyPrefix = "user_access_token";
        private String tokenSetKey = "token_set";
        private String beanName;

        public String getBeanName() {
            return beanName;
        }

        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }

        public String getAccessTokenUserKeyPrefix() {
            return accessTokenUserKeyPrefix;
        }

        public void setAccessTokenUserKeyPrefix(String accessTokenUserKeyPrefix) {
            this.accessTokenUserKeyPrefix = accessTokenUserKeyPrefix;
        }

        public String getRefreshTokenUserKeyPrefix() {
            return refreshTokenUserKeyPrefix;
        }

        public void setRefreshTokenUserKeyPrefix(String refreshTokenUserKeyPrefix) {
            this.refreshTokenUserKeyPrefix = refreshTokenUserKeyPrefix;
        }

        public String getUserAccessTokenSetKeyPrefix() {
            return userAccessTokenSetKeyPrefix;
        }

        public void setUserAccessTokenSetKeyPrefix(String userAccessTokenSetKeyPrefix) {
            this.userAccessTokenSetKeyPrefix = userAccessTokenSetKeyPrefix;
        }

        public String getTokenSetKey() {
            return tokenSetKey;
        }

        public void setTokenSetKey(String tokenSetKey) {
            this.tokenSetKey = tokenSetKey;
        }
    }
}
