package com.example.shorturl;

import com.example.shorturl.dto.CreateShortUrlRequest;
import com.example.shorturl.dto.ShortUrlResponse;
import com.example.shorturl.service.ShortUrlService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 高并发压测
 *
 * 测试场景：
 * 1. 短链生成：1000 并发同时生成
 * 2. 跳转测试：1000 并发同时访问同一短链
 * 3. 唯一性测试：10000 条短链无冲突
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
@DisplayName("高并发压测")
class ConcurrencyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShortUrlService shortUrlService;

    private static final String BASE_URL = "http://localhost:";
    private static final int THREAD_COUNT = 100;      // 线程数
    private static final int REQUESTS_PER_THREAD = 100; // 每线程请求数

    @Test
    @DisplayName("1. 短链生成 - 10000 次并发")
    void testConcurrentCreate() throws InterruptedException {
        log.info("========== 开始短链生成压测 ==========");

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT * REQUESTS_PER_THREAD);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        ConcurrentHashMap<String, String> results = new ConcurrentHashMap<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT * REQUESTS_PER_THREAD; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    String url = "https://example.com/test/" + index;
                    CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                            .originalUrl(url)
                            .build();

                    ResponseEntity<ShortUrlResponse> response = restTemplate.postForEntity(
                            BASE_URL + port + "/api/shorturl",
                            request,
                            ShortUrlResponse.class
                    );

                    if (response.getStatusCode() == HttpStatus.CREATED) {
                        successCount.incrementAndGet();
                        results.put(response.getBody().getShortKey(), url);
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    log.error("请求失败: {}", e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double qps = (successCount.get() * 1000.0) / duration;

        log.info("========== 短链生成压测结果 ==========");
        log.info("总请求数: {}", THREAD_COUNT * REQUESTS_PER_THREAD);
        log.info("成功: {}", successCount.get());
        log.info("失败: {}", failCount.get());
        log.info("耗时: {} ms", duration);
        log.info("QPS: {:.2f}/s", qps);

        // 验证唯一性
        assertThat(results.size()).isEqualTo(successCount.get());
        log.info("唯一性验证: 通过 (无重复短链)");
    }

    @Test
    @DisplayName("2. 跳转测试 - 1000 并发访问")
    void testConcurrentRedirect() throws InterruptedException {
        log.info("========== 开始跳转压测 ==========");

        // 先创建一个短链
        CreateShortUrlRequest createRequest = CreateShortUrlRequest.builder()
                .originalUrl("https://example.com/redirect-test")
                .build();
        ShortUrlResponse created = shortUrlService.createShortUrl(createRequest);
        String shortKey = created.getShortKey();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT * REQUESTS_PER_THREAD);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT * REQUESTS_PER_THREAD; i++) {
            executor.submit(() -> {
                try {
                    ResponseEntity<Void> response = restTemplate.getForEntity(
                            BASE_URL + port + "/api/shorturl/redirect/" + shortKey,
                            Void.class
                    );

                    if (response.getStatusCode() == HttpStatus.FOUND) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double qps = ((THREAD_COUNT * REQUESTS_PER_THREAD) * 1000.0) / duration;

        log.info("========== 跳转压测结果 ==========");
        log.info("总请求数: {}", THREAD_COUNT * REQUESTS_PER_THREAD);
        log.info("成功: {}", successCount.get());
        log.info("失败: {}", failCount.get());
        log.info("耗时: {} ms", duration);
        log.info("QPS: {:.2f}/s", qps);

        assertThat(successCount.get()).isGreaterThan(0);
    }

    @Test
    @DisplayName("3. 唯一性测试 - 10000 条短链无冲突")
    void testUniqueness() throws InterruptedException {
        log.info("========== 开始唯一性测试 ==========");

        int totalRequests = 10000;
        ExecutorService executor = Executors.newFixedThreadPool(200);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        ConcurrentHashMap<String, String> shortKeys = new ConcurrentHashMap<>();
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    String url = "https://example.com/unique/" + index;
                    CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                            .originalUrl(url)
                            .build();

                    ShortUrlResponse response = shortUrlService.createShortUrl(request);
                    shortKeys.put(response.getShortKey(), url);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("创建失败: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        log.info("========== 唯一性测试结果 ==========");
        log.info("生成数量: {}", successCount.get());
        log.info("唯一 Key 数量: {}", shortKeys.size());

        // 验证无重复
        assertThat(shortKeys.size()).isEqualTo(successCount.get());
        log.info("唯一性验证: 通过 ✓");
    }

    @Test
    @DisplayName("4. 模拟 10000 TPS 生成")
    void testHighTPS() throws InterruptedException {
        log.info("========== 开始 10000 TPS 压测 ==========");

        int targetTPS = 10000;
        int testDurationSeconds = 3;
        int totalRequests = targetTPS * testDurationSeconds;

        ExecutorService executor = Executors.newFixedThreadPool(500);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalRequests; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    String url = "https://example.com/tps-test/" + index + "/" + System.nanoTime();
                    CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                            .originalUrl(url)
                            .build();

                    ResponseEntity<ShortUrlResponse> response = restTemplate.postForEntity(
                            BASE_URL + port + "/api/shorturl",
                            request,
                            ShortUrlResponse.class
                    );

                    if (response.getStatusCode() == HttpStatus.CREATED) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });

            // 控制 TPS
            if (i % targetTPS == 0 && i > 0) {
                Thread.sleep(1000);
            }
        }

        latch.await();
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double actualQPS = (successCount.get() * 1000.0) / duration;

        log.info("========== 10000 TPS 压测结果 ==========");
        log.info("目标 TPS: {}", targetTPS);
        log.info("测试时长: {} 秒", testDurationSeconds);
        log.info("总请求数: {}", totalRequests);
        log.info("成功: {}", successCount.get());
        log.info("失败: {}", failCount.get());
        log.info("实际耗时: {} ms", duration);
        log.info("实际 QPS: {:.2f}/s", actualQPS);
        log.info("成功率: {:.2f}%", (successCount.get() * 100.0) / totalRequests);

        // 验证
        assertThat(actualQPS).isGreaterThan(5000); // 至少达到 5000 TPS
    }
}
