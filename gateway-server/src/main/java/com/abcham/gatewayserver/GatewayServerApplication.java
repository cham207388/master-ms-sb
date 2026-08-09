package com.abcham.gatewayserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServerApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route(p -> p
                        .path("/accounts/**", "/ACCOUNTS/**")
                        .filters(f -> f.rewritePath("(?i)/accounts/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", String.valueOf(System.currentTimeMillis()))
                                .circuitBreaker(config -> config
                                        .setName("accountsCircuitBreaker")
                                        .setFallbackUri("forward:/accounts-fallback")
                                )
                        )
                        .uri("lb://ACCOUNTS"))
                .route(p -> p
                        .path("/cards/**", "/CARDS/**")
                        .filters(f -> f.rewritePath("(?i)/cards/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", String.valueOf(System.currentTimeMillis()))
                        )
                        .uri("lb://CARDS"))
                .route(p -> p
                        .path("/loans/**", "/LOANS/**")
                        .filters(f -> f.rewritePath("(?i)/loans/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", String.valueOf(System.currentTimeMillis()))
                        )
                        .uri("lb://LOANS"))
                .build();
    }

}
