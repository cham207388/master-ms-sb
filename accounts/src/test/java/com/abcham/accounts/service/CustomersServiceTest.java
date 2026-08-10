package com.abcham.accounts.service;

import com.abcham.accounts.constants.AccountsConstants;
import com.abcham.accounts.dto.CardsDto;
import com.abcham.accounts.dto.CustomerDetailsDto;
import com.abcham.accounts.dto.LoansDto;
import com.abcham.accounts.entity.Accounts;
import com.abcham.accounts.entity.Customer;
import com.abcham.accounts.exception.ResourceNotFoundException;
import com.abcham.accounts.repository.AccountsRepository;
import com.abcham.accounts.repository.CustomerRepository;
import com.abcham.accounts.service.client.CardsFeignClient;
import com.abcham.accounts.service.client.LoansFeignClient;
import com.abcham.accounts.service.impl.CustomersServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomersServiceTest {

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CardsFeignClient cardsFeignClient;

    @Mock
    private LoansFeignClient loansFeignClient;

    @InjectMocks
    private CustomersServiceImpl customersService;

    private Customer customer;
    private Accounts accounts;
    private CardsDto cardsDto;
    private LoansDto loansDto;

    @BeforeEach
    void setUp() {
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

        cardsDto = new CardsDto();
        cardsDto.setCardNumber("123456789012");
        cardsDto.setMobileNumber("1234567890");
        cardsDto.setCardType("CREDIT_CARD");

        loansDto = new LoansDto();
        loansDto.setLoanNumber("987654321098");
        loansDto.setMobileNumber("1234567890");
        loansDto.setLoanType("HOME_LOAN");
    }

    @Test
    void fetchCustomerDetails_Success() {
        when(customerRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(customer));
        when(accountsRepository.findByCustomerId(1L)).thenReturn(Optional.of(accounts));
        when(loansFeignClient.fetchLoanDetails("1234567890")).thenReturn(ResponseEntity.ok(loansDto));
        when(cardsFeignClient.fetchCardDetails("1234567890")).thenReturn(ResponseEntity.ok(cardsDto));

        CustomerDetailsDto result = customersService.fetchCustomerDetails("1234567890");

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("1234567890", result.getMobileNumber());
        assertNotNull(result.getAccountsDto());
        assertEquals(1234567890L, result.getAccountsDto().getAccountNumber());
        assertNotNull(result.getLoansDto());
        assertEquals("987654321098", result.getLoansDto().getLoanNumber());
        assertNotNull(result.getCardsDto());
        assertEquals("123456789012", result.getCardsDto().getCardNumber());

        verify(loansFeignClient, times(1)).fetchLoanDetails("1234567890");
        verify(cardsFeignClient, times(1)).fetchCardDetails("1234567890");
    }

    @Test
    void fetchCustomerDetails_FallbackHandling_WhenCardsAndLoansReturnNull() {
        when(customerRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(customer));
        when(accountsRepository.findByCustomerId(1L)).thenReturn(Optional.of(accounts));
        when(loansFeignClient.fetchLoanDetails("1234567890")).thenReturn(null);
        when(cardsFeignClient.fetchCardDetails("1234567890")).thenReturn(null);

        CustomerDetailsDto result = customersService.fetchCustomerDetails("1234567890");

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertNull(result.getLoansDto());
        assertNull(result.getCardsDto());
    }

    @Test
    void fetchCustomerDetails_ThrowsResourceNotFoundException_WhenCustomerNotFound() {
        when(customerRepository.findByMobileNumber("1234567890")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customersService.fetchCustomerDetails("1234567890"));
    }
}
