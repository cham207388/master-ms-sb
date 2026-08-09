package com.abcham.gatewayserver.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ResponseTraceFilter {

    private final FilterUtility filterUtility;

    @Bean
    public GlobalFilter postGlobalFilter() {

        return (exchange, chain) -> chain
                .filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
                    String correlationId = filterUtility.getCorrelationId(requestHeaders);
                    if (!(exchange.getResponse().getHeaders().containsHeader(FilterUtility.CORRELATION_ID))) {
                        log.debug("Updated the correlation id to the outbound headers: {}", correlationId);
                        exchange.getResponse().getHeaders().add(FilterUtility.CORRELATION_ID, correlationId);
                    }
                }));
    }

}
