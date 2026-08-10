package com.abcham.accounts.service.client;


import com.abcham.accounts.dto.CardsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "cards", path = "/api/cards", fallback = CardsFallback.class)
public interface CardsFeignClient {

    @GetMapping(value = "/fetch", consumes = "application/json")
    ResponseEntity<CardsDto> fetchCardDetails(@RequestHeader("securedbank-correlation-id") String correlationId,
                                              @RequestParam String mobileNumber);

}
