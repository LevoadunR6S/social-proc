package org.micro.social.eurekasecurity.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

// Клас для взаємодії з базою даних Redis
@Service
public class RedisService {

    @Value("${jwt-expiration-milliseconds}")
    Long lifetime;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //Збереження RefreshToken в базі даних Redis
    public void saveRefreshToken(String username, String token) {
        redisTemplate.opsForValue().set(username, token, lifetime*5, TimeUnit.MILLISECONDS);
    }
    public void deleteToken(String username) {
        redisTemplate.delete(username);
    }

}

