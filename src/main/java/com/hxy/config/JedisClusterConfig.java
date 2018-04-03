package com.hxy.config;

import com.google.common.collect.Lists;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class JedisClusterConfig {

    private String password;

    private int timeout;

    @Autowired
    private RedisClusterProperties redisClusterProperties;


   /* @Bean
    public RedisConnectionFactory connectionFactory() {
        String[] serverArray = redisClusterProperties.getNodes().split(",");
        return new JedisConnectionFactory(new RedisClusterConfiguration(Arrays.asList(serverArray)));
    }*/



    /**
     * 注意：
     * 这里返回的JedisCluster是单例的，并且可以直接注入到其他类中去使用
     * @return
     */
    @Bean
    public JedisCluster getJedisCluster() {
        String[] serverArray = redisClusterProperties.getNodes().split(",");//获取服务器数组(这里要相信自己的输入，所以没有考虑空指针问题)
        Set<HostAndPort> nodes = new HashSet<>();
 
        for (String ipPort : serverArray) {
            String[] ipPortPair = ipPort.split(":");
            nodes.add(new HostAndPort(ipPortPair[0].trim(), Integer.valueOf(ipPortPair[1].trim())));
        }
        return new JedisCluster(nodes,timeout,1000,1,password ,new GenericObjectPoolConfig());//需要密码连接的创建对象方式
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public RedisClusterProperties getRedisClusterProperties() {
        return redisClusterProperties;
    }

    public void setRedisClusterProperties(RedisClusterProperties redisClusterProperties) {
        this.redisClusterProperties = redisClusterProperties;
    }
}