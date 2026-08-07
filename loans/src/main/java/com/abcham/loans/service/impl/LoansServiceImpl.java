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

    /**
     * @param mobileNumber - Mobile Number of the Customer
     */
    @Override
    public void createLoan(String mobileNumber) {
        log.debug("Checking if loan already exists for mobileNumber: {}", mobileNumber);
        Optional<Loans> optionalLoans= loansRepository.findByMobileNumber(mobileNumber);
        if(optionalLoans.isPresent()){
            log.warn("Loan creation failed - Loan already exists for mobileNumber: {}", mobileNumber);
            throw new LoanAlreadyExistsException("Loan already registered with given mobileNumber "+mobileNumber);
        }
        Loans savedLoan = loansRepository.save(createNewLoan(mobileNumber));
        log.info("Successfully created loan number: {} for mobileNumber: {}", savedLoan.getLoanNumber(), mobileNumber);
    }

    /**
     * @param mobileNumber - Mobile Number of the Customer
     * @return the new loan details
     */
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

    /**
     *
     * @param mobileNumber - Input mobile Number
     * @return Loan Details based on a given mobileNumber
     */
    @Override
    public LoansDto fetchLoan(String mobileNumber) {
        log.debug("Fetching loan record from DB for mobileNumber: {}", mobileNumber);
        Loans loans = loansRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Loan", "mobileNumber", mobileNumber)
        );
        log.debug("Successfully found loan number: {} for mobileNumber: {}", loans.getLoanNumber(), mobileNumber);
        return LoansMapper.mapToLoansDto(loans, new LoansDto());
    }

    /**
     *
     * @param loansDto - LoansDto Object
     * @return boolean indicating if the update of loan details is successful or not
     */
    @Override
    public boolean updateLoan(LoansDto loansDto) {
        log.debug("Updating loan details for loan number: {}", loansDto.getLoanNumber());
        Loans loans = loansRepository.findByLoanNumber(loansDto.getLoanNumber()).orElseThrow(
                () -> new ResourceNotFoundException("Loan", "LoanNumber", loansDto.getLoanNumber()));
        LoansMapper.mapToLoans(loansDto, loans);
        loansRepository.save(loans);
        log.info("Successfully updated loan number: {}", loansDto.getLoanNumber());
        return  true;
    }

    /**
     * @param mobileNumber - Input MobileNumber
     * @return boolean indicating if the delete of loan details is successful or not
     */
    @Override
    public boolean deleteLoan(String mobileNumber) {
        log.debug("Deleting loan details for mobileNumber: {}", mobileNumber);
        Loans loans = loansRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Loan", "mobileNumber", mobileNumber)
        );
        loansRepository.deleteById(loans.getLoanId());
        log.info("Successfully deleted loan ID: {} for mobileNumber: {}", loans.getLoanId(), mobileNumber);
        return true;
    }


}
