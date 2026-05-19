package com.example.shorturl.aspect;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cacheable {

    String key();

    long expireSeconds() default 7 * 24 * 60 * 60;

    boolean keyIncludeParam() default true;

    enum CacheOp {
        GET,
        PUT,
        GET_PUT
    }

    CacheOp op() default CacheOp.GET_PUT;
}