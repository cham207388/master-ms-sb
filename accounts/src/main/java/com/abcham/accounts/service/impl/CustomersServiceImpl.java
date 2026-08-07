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

    /**
     * @param mobileNumber - Input Mobile Number
     * @return Customer Details based on a given mobileNumber
     */
    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber) {
        log.debug("Fetching Customer record from Accounts DB for mobileNumber: {}", mobileNumber);
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );

        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

        log.debug("Calling Loans Microservice via Feign Client for mobileNumber: {}", mobileNumber);
        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(mobileNumber);
        if (loansDtoResponseEntity != null && loansDtoResponseEntity.getBody() != null) {
            customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());
            log.debug("Successfully received Loans details for mobileNumber: {}", mobileNumber);
        }

        log.debug("Calling Cards Microservice via Feign Client for mobileNumber: {}", mobileNumber);
        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(mobileNumber);
        if (cardsDtoResponseEntity != null && cardsDtoResponseEntity.getBody() != null) {
            customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());
            log.debug("Successfully received Cards details for mobileNumber: {}", mobileNumber);
        }

        return customerDetailsDto;

    }
}