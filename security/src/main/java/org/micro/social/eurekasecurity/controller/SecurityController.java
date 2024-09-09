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
        String result = authService.createNewUser(userDto);
        if (result.equals("Створено")){
            return ResponseHandler.responseBuilder(HttpStatus.CREATED,
                    "Користувач успішно створений","result");
        }
        else if(result.equals("Пароль не співпадає")){
            return ResponseHandler.responseBuilder(HttpStatus.BAD_REQUEST,
                    "Паролі не співпадають","result");
        }
        else {
           return ResponseHandler.responseBuilder(HttpStatus.BAD_REQUEST,
                    "Помилка", "result");
        }
    }

    // Обробляє POST запити на /auth/login для входу користувача
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest body, ServerWebExchange exchange) {
        String result = authService.login(body, exchange);
        if (result.equals("Вхід успішний")){
            return ResponseHandler.responseBuilder(HttpStatus.OK,result,"result");
        }
            else return ResponseHandler.responseBuilder(HttpStatus.BAD_REQUEST,result,"result");

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(ServerWebExchange exchange){
        return ResponseHandler.responseBuilder(HttpStatus.OK,authService.logout(exchange),"result");
    }

    // Тестовий маршрут для перевірки захищеного ресурсу
    @GetMapping("/secured")
    public ResponseEntity<?> testSecuredUrl() {
        return ResponseHandler.responseBuilder(HttpStatus.OK, "Захищено", "response");
    }

    // Тестовий маршрут для перевірки відкритого ресурсу
    @GetMapping("/open")
    public ResponseEntity<?> testOpeUrl() {
        return ResponseHandler.responseBuilder(HttpStatus.OK, "Відкрито", "response");
    }
}
