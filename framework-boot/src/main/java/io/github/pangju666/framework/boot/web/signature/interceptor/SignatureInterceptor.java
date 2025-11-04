/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.framework.boot.web.signature.interceptor;

import io.github.pangju666.commons.lang.utils.DateUtils;
import io.github.pangju666.framework.boot.web.signature.annotation.Signature;
import io.github.pangju666.framework.boot.web.signature.configuration.SignatureConfiguration;
import io.github.pangju666.framework.boot.web.signature.enums.SignatureAlgorithm;
import io.github.pangju666.framework.boot.web.signature.storer.SignatureSecretKeyStorer;
import io.github.pangju666.framework.web.exception.base.ValidationException;
import io.github.pangju666.framework.web.servlet.builder.HttpResponseBuilder;
import io.github.pangju666.framework.web.servlet.interceptor.BaseHttpInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.HandlerMethod;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 签名校验拦截器。
 * <p>
 * 用于拦截 HTTP 请求并对请求的签名信息进行校验，确保接口调用的完整性和安全性。
 * 支持自定义签名校验逻辑，包括从请求头或请求参数中提取签名数据并验证合法性。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *     <li>校验请求是否包含必要的签名字段（如 appId、签名值）。</li>
 *     <li>从 {@link SignatureConfiguration} 配置中获取签名字段名称，支持灵活自定义。</li>
 *     <li>通过 {@link SignatureSecretKeyStorer} 动态加载对应 appId 的签名密钥，计算签名并进行对比。</li>
 *     <li>支持多种签名计算算法（如 MD5、SHA256）。</li>
 *     <li>检查签名的时效性，拒绝超时签名。</li>
 * </ul>
 *
 * <p>适用场景：</p>
 * <ul>
 *     <li>适用于保护重要接口，防止请求被篡改。</li>
 *     <li>校验第三方合作伙伴调用接口的合法性。</li>
 *     <li>结合 {@link Signature} 注解在控制器实现细粒度校验。</li>
 * </ul>
 *
 * <p>签名校验步骤：</p>
 * <ol>
 *     <li>从请求中提取签名相关字段（如 appId 和签名值）。</li>
 *     <li>比对请求的 appId 是否在注解允许的范围内。</li>
 *     <li>计算签名并与请求中的签名值比较。</li>
 *     <li>验证签名的时间戳是否在允许的时间范围内。</li>
 * </ol>
 *
 * @author pangju666
 * @see Signature
 * @see SignatureConfiguration
 * @see SignatureSecretKeyStorer
 * @see BaseHttpInterceptor
 * @since 1.0.0
 */
public class SignatureInterceptor extends BaseHttpInterceptor {
	/**
	 * 签名功能的核心配置。
	 * <p>
	 * 该字段用于存储签名相关的配置项，例如签名字段名称、时间戳字段名称等。
	 * 配置由 {@link SignatureConfiguration} 提供，支持灵活的自定义配置。
	 * </p>
	 *
	 * @see SignatureConfiguration
	 * @since 1.0.0
	 */
	private final SignatureConfiguration configuration;
	/**
	 * 签名密钥存储器。
	 * <p>
	 * 该字段用于加载签名校验所需的密钥信息。通过 {@link SignatureSecretKeyStorer} 动态加载指定
	 * 应用 ID 的密钥，用于签名验证。
	 * </p>
	 *
	 * @see SignatureSecretKeyStorer
	 * @since 1.0.0
	 */
	private final SignatureSecretKeyStorer secretKeyStorer;

	/**
	 * 构造函数，初始化拦截器。
	 * <p>
	 * 通过注入 {@link SignatureConfiguration} 配置和密钥存储器 {@link SignatureSecretKeyStorer}，
	 * 构建签名校验拦截器实例。
	 * </p>
	 *
	 * @param configuration 签名相关配置类，定义如签名字段名称及位置等信息。
	 * @param secretKeyStorer 签名密钥存储器，用于根据 appId 动态加载签名密钥。
	 * @since 1.0.0
	 */
	public SignatureInterceptor(SignatureConfiguration configuration, SignatureSecretKeyStorer secretKeyStorer) {
		super(Collections.singleton("/**"), Collections.emptySet());
		this.configuration = configuration;
		this.secretKeyStorer = secretKeyStorer;
	}

