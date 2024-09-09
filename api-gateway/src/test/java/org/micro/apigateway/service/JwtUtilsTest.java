package org.micro.apigateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.micro.shareable.model.Role;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private String secret = "testsecretkeytestsecretkeytestsecretkey";
    private Long lifetime = 60000L;

    private String accessToken;
    private String refreshToken;

    @BeforeEach
    public void setUp() {
        jwtUtils = new JwtUtils(secret, lifetime);

        Set<Role> roles = new HashSet<>();
        roles.add(new Role("USER"));
        accessToken = jwtUtils.generateAccessToken("testuser", roles);

        refreshToken = jwtUtils.generateAccessToken("testuser", roles);
    }

    @Test
    public void getAllClaimsFromToken_AccessToken() {

        Claims claims = jwtUtils.getAllClaimsFromToken(accessToken);
        assertEquals("testuser", claims.get("username"));
        assertNotNull(claims.get("roles"));
    }

    @Test
    public void getAllClaimsFromToken_RefreshToken() {

        Claims claims = jwtUtils.getAllClaimsFromToken(refreshToken);
        assertEquals("testuser", claims.get("username"));
        assertNotNull(claims.get("roles"));
    }

    @Test
    public void isTokenExpired_NotExpired() {
        assertFalse(jwtUtils.isTokenExpired(accessToken));
    }

    @Test
    public void isTokenExpired_Expired() {
        JwtUtils jwtUtilsSpy = Mockito.spy(jwtUtils);
        doThrow(new ExpiredJwtException(null, null, null)).when(jwtUtilsSpy).getAllClaimsFromToken(anyString());

        assertTrue(jwtUtilsSpy.isTokenExpired(accessToken));
    }

    @Test
    public void refreshAccessToken_Valid() {

        String newAccessToken = jwtUtils.refreshAccessToken(refreshToken);
        Claims claims = jwtUtils.getAllClaimsFromToken(newAccessToken);

        assertEquals("testuser", claims.get("username"));
        assertNotNull(claims.get("roles"));
    }

    @Test
    public void refreshAccessToken_Invalid() {

        assertThrows(RuntimeException.class, () -> jwtUtils.refreshAccessToken("invalidtoken"));
    }

    @Test
    public void generateAccessToken() {

        Set<Role> roles = new HashSet<>();
        roles.add(new Role("USER"));
        String newAccessToken = jwtUtils.generateAccessToken("testuser", roles);

        Claims claims = jwtUtils.getAllClaimsFromToken(newAccessToken);
        assertEquals("testuser", claims.get("username"));
        assertNotNull(claims.get("roles"));
    }
}
