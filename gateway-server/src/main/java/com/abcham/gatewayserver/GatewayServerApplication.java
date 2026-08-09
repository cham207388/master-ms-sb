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
                        .path("/ACCOUNTS/**")
                        .filters(f -> f.rewritePath("/ACCOUNTS/(?<segment>.*)", "/${segment}")
								.circuitBreaker(config -> config
										.setName("accountsCircuitBreaker")
//										.setFallbackUri("forward:/accounts-fallback")
								)
						)
                        .uri("lb://ACCOUNTS"))
                .route(p -> p
                        .path("/accounts/**")
                        .filters(f -> f.rewritePath("/accounts/(?<segment>.*)", "/${segment}"))
                        .uri("lb://ACCOUNTS"))
                .route(p -> p
                        .path("/CARDS/**")
                        .filters(f -> f.rewritePath("/CARDS/(?<segment>.*)", "/${segment}"))
                        .uri("lb://CARDS"))
                .route(p -> p
                        .path("/cards/**")
                        .filters(f -> f.rewritePath("/cards/(?<segment>.*)", "/${segment}"))
                        .uri("lb://CARDS"))
                .route(p -> p
                        .path("/LOANS/**")
                        .filters(f -> f.rewritePath("/LOANS/(?<segment>.*)", "/${segment}"))
                        .uri("lb://LOANS"))
                .route(p -> p
                        .path("/loans/**")
                        .filters(f -> f.rewritePath("/loans/(?<segment>.*)", "/${segment}"))
                        .uri("lb://LOANS"))
                .build();
    }

}
