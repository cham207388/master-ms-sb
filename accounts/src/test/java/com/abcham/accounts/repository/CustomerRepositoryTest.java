package com.abcham.accounts.repository;

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
class CustomerRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {

        customer = new Customer();
        customer.setName("Alice Smith");
        customer.setEmail("alice@example.com");
        customer.setMobileNumber("9876543210");
        customer.setCreatedBy("TestUser");
        customer.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findByMobileNumber_ReturnsCustomer_WhenExists() {

        customerRepository.save(customer);

        Optional<Customer> result = customerRepository.findByMobileNumber("9876543210");

        assertTrue(result.isPresent());
        assertEquals("Alice Smith", result.get().getName());
    }

    @Test
    void findByMobileNumber_ReturnsEmpty_WhenNotExists() {

        Optional<Customer> result = customerRepository.findByMobileNumber("0000000000");

        assertTrue(result.isEmpty());
    }

}
