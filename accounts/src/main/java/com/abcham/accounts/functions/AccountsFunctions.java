package com.abcham.accounts.functions;

import com.abcham.accounts.service.IAccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class AccountsFunctions {


    @Bean
    public Consumer<Long> updateCommunication(IAccountsService accountsService) {
        return accountNumber -> {
            log.info("Updating Communication status for the account number : {}", accountNumber.toString());
            boolean isUpdated = accountsService.updateCommunicationStatus(accountNumber);
            if (isUpdated) {
                log.info("Successfully updated Communication status for the account number : {}", accountNumber);
            } else {
                log.warn("Failed to update Communication status for the account number : {}", accountNumber);
            }
        };
    }

}