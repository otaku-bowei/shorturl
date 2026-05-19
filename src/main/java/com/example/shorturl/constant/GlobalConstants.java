package com.example.shorturl.constant;

public final class GlobalConstants {

    private GlobalConstants() {
    }

    // Business
    public static final String BASE62_CHAR_SET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final long ID_GENERATOR_INIT_VALUE = 100000L;
    public static final long ID_GENERATOR_MIN_VALUE = 100000L;
    public static final int DEFAULT_EXPIRE_DAYS = 7;
    public static final int SHORT_KEY_MAX_LENGTH = 16;
    public static final long CLICK_COUNT_INIT_VALUE = 0L;
    public static final long BASE62_INIT_INDEX = 0L;

    // Redis
    public static final String CACHE_KEY_PREFIX = "short:url:";
    public static final String REDIS_ID_GENERATOR_KEY = "shorturl:id:generator";
    public static final long CACHE_DEFAULT_EXPIRE_SECONDS = 7 * 24 * 60 * 60;

    // HTTP
    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_FOUND = 302;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_INTERNAL_SERVER_ERROR = 500;

    // DB
    public static final String DB_TABLE_SHORT_URL = "t_short_url";

    // API
    public static final String API_BASE_PATH = "/api/shorturl";
}