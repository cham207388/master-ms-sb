package com.abcham.accounts.service;

import com.abcham.accounts.constants.AccountsConstants;
import com.abcham.accounts.dto.AccountsDto;
import com.abcham.accounts.dto.CustomerDto;
import com.abcham.accounts.entity.Accounts;
import com.abcham.accounts.entity.Customer;
import com.abcham.accounts.exception.CustomerAlreadyExistsException;
import com.abcham.accounts.exception.ResourceNotFoundException;
import com.abcham.accounts.repository.AccountsRepository;
import com.abcham.accounts.repository.CustomerRepository;
import com.abcham.accounts.service.impl.AccountsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountsServiceTest {

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountsServiceImpl accountsService;

    private CustomerDto customerDto;
    private Customer customer;
    private Accounts accounts;

    @BeforeEach
    void setUp() {

        AccountsDto accountsDto = new AccountsDto();
        accountsDto.setAccountNumber(1234567890L);
        accountsDto.setAccountType(AccountsConstants.SAVINGS);
        accountsDto.setBranchAddress(AccountsConstants.ADDRESS);

        customerDto = new CustomerDto();
        customerDto.setName("John Doe");
        customerDto.setEmail("john@example.com");
        customerDto.setMobileNumber("1234567890");
        customerDto.setAccountsDto(accountsDto);

        customer = new Customer();
        customer.setCustomerId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setMobileNumber("1234567890");

        accounts = new Accounts();
        accounts.setCustomerId(1L);
        accounts.setAccountNumber(1234567890L);
        accounts.setAccountType(AccountsConstants.SAVINGS);
        accounts.setBranchAddress(AccountsConstants.ADDRESS);
    }

    @Test
    void createAccount_Success() {

        when(customerRepository.findByMobileNumber("1234567890")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(accountsRepository.save(any(Accounts.class))).thenReturn(accounts);

        assertDoesNotThrow(() -> accountsService.createAccount(customerDto));

        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(accountsRepository, times(1)).save(any(Accounts.class));
    }

    @Test
    void createAccount_ThrowsCustomerAlreadyExistsException() {

        when(customerRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(customer));

        assertThrows(CustomerAlreadyExistsException.class, () -> accountsService.createAccount(customerDto));

        verify(customerRepository, never()).save(any(Customer.class));
        verify(accountsRepository, never()).save(any(Accounts.class));
    }

    @Test
    void fetchAccount_Success() {

        when(customerRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(customer));
        when(accountsRepository.findByCustomerId(1L)).thenReturn(Optional.of(accounts));

        CustomerDto result = accountsService.fetchAccount("1234567890");

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("1234567890", result.getMobileNumber());
        assertNotNull(result.getAccountsDto());
        assertEquals(1234567890L, result.getAccountsDto().getAccountNumber());
    }

    @Test
    void fetchAccount_ThrowsResourceNotFoundException_WhenCustomerNotFound() {

        when(customerRepository.findByMobileNumber("1234567890")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountsService.fetchAccount("1234567890"));
    }

    @Test
    void updateAccount_Success() {

        when(accountsRepository.findById(1234567890L)).thenReturn(Optional.of(accounts));
        when(accountsRepository.save(any(Accounts.class))).thenReturn(accounts);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        boolean isUpdated = accountsService.updateAccount(customerDto);

        assertTrue(isUpdated);
        verify(accountsRepository, times(1)).save(any(Accounts.class));
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void updateAccount_ReturnsFalse_WhenAccountsDtoIsNull() {

        customerDto.setAccountsDto(null);

        boolean isUpdated = accountsService.updateAccount(customerDto);

        assertFalse(isUpdated);
        verify(accountsRepository, never()).save(any(Accounts.class));
    }

    @Test
    void deleteAccount_Success() {

        when(customerRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(customer));

        boolean isDeleted = accountsService.deleteAccount("1234567890");

        assertTrue(isDeleted);
        verify(accountsRepository, times(1)).deleteByCustomerId(1L);
        verify(customerRepository, times(1)).deleteById(1L);
    }

}
