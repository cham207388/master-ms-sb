package com.abcham.accounts.service.impl;

import com.abcham.accounts.dto.AccountsDto;
import com.abcham.accounts.dto.CardsDto;
import com.abcham.accounts.dto.CustomerDetailsDto;
import com.abcham.accounts.dto.LoansDto;
import com.abcham.accounts.entity.Accounts;
import com.abcham.accounts.entity.Customer;
import com.abcham.accounts.exception.ResourceNotFoundException;
import com.abcham.accounts.mapper.AccountsMapper;
import com.abcham.accounts.mapper.CustomerMapper;
import com.abcham.accounts.repository.AccountsRepository;
import com.abcham.accounts.repository.CustomerRepository;
import com.abcham.accounts.service.ICustomersService;
import com.abcham.accounts.service.client.CardsFeignClient;
import com.abcham.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class CustomersServiceImpl implements ICustomersService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;

    @Override
    public CustomerDetailsDto fetchCustomerDetails(String correlationId, String mobileNumber) {
        log.info("Fetching customer details for correlationId: {}, mobileNumber: {}", correlationId, mobileNumber);

        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        log.debug("Found customer with ID: {} for mobileNumber: {}", customer.getCustomerId(), mobileNumber);

        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );
        log.debug("Found account number: {} for customer ID: {}", accounts.getAccountNumber(), customer.getCustomerId());

        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

        log.info("Calling Loans microservice for correlationId: {}, mobileNumber: {}", correlationId, mobileNumber);
        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(correlationId, mobileNumber);
        if (null != loansDtoResponseEntity && null != loansDtoResponseEntity.getBody()) {
            log.info("Successfully fetched loan details for mobileNumber: {}", mobileNumber);
            customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());
        } else {
            log.warn("Loan details response is null or body empty for mobileNumber: {}", mobileNumber);
        }

        log.info("Calling Cards microservice for correlationId: {}, mobileNumber: {}", correlationId, mobileNumber);
        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(correlationId, mobileNumber);
        if (null != cardsDtoResponseEntity && null != cardsDtoResponseEntity.getBody()) {
            log.info("Successfully fetched card details for mobileNumber: {}", mobileNumber);
            customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());
        } else {
            log.warn("Card details response is null or body empty for mobileNumber: {}", mobileNumber);
        }

        return customerDetailsDto;
    }

}