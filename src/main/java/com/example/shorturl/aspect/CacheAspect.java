package com.example.shorturl.aspect;

import com.example.shorturl.constant.LogCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 缓存切面
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheAspect {

    private final StringRedisTemplate redisTemplate;

    @Value("${shorturl.cache.prefix:short:url:}")
    private String cachePrefix;

    @Value("${shorturl.cache.default-expire-days:7}")
    private long defaultExpireDays;

    /**
     * @Cacheable 处理
     */
    @Around("@annotation(com.example.shorturl.aspect.Cacheable)")
    public Object handleCacheable(ProceedingJoinPoint joinPoint) throws Throwable {
        Cacheable annotation = getAnnotation(joinPoint, Cacheable.class);
        String cacheKey = buildCacheKey(joinPoint, annotation.key(), annotation.keyIncludeParam());

        Cacheable.CacheOp op = annotation.op();
        long expireSeconds = annotation.expireSeconds() > 0
                ? annotation.expireSeconds()
                : defaultExpireDays * 24 * 60 * 60;

        try {
            switch (op) {
                case GET:
                    // 只从缓存读取
                    Object cached = redisTemplate.opsForValue().get(cacheKey);
                    log.debug(LogCode.T001.getTemplate(), cacheKey);
                    if (cached != null) {
                        return cached;
                    }
                    log.debug(LogCode.T002.getTemplate(), cacheKey);
                    return null;

                case PUT:
                    // 只写入缓存
                    Object result = joinPoint.proceed();
                    if (result != null) {
                        redisTemplate.opsForValue().set(cacheKey, String.valueOf(result), expireSeconds, TimeUnit.SECONDS);
                        log.debug(LogCode.T003.getTemplate(), cacheKey);
                    }
                    return result;

                case GET_PUT:
                default:
                    // 先读缓存，miss 时执行方法并写入缓存
                    Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
                    if (cachedValue != null) {
                        log.debug(LogCode.T001.getTemplate(), cacheKey);
                        return cachedValue;
                    }

                    log.debug(LogCode.T002.getTemplate(), cacheKey);

                    // 执行方法
                    Object proceedResult = joinPoint.proceed();

                    // 写入缓存
                    if (proceedResult != null) {
                        String valueToCache = convertToString(proceedResult);
                        redisTemplate.opsForValue().set(cacheKey, valueToCache, expireSeconds, TimeUnit.SECONDS);
                        log.debug(LogCode.T003.getTemplate(), cacheKey);
                    }
                    return proceedResult;
            }
        } catch (Exception e) {
            log.error(LogCode.T004.getTemplate(), cacheKey, e);
            // 缓存失败时执行原方法
            return joinPoint.proceed();
        }
    }

    /**
     * @CacheEvict 处理
     */
    @Around("@annotation(com.example.shorturl.aspect.CacheEvict)")
    public Object handleCacheEvict(ProceedingJoinPoint joinPoint) throws Throwable {
        CacheEvict annotation = getAnnotation(joinPoint, CacheEvict.class);
        String cacheKey = buildCacheKey(joinPoint, annotation.key(), annotation.keyIncludeParam());

        boolean afterInvocation = annotation.afterInvocation();

        try {
            if (!afterInvocation) {
                // 方法前淘汰
                redisTemplate.delete(cacheKey);
                log.debug(LogCode.T005.getTemplate(), cacheKey);
            }

            // 执行方法
            Object result = joinPoint.proceed();

            if (afterInvocation) {
                // 方法后淘汰
                redisTemplate.delete(cacheKey);
                log.debug(LogCode.T005.getTemplate(), cacheKey);
            }

            return result;
        } catch (Exception e) {
            log.error(LogCode.T006.getTemplate(), cacheKey, e);
            throw e;
        }
    }

    /**
     * 构建缓存 key
     */
    private String buildCacheKey(ProceedingJoinPoint joinPoint, String keyTemplate, boolean includeParam) {
        String key = cachePrefix + keyTemplate;

        if (includeParam && joinPoint.getArgs().length > 0) {
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                key = key.replace("{" + i + "}", String.valueOf(args[i]));
            }
        }

        return key;
    }

    /**
     * 获取方法注解
     */
    private <T extends Annotation> T getAnnotation(ProceedingJoinPoint joinPoint, Class<T> annotationClass) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(annotationClass);
    }

    /**
     * 对象转字符串
     */
    private String convertToString(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }
}