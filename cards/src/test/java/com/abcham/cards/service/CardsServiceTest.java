package com.abcham.cards.service;

import com.abcham.cards.constants.CardsConstants;
import com.abcham.cards.dto.CardsDto;
import com.abcham.cards.entity.Cards;
import com.abcham.cards.exception.CardAlreadyExistsException;
import com.abcham.cards.exception.ResourceNotFoundException;
import com.abcham.cards.repository.CardsRepository;
import com.abcham.cards.service.impl.CardsServiceImpl;
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
class CardsServiceTest {

    @Mock
    private CardsRepository cardsRepository;

    @InjectMocks
    private CardsServiceImpl cardsService;

    private Cards cards;
    private CardsDto cardsDto;
    private String correlationId = "123-abc";

    @BeforeEach
    void setUp() {
        cards = new Cards();
        cards.setCardId(1L);
        cards.setMobileNumber("1234567890");
        cards.setCardNumber("100646930341");
        cards.setCardType(CardsConstants.CREDIT_CARD);
        cards.setTotalLimit(CardsConstants.NEW_CARD_LIMIT);
        cards.setAmountUsed(0);
        cards.setAvailableAmount(CardsConstants.NEW_CARD_LIMIT);

        cardsDto = new CardsDto();
        cardsDto.setMobileNumber("1234567890");
        cardsDto.setCardNumber("100646930341");
        cardsDto.setCardType(CardsConstants.CREDIT_CARD);
        cardsDto.setTotalLimit(CardsConstants.NEW_CARD_LIMIT);
        cardsDto.setAmountUsed(0);
        cardsDto.setAvailableAmount(CardsConstants.NEW_CARD_LIMIT);
    }

    @Test
    void createCard_Success() {
        when(cardsRepository.findByMobileNumber("1234567890")).thenReturn(Optional.empty());
        when(cardsRepository.save(any(Cards.class))).thenReturn(cards);

        assertDoesNotThrow(() -> cardsService.createCard("1234567890"));

        verify(cardsRepository, times(1)).save(any(Cards.class));
    }

    @Test
    void createCard_ThrowsCardAlreadyExistsException() {
        when(cardsRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(cards));

        assertThrows(CardAlreadyExistsException.class, () -> cardsService.createCard("1234567890"));
        verify(cardsRepository, never()).save(any(Cards.class));
    }

    @Test
    void fetchCard_Success() {
        when(cardsRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(cards));

        CardsDto result = cardsService.fetchCard(correlationId,"1234567890");

        assertNotNull(result);
        assertEquals("1234567890", result.getMobileNumber());
        assertEquals("100646930341", result.getCardNumber());
    }

    @Test
    void fetchCard_ThrowsResourceNotFoundException() {
        when(cardsRepository.findByMobileNumber("1234567890")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cardsService.fetchCard(correlationId, "1234567890"));
    }

    @Test
    void updateCard_Success() {
        when(cardsRepository.findByCardNumber("100646930341")).thenReturn(Optional.of(cards));
        when(cardsRepository.save(any(Cards.class))).thenReturn(cards);

        boolean isUpdated = cardsService.updateCard(cardsDto);

        assertTrue(isUpdated);
        verify(cardsRepository, times(1)).save(any(Cards.class));
    }

    @Test
    void deleteCard_Success() {
        when(cardsRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(cards));

        boolean isDeleted = cardsService.deleteCard("1234567890");

        assertTrue(isDeleted);
        verify(cardsRepository, times(1)).deleteById(1L);
    }
}
