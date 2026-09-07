package com.abcham.accounts.service.impl;

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
import com.abcham.accounts.mapper.AccountsMapper;
import com.abcham.accounts.mapper.CustomerMapper;
import com.abcham.accounts.repository.AccountsRepository;
import com.abcham.accounts.repository.CustomerRepository;
import com.abcham.accounts.repository.TransactionRepository;
import com.abcham.accounts.service.IAccountsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountsServiceImpl implements IAccountsService {

    private final AccountsRepository accountsRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final StreamBridge streamBridge;

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
        publishNotification(NotificationType.ACCOUNT_OPENED, savedAccount, savedCustomer, null, null, null);
    }

    private Accounts createNewAccount(Customer customer) {

        Accounts newAccount = new Accounts();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);
        newAccount.setBalance(BigDecimal.ZERO);
        newAccount.setCommunicationSw(false);
        return newAccount;
    }

    private void publishNotification(NotificationType type, Accounts account, Customer customer,
                                     BigDecimal amount, BigDecimal balance, Long counterpartyAccount) {

        var msg = new NotificationMsgDto(
                type,
                account.getAccountNumber(),
                customer.getName(),
                customer.getEmail(),
                customer.getMobileNumber(),
                amount,
                balance,
                counterpartyAccount
        );
        // Publish only after successful commit so emails are not sent for rolled-back money moves.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendNotification(msg);
                }
            });
        } else {
            sendNotification(msg);
        }
    }

    private void sendNotification(NotificationMsgDto msg) {
        log.info("Sending notification {}: {}", msg.type(), msg);
        var result = streamBridge.send("sendCommunication-out-0", msg);
        log.info("Notification {} triggered successfully? {}", msg.type(), result);
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
    @Transactional
    public boolean deleteAccount(String mobileNumber) {

        log.info("Deleting account for mobileNumber: {}", mobileNumber);

        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts account = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );
        transactionRepository.deleteByAccountNumber(account.getAccountNumber());
        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        log.info("Successfully deleted account and customer with customerId: {} for mobileNumber: {}",
                customer.getCustomerId(), mobileNumber);
        return true;
    }

    @Override
    public boolean updateCommunicationStatus(Long accountNumber) {

        boolean isUpdated = false;
        if (accountNumber != null) {
            Accounts accounts = accountsRepository.findById(accountNumber).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "AccountNumber", accountNumber.toString())
            );
            accounts.setCommunicationSw(true);
            accountsRepository.save(accounts);
            isUpdated = true;
        }
        return isUpdated;
    }

    @Override
    @Transactional
    public AccountsDto deposit(AmountRequestDto request) {

        Accounts account = lockAccount(request.getAccountNumber());
        BigDecimal amount = request.getAmount();
        account.setBalance(account.getBalance().add(amount));
        accountsRepository.save(account);
        saveTransaction(account, TransactionType.DEPOSIT, amount, null, "Deposit");
        log.info("Deposited {} to account {}", amount, account.getAccountNumber());
        return AccountsMapper.mapToAccountsDto(account, new AccountsDto());
    }

    @Override
    @Transactional
    public AccountsDto withdraw(AmountRequestDto request) {

        Accounts account = lockAccount(request.getAccountNumber());
        BigDecimal previous = account.getBalance();
        BigDecimal amount = request.getAmount();
        ensureSufficientFunds(previous, amount);
        account.setBalance(previous.subtract(amount));
        accountsRepository.save(account);
        saveTransaction(account, TransactionType.WITHDRAWAL, amount, null, "Withdrawal");
        maybePublishLowBalance(account, previous);
        log.info("Withdrew {} from account {}", amount, account.getAccountNumber());
        return AccountsMapper.mapToAccountsDto(account, new AccountsDto());
    }

    @Override
    @Transactional
    public AccountsDto transfer(TransferRequestDto request) {

        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new InvalidTransactionException("Source and destination accounts must be different");
        }

        Long firstLock = Math.min(request.getSourceAccountNumber(), request.getDestinationAccountNumber());
        Long secondLock = Math.max(request.getSourceAccountNumber(), request.getDestinationAccountNumber());
        Accounts first = lockAccount(firstLock);
        Accounts second = lockAccount(secondLock);

        Accounts source = first.getAccountNumber().equals(request.getSourceAccountNumber()) ? first : second;
        Accounts destination = source == first ? second : first;

        BigDecimal previousSource = source.getBalance();
        BigDecimal amount = request.getAmount();
        ensureSufficientFunds(previousSource, amount);

        source.setBalance(previousSource.subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));
        accountsRepository.save(source);
        accountsRepository.save(destination);

        saveTransaction(source, TransactionType.TRANSFER_OUT, amount, destination.getAccountNumber(), "Transfer out");
        saveTransaction(destination, TransactionType.TRANSFER_IN, amount, source.getAccountNumber(), "Transfer in");

        Customer sourceCustomer = customerRepository.findById(source.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "CustomerID", source.getCustomerId().toString())
        );
        Customer destinationCustomer = customerRepository.findById(destination.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "CustomerID", destination.getCustomerId().toString())
        );

        publishNotification(NotificationType.TRANSFER_COMPLETED, source, sourceCustomer, amount,
                source.getBalance(), destination.getAccountNumber());
        publishNotification(NotificationType.TRANSFER_COMPLETED, destination, destinationCustomer, amount,
                destination.getBalance(), source.getAccountNumber());
        maybePublishLowBalance(source, previousSource);

        log.info("Transferred {} from {} to {}", amount, source.getAccountNumber(), destination.getAccountNumber());
        return AccountsMapper.mapToAccountsDto(source, new AccountsDto());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> listTransactions(Long accountNumber, String mobileNumber) {

        Long resolvedAccountNumber = accountNumber;
        if (resolvedAccountNumber == null) {
            if (mobileNumber == null || mobileNumber.isBlank()) {
                throw new InvalidTransactionException("Provide accountNumber or mobileNumber");
            }
            Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                    () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
            );
            Accounts account = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
            );
            resolvedAccountNumber = account.getAccountNumber();
        } else if (!accountsRepository.existsById(resolvedAccountNumber)) {
            throw new ResourceNotFoundException("Account", "AccountNumber", resolvedAccountNumber.toString());
        }

        return transactionRepository.findByAccountNumberOrderByCreatedAtDesc(resolvedAccountNumber).stream()
                .map(this::toTransactionDto)
                .toList();
    }

    private Accounts lockAccount(Long accountNumber) {
        return accountsRepository.findByIdForUpdate(accountNumber).orElseThrow(
                () -> new ResourceNotFoundException("Account", "AccountNumber", accountNumber.toString())
        );
    }

    private void ensureSufficientFunds(BigDecimal balance, BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds: available=" + balance + ", requested=" + amount);
        }
    }

    private void saveTransaction(Accounts account, String type, BigDecimal amount,
                                 Long counterpartyAccount, String description) {
        Transaction txn = new Transaction();
        txn.setAccountNumber(account.getAccountNumber());
        txn.setType(type);
        txn.setAmount(amount);
        txn.setBalanceAfter(account.getBalance());
        txn.setCounterpartyAccount(counterpartyAccount);
        txn.setDescription(description);
        transactionRepository.save(txn);
    }

    private void maybePublishLowBalance(Accounts account, BigDecimal previousBalance) {
        BigDecimal threshold = AccountsConstants.LOW_BALANCE_THRESHOLD;
        if (previousBalance.compareTo(threshold) >= 0 && account.getBalance().compareTo(threshold) < 0) {
            Customer customer = customerRepository.findById(account.getCustomerId()).orElseThrow(
                    () -> new ResourceNotFoundException("Customer", "CustomerID", account.getCustomerId().toString())
            );
            publishNotification(NotificationType.LOW_BALANCE, account, customer, null, account.getBalance(), null);
        }
    }

    private TransactionDto toTransactionDto(Transaction txn) {
        TransactionDto dto = new TransactionDto();
        dto.setId(txn.getId());
        dto.setAccountNumber(txn.getAccountNumber());
        dto.setType(txn.getType());
        dto.setAmount(txn.getAmount());
        dto.setBalanceAfter(txn.getBalanceAfter());
        dto.setCounterpartyAccount(txn.getCounterpartyAccount());
        dto.setDescription(txn.getDescription());
        dto.setCreatedAt(txn.getCreatedAt());
        return dto;
    }

}
