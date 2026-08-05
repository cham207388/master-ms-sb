package com.abcham.cards.repository;

import com.abcham.cards.constants.CardsConstants;
import com.abcham.cards.entity.Cards;
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
class CardsRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    private CardsRepository cardsRepository;

    private Cards cards;

    @BeforeEach
    void setUp() {
        cards = new Cards();
        cards.setMobileNumber("1234567890");
        cards.setCardNumber("100646930341");
        cards.setCardType(CardsConstants.CREDIT_CARD);
        cards.setTotalLimit(CardsConstants.NEW_CARD_LIMIT);
        cards.setAmountUsed(0);
        cards.setAvailableAmount(CardsConstants.NEW_CARD_LIMIT);
        cards.setCreatedBy("TestUser");
        cards.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findByMobileNumber_ReturnsCards_WhenExists() {
        cardsRepository.save(cards);

        Optional<Cards> result = cardsRepository.findByMobileNumber("1234567890");

        assertTrue(result.isPresent());
        assertEquals("100646930341", result.get().getCardNumber());
    }

    @Test
    void findByCardNumber_ReturnsCards_WhenExists() {
        cardsRepository.save(cards);

        Optional<Cards> result = cardsRepository.findByCardNumber("100646930341");

        assertTrue(result.isPresent());
        assertEquals("1234567890", result.get().getMobileNumber());
    }
}
