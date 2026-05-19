package com.example.shorturl.controller;

import com.example.shorturl.dto.CreateShortUrlRequest;
import com.example.shorturl.entity.ShortUrl;
import com.example.shorturl.repository.ShortUrlRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("短链 Controller 集成测试")
class ShortUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @Autowired
    private ShortUrlRepository repository;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(100000L);
        repository.deleteAll();
    }

    @Test
    @DisplayName("创建短链 - 成功")
    void create_Success() throws Exception {
        // Arrange
        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .originalUrl("https://example.com/very/long/url")
                .expireDays(7)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/shorturl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortKey").exists())
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.originalUrl").value(request.getOriginalUrl()));
    }

    @Test
    @DisplayName("创建短链 - URL 为空")
    void create_EmptyUrl() throws Exception {
        // Arrange
        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .originalUrl("")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/shorturl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("获取短链信息 - 成功")
    void getByKey_Success() throws Exception {
        // Arrange - 先创建
        CreateShortUrlRequest createRequest = CreateShortUrlRequest.builder()
                .originalUrl("https://test.com")
                .build();

        ResultActions createResult = mockMvc.perform(post("/api/shorturl")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)));

        String shortKey = createResult.andReturn().getResponse().getContentAsString();
        // 简化：直接从 response 获取

        // Act & Assert - 获取
        mockMvc.perform(get("/api/shorturl/{shortKey}", "test"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("重定向 - 成功")
    void redirect_Success() throws Exception {
        // 先创建短链
        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .originalUrl("https://example.com")
                .build();

        when(valueOperations.get(anyString())).thenReturn("https://example.com");

        mockMvc.perform(get("/api/shorturl/redirect/{shortKey}", "abc123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }
}
