package org.micro.apigateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.micro.shareable.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

//Клас для роботи з JWT
@Component
public class JwtUtils {

    private String secret;

    private Long lifetime;

    // Ключ для підпису токенів
    private Key key;

    // Конструктор, який ініціалізує секретний ключ та час життя токена з application.properties
    @Autowired
    public JwtUtils(@Value("${jwt-secret-key}") String secret,
                    @Value("${jwt-expiration-milliseconds}") Long lifetime) {
        this.secret = secret;
        this.lifetime = lifetime;
        // Генерує ключ для підпису токенів на основі секретного ключа
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Метод для отримання всіх claims з JWT токена
    public Claims getAllClaimsFromToken(String token) throws ExpiredJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key) // Вказує ключ для перевірки підпису токена
                .build()
                .parseClaimsJws(token) // Парсить токен і перевіряє його дійсність
                .getBody(); // Повертає дані токена
    }

    // Метод для перевірки, чи строк дії токена закінчився
    public Boolean isTokenExpired(String token) {
        try {
            // Порівнює дату закінчення токена з поточною датою
            return getAllClaimsFromToken(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // Якщо токен прострочений, повертає true
        }
    }

    // Метод для оновлення access токена на основі refresh токена
    public String refreshAccessToken(String refreshToken) {
        try {
            // Отримує claims з refresh-токена
            Claims claims = getAllClaimsFromToken(refreshToken);
            // Отримує username з claims
            String username = claims.get("username", String.class);


            // Отримує список ролей з claims
            List<Map<String, Object>> rolesList = (List<Map<String, Object>>) claims.get("roles");
            // Перетворює кожну мапу у об'єкт Role
            Set<Role> roles = new HashSet<>();
            for (Map<String, Object> roleMap : rolesList) {
                Role name = new Role(roleMap.get("name").toString());
                roles.add(name);
            }


            // Генерує новий access-токен
            return generateAccessToken(username, roles);
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token", e);
        }
    }

    // Метод для генерації нового AccessToken на основі імені користувача та ролей
    public String generateAccessToken(String username, Set<Role> roles) {

        // Claims для токена: username та ролі
        Map<String, ?> claims = Map.of(
                "username", username,
                "roles", roles
        );

        // Розрахунок часу закінчення дії токена
        long expMillis = lifetime;
        final Date now = new Date();
        final Date exp = new Date(now.getTime() + expMillis);

        // Створення та підпис токена
        return Jwts.builder()
                .setClaims(claims) // Додає дані до токена
                .setSubject((String) claims.get(username))  // Встановлює тему
                .setIssuedAt(now) // Додає час створення токена
                .setExpiration(exp)  // Додає час закінчення дії токена
                .signWith(key) // Підписує токен за допомогою секретного ключа
                .compact(); // Генерує підписаний JWT токен
    }
}
