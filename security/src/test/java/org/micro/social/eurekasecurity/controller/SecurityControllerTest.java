package org.micro.social.eurekasecurity.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.micro.social.eurekasecurity.dto.JwtRequest;
import org.micro.social.eurekasecurity.dto.RegistrationUserDto;
import org.micro.social.eurekasecurity.service.AuthService;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import java.time.LocalDate;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@WebFluxTest(SecurityController.class)
class SecurityControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthService authService;

    RegistrationUserDto correctUser;

    RegistrationUserDto incorrectUser;

    JwtRequest jwtRequest;

    AutoCloseable autoCloseable;


    @BeforeEach
    public void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        jwtRequest = new JwtRequest("Bob", "Fischer");

        correctUser = new RegistrationUserDto(
                "Bob",
                "bob@gmail.com",
                "bob",
                "bob",
                LocalDate.of(1990, 12, 1));

        incorrectUser = new RegistrationUserDto(
                "josh",
                "josh@gmail.com",
                "joh",
                "josh",
                LocalDate.of(1200, 12, 10));
    }


    @AfterEach
    void tearDown() throws Exception{
        autoCloseable.close();
    }




    @Test
    void createNewUser_CorrectUser() {
        when(authService.createNewUser(correctUser))
                .thenReturn("Створено");

        // Виконання тесту
        webTestClient.post()
                .uri("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(correctUser)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.result").isEqualTo("Користувач успішно створений");
    }


    @Test
    void createNewUser_IncorrectUser() {
        when(authService.createNewUser(incorrectUser))
                .thenReturn("Пароль не співпадає");

        // Виконання тесту
        webTestClient.post()
                .uri("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(incorrectUser)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.result").isEqualTo("Паролі не співпадають");
        ;
    }

    @Test
    void createNewUser_ErrorCase() {
        when(authService.createNewUser(incorrectUser))
                .thenReturn("Помилка");

        // Виконання тесту
        webTestClient.post()
                .uri("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(incorrectUser)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.result").isEqualTo("Помилка");
    }

    @Test
    void login_CorrectRequest() {
        when(authService.login(any(), any())).thenAnswer(invocation ->
        {
            ServerWebExchange response = invocation.getArgument(1);
            response.getResponse().addCookie(ResponseCookie.from("AccessToken", "token").build());
            return "Вхід успішний";
        });

        // Виконання тестового запиту через WebTestClient
        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jwtRequest)
                .exchange()
                .expectStatus().isOk().expectCookie().exists("AccessToken")
                .expectBody().jsonPath("$.result").isEqualTo("Вхід успішний");
    }

    @Test
    void login_IncorrectRequest() {
        when(authService.login(any(), any())).thenReturn("Пароль або логін невірний");
        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jwtRequest)
                .exchange()
                .expectCookie().doesNotExist("AccessToken")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.result").isEqualTo("Пароль або логін невірний");
    }


    @Test
    void logout() {
        webTestClient.post()
                .uri("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie("AccessToken","dfsdfds")
                .exchange()
                .expectCookie().doesNotExist("AccessToken")
                .expectStatus().isOk();
    }
}