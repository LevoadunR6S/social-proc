package org.micro.apigateway.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtRequestFilter jwtFilter;


    //Вказуємо який шаблон url для кожного сервісу
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user_route", r -> r.path("/users/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://ECLIENT"))
                .route("security_route", r -> r.path("/auth/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://ESECURITY"))
                .route("block_other_routes", r -> r.path("/**")
                        .filters(f -> f.setStatus(404)) // Відхиляє всі інші запити
                        .uri("no://op"))
                .build();
    }
}
