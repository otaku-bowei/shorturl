package com.example.shorturl;

import com.example.shorturl.constant.GlobalConstants;
import com.example.shorturl.dto.CreateShortUrlRequest;
import com.example.shorturl.service.ShortUrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class GenerateTestData {

    @Autowired
    private ShortUrlService shortUrlService;
    
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void generate10wData() {
        // 重置 ID 生成器
        redisTemplate.opsForValue().set(
            GlobalConstants.REDIS_ID_GENERATOR_KEY, 
            String.valueOf(GlobalConstants.ID_GENERATOR_INIT_VALUE)
        );
        redisTemplate.delete(GlobalConstants.CACHE_KEY_PREFIX + "*");
        
        System.out.println("Start generating 100000 records...");
        
        for (int i = 0; i < 100000; i++) {
            try {
                CreateShortUrlRequest req = new CreateShortUrlRequest();
                req.setOriginalUrl("http://test.com/url/" + (i + GlobalConstants.ID_GENERATOR_INIT_VALUE));
                req.setExpireDays(30);
                shortUrlService.createShortUrl(req);
                
                if ((i + 1) % 10000 == 0) {
                    System.out.println("Progress: " + (i + 1));
                }
            } catch (Exception e) {
                System.err.println("Error at " + i + ": " + e.getMessage());
            }
        }
        
        System.out.println("Done: 100000 records");
    }
}