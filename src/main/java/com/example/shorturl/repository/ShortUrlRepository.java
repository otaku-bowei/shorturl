package com.example.shorturl.repository;

import com.example.shorturl.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    /**
     * 根据 shortKey 查询
     */
    Optional<ShortUrl> findByShortKey(String shortKey);

    /**
     * 根据 shortKey 查询（带锁）
     */
    @Query(value = "SELECT * FROM t_short_url WHERE short_key = :shortKey FOR UPDATE", nativeQuery = true)
    Optional<ShortUrl> findByShortKeyForUpdate(@Param("shortKey") String shortKey);

    /**
     * 检查 shortKey 是否存在
     */
    boolean existsByShortKey(String shortKey);
}
