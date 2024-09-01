package org.micro.social.eurekasecurity.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.micro.shareable.model.Role;
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
    public JwtUtils(@Value("${jwt-secret-key}") String secret,
                    @Value("${jwt-expiration-milliseconds}") Long lifetime) {
        this.secret = secret;
        this.lifetime = lifetime;
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }



    public String generate(String username, String password, Set<Role> roles, String tokenType) {

        Map<String, String> claims = Map.of(
                "username", username,
                "password", password,
                "roles", roles.toString()
        );
        long expMillis = "ACCESS".equalsIgnoreCase(tokenType)
                ? lifetime
                : lifetime * 5;

        final Date now = new Date();
        final Date exp = new Date(now.getTime() + expMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(claims.get("id"))
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key)
                .compact();
    }


    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJwt(token).getBody();
    }

    public Boolean isTokenExpired(String token) {
        return getAllClaimsFromToken(token).getExpiration().before(new Date());
    }


}

