package com.abcham.gatewayserver.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

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
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setBackoff(Duration.ofMillis(100L), Duration.ofSeconds(1), 2, true)
                                )
                        )
                        .uri("lb://ACCOUNTS"))
                .route(p -> p
                        .path("/cards/**", "/CARDS/**")
                        .filters(f -> f.rewritePath("(?i)/cards/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", String.valueOf(System.currentTimeMillis()))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setBackoff(Duration.ofMillis(100L), Duration.ofSeconds(1), 2, true)
                                )
                        )
                        .uri("lb://CARDS"))
                .route(p -> p
                        .path("/loans/**", "/LOANS/**")
                        .filters(f -> f.rewritePath("(?i)/loans/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", String.valueOf(System.currentTimeMillis()))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setBackoff(Duration.ofMillis(100L), Duration.ofSeconds(1), 2, true)
                                )
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()).setKeyResolver(userKeyResolver()))
                        )
                        .uri("lb://LOANS"))
                .build();
    }

    @Bean // overrides the default configuration retry
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {

        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(10)).build()).build());
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {

        return new RedisRateLimiter(1, 1, 1);
    }

    @Bean
    KeyResolver userKeyResolver() {

        return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("user"))
                .defaultIfEmpty("anonymous");
    }

}
