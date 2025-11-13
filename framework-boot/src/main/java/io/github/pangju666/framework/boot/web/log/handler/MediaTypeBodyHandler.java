package io.github.pangju666.framework.boot.web.log.handler;

import org.springframework.http.MediaType;

/**
 * 媒体类型请求/响应体解析器接口。
 *
 * <p>用于将原始字节内容依据 {@link MediaType} 解码并转换为可记录的对象。</p>
 *
 * <p>实现约定：</p>
 * <ul>
 *   <li>实现应是无状态或线程安全的。</li>
 *   <li>不应持久化或修改输入数据，仅负责解码与转换。</li>
 *   <li>返回对象应可被序列化（例如 Map、List、String、基本类型等）。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public interface MediaTypeBodyHandler {
    /**
     * 是否支持指定媒体类型。
     *
     * <p>匹配规则由实现自行决定；当前内置实现采用严格等值匹配
     * （例如仅当 {@code mediaType} 与 {@link MediaType#APPLICATION_JSON} 或
     * {@link MediaType#TEXT_PLAIN} 完全相等时返回 {@code true}）。</p>
     *
     * @param mediaType 请求或响应的媒体类型（可能包含参数，如 charset）
     * @return 支持返回 {@code true}，否则返回 {@code false}
	 * @since 1.0.0
	 */
    boolean supports(MediaType mediaType);

    /**
     * 将原始字节内容按媒体类型解码并返回解析后的对象。
     *
     * <p>通常根据媒体类型的字符集参数进行解码，若未提供字符集应使用合理默认（如 UTF-8）。
     * 返回的对象类型由实现定义：JSON 可返回 {@code Map/List}，文本可返回 {@code String}。</p>
     *
     * @param rawBody   原始字节内容
     * @param mediaType 媒体类型（可用于获取字符集或子类型）
     * @return 解析后的对象；无法解析时可抛出异常或返回基础类型，由上层策略决定
	 * @since 1.0.0
	 */
    Object getBody(byte[] rawBody, MediaType mediaType);
}
