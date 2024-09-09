package org.micro.social.eurekasecurity.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.catalina.User;
import org.micro.shareable.dto.UserDto;
import org.micro.shareable.model.Role;
import org.micro.social.eurekasecurity.kafka.KafkaUserClient;
import org.micro.social.eurekasecurity.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Set;


@Service
public class JwtUtils {
    private String secret;
    private Long lifetime;

    private Key key;


    @Autowired
    KafkaUserClient kafkaUserClient;

    @Autowired
    public JwtUtils(@Value("${jwt-secret-key}") String secret,
                    @Value("${jwt-expiration-milliseconds}") Long lifetime) {
        this.secret = secret;
        this.lifetime = lifetime;
        this.key = Keys.hmacShaKeyFor(secret.getBytes()); // Ініціалізація ключа для підписання токенів
    }


    // Генерація JWT токена
    public String generate(String username, Set<Role> roles, String tokenType) {
        Map<String, ?> claims = Map.of(
                "username", username,
                "roles", roles
        );
        // Визначення часу життя токена залежно від типу (ACCESS або REFRESH)
        long expMillis = "ACCESS".equalsIgnoreCase(tokenType) ? lifetime : lifetime * 5;

        final Date now = new Date();
        final Date exp = new Date(now.getTime() + expMillis);

        return Jwts.builder()
                .setClaims(claims) // Додавання даних до токена
                .setSubject(username) // Встановлення username як теми
                .setIssuedAt(now) // Встановлення дати видачі токена
                .setExpiration(exp) // Встановлення дати закінчення строку дії токена
                .signWith(key) // Підписання токена
                .compact(); // Формування токена у вигляді String
    }


    // Отримання всіх claims з токена
    public Claims getAllClaimsFromToken(String token) throws ExpiredJwtException {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }



}

