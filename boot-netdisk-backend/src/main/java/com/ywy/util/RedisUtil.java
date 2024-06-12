package com.ywy.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtil<V> {
    @Resource
    private RedisTemplate<String, V> redisTemplate;

    /**
     * 根据键获取值
     * @param key
     * @return
     */
    public V get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 存入值
     * @param key
     * @param value
     * @return
     */
    public boolean set(String key, V value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Setting redis key: {}, value: {} failed", key, value);
            return false;
        }
    }

    /**
     * 存入值并设置过期时间
     * @param key
     * @param value
     * @param time
     * @return
     */
    public boolean setex(String key, V value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("Setting redis key: {}, value: {} failed", key, value);
            return false;
        }
    }

    /**
     * 删除值
     * @param keys
     */
    public void delete(String... keys) {
        if (keys != null && keys.length > 0) {
            if (keys.length == 1) {
                redisTemplate.delete(keys[0]);
            } else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(keys));
            }
        }
    }
}
