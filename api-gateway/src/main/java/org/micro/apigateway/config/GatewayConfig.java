package org.micro.apigateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//Клас для налаштування маршрутів та перенаправлення запитів на інші сервіси
@Configuration
public class GatewayConfig {

    @Autowired
    private JwtRequestFilter jwtFilter;



    //Вказуємо який шаблон url для кожного сервісу, необхідні фільтри та за необхідності Http код
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user_route", r -> r.path("/users/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://ECLIENT"))
                .route("security_route", r -> r.path("/auth/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://ESECURITY"))
                .route("data_route", r -> r.path("/data/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://DATA"))
                .route("block_other_routes", r -> r.path("/**")
                        .filters(f -> f.setStatus(404)) // Відхиляє всі інші запити
                        .uri("no://op"))

                .build();
    }
}
