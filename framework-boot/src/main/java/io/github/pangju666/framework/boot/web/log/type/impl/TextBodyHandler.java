package io.github.pangju666.framework.boot.web.log.type.impl;

import io.github.pangju666.framework.boot.web.log.type.MediaTypeBodyHandler;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

/**
 * 纯文本媒体类型解析器。
 *
 * <p>职责：当媒体类型的类型与子类型为 {@link MediaType#TEXT_PLAIN} 时，
 * 将原始字节按媒体类型字符集解码为 {@link String}。</p>
 *
 * <p>行为与约束：</p>
 * <ul>
 *   <li>当媒体类型的类型与子类型为 {@code text/plain} 时返回支持；
 *   媒体类型参数（如 {@code charset}）不影响支持判断（基于类型与子类型匹配）。</li>
 *   <li>解码字符集优先使用 {@link MediaType#getCharset()}，否则回退为 UTF-8。</li>
 *   <li>返回值为解码后的文本字符串。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class TextBodyHandler implements MediaTypeBodyHandler {
	/**
	 * 是否支持指定媒体类型。
	 *
	 * <p>按类型与子类型判断，仅当类型与子类型为
	 * {@link MediaType#TEXT_PLAIN} 时返回 {@code true}；
	 * 媒体类型参数（如 {@code charset}）将被忽略。</p>
	 *
	 * @param mediaType 媒体类型
	 * @return 是否支持该媒体类型
	 */
	@Override
    public boolean supports(MediaType mediaType) {
        return MediaType.TEXT_PLAIN.equalsTypeAndSubtype(mediaType);
    }

	/**
	 * 将原始字节解码为文本字符串。
	 *
	 * @param rawBody   原始字节内容
	 * @param mediaType 媒体类型（用于获取字符集）
	 * @return 解码后的文本内容
	 */
    @Override
    public Object getBody(byte[] rawBody, MediaType mediaType) {
        return new String(rawBody, ObjectUtils.getIfNull(mediaType.getCharset(),
            StandardCharsets.UTF_8));
    }
}
