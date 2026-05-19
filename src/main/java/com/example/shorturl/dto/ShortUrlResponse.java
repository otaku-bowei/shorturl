package com.example.shorturl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 短链响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlResponse {

    private Long id;

    /**
     * 短链 key
     */
    private String shortKey;

    /**
     * 完整短链 URL
     */
    private String shortUrl;

    /**
     * 原始 URL
     */
    private String originalUrl;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 点击次数
     */
    private Long clickCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
