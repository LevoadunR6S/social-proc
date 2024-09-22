package org.micro.social.eurekasecurity.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;


class RedisServiceTest {

    @InjectMocks
    private RedisService redisService;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;


    @BeforeEach
    void setUp() {
       MockitoAnnotations.openMocks(this);
    }



    @Test
    void saveRefreshToken() {
        String username = "bob";
        String refreshToken = "someRefreshToken";

        redisService.lifetime = 1000L ;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisService.saveRefreshToken(username, refreshToken);

        verify(valueOperations, times(1)).set(username, refreshToken, 5000L, TimeUnit.MILLISECONDS);
    }

    @Test
    void deleteToken() {
        String username = "bob";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisService.deleteToken(username);

        verify(redisTemplate, times(1)).delete(username);
    }
}