	/**
	 * 拦截请求并校验签名。
	 * <p>
	 * 针对带有 {@link Signature} 注解的方法或类，执行签名校验逻辑。
	 * 根据注解配置的签名校验类型，选择从请求头或请求参数中提取签名进行验证。
	 * </p>
	 *
	 * @param request 当前的 HTTP 请求。
	 * @param response 当前的 HTTP 响应。
	 * @param handler 请求处理器（例如控制器方法）。
	 * @return 如果签名校验通过，返回 {@code true}，否则返回 {@code false}。
	 * @throws MissingRequestValueException 当缺少必要的签名字段（如 appId、签名值等）时抛出。
	 * @since 1.0.0
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws MissingRequestValueException {
		if (handler instanceof HandlerMethod handlerMethod) {
			Signature annotation = handlerMethod.getMethodAnnotation(Signature.class);
			if (Objects.isNull(annotation)) {
				Class<?> targetClass = handlerMethod.getBeanType();
				annotation = targetClass.getAnnotation(Signature.class);
				if (Objects.isNull(annotation)) {
					return true;
				}
			}

			return switch (annotation.type()) {
				case HEADER -> validateSignatureByHeaders(request, response, annotation);
				case PARAMS -> validateSignatureByParams(request, response, annotation);
				default -> {
					String signatureHeader = request.getHeader(configuration.getSignatureHeaderName());
					String timestampHeader = request.getHeader(configuration.getTimestampHeaderName());
					if (StringUtils.isAllBlank(signatureHeader, timestampHeader)) {
						yield validateSignatureByParams(request, response, annotation);
					} else {
						yield validateSignatureByHeaders(request, response, annotation);
					}
				}
			};
		}
		return true;
	}

	/**
	 * 根据请求参数校验签名。
	 * <p>
	 * 从请求参数中提取签名信息，并与计算出的签名值比较。
	 * </p>
	 *
	 * @param request HTTP 请求。
	 * @param response HTTP 响应。
	 * @param annotation 签名注解实例。
	 * @return 如果签名校验通过，返回 {@code true}，否则返回 {@code false}。
	 * @throws MissingServletRequestParameterException 当缺少必要的请求参数时抛出。
	 * @since 1.0.0
	 */
	private boolean validateSignatureByParams(HttpServletRequest request, HttpServletResponse response,
											  Signature annotation) throws MissingServletRequestParameterException {
		String appId = request.getParameter(configuration.getAppIdParamName());
		if (StringUtils.isBlank(appId)) {
			throw new MissingServletRequestParameterException(configuration.getAppIdParamName(), "string");
		}
		if (ArrayUtils.isNotEmpty(annotation.appId()) && !ArrayUtils.contains(annotation.appId(), appId)) {
			HttpResponseBuilder.from(response).buffer(false).writeHttpException(new ValidationException("不是指定的appId"));
			return false;
		}

		String signature = request.getParameter(configuration.getSignatureParamName());
		if (StringUtils.isBlank(signature)) {
			throw new MissingServletRequestParameterException(configuration.getSignatureParamName(), "string");
		}

		String secretKey = secretKeyStorer.loadSecretKey(appId);
		if (StringUtils.isBlank(secretKey)) {
			HttpResponseBuilder.from(response).buffer(false).writeHttpException(new ValidationException("应用标识符不存在"));
			return false;
		}

		String requestUrl = getRequestUrl(request);
		String signStr = StringUtils.joinWith("&", appId, secretKey, requestUrl);
		String expectSignature = computeSignature(signStr, annotation.algorithm());

		if (!StringUtils.equals(expectSignature, signature)) {
			HttpResponseBuilder.from(response).buffer(false).writeHttpException(new ValidationException("签名错误"));
			return false;
		}
		return true;
	}

