package com.example.shorturl.exception;

import com.example.shorturl.constant.LogCode;
import com.example.shorturl.constant.LogUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ShortUrlNotFoundException ex) {
        LogUtils.log(LogCode.E001, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        LogUtils.log(LogCode.E002, message);
        return ResponseEntity.badRequest()
                .body(new ApiError(400, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        LogUtils.log(LogCode.E003, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(500, "Internal server error"));
    }

    public static class ApiError {
        private int code;
        private String message;
        private LocalDateTime timestamp;

        public ApiError(int code, String message) {
            this.code = code;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }
    }
}