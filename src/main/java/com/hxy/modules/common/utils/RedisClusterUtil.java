package com.hxy.modules.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
 
import redis.clients.jedis.JedisCluster;
 
@Component
public class RedisClusterUtil {
    private static final Logger LOGGER    = LoggerFactory.getLogger(RedisClusterUtil.class);
 
    @Autowired
    private JedisCluster  jedisCluster;
 
    /**
     * 设置缓存
     * @param key    缓存key
     * @param value  缓存value
     */
    public void setString(String key, String value) {
        jedisCluster.set(key, value);
        LOGGER.debug("RedisClusterUtil:set cache key={},value={}", key, value);
    }
 
    /**
     * 设置缓存对象
     * @param key    缓存key
     * @param object  缓存value
     */
    public boolean setObject(String key, Object object , int expireTime) {
        String value = SerializeUtil.serialize(object);
        boolean result = false;
        try {
            //为-1时不设置超时时间
            if (expireTime != -1) {
                setString(key,value,expireTime);
            } else {
                setString(key,value);
            }
            result = false;
        } catch (Exception e) {
            throw e;
        }
        return  result;
    }

    /**
     * 设置缓存对象
     * @param key    缓存key
     * @param object  缓存value
     */
    public boolean setObject(String key, Object object ) {
        return setObject(key, object, -1);
    }

    /**
     * 获取指定key的缓存
     * @param key---JSON.parseObject(value, User.class);
     */
    public Object getObject(String key) {
        Object object = null;
        try {
            String serializeObj = getString(key);
            if (null == serializeObj || serializeObj.length() == 0) {
                object = null;
            } else {
                object = SerializeUtil.deserialize(serializeObj);
            }
        }  catch (Exception e) {
            throw e;
        }
        return object;
    }
 
    /**
     * 判断当前key值 是否存在
     *
     * @param key
     */
    public boolean hasKey(String key) {
        return jedisCluster.exists(key);
    }
 
 
    /**
     * 设置缓存，并且自己指定过期时间
     * @param key
     * @param value
     * @param expireTime 过期时间
     */
    public String setString( String key, String value, int expireTime) {
        String result = jedisCluster.setex(key, expireTime, value);
        LOGGER.debug("RedisClusterUtil:setWithExpireTime cache key={},value={},expireTime={}", key, value, expireTime);
        return result;
    }
 
 
    /**
     * 获取指定key的缓存
     * @param key
     */
    public String getString(String key) {
        String value = jedisCluster.get(key);
        LOGGER.debug("RedisClusterUtil:get cache key={},value={}",key, value);
        return value;
    }
 
    /**
     * 删除指定key的缓存
     * @param key
     */
    public void deleteString(String key) {
        jedisCluster.del(key);
        LOGGER.debug("RedisClusterUtil:delete cache key={}", key);
    }
 
}