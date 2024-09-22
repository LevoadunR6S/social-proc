package org.micro.social.eurekasecurity.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.micro.shareable.dto.UserDto;
import org.micro.shareable.model.Role;
import org.micro.social.eurekasecurity.dto.JwtRequest;
import org.micro.social.eurekasecurity.dto.RegistrationUserDto;
import org.micro.social.eurekasecurity.kafka.KafkaUserClient;
import org.micro.social.eurekasecurity.redis.RedisService;
import org.micro.social.eurekasecurity.repository.RoleRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;


@SpringBootTest(classes = AuthServiceTest.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private KafkaUserClient kafkaUserClient;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RedisService redisService;

    @Mock
    private RoleRepository roleRepository;

    UserDto user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authService.lifetime = 3600000L;

        user = new UserDto("bob",
                "bob",
                "Bob@gmail.com",
                LocalDate.of(2000, 10, 10),
                Set.of(new Role("User")));
    }





    @Test
    void login_CorrectRequest() {
        JwtRequest jwtRequest = new JwtRequest();
        jwtRequest.setUsername("bob");


        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        when(exchange.getResponse()).thenReturn(response);

        when(kafkaUserClient.getUserByUsername("bob")).thenReturn(Optional.of(user));

        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        when(jwtUtils.generate("bob", user.getRoles(), "ACCESS")).thenReturn(accessToken);
        when(jwtUtils.generate("bob", user.getRoles(), "REFRESH")).thenReturn(refreshToken);

        doNothing().when(redisService).saveRefreshToken("bob", refreshToken);

        String result = authService.login(jwtRequest, exchange);

        assertEquals("Вхід успішний", result);
        verify(exchange.getResponse()).addCookie(argThat(cookie -> "AccessToken".equals(cookie.getName())));
        verify(redisService).saveRefreshToken("bob", refreshToken);
    }


    @Test
    void login_IncorrectRequest() {
        JwtRequest jwtRequest = new JwtRequest();
        jwtRequest.setUsername("bob");


        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        when(exchange.getResponse()).thenReturn(response);

        when(kafkaUserClient.getUserByUsername("bob")).thenReturn(Optional.empty());


        String result = authService.login(jwtRequest, exchange);

        verify(redisService, never()).saveRefreshToken(any(), any());
        verify(exchange.getResponse(), never()).addCookie(any());
        assertEquals("Пароль або логін невірний", result);
    }

    @Test
    void createNewUser_CorrectUser() {
        RegistrationUserDto userDto = new RegistrationUserDto("bob",
                "bob@gmail.com",
                "bob",
                "bob",
                LocalDate.of(2000, 10, 10));

        when(roleRepository.findByName(any())).thenReturn(Optional.of(new Role("USER")));
        when(kafkaUserClient.createUser(any(UserDto.class))).thenReturn(Optional.of("Created"));

        String result = authService.createNewUser(userDto);

        assertEquals("Створено", result);
        verify(kafkaUserClient).createUser(any(UserDto.class));
    }


    @Test
    void createNewUser_IncorrectPassword() {
        RegistrationUserDto userDto = new RegistrationUserDto("bob",
                "bob@gmail.com",
                "bo",
                "bob",
                LocalDate.of(2000, 10, 10));

        String result = authService.createNewUser(userDto);

        assertEquals("Пароль не співпадає", result);
        verify(kafkaUserClient, never()).createUser(any(UserDto.class));
    }


    @Test
    void createNewUser_ErrorCase() {
        RegistrationUserDto userDto = new RegistrationUserDto("bob",
                "bob@gmail.com",
                "bob",
                "bob",
                LocalDate.of(2000, 10, 10));


        when(roleRepository.findByName(any())).thenReturn(Optional.of(new Role("USER")));
        when(kafkaUserClient.createUser(any(UserDto.class))).thenReturn(Optional.empty());

        String result = authService.createNewUser(userDto);

        assertEquals("Помилка", result);
        verify(kafkaUserClient, atLeastOnce()).createUser(any(UserDto.class));
    }

    @Test
    void logout_withCookie() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        Claims claims = mock(Claims.class);

        when(exchange.getResponse()).thenReturn(response);
        when(jwtUtils.getAllClaimsFromToken(any())).thenReturn(claims);

        MultiValueMap<String, ResponseCookie> cookies = new LinkedMultiValueMap<>();
        cookies.add("AccessToken", ResponseCookie.from("AccessToken", "test").build());

        when(response.getCookies()).thenReturn(cookies);

        authService.logout(exchange);

        ArgumentCaptor<ResponseCookie> cookieCaptor = ArgumentCaptor.forClass(ResponseCookie.class);
        verify(response).addCookie(cookieCaptor.capture());

        ResponseCookie capturedCookie = cookieCaptor.getValue();
        assertEquals("AccessToken", capturedCookie.getName());
        assertEquals("", capturedCookie.getValue());
    }


    @Test
    void registrationUserToDto() {
        RegistrationUserDto registrationUserDto = new RegistrationUserDto();
        registrationUserDto.setUsername("bob");
        registrationUserDto.setPassword("bob");
        registrationUserDto.setEmail("bob@gmail.com");
        registrationUserDto.setBirthDate(LocalDate.of(2000, 10, 10));

        Role role = new Role();
        role.setName("USER");

        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));

        UserDto userDto = authService.registrationUserToDto(registrationUserDto);

        assertEquals("bob", userDto.getUsername());
        assertTrue(BCrypt.checkpw("bob", userDto.getPassword())); // Перевірка хешування паролю
        assertEquals("bob@gmail.com", userDto.getEmail());
        assertEquals(LocalDate.of(2000, 10, 10), userDto.getBirthDate());
        assertEquals(1, userDto.getRoles().size());
        assertTrue(userDto.getRoles().contains(role));
    }

    @Test
    void createCookie() {
        // Вхідні дані для тесту
        String cookieName = "AccessToken";
        String token = "testToken";
        Long durationMillis = 3600000L;

        ResponseCookie responseCookie = authService.createCookie(cookieName, token, durationMillis);

        assertEquals(cookieName, responseCookie.getName());
        assertEquals(token, responseCookie.getValue());
        assertTrue(responseCookie.isHttpOnly());
        assertTrue(responseCookie.isSecure());
        assertEquals("/", responseCookie.getPath());
        assertEquals(Duration.ofSeconds(durationMillis / 1000), responseCookie.getMaxAge());
        assertEquals("Lax", responseCookie.getSameSite());
    }

}