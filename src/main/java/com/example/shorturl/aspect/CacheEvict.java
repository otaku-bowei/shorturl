package com.example.shorturl.aspect;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheEvict {

    String key();

    boolean keyIncludeParam() default true;

    boolean afterInvocation() default true;
}