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

    /**
     * @param mobileNumber - Mobile Number of the Customer
     */
    @Override
    public void createCard(String mobileNumber) {
        log.debug("Checking if card already exists for mobileNumber: {}", mobileNumber);
        Optional<Cards> optionalCards= cardsRepository.findByMobileNumber(mobileNumber);
        if(optionalCards.isPresent()){
            log.warn("Card creation failed - Card already exists for mobileNumber: {}", mobileNumber);
            throw new CardAlreadyExistsException("Card already registered with given mobileNumber "+mobileNumber);
        }
        Cards savedCard = cardsRepository.save(createNewCard(mobileNumber));
        log.info("Successfully created card number: {} for mobileNumber: {}", savedCard.getCardNumber(), mobileNumber);
    }

    /**
     * @param mobileNumber - Mobile Number of the Customer
     * @return the new card details
     */
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

    /**
     *
     * @param mobileNumber - Input mobile Number
     * @return Card Details based on a given mobileNumber
     */
    @Override
    public CardsDto fetchCard(String mobileNumber) {
        log.debug("Fetching card record from DB for mobileNumber: {}", mobileNumber);
        Cards cards = cardsRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Card", "mobileNumber", mobileNumber)
        );
        log.debug("Successfully found card number: {} for mobileNumber: {}", cards.getCardNumber(), mobileNumber);
        return CardsMapper.mapToCardsDto(cards, new CardsDto());
    }

    /**
     *
     * @param cardsDto - CardsDto Object
     * @return boolean indicating if the update of card details is successful or not
     */
    @Override
    public boolean updateCard(CardsDto cardsDto) {
        log.debug("Updating card details for card number: {}", cardsDto.getCardNumber());
        Cards cards = cardsRepository.findByCardNumber(cardsDto.getCardNumber()).orElseThrow(
                () -> new ResourceNotFoundException("Card", "CardNumber", cardsDto.getCardNumber()));
        CardsMapper.mapToCards(cardsDto, cards);
        cardsRepository.save(cards);
        log.info("Successfully updated card number: {}", cardsDto.getCardNumber());
        return  true;
    }

    /**
     * @param mobileNumber - Input MobileNumber
     * @return boolean indicating if the delete of card details is successful or not
     */
    @Override
    public boolean deleteCard(String mobileNumber) {
        log.debug("Deleting card details for mobileNumber: {}", mobileNumber);
        Cards cards = cardsRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Card", "mobileNumber", mobileNumber)
        );
        cardsRepository.deleteById(cards.getCardId());
        log.info("Successfully deleted card ID: {} for mobileNumber: {}", cards.getCardId(), mobileNumber);
        return true;
    }


}
