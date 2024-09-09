package org.micro.apigateway.config;

import io.jsonwebtoken.ExpiredJwtException;
import org.micro.apigateway.redis.RedisService;
import org.micro.apigateway.service.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

//Фільтр для перевірки jwt токена, оновлення токена якщо термін вийшов
@Component
public class JwtRequestFilter implements GatewayFilter {

    private RouteValidator validator;

    @Autowired
    private RedisService redisService;

    private JwtUtils jwtUtils;

    @Autowired
    public JwtRequestFilter(RouteValidator validator, JwtUtils jwtUtils) {
        this.validator = validator;
        this.jwtUtils = jwtUtils;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //Отримуємо запит
        ServerHttpRequest request = exchange.getRequest();

        // Якщо маршрут відкритий, пропускаємо перевірку авторизації
        if (!validator.isSecured.test(request)) {
            return chain.filter(exchange);
        }

        // Перевіряємо, чи є cookie AccessToken, якщо немає, то повертаємо код 401
        if (authCookieMissing(request)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        //Отримуємо токен з cookie
        String token = request.getCookies().get("AccessToken").get(0).getValue();




        // Перевіряємо, чи не прострочений токен
        if (jwtUtils.isTokenExpired(token)) {
            String username;

            // Отримуємо ім'я користувача з токену, навіть якщо він прострочений
            try {
                username = jwtUtils.getAllClaimsFromToken(token).get("username", String.class);
            } catch (ExpiredJwtException e) {
                username = e.getClaims().get("username", String.class);
            }

            //Отримуємо RefreshToken з Redis для оновлення AccessToken
            String refreshToken = redisService.getToken(username);

            //Якщо RefreshToken null, або являє собою набір пустих символів (пробіл, табуляція, переніс рядка),
            //то повертаємо код 401
            if (refreshToken == null || refreshToken.isBlank()) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            // Спроба оновити Access токен та добавити у відповідь у вигляді cookie з назвою AccessToken
            try {
                String newAccessToken = jwtUtils.refreshAccessToken(refreshToken);
                exchange.getResponse().addCookie(createCookie("AccessToken", newAccessToken, 86400000L));
            } catch (Exception e) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        }


        return chain.filter(exchange);
    }

    //Метод, який викликається під час помилок у фільтрі (Закінчення роботи фільтру та подальшої логіки)
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    //Метод, який перевіряє чи є AccessToken в списку cookie
    private boolean authCookieMissing(ServerHttpRequest request) {
        return request.getCookies().get("AccessToken") == null;
    }

    //Метод для створення сookie, яке зберігатиме токен
    public ResponseCookie createCookie(String cookieName, String token, Long durationMillis) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(true) //Тільки для протоколу HTTP
                .secure(true) //Використовує HTTPS
                .path("/") //Шлях по якому cookie буде доступна (в даному випадку, доступна для всіх шляхів)
                .maxAge(durationMillis / 1000) //Час життя cookie в секундах


                //Lax. Режим, який дозволяє надсилати cookie в межах запитів з того самого домену
                // або з піддомена (наприклад, при навігації користувача).
                // Однак, у випадку запитів, зроблених через сторонні джерела
                // (наприклад, при запитах з форм або посилань з інших сайтів), cookies не будуть відправлені.

                //Strict. cookies не будуть відправлятися при жодних запитах з інших сайтів,
                // навіть якщо користувач перейде на сайт через посилання.
                // Тобто cookie надсилаються лише для запитів, які відбуваються у межах того ж домену
                // (переміщення між сторінками цього ж сайту).

                //None. Cookie будуть відправлятися з будь-якими запитами,
                // включаючи сторонні джерела (запити з інших сайтів, iFrame, запити через форми або JavaScript).
                //Небезпечний через те, що можливе перехоплення cookie з інших сайтів!!!!!
                .sameSite("Lax").build(); //

    }


}

