package com.abcham.accounts.controller;

import com.abcham.accounts.constants.AccountsConstants;
import com.abcham.accounts.dto.AccountsDto;
import com.abcham.accounts.dto.AmountRequestDto;
import com.abcham.accounts.dto.CustomerDto;
import com.abcham.accounts.dto.TransactionDto;
import com.abcham.accounts.dto.TransferRequestDto;
import com.abcham.accounts.service.IAccountsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountsController.class)
class AccountsControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private IAccountsService iAccountsService;

    private CustomerDto customerDto;

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
    }

    @Test
    void createAccount_Success() throws Exception {

        doNothing().when(iAccountsService).createAccount(any(CustomerDto.class));

        mockMvc.perform(post("/api/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(AccountsConstants.STATUS_201))
                .andExpect(jsonPath("$.statusMsg").value(AccountsConstants.MESSAGE_201));
    }

    @Test
    void createAccount_ValidationError_InvalidEmail() throws Exception {

        customerDto.setEmail("invalid-email");

        mockMvc.perform(post("/api/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fetchAccountDetails_Success() throws Exception {

        when(iAccountsService.fetchAccount("1234567890")).thenReturn(customerDto);

        mockMvc.perform(get("/api/accounts/fetch")
                        .param("mobileNumber", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.mobileNumber").value("1234567890"))
                .andExpect(jsonPath("$.accountsDto.accountNumber").value(1234567890L));
    }

    @Test
    void updateAccountDetails_Success() throws Exception {

        when(iAccountsService.updateAccount(any(CustomerDto.class))).thenReturn(true);

        mockMvc.perform(put("/api/accounts/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(AccountsConstants.STATUS_200))
                .andExpect(jsonPath("$.statusMsg").value(AccountsConstants.MESSAGE_200));
    }

    @Test
    void updateAccountDetails_Failed() throws Exception {

        when(iAccountsService.updateAccount(any(CustomerDto.class))).thenReturn(false);

        mockMvc.perform(put("/api/accounts/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isExpectationFailed())
                .andExpect(jsonPath("$.statusCode").value(AccountsConstants.STATUS_417));
    }

    @Test
    void deleteAccountDetails_Success() throws Exception {

        when(iAccountsService.deleteAccount("1234567890")).thenReturn(true);

        mockMvc.perform(delete("/api/accounts/delete")
                        .param("mobileNumber", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(AccountsConstants.STATUS_200))
                .andExpect(jsonPath("$.statusMsg").value(AccountsConstants.MESSAGE_200));
    }

    @Test
    void deleteAccountDetails_Failed() throws Exception {

        when(iAccountsService.deleteAccount("1234567890")).thenReturn(false);

        mockMvc.perform(delete("/api/accounts/delete")
                        .param("mobileNumber", "1234567890"))
                .andExpect(status().isExpectationFailed())
                .andExpect(jsonPath("$.statusCode").value(AccountsConstants.STATUS_417));
    }

    @Test
    void deposit_Success() throws Exception {

        AccountsDto result = new AccountsDto();
        result.setAccountNumber(1234567890L);
        result.setAccountType(AccountsConstants.SAVINGS);
        result.setBranchAddress(AccountsConstants.ADDRESS);
        result.setBalance(new BigDecimal("100.00"));
        when(iAccountsService.deposit(any(AmountRequestDto.class))).thenReturn(result);

        AmountRequestDto request = new AmountRequestDto();
        request.setAccountNumber(1234567890L);
        request.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/accounts/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void transfer_Success() throws Exception {

        AccountsDto result = new AccountsDto();
        result.setAccountNumber(1234567890L);
        result.setAccountType(AccountsConstants.SAVINGS);
        result.setBranchAddress(AccountsConstants.ADDRESS);
        result.setBalance(new BigDecimal("60.00"));
        when(iAccountsService.transfer(any(TransferRequestDto.class))).thenReturn(result);

        TransferRequestDto request = new TransferRequestDto();
        request.setSourceAccountNumber(1234567890L);
        request.setDestinationAccountNumber(9876543210L);
        request.setAmount(new BigDecimal("40.00"));

        mockMvc.perform(post("/api/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(60.00));
    }

    @Test
    void listTransactions_Success() throws Exception {

        TransactionDto txn = new TransactionDto();
        txn.setId(1L);
        txn.setAccountNumber(1234567890L);
        txn.setType("DEPOSIT");
        txn.setAmount(new BigDecimal("25.00"));
        when(iAccountsService.listTransactions(eq(1234567890L), isNull())).thenReturn(List.of(txn));

        mockMvc.perform(get("/api/accounts/transactions")
                        .param("accountNumber", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"));
    }

}
