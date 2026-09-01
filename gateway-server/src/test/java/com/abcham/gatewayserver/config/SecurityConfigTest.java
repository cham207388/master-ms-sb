package com.abcham.gatewayserver.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

class SecurityConfigTest {

    private GenericApplicationContext applicationContext;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        applicationContext = new GenericApplicationContext();
        applicationContext.registerBean(ReactiveJwtDecoder.class, () -> token -> Mono.empty());
        applicationContext.refresh();

        TestServerHttpSecurity http = new TestServerHttpSecurity();
        http.applicationContext(applicationContext);
        SecurityWebFilterChain securityWebFilterChain = new SecurityConfig().springSecurityFilterChain(http);

        webTestClient = WebTestClient.bindToWebHandler(exchange -> {
                    exchange.getResponse().setStatusCode(HttpStatus.NO_CONTENT);
                    return exchange.getResponse().setComplete();
                })
                .webFilter(new WebFilterChainProxy(securityWebFilterChain))
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    void tearDown() {
        applicationContext.close();
    }

    @Test
    void accountsRoleCanPostToGatewayAccountsPath() {
        webTestClient.mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_ACCOUNTS")))
                .post()
                .uri("/accounts/api/accounts/create")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void differentRoleCannotPostToGatewayAccountsPath() {
        webTestClient.mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_CARDS")))
                .post()
                .uri("/accounts/api/accounts/create")
                .exchange()
                .expectStatus().isForbidden();
    }

    private static final class TestServerHttpSecurity extends ServerHttpSecurity {

        private void applicationContext(GenericApplicationContext applicationContext) {
            setApplicationContext(applicationContext);
        }
    }
}
