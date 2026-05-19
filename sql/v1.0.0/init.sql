-- 短链服务建表脚本
CREATE DATABASE IF NOT EXISTS shorturl DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE shorturl;

CREATE TABLE IF NOT EXISTS t_short_url (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_key VARCHAR(16) NOT NULL UNIQUE,
    original_url TEXT NOT NULL,
    expire_time DATETIME,
    click_count BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_short_key (short_key),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短链表';