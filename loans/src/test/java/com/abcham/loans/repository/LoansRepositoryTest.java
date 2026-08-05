package com.abcham.loans.repository;

import com.abcham.loans.constants.LoansConstants;
import com.abcham.loans.entity.Loans;
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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class LoansRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    private LoansRepository loansRepository;

    private Loans loans;

    @BeforeEach
    void setUp() {
        loans = new Loans();
        loans.setMobileNumber("1234567890");
        loans.setLoanNumber("548732457654");
        loans.setLoanType(LoansConstants.HOME_LOAN);
        loans.setTotalLoan(LoansConstants.NEW_LOAN_LIMIT);
        loans.setAmountPaid(0);
        loans.setOutstandingAmount(LoansConstants.NEW_LOAN_LIMIT);
        loans.setCreatedBy("TestUser");
        loans.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findByMobileNumber_ReturnsLoans_WhenExists() {
        loansRepository.save(loans);

        Optional<Loans> result = loansRepository.findByMobileNumber("1234567890");

        assertTrue(result.isPresent());
        assertEquals("548732457654", result.get().getLoanNumber());
    }

    @Test
    void findByLoanNumber_ReturnsLoans_WhenExists() {
        loansRepository.save(loans);

        Optional<Loans> result = loansRepository.findByLoanNumber("548732457654");

        assertTrue(result.isPresent());
        assertEquals("1234567890", result.get().getMobileNumber());
    }
}
