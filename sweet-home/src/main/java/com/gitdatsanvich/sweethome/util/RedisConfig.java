package com.gitdatsanvich.sweethome.util;

import com.gitdatsanvich.common.constants.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author TangChen
 * @date 2021/6/28 12:09
 */
@Slf4j
@Configuration
@EnableCaching //启用缓存
public class RedisConfig {

    /**
     * 自定义缓存管理器
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        Set<String> cacheNames = new HashSet<>();
        cacheNames.add(CacheConstants.BLACK_IP);
        ConcurrentHashMap<String, RedisCacheConfiguration> configMap = new ConcurrentHashMap<>();
        configMap.put(CacheConstants.BLACK_IP, config.entryTtl(Duration.ofMinutes(120L)));
        //需要先初始化缓存名称，再初始化其它的配置
        return RedisCacheManager.builder(factory).initialCacheNames(cacheNames).withInitialCacheConfigurations(configMap).build();
    }
}