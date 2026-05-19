package com.example.shorturl;

import com.example.shorturl.dto.CreateShortUrlRequest;
import com.example.shorturl.service.ShortUrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class LoadTest {

    @Autowired
    private ShortUrlService shortUrlService;

    @Test
    void generate10wData() throws Exception {
        int total = 100000;
        int batchSize = 1000;
        int threads = 10;
        
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(total / batchSize);
        
        List<Future<?>> results = new ArrayList<>();
        for (int i = 0; i < total; i += batchSize) {
            final int start = i;
            results.add(executor.submit(() -> {
                for (int j = 0; j < batchSize; j++) {
                    try {
                        CreateShortUrlRequest req = new CreateShortUrlRequest();
                        req.setOriginalUrl("http://test.com/url/" + (start + j));
                        req.setExpireDays(30);
                        shortUrlService.createShortUrl(req);
                    } catch (Exception e) {
                        // ignore duplicates
                    }
                }
                latch.countDown();
            }));
        }
        
        latch.await(5, TimeUnit.MINUTES);
        executor.shutdown();
        System.out.println("Done: " + total + " records");
    }
}