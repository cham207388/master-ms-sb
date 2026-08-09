package com.abcham.loans.controller;

import com.abcham.loans.constants.LoansConstants;
import com.abcham.loans.dto.LoansDto;
import com.abcham.loans.service.ILoansService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoansController.class)
class LoansControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ILoansService iLoansService;

    private LoansDto loansDto;

    @BeforeEach
    void setUp() {
        loansDto = new LoansDto();
        loansDto.setMobileNumber("1234567890");
        loansDto.setLoanNumber("548732457654");
        loansDto.setLoanType(LoansConstants.HOME_LOAN);
        loansDto.setTotalLoan(LoansConstants.NEW_LOAN_LIMIT);
        loansDto.setAmountPaid(0);
        loansDto.setOutstandingAmount(LoansConstants.NEW_LOAN_LIMIT);
    }

    @Test
    void createLoan_Success() throws Exception {
        doNothing().when(iLoansService).createLoan("1234567890");

        mockMvc.perform(post("/api/loans/create")
                .param("mobileNumber", "1234567890"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(LoansConstants.STATUS_201))
                .andExpect(jsonPath("$.statusMsg").value(LoansConstants.MESSAGE_201));
    }

    @Test
    void fetchLoanDetails_Success() throws Exception {
        when(iLoansService.fetchLoan("sas8-129s-aqwq-qwq12", "1234567890")).thenReturn(loansDto);

        mockMvc.perform(get("/api/loans/fetch")
                .param("mobileNumber", "1234567890")
                .header("securedbank-correlation-id", "sas8-129s-aqwq-qwq12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mobileNumber").value("1234567890"))
                .andExpect(jsonPath("$.loanNumber").value("548732457654"));
    }

    @Test
    void updateLoanDetails_Success() throws Exception {
        when(iLoansService.updateLoan(any(LoansDto.class))).thenReturn(true);

        mockMvc.perform(put("/api/loans/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loansDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(LoansConstants.STATUS_200));
    }

    @Test
    void updateLoanDetails_Failed() throws Exception {
        when(iLoansService.updateLoan(any(LoansDto.class))).thenReturn(false);

        mockMvc.perform(put("/api/loans/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loansDto)))
                .andExpect(status().isExpectationFailed())
                .andExpect(jsonPath("$.statusCode").value(LoansConstants.STATUS_417));
    }

    @Test
    void deleteLoanDetails_Success() throws Exception {
        when(iLoansService.deleteLoan("1234567890")).thenReturn(true);

        mockMvc.perform(delete("/api/loans/delete")
                .param("mobileNumber", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(LoansConstants.STATUS_200));
    }
}
