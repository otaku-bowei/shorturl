package com.example.shorturl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 短链实体
 */
@Entity
@Table(name = "t_short_url", indexes = {
    @Index(name = "idx_short_key", columnList = "shortKey", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 短链 key
     */
    @Column(name = "short_key", nullable = false, unique = true, length = 16)
    private String shortKey;

    /**
     * 原始 URL
     */
    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    /**
     * 过期时间
     */
    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    /**
     * 点击次数
     */
    @Column(name = "click_count")
    @Builder.Default
    private Long clickCount = 0L;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
