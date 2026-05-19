package com.example.shorturl.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogCode {
    C001("C001", "create_short_url request={}", LogLevel.INFO),
    C002("C002", "get_short_url shortKey={}", LogLevel.INFO),
    S001("S001", "create_short_url originalUrl={}", LogLevel.INFO),
    S002("S002", "create_short_url success shortKey={}", LogLevel.INFO),
    S003("S003", "redirect shortKey={}", LogLevel.INFO),
    S004("S004", "redirect success originalUrl={}", LogLevel.DEBUG),
    S005("S005", "increment_click_count_failed shortKey={}", LogLevel.ERROR),
    E001("E001", "not_found error={}", LogLevel.WARN),
    E002("E002", "validation_error message={}", LogLevel.WARN),
    E003("E003", "internal_error", LogLevel.ERROR),
    T001("T001", "cache_hit key={}", LogLevel.DEBUG),
    T002("T002", "cache_miss key={}", LogLevel.DEBUG),
    T003("T003", "cache_put key={}", LogLevel.DEBUG),
    T004("T004", "cache_error key={}", LogLevel.ERROR),
    T005("T005", "cache_evict key={}", LogLevel.DEBUG),
    T006("T006", "cache_evict_error key={}", LogLevel.ERROR),
    G001("G001", "id_generator_init", LogLevel.DEBUG),
    G002("G002", "id_generator_next value={}", LogLevel.DEBUG);

    private final String code;
    private final String template;
    private final LogLevel level;
}