package com.example.shorturl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class TpsTest {

    private final String baseUrl = "http://localhost:8080";
    private final String shortKey = "q0U";

    /**
     * 测试 GET /{shortKey} 查询接口的真实并发 TPS
     */
    @Test
    void testQueryConcurrency() throws Exception {
        int total = 10000;
        int threads = 100; // 减少线程数，复用连接
        RestTemplate template = new RestTemplate();

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(total);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();

        long startTime = System.currentTimeMillis();

        // 100 个线程循环发送 10000 次请求
        for (int i = 0; i < total; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ResponseEntity<String> res = template.getForEntity(
                            baseUrl + "/api/shorturl/" + shortKey,
                            String.class
                    );
                    if (res.getStatusCode() == HttpStatus.OK) {
                        success.incrementAndGet();
                        if (success.get() <= 3) {
                            System.out.println("Result: " + res.getBody());
                        }
                    } else {
                        error.incrementAndGet();
                    }
                } catch (Exception e) {
                    error.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        long cost = System.currentTimeMillis() - startTime;
        double tps = success.get() * 1000.0 / cost;

        System.out.println("========== GET /{shortKey} 并发测试 ==========");
        System.out.println("总请求数: " + total);
        System.out.println("线程数: " + threads);
        System.out.println("成功: " + success.get());
        System.out.println("失败: " + error.get());
        System.out.println("耗时: " + cost + " ms");
        System.out.println("TPS: " + String.format("%.2f", tps));
        System.out.println("=========================================");
    }

    /**
     * 测试 GET /redirect/{shortKey} 跳转接口的并发 TPS
     */
    @Test
    void testRedirectConcurrency() throws Exception {
        int total = 10000;
        int threads = 100;
        RestTemplate template = new RestTemplate();

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(total);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < total; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ResponseEntity<String> res = template.getForEntity(
                            baseUrl + "/api/shorturl/redirect/" + shortKey,
                            String.class
                    );
                    if (res.getStatusCode() == HttpStatus.FOUND) {
                        success.incrementAndGet();
                        if (success.get() <= 3) {
                            System.out.println("Redirect Location: " + res.getHeaders().getFirst("Location"));
                        } else {
                            error.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    error.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        long cost = System.currentTimeMillis() - startTime;
        double tps = success.get() * 1000.0 / cost;

        System.out.println("========== GET /redirect 并发测试 ==========");
        System.out.println("总请求数: " + total);
        System.out.println("线程数: " + threads);
        System.out.println("成功: " + success.get());
        System.out.println("失败: " + error.get());
        System.out.println("耗时: " + cost + " ms");
        System.out.println("TPS: " + String.format("%.2f", tps));
        System.out.println("==========================================");
    }
}