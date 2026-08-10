package com.abcham.accounts.service.client;

import com.abcham.accounts.dto.LoansDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoansFallback implements LoansFeignClient {

    @Override
    public ResponseEntity<LoansDto> fetchLoanDetails(String correlationId, String mobileNumber) {
        log.warn("LoansFallback triggered for correlationId: {}, mobileNumber: {}", correlationId, mobileNumber);
        return null;
    }

}
