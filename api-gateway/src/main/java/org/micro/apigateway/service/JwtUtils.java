package org.micro.apigateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;


@Component
public class JwtUtils {
    private String secret;
    private String lifetime;

    private Key key;

    @Autowired
    public JwtUtils(@Value("${jwt-secret-key}") String secret,
                    @Value("${jwt-expiration-milliseconds}") String lifetime) {
        this.secret = secret;
        this.lifetime = lifetime;
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Метод для отримання всіх Claims
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    //Метод, який перевіряє чи токен дійсний
    public Boolean isTokenExpired(String token) {
        return getAllClaimsFromToken(token).getExpiration().before(new Date());
    }


}