	/**
	 * 根据请求头校验签名。
	 * <p>
	 * 从请求头中提取签名信息，并与计算出的签名值比较。
	 * </p>
	 *
	 * @param request HTTP 请求。
	 * @param response HTTP 响应。
	 * @param annotation 签名注解实例。
	 * @return 如果签名校验通过，返回 {@code true}，否则返回 {@code false}。
	 * @throws MissingRequestValueException 当缺少必要的请求头时抛出。
	 * @since 1.0.0
	 */
	private boolean validateSignatureByHeaders(HttpServletRequest request, HttpServletResponse response,
											   Signature annotation) throws MissingRequestValueException {
		try {
			String appId = request.getHeader(configuration.getAppIdHeaderName());
			if (StringUtils.isBlank(appId)) {
				throw new MissingRequestValueException("缺少请求头：" + configuration.getAppIdHeaderName());
			}
			if (ArrayUtils.isNotEmpty(annotation.appId()) && !ArrayUtils.contains(annotation.appId(), appId)) {
				HttpResponseBuilder.from(response).buffer(false).writeHttpException(new ValidationException("不是指定的appId"));
				return false;
			}

			String signature = request.getHeader(configuration.getSignatureParamName());
			if (StringUtils.isBlank(signature)) {
				throw new MissingRequestValueException("缺少请求头：" + configuration.getSignatureParamName());
			}

			String timestamp = request.getHeader(configuration.getTimestampHeaderName());
			if (StringUtils.isBlank(timestamp)) {
				throw new MissingRequestValueException("缺少请求头：" + configuration.getTimestampHeaderName());
			}
			Long requestTimestamp = Long.parseLong(timestamp);
			Long nowTimestamp = DateUtils.nowDate().getTime();
			if (nowTimestamp - requestTimestamp > annotation.timeUnit().toMillis(annotation.timeout())) {
				HttpResponseBuilder.from(response).buffer(false).writeHttpException(new ValidationException("签名已过期"));
				return false;
			}

			String secretKey = secretKeyStorer.loadSecretKey(appId);
			if (StringUtils.isBlank(secretKey)) {
				HttpResponseBuilder.from(response).buffer(false).writeHttpException(new ValidationException("应用标识符不存在"));
				return false;
			}

			String requestUrl = URLEncoder.encode(request.getRequestURL().toString(), StandardCharsets.UTF_8);
			String signStr = StringUtils.joinWith("&", appId, secretKey, requestUrl, timestamp);
			String expectSignature = computeSignature(signStr, annotation.algorithm());

			if (!StringUtils.equals(expectSignature, signature)) {
				HttpResponseBuilder.from(response).buffer(false).writeHttpException(new ValidationException("签名错误"));
				return false;
			}
			return true;
		} catch (NumberFormatException e) {
			HttpResponseBuilder.from(response).buffer(false).writeHttpException(new ValidationException("无效的时间戳"));
			return false;
		}
	}

	/**
	 * 获取请求的完整 URL（不包含签名相关参数）。
	 * <p>
	 * 该方法从 HTTP 请求中提取请求的完整 URL（包括路径和查询参数），
	 * 并移除签名相关的参数（如签名值和应用 ID），以便在签名校验时构造签名字符串进行对比。
	 * 最终的 URL 会进行 URL 编码，保证其在签名计算中的合法性。
	 * </p>
	 *
	 * <p>主要功能：</p>
	 * <ul>
	 *     <li>移除签名参数（如 {@code AppId} 和 {@code Signature} 参数）。</li>
	 *     <li>对剩余的 URL 包含的查询参数进行重新拼接。</li>
	 *     <li>对最终生成的 URL 进行 UTF-8 编码。</li>
	 * </ul>
	 *
	 * @param request 当前的 HTTP 请求对象。
	 * @return 处理后的完整 URL 字符串，已经移除了签名相关参数并经过 URL 编码。
	 * @since 1.0.0
	 */
	private String getRequestUrl(HttpServletRequest request) {
		String requestUrl = request.getRequestURL().toString();
		String queryString = request.getQueryString();
		List<String> queryParams = new ArrayList<>();
		for (String queryParam : queryString.split("&")) {
			if (!StringUtils.startsWithAny(queryParam, configuration.getSignatureParamName(), configuration.getAppIdParamName())) {
				queryParams.add(queryParam);
			}
		}
		if (queryParams.isEmpty()) {
			return URLEncoder.encode(requestUrl, StandardCharsets.UTF_8);
		}
		return URLEncoder.encode(requestUrl + "?" + StringUtils.join(queryParams, "&"), StandardCharsets.UTF_8);
	}

	/**
	 * 计算签名值。
	 * <p>
	 * 根据传入的字符串和签名算法，计算签名值。
	 * </p>
	 *
	 * @param str 签名前的原始字符串。
	 * @param algorithm 签名算法。
	 * @return 计算出的签名值。
	 * @since 1.0.0
	 */
	private String computeSignature(String str, SignatureAlgorithm algorithm) {
		return switch (algorithm) {
			case MD5 -> DigestUtils.md5Hex(str);
			case SHA1 -> DigestUtils.sha1Hex(str);
			case SHA256 -> DigestUtils.sha256Hex(str);
			case SHA512 -> DigestUtils.sha512Hex(str);
		};
	}
}
