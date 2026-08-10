package com.abcham.accounts.service.client;

import com.abcham.accounts.dto.CardsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CardsFallback implements CardsFeignClient {

    @Override
    public ResponseEntity<CardsDto> fetchCardDetails(String mobileNumber) {
        log.warn("CardsFallback triggered for mobileNumber: {}", mobileNumber);
        return null;
    }

}
