package org.micro.social.eurekasecurity.controller;

import org.micro.shareable.response.ResponseHandler;
import org.micro.social.eurekasecurity.dto.JwtRequest;
import org.micro.social.eurekasecurity.dto.RegistrationUserDto;
import org.micro.social.eurekasecurity.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

@RestController
@RequestMapping("/auth")
public class SecurityController {

    @Autowired
    private AuthService authService;

    // Обробляє POST запити на /auth/signup для реєстрації нового користувача
    @PostMapping("/signup")
    public ResponseEntity<?> createNewUser(@RequestBody RegistrationUserDto userDto) {
        return authService.createNewUser(userDto);
    }

    // Обробляє POST запити на /auth/login для входу користувача
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest body, ServerWebExchange exchange) {
        return authService.login(body, exchange);
    }

    // Тестовий маршрут для перевірки захищеного ресурсу
    @GetMapping("/secured")
    public ResponseEntity<?> test1() {
        return ResponseHandler.responseBuilder(HttpStatus.OK, "secured", "response");
    }

    // Тестовий маршрут для перевірки відкритого ресурсу
    @GetMapping("/open")
    public ResponseEntity<?> test2() {
        return ResponseHandler.responseBuilder(HttpStatus.OK, "open", "response");
    }
}
