package com.abcham.accounts.service;

import com.abcham.accounts.dto.AccountsDto;
import com.abcham.accounts.dto.AmountRequestDto;
import com.abcham.accounts.dto.CustomerDto;
import com.abcham.accounts.dto.TransactionDto;
import com.abcham.accounts.dto.TransferRequestDto;

import java.util.List;

public interface IAccountsService {

    void createAccount(CustomerDto customerDto);

    CustomerDto fetchAccount(String mobileNumber);

    boolean updateAccount(CustomerDto customerDto);

    boolean deleteAccount(String mobileNumber);

    boolean updateCommunicationStatus(Long accountNumber);

    AccountsDto deposit(AmountRequestDto request);

    AccountsDto withdraw(AmountRequestDto request);

    AccountsDto transfer(TransferRequestDto request);

    List<TransactionDto> listTransactions(Long accountNumber, String mobileNumber);
}
