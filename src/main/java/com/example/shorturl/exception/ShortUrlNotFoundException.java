package com.example.shorturl.exception;

/**
 * 短链不存在异常
 */
public class ShortUrlNotFoundException extends RuntimeException {
    public ShortUrlNotFoundException(String message) {
        super(message);
    }
}
