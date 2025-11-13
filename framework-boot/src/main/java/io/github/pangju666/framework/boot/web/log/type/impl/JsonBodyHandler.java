package io.github.pangju666.framework.boot.web.log.type.impl;

import io.github.pangju666.commons.lang.pool.Constants;
import io.github.pangju666.commons.lang.utils.JsonUtils;
import io.github.pangju666.framework.boot.web.log.type.MediaTypeBodyHandler;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

/**
 * JSON 媒体类型解析器。
 *
 * <p>职责：当媒体类型的类型与子类型为 {@link MediaType#APPLICATION_JSON} 时，
 * 将字节内容按媒体类型字符集解码为字符串并解析为 Java 对象。</p>
 *
 * <p>行为与约束：</p>
 * <ul>
 *   <li>当媒体类型的类型与子类型为 {@code application/json} 时返回支持；
 *   媒体类型参数（如 {@code charset}）不影响支持判断（基于类型与子类型匹配）。</li>
 *   <li>解码字符集优先使用 {@link MediaType#getCharset()}，否则回退为 UTF-8。</li>
 *   <li>空或空白内容将以空对象字符串（{@link Constants#EMPTY_JSON_OBJECT_STR}）解析，确保返回非空对象。</li>
 *   <li>返回类型为通用 {@link Object}，通常为 {@code Map} 或 {@code List}。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class JsonBodyHandler implements MediaTypeBodyHandler {
	/**
	 * 是否支持指定媒体类型。
	 *
	 * <p>按类型与子类型判断，仅当类型与子类型为
	 * {@link MediaType#APPLICATION_JSON} 时返回 {@code true}；
	 * 媒体类型参数（如 {@code charset}）将被忽略。</p>
	 *
	 * @param mediaType 媒体类型
	 * @return 是否支持该媒体类型
	 */
	@Override
    public boolean supports(MediaType mediaType) {
        return MediaType.APPLICATION_JSON.equalsTypeAndSubtype(mediaType);
    }

	/**
	 * 将原始字节解码为 JSON 字符串并解析为 Java 对象。
	 *
	 * @param rawBody   原始字节内容
	 * @param mediaType 媒体类型（用于获取字符集）
	 * @return 解析后的对象；空内容将按空 JSON 对象处理
	 */
    @Override
    public Object getBody(byte[] rawBody, MediaType mediaType) {
        String jsonStr = new String(rawBody, ObjectUtils.getIfNull(mediaType.getCharset(),
            StandardCharsets.UTF_8));
        return JsonUtils.fromString(StringUtils.defaultIfBlank(jsonStr, Constants.EMPTY_JSON_OBJECT_STR),
            Object.class);
    }
}
