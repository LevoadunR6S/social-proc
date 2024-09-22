package org.micro.social.eurekasecurity.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.micro.shareable.model.Role;
import org.mockito.MockitoAnnotations;

import java.security.Key;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private Long lifetime;

    private String secret;
    private Key key;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.lifetime = 360000L;
        this.secret = "veryveryveryverysecretsecretcodeorpasswordirono";
        key = Keys.hmacShaKeyFor(secret.getBytes());
        jwtUtils = new JwtUtils(secret, lifetime);

    }

    @Test
    void generate(){
        String username = "bob";
        Set<Role> roles = Set.of(new Role("User"));
        String refresh = "Access";

        String result = jwtUtils.generate(username,roles,refresh);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(result)
                .getBody();

        List<Map<String, Object>> rolesList = (List<Map<String, Object>>) claims.get("roles");
        // Перетворює кожну мапу у об'єкт Role
        Set<Role> roleSet = new HashSet<>();
        for (Map<String, Object> roleMap : rolesList) {
            Role name = new Role(roleMap.get("name").toString());
            roleSet.add(name);
        }


        // Перевірка claims
        assertEquals(username, claims.get("username"));
        assertEquals(roles, roleSet);

        // Перевірка часу дії
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();
        assertNotNull(issuedAt);
        assertNotNull(expiration);

        long expMillis = lifetime;
        assertEquals(expMillis, expiration.getTime() - issuedAt.getTime(), 1000); // Допускаємо похибку 1 сек
    }


    @Test
    void getAllClaimsFromToken_ValidToken() {
        String username = "bob";
        Set<Role> roles = Set.of(new Role("USER"));
        String token = jwtUtils.generate(username, roles, "ACCESS");

        Claims claims = jwtUtils.getAllClaimsFromToken(token);

        List<Map<String, Object>> rolesList = (List<Map<String, Object>>) claims.get("roles");
        // Перетворює кожну мапу у об'єкт Role
        Set<Role> roleSet = new HashSet<>();
        for (Map<String, Object> roleMap : rolesList) {
            Role name = new Role(roleMap.get("name").toString());
            roleSet.add(name);
        }

        assertNotNull(claims);
        assertEquals(username, claims.get("username"));
        assertEquals(roles, roleSet);
    }

    @Test
    void getAllClaimsFromToken_ExpiredToken() {
        // Створення токена з минувшою датою закінчення
        Date now = new Date();
        Date past = new Date(now.getTime() - 1000); // Токен, що вже застарів
        String expiredToken = Jwts.builder()
                .setSubject("expiredUser")
                .setIssuedAt(now)
                .setExpiration(past)
                .signWith(key)
                .compact();

        // Перевірка на виключення ExpiredJwtException
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtils.getAllClaimsFromToken(expiredToken);
        });
    }
}