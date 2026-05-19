package com.example.shorturl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建短链请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShortUrlRequest {

    @NotBlank(message = "原始URL不能为空")
    @Size(max = 2048, message = "URL长度不能超过2048")
    private String originalUrl;

    /**
     * 过期天数，默认30天
     */
    @Builder.Default
    private Integer expireDays = 30;
}
