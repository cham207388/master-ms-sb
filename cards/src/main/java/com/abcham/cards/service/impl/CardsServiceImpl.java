package com.abcham.cards.service.impl;

import com.abcham.cards.constants.CardsConstants;
import com.abcham.cards.dto.CardsDto;
import com.abcham.cards.entity.Cards;
import com.abcham.cards.exception.CardAlreadyExistsException;
import com.abcham.cards.exception.ResourceNotFoundException;
import com.abcham.cards.mapper.CardsMapper;
import com.abcham.cards.repository.CardsRepository;
import com.abcham.cards.service.ICardsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@AllArgsConstructor
public class CardsServiceImpl implements ICardsService {

    private CardsRepository cardsRepository;
    
    @Override
    public void createCard(String mobileNumber) {
        log.info("Creating card for mobileNumber: {}", mobileNumber);

        Optional<Cards> optionalCards = cardsRepository.findByMobileNumber(mobileNumber);
        if (optionalCards.isPresent()) {
            log.warn("Card already registered with mobileNumber: {}", mobileNumber);
            throw new CardAlreadyExistsException("Card already registered with given mobileNumber " + mobileNumber);
        }
        Cards savedCard = cardsRepository.save(createNewCard(mobileNumber));
        log.info("Card created successfully with cardNumber: {} for mobileNumber: {}", savedCard.getCardNumber(), mobileNumber);
    }

    private Cards createNewCard(String mobileNumber) {
        Cards newCard = new Cards();
        long randomCardNumber = 100000000000L + new Random().nextInt(900000000);
        newCard.setCardNumber(Long.toString(randomCardNumber));
        newCard.setMobileNumber(mobileNumber);
        newCard.setCardType(CardsConstants.CREDIT_CARD);
        newCard.setTotalLimit(CardsConstants.NEW_CARD_LIMIT);
        newCard.setAmountUsed(0);
        newCard.setAvailableAmount(CardsConstants.NEW_CARD_LIMIT);
        return newCard;
    }

    @Override
    public CardsDto fetchCard(String correlationId, String mobileNumber) {
        log.info("Fetching card details for correlationId: {}, mobileNumber: {}", correlationId, mobileNumber);

        Cards cards = cardsRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Card", "mobileNumber", mobileNumber)
        );
        log.debug("Found card number: {} for mobileNumber: {}", cards.getCardNumber(), mobileNumber);
        return CardsMapper.mapToCardsDto(cards, new CardsDto());
    }

    @Override
    public boolean updateCard(CardsDto cardsDto) {
        log.info("Updating card for cardNumber: {}", cardsDto.getCardNumber());

        Cards cards = cardsRepository.findByCardNumber(cardsDto.getCardNumber()).orElseThrow(
                () -> new ResourceNotFoundException("Card", "CardNumber", cardsDto.getCardNumber()));
        CardsMapper.mapToCards(cardsDto, cards);
        cardsRepository.save(cards);
        log.info("Successfully updated card details for cardNumber: {}", cardsDto.getCardNumber());
        return true;
    }

    @Override
    public boolean deleteCard(String mobileNumber) {
        log.info("Deleting card for mobileNumber: {}", mobileNumber);

        Cards cards = cardsRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Card", "mobileNumber", mobileNumber)
        );
        cardsRepository.deleteById(cards.getCardId());
        log.info("Successfully deleted card for mobileNumber: {}", mobileNumber);
        return true;
    }

}
