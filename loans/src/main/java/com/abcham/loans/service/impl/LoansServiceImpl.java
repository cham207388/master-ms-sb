package com.abcham.loans.service.impl;

import com.abcham.loans.constants.LoansConstants;
import com.abcham.loans.dto.LoansDto;
import com.abcham.loans.entity.Loans;
import com.abcham.loans.exception.LoanAlreadyExistsException;
import com.abcham.loans.exception.ResourceNotFoundException;
import com.abcham.loans.mapper.LoansMapper;
import com.abcham.loans.repository.LoansRepository;
import com.abcham.loans.service.ILoansService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@AllArgsConstructor
public class LoansServiceImpl implements ILoansService {

    private LoansRepository loansRepository;

    @Override
    public void createLoan(String mobileNumber) {
        log.info("Creating loan for mobileNumber: {}", mobileNumber);

        Optional<Loans> optionalLoans = loansRepository.findByMobileNumber(mobileNumber);
        if (optionalLoans.isPresent()) {
            log.warn("Loan already registered with mobileNumber: {}", mobileNumber);
            throw new LoanAlreadyExistsException("Loan already registered with given mobileNumber " + mobileNumber);
        }
        Loans savedLoan = loansRepository.save(createNewLoan(mobileNumber));
        log.info("Loan created successfully with loanNumber: {} for mobileNumber: {}", savedLoan.getLoanNumber(), mobileNumber);
    }

    private Loans createNewLoan(String mobileNumber) {
        Loans newLoan = new Loans();
        long randomLoanNumber = 100000000000L + new Random().nextInt(900000000);
        newLoan.setLoanNumber(Long.toString(randomLoanNumber));
        newLoan.setMobileNumber(mobileNumber);
        newLoan.setLoanType(LoansConstants.HOME_LOAN);
        newLoan.setTotalLoan(LoansConstants.NEW_LOAN_LIMIT);
        newLoan.setAmountPaid(0);
        newLoan.setOutstandingAmount(LoansConstants.NEW_LOAN_LIMIT);
        return newLoan;
    }

    @Override
    public LoansDto fetchLoan(String correlationId, String mobileNumber) {
        log.info("Fetching loan details for correlationId: {}, mobileNumber: {}", correlationId, mobileNumber);

        Loans loans = loansRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Loan", "mobileNumber", mobileNumber));
        log.debug("Found loan number: {} for mobileNumber: {}", loans.getLoanNumber(), mobileNumber);
        return LoansMapper.mapToLoansDto(loans, new LoansDto());
    }

    @Override
    public boolean updateLoan(LoansDto loansDto) {
        log.info("Updating loan for loanNumber: {}", loansDto.getLoanNumber());

        Loans loans = loansRepository.findByLoanNumber(loansDto.getLoanNumber()).orElseThrow(
                () -> new ResourceNotFoundException("Loan", "LoanNumber", loansDto.getLoanNumber()));
        LoansMapper.mapToLoans(loansDto, loans);
        loansRepository.save(loans);
        log.info("Successfully updated loan details for loanNumber: {}", loansDto.getLoanNumber());
        return true;
    }

    /**
     * @param mobileNumber - Input MobileNumber
     * @return boolean indicating if the delete of loan details is successful or not
     */
    @Override
    public boolean deleteLoan(String mobileNumber) {
        log.info("Deleting loan for mobileNumber: {}", mobileNumber);

        Loans loans = loansRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Loan", "mobileNumber", mobileNumber));
        loansRepository.deleteById(loans.getLoanId());
        log.info("Successfully deleted loan for mobileNumber: {}", mobileNumber);
        return true;
    }

}
