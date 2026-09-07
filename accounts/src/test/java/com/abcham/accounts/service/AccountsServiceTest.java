package com.abcham.accounts.service;

import com.abcham.accounts.constants.AccountsConstants;
import com.abcham.accounts.constants.TransactionType;
import com.abcham.accounts.dto.AccountsDto;
import com.abcham.accounts.dto.AmountRequestDto;
import com.abcham.accounts.dto.CustomerDto;
import com.abcham.accounts.dto.NotificationMsgDto;
import com.abcham.accounts.dto.NotificationType;
import com.abcham.accounts.dto.TransactionDto;
import com.abcham.accounts.dto.TransferRequestDto;
import com.abcham.accounts.entity.Accounts;
import com.abcham.accounts.entity.Customer;
import com.abcham.accounts.entity.Transaction;
import com.abcham.accounts.exception.CustomerAlreadyExistsException;
import com.abcham.accounts.exception.InsufficientFundsException;
import com.abcham.accounts.exception.InvalidTransactionException;
import com.abcham.accounts.exception.ResourceNotFoundException;
import com.abcham.accounts.repository.AccountsRepository;
import com.abcham.accounts.repository.CustomerRepository;
import com.abcham.accounts.repository.TransactionRepository;
import com.abcham.accounts.service.impl.AccountsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountsServiceTest {

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private StreamBridge streamBridge;

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
        accountsDto.setBalance(BigDecimal.ZERO);

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
        accounts.setBalance(BigDecimal.ZERO);
        accounts.setCommunicationSw(false);
    }

    @Test
    void createAccount_Success() {

        when(customerRepository.findByMobileNumber("1234567890")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(accountsRepository.save(any(Accounts.class))).thenReturn(accounts);
        when(streamBridge.send(eq("sendCommunication-out-0"), any())).thenReturn(true);

        assertDoesNotThrow(() -> accountsService.createAccount(customerDto));

        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(accountsRepository, times(1)).save(any(Accounts.class));
        ArgumentCaptor<NotificationMsgDto> captor = ArgumentCaptor.forClass(NotificationMsgDto.class);
        verify(streamBridge, times(1)).send(eq("sendCommunication-out-0"), captor.capture());
        assertEquals(NotificationType.ACCOUNT_OPENED, captor.getValue().type());
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
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getAccountsDto().getBalance()));
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
        when(accountsRepository.findByCustomerId(1L)).thenReturn(Optional.of(accounts));

        boolean isDeleted = accountsService.deleteAccount("1234567890");

        assertTrue(isDeleted);
        verify(transactionRepository, times(1)).deleteByAccountNumber(1234567890L);
        verify(accountsRepository, times(1)).deleteByCustomerId(1L);
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    void deposit_Success() {

        accounts.setBalance(new BigDecimal("50.00"));
        when(accountsRepository.findByIdForUpdate(1234567890L)).thenReturn(Optional.of(accounts));
        when(accountsRepository.save(any(Accounts.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        AmountRequestDto request = new AmountRequestDto();
        request.setAccountNumber(1234567890L);
        request.setAmount(new BigDecimal("25.00"));

        AccountsDto result = accountsService.deposit(request);

        assertEquals(0, new BigDecimal("75.00").compareTo(result.getBalance()));
        ArgumentCaptor<Transaction> txnCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txnCaptor.capture());
        assertEquals(TransactionType.DEPOSIT, txnCaptor.getValue().getType());
    }

    @Test
    void withdraw_InsufficientFunds() {

        accounts.setBalance(new BigDecimal("10.00"));
        when(accountsRepository.findByIdForUpdate(1234567890L)).thenReturn(Optional.of(accounts));

        AmountRequestDto request = new AmountRequestDto();
        request.setAccountNumber(1234567890L);
        request.setAmount(new BigDecimal("25.00"));

        assertThrows(InsufficientFundsException.class, () -> accountsService.withdraw(request));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void withdraw_PublishesLowBalanceWhenCrossingThreshold() {

        accounts.setBalance(new BigDecimal("150.00"));
        when(accountsRepository.findByIdForUpdate(1234567890L)).thenReturn(Optional.of(accounts));
        when(accountsRepository.save(any(Accounts.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(streamBridge.send(eq("sendCommunication-out-0"), any())).thenReturn(true);

        AmountRequestDto request = new AmountRequestDto();
        request.setAccountNumber(1234567890L);
        request.setAmount(new BigDecimal("60.00"));

        AccountsDto result = accountsService.withdraw(request);

        assertEquals(0, new BigDecimal("90.00").compareTo(result.getBalance()));
        ArgumentCaptor<NotificationMsgDto> captor = ArgumentCaptor.forClass(NotificationMsgDto.class);
        verify(streamBridge).send(eq("sendCommunication-out-0"), captor.capture());
        assertEquals(NotificationType.LOW_BALANCE, captor.getValue().type());
    }

    @Test
    void transfer_RejectsSelfTransfer() {

        TransferRequestDto request = new TransferRequestDto();
        request.setSourceAccountNumber(1234567890L);
        request.setDestinationAccountNumber(1234567890L);
        request.setAmount(new BigDecimal("10.00"));

        assertThrows(InvalidTransactionException.class, () -> accountsService.transfer(request));
    }

    @Test
    void transfer_Success() {

        Accounts destination = new Accounts();
        destination.setCustomerId(2L);
        destination.setAccountNumber(9876543210L);
        destination.setAccountType(AccountsConstants.SAVINGS);
        destination.setBranchAddress(AccountsConstants.ADDRESS);
        destination.setBalance(new BigDecimal("20.00"));

        Customer destinationCustomer = new Customer();
        destinationCustomer.setCustomerId(2L);
        destinationCustomer.setName("Jane Doe");
        destinationCustomer.setEmail("jane@example.com");
        destinationCustomer.setMobileNumber("0987654321");

        accounts.setBalance(new BigDecimal("200.00"));

        when(accountsRepository.findByIdForUpdate(1234567890L)).thenReturn(Optional.of(accounts));
        when(accountsRepository.findByIdForUpdate(9876543210L)).thenReturn(Optional.of(destination));
        when(accountsRepository.save(any(Accounts.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(destinationCustomer));
        when(streamBridge.send(eq("sendCommunication-out-0"), any())).thenReturn(true);

        TransferRequestDto request = new TransferRequestDto();
        request.setSourceAccountNumber(1234567890L);
        request.setDestinationAccountNumber(9876543210L);
        request.setAmount(new BigDecimal("40.00"));

        AccountsDto result = accountsService.transfer(request);

        assertEquals(0, new BigDecimal("160.00").compareTo(result.getBalance()));
        assertEquals(0, new BigDecimal("60.00").compareTo(destination.getBalance()));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(streamBridge, times(2)).send(eq("sendCommunication-out-0"), any());
    }

    @Test
    void listTransactions_ByAccountNumber() {

        Transaction txn = new Transaction();
        txn.setId(1L);
        txn.setAccountNumber(1234567890L);
        txn.setType(TransactionType.DEPOSIT);
        txn.setAmount(new BigDecimal("10.00"));
        txn.setBalanceAfter(new BigDecimal("10.00"));

        when(accountsRepository.existsById(1234567890L)).thenReturn(true);
        when(transactionRepository.findByAccountNumberOrderByCreatedAtDesc(1234567890L))
                .thenReturn(List.of(txn));

        List<TransactionDto> result = accountsService.listTransactions(1234567890L, null);

        assertEquals(1, result.size());
        assertEquals(TransactionType.DEPOSIT, result.getFirst().getType());
    }

}
