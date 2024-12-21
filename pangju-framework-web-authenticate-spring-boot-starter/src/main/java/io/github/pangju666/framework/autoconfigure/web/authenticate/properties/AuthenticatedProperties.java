package io.github.pangju666.framework.autoconfigure.web.authenticate.properties;

import io.github.pangju666.framework.autoconfigure.web.authenticate.enums.PasswordAlgorithm;
import io.github.pangju666.framework.core.lang.pool.Constants;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@ConfigurationProperties(prefix = "pangju.web.authenticate")
public class AuthenticatedProperties {
	private Request request = new Request();
	private PasswordAlgorithm passwordAlgorithm = PasswordAlgorithm.RSA;
	private Aes256 aes256 = new Aes256();
	private Rsa rsa = new Rsa();
	private Duration duration = Duration.ofDays(7);
	private List<User> users = Collections.singletonList(new User("admin", "123456", Collections.singleton(Constants.ADMIN_ROLE)));

	public PasswordAlgorithm getPasswordAlgorithm() {
		return passwordAlgorithm;
	}

	public void setPasswordAlgorithm(PasswordAlgorithm passwordAlgorithm) {
		this.passwordAlgorithm = passwordAlgorithm;
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

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

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

	public static class Request {
		private String keyUrl = "/authenticate/key";
		private String keyMethod = "GET";
		private String loginUrl = "/authenticate/login";
		private String loginMethod = "POST";
		private String usernameParameter = "username";
		private String passwordParameter = "password";

		public String getKeyMethod() {
			return keyMethod;
		}

		public void setKeyMethod(String keyMethod) {
			this.keyMethod = keyMethod;
		}

		public String getLoginMethod() {
			return loginMethod;
		}

		public void setLoginMethod(String loginMethod) {
			this.loginMethod = loginMethod;
		}

		public String getKeyUrl() {
			return keyUrl;
		}

		public void setKeyUrl(String keyUrl) {
			this.keyUrl = keyUrl;
		}

		public String getLoginUrl() {
			return loginUrl;
		}

		public void setLoginUrl(String loginUrl) {
			this.loginUrl = loginUrl;
		}

		public String getUsernameParameter() {
			return usernameParameter;
		}

		public void setUsernameParameter(String usernameParameter) {
			this.usernameParameter = usernameParameter;
		}

		public String getPasswordParameter() {
			return passwordParameter;
		}

		public void setPasswordParameter(String passwordParameter) {
			this.passwordParameter = passwordParameter;
		}
	}

	public static class Aes256 {
		private String key = "iCAk/h8jg36GU1B3";

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}
	}

	public static class Rsa {
		private String privateKey =
			"MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANdeCpeLQSsY0C8K" +
				"HpTFctuNFHoZW1W931kOQgS0Bz+O4DuhOMimvyL0dvjMHqQCStD4zPYxl8jo79Hw" +
				"LcZx0u9yMSK3iMIQ4gWrwcm192OevUzS+zCTpnH4RCVjojf62fN1Fst2xxAOCahI" +
				"KbHhgDrgLzyQ93zZzzOc3js0wEiBAgMBAAECgYB6pbmOd+VS77yPLUohGxi/42YH" +
				"6qzS9WBeTPpXx1ZhYbJdAwFdc2hiNaoTIdtJN2z3+NhwU+nGprD8c7T90BB49I4h" +
				"rUcKEBH+CSJ9FKTgZBT4qt69UXm4fKG63vGO+pQpqF0LwouZAUD9JBsGTjKP9st2" +
				"11hiGe0AYxEk0PNQSQJBAPLOzKyFdmbjgTGzw3mLip57d5P9Itv/mqzc90V9j2L9" +
				"kLV7QjtL8Muz+7Sqq8kY3YU2Y5+qkQzKdfvsp7vLUmMCQQDjEZLxellK5cTH+NPv" +
				"uMKJjkfg5tAwTGG9HSGhHDxol846mjRb7fLYvmlqtcj4o70LxAGnRG7kaiU3WyBq" +
				"SnzLAkAvy//Ecl1VcbGL/CwdsBdwjTOD4U/MaOuk3babalUgknO7FfF6xL85Ckwh" +
				"S3uXkZqBz6wa0TPOhchl+Dcoo6SrAkEAvkpaBLht4FCSCqvCoOELVs+/+QA1dGRu" +
				"fFfPeP76uQSPNZlJS8krfOyF14GkfIprwVJvuEenTxCNQ8jAb5Mg5QJAGQZxnxbf" +
				"p/mlQPwLPGAG97A/hl9gMzIZ5lGkz/aPndvIODavxyiSIyEnfYSVPWcN1IBmYEpx" +
				"ugG1HqtrMYbD6g==";
		private String publicKey =
			"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDXXgqXi0ErGNAvCh6UxXLbjRR6" +
				"GVtVvd9ZDkIEtAc/juA7oTjIpr8i9Hb4zB6kAkrQ+Mz2MZfI6O/R8C3GcdLvcjEi" +
				"t4jCEOIFq8HJtfdjnr1M0vswk6Zx+EQlY6I3+tnzdRbLdscQDgmoSCmx4YA64C88" +
				"kPd82c8znN47NMBIgQIDAQAB";

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
	}

	public static class User {
		private String username;
		private String password;
		private Set<String> roles = Collections.emptySet();

		public User() {
		}

		public User(String username, String password, Set<String> roles) {
			this.username = username;
			this.password = password;
			this.roles = roles;
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
}
