package com.abcham.gatewayserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    @RequestMapping("/accounts-fallback")
    public Mono<String> accountsServiceFallback() {

        return Mono.just("Accounts service is currently unavailable. Please try again later.");
    }
}
