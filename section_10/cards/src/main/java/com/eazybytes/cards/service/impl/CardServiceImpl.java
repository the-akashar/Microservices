package com.eazybytes.cards.service.impl;

import com.eazybytes.cards.constants.CardsConstants;
import com.eazybytes.cards.dto.CardsDto;
import com.eazybytes.cards.entity.Cards;
import com.eazybytes.cards.exception.CardAlreadyExistsException;
import com.eazybytes.cards.exception.ResourceNotFoundException;
import com.eazybytes.cards.mapper.CardsMapper;
import com.eazybytes.cards.repository.CardsRepository;
import com.eazybytes.cards.service.ICardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;


@Service
@AllArgsConstructor
public class CardServiceImpl implements ICardService {

    CardsRepository cardsRepository;
    @Override
    public void createCard(String mobileNumber) {
        Optional<Cards> optionalCards = cardsRepository.findByMobileNumber(mobileNumber);
        if (optionalCards.isPresent()){
            throw new CardAlreadyExistsException("Card already registered with given mobileNumber "+mobileNumber);
        }

        cardsRepository.save(createNewCards(mobileNumber));

    }

    @Override
    public CardsDto fetchCard(String mobileNumber) {
        Cards cards = cardsRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Card", "mobileNumber", mobileNumber)
        );
                return CardsMapper.mapToCardsDto(cards,new CardsDto());
    }



    private Cards createNewCards(String mobileNumber){

        Cards newCards = new Cards();
        long randomCardNumber = 100000000000L + new Random().nextInt(900000000);
        newCards.setCardNumber(Long.toString(randomCardNumber));
        newCards.setMobileNumber(mobileNumber);
        newCards.setCardType(CardsConstants.CREDIT_CARD);
        newCards.setTotalLimit(CardsConstants.NEW_CARD_LIMIT);
        newCards.setAmountUsed(0);
        newCards.setAvailableAmount(CardsConstants.NEW_CARD_LIMIT);
        newCards.setCreatedBy("Akash");
        newCards.setCreatedAt(LocalDateTime.now());
        newCards.setUpdatedAt("10-10-2025");
        newCards.setUpdatedBy("Akash");
        return newCards;
    }

    @Override
    public boolean updateCard(CardsDto cardsDto) {
        Cards cards = cardsRepository.findByMobileNumber(cardsDto.getMobileNumber()).orElseThrow(
                ()->new ResourceNotFoundException("Card", "CardNumber", cardsDto.getCardNumber())
        );
        CardsMapper.mapToCards(cardsDto , cards);
        cardsRepository.save(cards);
        return true;
    }

    @Override
    public boolean deleteCard(String mobileNumber) {
        Cards cards = cardsRepository.findByMobileNumber(mobileNumber).orElseThrow(
                ()->new ResourceNotFoundException("Card","mobileNumber",mobileNumber)
        );
        cardsRepository.deleteById(cards.getCardsId());
        return true;
    }


}
