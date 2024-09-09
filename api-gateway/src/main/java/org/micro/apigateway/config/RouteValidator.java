package org.micro.apigateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    //Відкриті маршрути
    public static final List<String> openApiEndpoints = List.of(
            "/auth/signup",
            "/auth/login",
            "/auth/open"    //todo delete
    );


    //Перевіряємо чи маршрут потребує авторизації
    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
