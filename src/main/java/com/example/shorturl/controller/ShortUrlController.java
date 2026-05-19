package com.example.shorturl.controller;

import com.example.shorturl.dto.CreateShortUrlRequest;
import com.example.shorturl.dto.ShortUrlResponse;
import com.example.shorturl.constant.LogCode;
import com.example.shorturl.constant.LogUtils;
import com.example.shorturl.service.ShortUrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 短链 Controller
 */
@RestController
@RequestMapping("/api/shorturl")
@RequiredArgsConstructor
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    /**
     * 创建短链
     */
    @PostMapping
    public ResponseEntity<ShortUrlResponse> create(@Valid @RequestBody CreateShortUrlRequest request) {
        LogUtils.log(LogCode.C001, request);
        ShortUrlResponse response = shortUrlService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 获取短链信息
     */
    @GetMapping("/{shortKey}")
    public ResponseEntity<ShortUrlResponse> get(@PathVariable String shortKey) {
        LogUtils.log(LogCode.C002, shortKey);
        ShortUrlResponse response = shortUrlService.getByKey(shortKey);
        return ResponseEntity.ok(response);
    }

    /**
     * 跳转（重定向）
     */
    @GetMapping("/redirect/{shortKey}")
    public ResponseEntity<Void> redirect(@PathVariable String shortKey) {
        String originalUrl = shortUrlService.redirect(shortKey);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", originalUrl)
                .build();
    }
}