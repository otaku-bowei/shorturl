package com.example.shorturl.service;

import com.example.shorturl.aspect.Cacheable;
import com.example.shorturl.aspect.CacheEvict;
import com.example.shorturl.constant.GlobalConstants;
import com.example.shorturl.constant.LogCode;
import com.example.shorturl.constant.LogUtils;
import com.example.shorturl.dto.CreateShortUrlRequest;
import com.example.shorturl.dto.ShortUrlResponse;
import com.example.shorturl.entity.ShortUrl;
import com.example.shorturl.exception.ShortUrlNotFoundException;
import com.example.shorturl.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 短链服务
 */
@Service
@RequiredArgsConstructor
public class ShortUrlService {

    private final ShortUrlRepository repository;
    private final StringRedisTemplate redisTemplate;

    @Value("${shorturl.domain}")
    private String domain;

    /**
     * Base62 字符集
     */
    private static final String BASE62 = GlobalConstants.BASE62_CHAR_SET;

    /**
     * 生成短链
     */
    @Transactional
    public ShortUrlResponse createShortUrl(CreateShortUrlRequest request) {
        LogUtils.log(LogCode.S001, request.getOriginalUrl());

        // 1. 生成短链 ID
        Long id = generateId();

        // 2. 转换为 Base62
        String shortKey = encode62(id);

        // 3. 保存到数据库
        LocalDateTime expireTime = LocalDateTime.now()
                .plusDays(request.getExpireDays() != null ? request.getExpireDays() : GlobalConstants.DEFAULT_EXPIRE_DAYS);

        ShortUrl shortUrl = ShortUrl.builder()
                .shortKey(shortKey)
                .originalUrl(request.getOriginalUrl())
                .expireTime(expireTime)
                .clickCount(GlobalConstants.CLICK_COUNT_INIT_VALUE)
                .build();

        ShortUrl saved = repository.save(shortUrl);

        LogUtils.log(LogCode.S002, shortKey);

        return toResponse(saved);
    }

    /**
     * 跳转（根据 shortKey 获取原始 URL）
     * 使用缓存切面：GET_PUT 先读缓存，miss 时查库并写入缓存
     */
    @Cacheable(key = "{0}", expireSeconds = GlobalConstants.CACHE_DEFAULT_EXPIRE_SECONDS, op = Cacheable.CacheOp.GET_PUT)
    public String redirect(String shortKey) {
        LogUtils.log(LogCode.S003, shortKey);

        // 从数据库获取（在切面中处理缓存 miss 时的逻辑）
        ShortUrl shortUrl = repository.findByShortKey(shortKey)
                .orElseThrow(() -> new ShortUrlNotFoundException("短链不存在: " + shortKey));

        // 检查是否过期
        if (shortUrl.getExpireTime() != null && shortUrl.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new ShortUrlNotFoundException("短链已过期");
        }

        // 增加点击数
        incrementClickCount(shortKey);

        return shortUrl.getOriginalUrl();
    }

    /**
     * 获取短链信息
     */
    public ShortUrlResponse getByKey(String shortKey) {
        ShortUrl shortUrl = repository.findByShortKey(shortKey)
                .orElseThrow(() -> new ShortUrlNotFoundException("短链不存在: " + shortKey));
        return toResponse(shortUrl);
    }

    /**
     * 生成唯一 ID（从 Redis 自增）
     */
    private Long generateId() {
        Long id = redisTemplate.opsForValue().increment(GlobalConstants.REDIS_ID_GENERATOR_KEY);
        if (id == null || id < GlobalConstants.ID_GENERATOR_INIT_VALUE) {
            redisTemplate.opsForValue().set(GlobalConstants.REDIS_ID_GENERATOR_KEY, String.valueOf(GlobalConstants.ID_GENERATOR_INIT_VALUE));
            id = GlobalConstants.ID_GENERATOR_INIT_VALUE;
        }
        return id;
    }

    /**
     * 数字转 Base62
     */
    private String encode62(long num) {
        if (num == GlobalConstants.BASE62_INIT_INDEX) {
            return String.valueOf(BASE62.charAt((int) GlobalConstants.BASE62_INIT_INDEX));
        }

        StringBuilder sb = new StringBuilder();
        int base = BASE62.length();
        while (num > 0) {
            int rem = (int) (num % base);
            sb.append(BASE62.charAt(rem));
            num = num / base;
        }
        return sb.reverse().toString();
    }

    /**
     * 增加点击数
     */
    private void incrementClickCount(String shortKey) {
        new Thread(() -> {
            try {
                repository.findByShortKey(shortKey).ifPresent(url -> {
                    url.setClickCount(url.getClickCount() + 1);
                    repository.save(url);
                });
            } catch (Exception e) {
                LogUtils.log(LogCode.S005, e, shortKey);
            }
        }).start();
    }

    /**
     * 转换为响应对象
     */
    private ShortUrlResponse toResponse(ShortUrl shortUrl) {
        return ShortUrlResponse.builder()
                .id(shortUrl.getId())
                .shortKey(shortUrl.getShortKey())
                .shortUrl(domain + "/" + shortUrl.getShortKey())
                .originalUrl(shortUrl.getOriginalUrl())
                .expireTime(shortUrl.getExpireTime())
                .clickCount(shortUrl.getClickCount())
                .createdAt(shortUrl.getCreatedAt())
                .build();
    }
}