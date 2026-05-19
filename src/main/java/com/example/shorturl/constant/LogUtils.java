package com.example.shorturl.constant;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public final class LogUtils {

    private LogUtils() {
    }

    public static void log(LogCode code, Object... args) {
        switch (code.getLevel()) {
            case DEBUG:
                log.debug(code.getTemplate(), args);
                break;
            case INFO:
                log.info(code.getTemplate(), args);
                break;
            case WARN:
                log.warn(code.getTemplate(), args);
                break;
            case ERROR:
                if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
                    Throwable ex = (Throwable) args[args.length - 1];
                    Object[] msgArgs = Arrays.copyOf(args, args.length - 1);
                    log.error(code.getTemplate(), msgArgs, ex);
                } else {
                    log.error(code.getTemplate(), args);
                }
                break;
        }
    }

    public static void debug(LogCode code, Object... args) {
        log.debug(code.getTemplate(), args);
    }

    public static void info(LogCode code, Object... args) {
        log.info(code.getTemplate(), args);
    }

    public static void warn(LogCode code, Object... args) {
        log.warn(code.getTemplate(), args);
    }

    public static void error(LogCode code, Object... args) {
        error(code, null, args);
    }

    public static void error(LogCode code, Throwable ex, Object... args) {
        if (ex != null) {
            log.error(code.getTemplate(), args, ex);
        } else {
            log.error(code.getTemplate(), args);
        }
    }
}