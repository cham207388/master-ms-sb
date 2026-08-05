package com.abcham.cards.controller;

import com.abcham.cards.constants.CardsConstants;
import com.abcham.cards.dto.CardsDto;
import com.abcham.cards.service.ICardsService;
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

@WebMvcTest(CardsController.class)
class CardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ICardsService iCardsService;

    private CardsDto cardsDto;

    @BeforeEach
    void setUp() {
        cardsDto = new CardsDto();
        cardsDto.setMobileNumber("1234567890");
        cardsDto.setCardNumber("100646930341");
        cardsDto.setCardType(CardsConstants.CREDIT_CARD);
        cardsDto.setTotalLimit(CardsConstants.NEW_CARD_LIMIT);
        cardsDto.setAmountUsed(0);
        cardsDto.setAvailableAmount(CardsConstants.NEW_CARD_LIMIT);
    }

    @Test
    void createCard_Success() throws Exception {
        doNothing().when(iCardsService).createCard("1234567890");

        mockMvc.perform(post("/api/create")
                        .param("mobileNumber", "1234567890"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(CardsConstants.STATUS_201))
                .andExpect(jsonPath("$.statusMsg").value(CardsConstants.MESSAGE_201));
    }

    @Test
    void fetchCardDetails_Success() throws Exception {
        when(iCardsService.fetchCard("1234567890")).thenReturn(cardsDto);

        mockMvc.perform(get("/api/fetch")
                        .param("mobileNumber", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mobileNumber").value("1234567890"))
                .andExpect(jsonPath("$.cardNumber").value("100646930341"));
    }

    @Test
    void updateCardDetails_Success() throws Exception {
        when(iCardsService.updateCard(any(CardsDto.class))).thenReturn(true);

        mockMvc.perform(put("/api/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardsDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(CardsConstants.STATUS_200));
    }

    @Test
    void updateCardDetails_Failed() throws Exception {
        when(iCardsService.updateCard(any(CardsDto.class))).thenReturn(false);

        mockMvc.perform(put("/api/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardsDto)))
                .andExpect(status().isExpectationFailed())
                .andExpect(jsonPath("$.statusCode").value(CardsConstants.STATUS_417));
    }

    @Test
    void deleteCardDetails_Success() throws Exception {
        when(iCardsService.deleteCard("1234567890")).thenReturn(true);

        mockMvc.perform(delete("/api/delete")
                        .param("mobileNumber", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(CardsConstants.STATUS_200));
    }
}
