package com.socion.backend.config;

import com.socion.backend.utils.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfiguration extends CachingConfigurerSupport {

    @Value("${spring.redis.host}")
    private String redisHostName;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.cache.redis.profile.ttl}")
    private Long redisProfileCacheTTl;

    @Value("${spring.cache.redis.keycloak.ttl}")
    private Long redisKeycloakCacheTTl;

    List<String> clusterNodes = Arrays.asList("172.31.43.149:6379", "172.31.32.254:6379", "172.31.38.184:6379","172.31.34.10:6379","172.31.33.173:6379","172.31.33.176:6379","172.31.35.156:6379"
    ,"172.31.34.172:6379","172.31.36.145:6379");

    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConf = new RedisStandaloneConfiguration(redisHostName, redisPort);

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisConf);
        jedisConnectionFactory.getPoolConfig().setMaxIdle(Constants.MAX_IDLE);
        jedisConnectionFactory.getPoolConfig().setMaxTotal(Constants.MAX_TOTAL);
        jedisConnectionFactory.getPoolConfig().setMinIdle(Constants.MIN_IDLE);
        jedisConnectionFactory.getPoolConfig().setTestOnBorrow(true);
        jedisConnectionFactory.getPoolConfig().setMinEvictableIdleTimeMillis(Duration.ofSeconds(Constants.SIXTY_SECONDS).toMillis());
        jedisConnectionFactory.getPoolConfig().setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(Constants.THIRTY_SECONDS).toMillis());
        jedisConnectionFactory.getPoolConfig().setBlockWhenExhausted(true);
        return jedisConnectionFactory;
    }

    @Bean
    public RedisCacheConfiguration redisUserProfileCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(redisProfileCacheTTl))
                .disableCachingNullValues();

    }

    @Bean
    public RedisCacheConfiguration redisKeyCloakCacheConfiguration() {
       return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(redisKeycloakCacheTTl))
                .disableCachingNullValues();

    }

    @Bean
    @Primary
    public RedisCacheManager redisUserCacheManager() {
        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(redisUserProfileCacheConfiguration())
                .transactionAware()
                .build();

    }

    @Bean
    public RedisCacheManager redisKeyCloakCacheManager() {
        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(redisKeyCloakCacheConfiguration())
                .transactionAware()
                .build();

    }
}