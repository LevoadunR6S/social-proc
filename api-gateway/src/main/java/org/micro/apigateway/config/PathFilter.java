package org.micro.apigateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

//Фільтр, який використовується для видалення не потрібних cookie
@Component
public class PathFilter implements GatewayFilter {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //Видаляє cookie AccessToken
            ResponseCookie accessTokenCookie = ResponseCookie.from("AccessToken", "")
                    .maxAge(0)
                    .path("/")
                    .build();
            exchange.getResponse().addCookie(accessTokenCookie);

            return chain.filter(exchange);
    }
}
