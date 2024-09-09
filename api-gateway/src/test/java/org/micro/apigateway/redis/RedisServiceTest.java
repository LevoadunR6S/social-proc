package org.micro.apigateway.redis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private AutoCloseable autoCloseable;

    @InjectMocks
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void getToken() {
        String username = "bob";
        String refreshToken = "someRefreshToken";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(username)).thenReturn(refreshToken);

        String result = redisService.getToken(username);

        assertEquals(refreshToken, result);
        verify(valueOperations, times(1)).get(username);
    }

}