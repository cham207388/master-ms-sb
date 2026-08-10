package com.abcham.accounts.service.impl;

import com.abcham.accounts.constants.AccountsConstants;
import com.abcham.accounts.dto.AccountsDto;
import com.abcham.accounts.dto.CustomerDto;
import com.abcham.accounts.entity.Accounts;
import com.abcham.accounts.entity.Customer;
import com.abcham.accounts.exception.CustomerAlreadyExistsException;
import com.abcham.accounts.exception.ResourceNotFoundException;
import com.abcham.accounts.mapper.AccountsMapper;
import com.abcham.accounts.mapper.CustomerMapper;
import com.abcham.accounts.repository.AccountsRepository;
import com.abcham.accounts.repository.CustomerRepository;
import com.abcham.accounts.service.IAccountsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@AllArgsConstructor
public class AccountsServiceImpl implements IAccountsService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;

    @Override
    public void createAccount(CustomerDto customerDto) {
        log.info("Creating account for customer with mobileNumber: {}", customerDto.getMobileNumber());

        Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());
        Optional<Customer> optionalCustomer = customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if (optionalCustomer.isPresent()) {
            log.warn("Customer already registered with mobileNumber: {}", customerDto.getMobileNumber());
            throw new CustomerAlreadyExistsException("Customer already registered with given mobileNumber "
                    + customerDto.getMobileNumber());
        }
        Customer savedCustomer = customerRepository.save(customer);
        Accounts savedAccount = accountsRepository.save(createNewAccount(savedCustomer));
        log.info("Account created successfully with accountNumber: {} for customerId: {}",
                savedAccount.getAccountNumber(), savedCustomer.getCustomerId());
    }

    private Accounts createNewAccount(Customer customer) {
        Accounts newAccount = new Accounts();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);
        return newAccount;
    }

    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        log.info("Fetching account details for mobileNumber: {}", mobileNumber);

        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );
        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));
        log.debug("Successfully fetched account details for mobileNumber: {}", mobileNumber);
        return customerDto;
    }

    @Override
    public boolean updateAccount(CustomerDto customerDto) {
        log.info("Updating account for customer with mobileNumber: {}", customerDto.getMobileNumber());

        boolean isUpdated = false;
        AccountsDto accountsDto = customerDto.getAccountsDto();
        if (accountsDto != null) {
            Accounts accounts = accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "AccountNumber", accountsDto.getAccountNumber().toString())
            );
            AccountsMapper.mapToAccounts(accountsDto, accounts);
            accounts = accountsRepository.save(accounts);

            Long customerId = accounts.getCustomerId();
            Customer customer = customerRepository.findById(customerId).orElseThrow(
                    () -> new ResourceNotFoundException("Customer", "CustomerID", customerId.toString())
            );
            CustomerMapper.mapToCustomer(customerDto, customer);
            customerRepository.save(customer);
            isUpdated = true;
            log.info("Successfully updated account and customer details for accountNumber: {}", accountsDto.getAccountNumber());
        } else {
            log.warn("Account update failed: accountsDto is null for mobileNumber: {}", customerDto.getMobileNumber());
        }
        return isUpdated;
    }

    @Override
    public boolean deleteAccount(String mobileNumber) {
        log.info("Deleting account for mobileNumber: {}", mobileNumber);

        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        log.info("Successfully deleted account and customer with customerId: {} for mobileNumber: {}",
                customer.getCustomerId(), mobileNumber);
        return true;
    }

}
