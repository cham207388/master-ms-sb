package com.abcham.accounts.repository;

import com.abcham.accounts.constants.AccountsConstants;
import com.abcham.accounts.entity.Accounts;
import com.abcham.accounts.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class AccountsRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer savedCustomer;
    private Accounts accounts;

    @BeforeEach
    void setUp() {

        Customer customer = new Customer();
        customer.setName("Bob Jones");
        customer.setEmail("bob@example.com");
        customer.setMobileNumber("9123456789");
        customer.setCreatedBy("TestUser");
        customer.setCreatedAt(LocalDateTime.now());
        savedCustomer = customerRepository.save(customer);

        accounts = new Accounts();
        accounts.setAccountNumber(1000000001L);
        accounts.setCustomerId(savedCustomer.getCustomerId());
        accounts.setAccountType(AccountsConstants.SAVINGS);
        accounts.setBranchAddress(AccountsConstants.ADDRESS);
        accounts.setCreatedBy("TestUser");
        accounts.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findByCustomerId_ReturnsAccounts_WhenExists() {

        accountsRepository.save(accounts);

        Optional<Accounts> result = accountsRepository.findByCustomerId(savedCustomer.getCustomerId());

        assertTrue(result.isPresent());
        assertEquals(1000000001L, result.get().getAccountNumber());
    }

    @Test
    void deleteByCustomerId_DeletesAccountSuccessfully() {

        accountsRepository.save(accounts);

        accountsRepository.deleteByCustomerId(savedCustomer.getCustomerId());

        Optional<Accounts> result = accountsRepository.findByCustomerId(savedCustomer.getCustomerId());
        assertTrue(result.isEmpty());
    }

}
