package com.gitdatsanvich.sweethome.util;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.Collections;

@Component
public class RedisLockUtil {
    private static final Long RELEASE_SUCCESS = 1L;
    //加锁成功
    private static final String LOCK_SUCCESS = "OK";
    //若不存在
    private static final String SET_IF_NOT_EXIST = "NX";
    // 过期标志
    private static final String SET_WITH_EXPIRE_TIME = "EX";
    //需要执行的lua脚本
    private static final String RELEASE_LOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 该加锁方法仅针对单实例 Redis 可实现分布式加锁
     * 对于 Redis 集群则无法使用
     * <p>
     * 支持重复，线程安全
     *
     * @param lockKey  加锁键
     * @param clientId 加锁客户端唯一标识(采用UUID)
     * @param seconds  锁过期时间
     * @return flag
     */
    public Boolean tryLock(String lockKey, String clientId, long seconds) {
        return stringRedisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            Jedis jedis = (Jedis) redisConnection.getNativeConnection();
            String result = jedis.set(lockKey, clientId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, seconds);
            if (LOCK_SUCCESS.equals(result)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        });
    }

    /**
     * 与 tryLock 相对应，用作释放锁
     *
     * @param lockKey  lockKey
     * @param clientId clientId
     * @return flag
     */
    public Boolean releaseLock(String lockKey, String clientId) {
        return stringRedisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            Jedis jedis = (Jedis) redisConnection.getNativeConnection();
            Object result = jedis.eval(RELEASE_LOCK_SCRIPT, Collections.singletonList(lockKey),
                    Collections.singletonList(clientId));
            if (RELEASE_SUCCESS.equals(result)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        });
    }
}
