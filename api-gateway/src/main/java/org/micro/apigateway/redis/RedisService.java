package org.micro.apigateway.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

// Клас для взаємодії з базою даних Redis
@Service
public class RedisService {

    @Value("${jwt-expiration-milliseconds}")
    Long lifetime;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    //Отримання RefreshToken з бази Redis
    public String getToken(String username) {
        return (String) redisTemplate.opsForValue().get(username);
    }

    //Видалення RefreshToken з бази Redis
    public void deleteToken(String username) {
        redisTemplate.delete(username);
    }
}

