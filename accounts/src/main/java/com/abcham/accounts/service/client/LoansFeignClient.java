package com.abcham.accounts.service.client;


import com.abcham.accounts.dto.LoansDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "loans", path = "/api/loans", fallback = LoansFallback.class)
public interface LoansFeignClient {

    @GetMapping(value = "/fetch", consumes = "application/json")
    ResponseEntity<LoansDto> fetchLoanDetails(@RequestParam String correlationId,
                                              @RequestParam String mobileNumber);

}
