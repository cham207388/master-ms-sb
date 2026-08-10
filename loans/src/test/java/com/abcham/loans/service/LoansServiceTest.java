package com.abcham.loans.service;

import com.abcham.loans.constants.LoansConstants;
import com.abcham.loans.dto.LoansDto;
import com.abcham.loans.entity.Loans;
import com.abcham.loans.exception.LoanAlreadyExistsException;
import com.abcham.loans.exception.ResourceNotFoundException;
import com.abcham.loans.repository.LoansRepository;
import com.abcham.loans.service.impl.LoansServiceImpl;
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
class LoansServiceTest {

    @Mock
    private LoansRepository loansRepository;

    @InjectMocks
    private LoansServiceImpl loansService;

    private Loans loans;
    private LoansDto loansDto;

    @BeforeEach
    void setUp() {
        loans = new Loans();
        loans.setLoanId(1L);
        loans.setMobileNumber("1234567890");
        loans.setLoanNumber("548732457654");
        loans.setLoanType(LoansConstants.HOME_LOAN);
        loans.setTotalLoan(LoansConstants.NEW_LOAN_LIMIT);
        loans.setAmountPaid(0);
        loans.setOutstandingAmount(LoansConstants.NEW_LOAN_LIMIT);

        loansDto = new LoansDto();
        loansDto.setMobileNumber("1234567890");
        loansDto.setLoanNumber("548732457654");
        loansDto.setLoanType(LoansConstants.HOME_LOAN);
        loansDto.setTotalLoan(LoansConstants.NEW_LOAN_LIMIT);
        loansDto.setAmountPaid(0);
        loansDto.setOutstandingAmount(LoansConstants.NEW_LOAN_LIMIT);
    }

    @Test
    void createLoan_Success() {
        when(loansRepository.findByMobileNumber("1234567890")).thenReturn(Optional.empty());
        when(loansRepository.save(any(Loans.class))).thenReturn(loans);

        assertDoesNotThrow(() -> loansService.createLoan("1234567890"));

        verify(loansRepository, times(1)).save(any(Loans.class));
    }

    @Test
    void createLoan_ThrowsLoanAlreadyExistsException() {
        when(loansRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(loans));

        assertThrows(LoanAlreadyExistsException.class, () -> loansService.createLoan("1234567890"));
        verify(loansRepository, never()).save(any(Loans.class));
    }

    @Test
    void fetchLoan_Success() {
        when(loansRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(loans));

        LoansDto result = loansService.fetchLoan("1234567890");

        assertNotNull(result);
        assertEquals("1234567890", result.getMobileNumber());
        assertEquals("548732457654", result.getLoanNumber());
    }

    @Test
    void fetchLoan_ThrowsResourceNotFoundException() {
        when(loansRepository.findByMobileNumber("1234567890")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loansService.fetchLoan("1234567890"));
    }

    @Test
    void updateLoan_Success() {
        when(loansRepository.findByLoanNumber("548732457654")).thenReturn(Optional.of(loans));
        when(loansRepository.save(any(Loans.class))).thenReturn(loans);

        boolean isUpdated = loansService.updateLoan(loansDto);

        assertTrue(isUpdated);
        verify(loansRepository, times(1)).save(any(Loans.class));
    }

    @Test
    void deleteLoan_Success() {
        when(loansRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(loans));

        boolean isDeleted = loansService.deleteLoan("1234567890");

        assertTrue(isDeleted);
        verify(loansRepository, times(1)).deleteById(1L);
    }
}
