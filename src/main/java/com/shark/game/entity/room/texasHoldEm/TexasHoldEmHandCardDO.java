package com.shark.game.entity.room.texasHoldEm;

import com.google.gson.Gson;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
public class TexasHoldEmHandCardDO {

    private final int CARD_TYPE_HIGH_CARD = 0, CARD_TYPE_ONE_PAIR = 1, CARD_TYPE_TWO_PAIR = 2, CARD_TYPE_THREE_OF_KIND = 3,
            CARD_TYPE_STRAIGHT = 4, CARD_TYPE_FLUSH = 5, CARD_TYPE_FULL_HOUSE = 6, CARD_TYPE_FOUR_OF_KIND = 7,
            CARD_TYPE_STRAIGHT_FLUSH = 8, CARD_TYPE_ROYAL_STRAIGHT_FLUSH = 9;

    private int cardType;

    private List<Integer> publicCardList = new ArrayList<>();
    private List<Integer> handCardList = new ArrayList<>();
    private List<Integer> allCardList = new ArrayList<>();

    private HashMap<Integer, List<Integer>> colorCardNumberListMap = new HashMap<>();
    private HashMap<Integer, List<Integer>> numberCardColorListMap = new HashMap<>();

    private Integer firstCompareNumber = -1;
    private Integer secondCompareNumber = -1;
    private Integer thirdCompareNumber = -1;
    private Integer fourthCompareNumber = -1;
    private Integer fifthCompareNumber = -1;

    public void addPublicCard(List<Integer> cardList) {
        publicCardList.addAll(cardList);
        allCardList.addAll(cardList);
        handleColorAndNumberMap(allCardList);
        checkCardType();
    }

    public void addHandCard(List<Integer> cardList) {
        handCardList.addAll(cardList);
        allCardList.addAll(cardList);
        handleColorAndNumberMap(allCardList);
        if (checkCardTypeOnePair()) {
            return;
        }
        checkCardTypeHighCard();
    }

    private void handleColorAndNumberMap(List<Integer> cardList) {
        colorCardNumberListMap.clear();
        numberCardColorListMap.clear();
        for (Integer card : cardList) {
            //[0,1,2,3,...51]
            //[梅花2,梅花3,梅花4,梅花5,...黑桃A]

            int color = card / 13;
            int number = card % 13;
            List<Integer> colorCardNumberList = colorCardNumberListMap.get(color);
            if (colorCardNumberList == null) {
                colorCardNumberList = new ArrayList<>();
                colorCardNumberListMap.put(color, colorCardNumberList);
            }
            colorCardNumberList.add(number);

            List<Integer> numberCardColorList = numberCardColorListMap.get(number);
            if (numberCardColorList == null) {
                numberCardColorList = new ArrayList<>();
                numberCardColorListMap.put(number, numberCardColorList);
            }

            numberCardColorList.add(color);
        }
    }

    private void checkCardType() {
        if (checkCardTypeRoyalStraightFlush()) {
            return;
        }
        if (checkCardTypeStraightFlush()) {
            return;
        }
        if (checkCardTypeFourOfKind()) {
            return;
        }
        if (checkCardTypeFullHouse()) {
            return;
        }
        if (checkCardTypeFlush()) {
            return;
        }
        if (checkCardTypeStraight()) {
            return;
        }
        if (checkCardTypeThreeOfKind()) {
            return;
        }
        if (checkCardTypeTwoPair()) {
            return;
        }
        if (checkCardTypeOnePair()) {
            return;
        }
        checkCardTypeHighCard();
    }

    private boolean checkCardTypeRoyalStraightFlush() {
        firstCompareNumber = -1;
        secondCompareNumber = -1;
        thirdCompareNumber = -1;
        fourthCompareNumber = -1;
        fifthCompareNumber = -1;
        List<Integer> royalStraightFlushCardNumberList = List.of(12, 11, 10, 9, 8);
        for (Integer color : colorCardNumberListMap.keySet()) {
            List<Integer> cardNumberList = colorCardNumberListMap.get(color);
            if (cardNumberList == null && cardNumberList.size() < 5) {
                continue;
            }
            int count = 0;
            for (Integer cardNumber : royalStraightFlushCardNumberList) {
                if (cardNumberList.contains(cardNumber)) {
                    count = count + 1;
                }
            }
            if (count == 5) {
                cardType = CARD_TYPE_ROYAL_STRAIGHT_FLUSH;
                return true;
            }
        }
        return false;
    }

    private boolean checkCardTypeStraightFlush() {
        firstCompareNumber = -1;
        secondCompareNumber = -1;
        thirdCompareNumber = -1;
        fourthCompareNumber = -1;
        fifthCompareNumber = -1;
        for (Integer color : colorCardNumberListMap.keySet()) {
            List<Integer> cardNumberList = colorCardNumberListMap.get(color);
            if (cardNumberList == null || cardNumberList.size() < 5) {
                continue;
            }
            int count;
            for (int maxNumber = 11; maxNumber > 3; maxNumber--) {
                count = 0;
                for (int i = 0; i < 5; i++) {
                    Integer cardNumber = maxNumber - i;
                    if (cardNumberList.contains(cardNumber)) {
                        count = count + 1;
                    } else {
                        break;
                    }
                }
                if (count == 5) {
                    cardType = CARD_TYPE_STRAIGHT_FLUSH;
                    firstCompareNumber = maxNumber;
                    return true;
                }
            }
            //5, 4, 3, 2, 1
            count = 0;
            for (Integer cardNumber : List.of(3, 2, 1, 0, 12)) {
                if (cardNumberList.contains(cardNumber)) {
                    count = count + 1;
                }
            }
            if (count == 5) {
                cardType = CARD_TYPE_STRAIGHT_FLUSH;
                firstCompareNumber = 3;
                return true;
            }
        }
        return false;
    }

    private boolean checkCardTypeFourOfKind() {
        firstCompareNumber = -1;
        secondCompareNumber = -1;
        thirdCompareNumber = -1;
        fourthCompareNumber = -1;
        fifthCompareNumber = -1;

        for (int number = 12; number >= 0; number--) {
            List<Integer> cardColorList = numberCardColorListMap.get(number);
            if (cardColorList == null) {
                continue;
            }

            if (firstCompareNumber == -1 && cardColorList.size() == 4) {
                cardType = CARD_TYPE_FOUR_OF_KIND;
                firstCompareNumber = number;
                continue;
            }

            if (secondCompareNumber == -1) {
                secondCompareNumber = number;
                continue;
            }
        }

        return firstCompareNumber != -1;
    }

    private boolean checkCardTypeFullHouse() {
        firstCompareNumber = -1;
        secondCompareNumber = -1;
        thirdCompareNumber = -1;
        fourthCompareNumber = -1;
        fifthCompareNumber = -1;

        for (int number = 12; number >= 0; number--) {
            List<Integer> cardColorList = numberCardColorListMap.get(number);
            if (cardColorList == null) {
                continue;
            }
            if (firstCompareNumber == -1 && cardColorList.size() == 3) {
                firstCompareNumber = number;
                continue;
            }
            if (secondCompareNumber == -1 && cardColorList.size() == 2) {
                secondCompareNumber = number;
                continue;
            }
        }

        if ((firstCompareNumber != -1) && (secondCompareNumber != -1)) {
            cardType = CARD_TYPE_FULL_HOUSE;
            return true;
        }
        return false;
    }

    private boolean checkCardTypeFlush() {
        firstCompareNumber = -1;
        secondCompareNumber = -1;
        thirdCompareNumber = -1;
        fourthCompareNumber = -1;
        fifthCompareNumber = -1;
        for (Integer color : colorCardNumberListMap.keySet()) {
            List<Integer> cardNumberList = colorCardNumberListMap.get(color);
            if (cardNumberList == null) {
                continue;
            }
            if (cardNumberList.size() >= 5) {
                cardType = CARD_TYPE_FLUSH;
                cardNumberList.sort((value1, value2) -> {
                    if (value1 > value2) {
                        return -1;
                    } else if (value1.intValue() == value2.intValue()) {
                        return 0;
                    } else {
                        return 1;
                    }
                });
                for (int i = 0; i < 5; i++) {
                    switch (i) {
                        case 0:
                            firstCompareNumber = cardNumberList.get(i);
                            break;
                        case 1:
                            secondCompareNumber = cardNumberList.get(i);
                            break;
                        case 2:
                            thirdCompareNumber = cardNumberList.get(i);
                            break;
                        case 3:
                            fourthCompareNumber = cardNumberList.get(i);
                            break;
                        case 4:
                            fifthCompareNumber = cardNumberList.get(i);
                            break;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean checkCardTypeStraight() {
        firstCompareNumber = -1;
        secondCompareNumber = -1;
        thirdCompareNumber = -1;
        fourthCompareNumber = -1;
        fifthCompareNumber = -1;
        int count;

        for (int number = 12; number > 3; number--) {
            count = 0;
            for (int i = 0; i < 5; i++) {
                Integer cardNumber = number - i;
                if (numberCardColorListMap.get(cardNumber) != null) {
                    count = count + 1;
                } else {
                    break;
                }
            }
            if (count == 5) {
                cardType = CARD_TYPE_STRAIGHT;
                firstCompareNumber = number;
                return true;
            }
        }

        if (firstCompareNumber == -1 &&
                (numberCardColorListMap.get(12) != null) &&
                (numberCardColorListMap.get(0) != null) &&
                (numberCardColorListMap.get(1) != null) &&
                (numberCardColorListMap.get(2) != null) &&
                (numberCardColorListMap.get(3) != null)) {
            //1 2 3 4 5
            cardType = CARD_TYPE_STRAIGHT;
            firstCompareNumber = 3;
            return true;
        }

        return false;
    }

    private boolean checkCardTypeThreeOfKind() {
        firstCompareNumber = -1;
        secondCompareNumber = -1;
        thirdCompareNumber = -1;
        fourthCompareNumber = -1;
        fifthCompareNumber = -1;
        for (int number = 12; number >= 0; number--) {
            List<Integer> cardColorList = numberCardColorListMap.get(number);
            if (cardColorList == null) {
                continue;
            }
            if (cardColorList.size() == 3 && firstCompareNumber == -1) {
                cardType = CARD_TYPE_THREE_OF_KIND;
                firstCompareNumber = number;
                continue;
            }
            if (cardColorList.size() == 1 && secondCompareNumber == -1) {
                secondCompareNumber = number;
                continue;
            }
            if (cardColorList.size() == 1 && thirdCompareNumber == -1) {
                thirdCompareNumber = number;
                continue;
            }

        }

        return firstCompareNumber != -1;
    }

    private boolean checkCardTypeTwoPair() {
        firstCompareNumber = -1;
        secondCompareNumber = -1;
        thirdCompareNumber = -1;
        fourthCompareNumber = -1;
        fifthCompareNumber = -1;
        for (int number = 12; number >= 0; number--) {
            List<Integer> cardColorList = numberCardColorListMap.get(number);
            if (cardColorList == null) {
                continue;
            }
            if (cardColorList.size() == 2) {
                if (firstCompareNumber == -1) {
                    firstCompareNumber = number;
                    continue;
                }
                if (secondCompareNumber == -1) {
                    secondCompareNumber = number;
                    continue;
                }
            }
            if (cardColorList.size() == 1 && thirdCompareNumber == -1) {
                thirdCompareNumber = number;
                continue;
            }
        }

        if (firstCompareNumber != -1 && secondCompareNumber != -1) {
            cardType = CARD_TYPE_TWO_PAIR;
            return true;
        }
        return false;
    }

    private boolean checkCardTypeOnePair() {
        firstCompareNumber = -1;
        secondCompareNumber = -1;
        thirdCompareNumber = -1;
        fourthCompareNumber = -1;
        fifthCompareNumber = -1;
        for (int number = 12; number >= 0; number--) {
            List<Integer> cardColorList = numberCardColorListMap.get(number);
            if (cardColorList == null) {
                continue;
            }
            if (cardColorList.size() == 2 && firstCompareNumber == -1) {
                cardType = CARD_TYPE_ONE_PAIR;
                firstCompareNumber = number;
                continue;
            }
            if (cardColorList.size() == 1 && secondCompareNumber == -1) {
                secondCompareNumber = number;
                continue;
            }
            if (cardColorList.size() == 1 && thirdCompareNumber == -1) {
                thirdCompareNumber = number;
                continue;
            }
            if (cardColorList.size() == 1 && fourthCompareNumber == -1) {
                fourthCompareNumber = number;
                continue;
            }
        }

        return (firstCompareNumber != -1);
    }

    private void checkCardTypeHighCard() {
        firstCompareNumber = -1;
        secondCompareNumber = -1;
        thirdCompareNumber = -1;
        fourthCompareNumber = -1;
        fifthCompareNumber = -1;

        for (int number = 12; number >= 0; number--) {
            List<Integer> cardColorList = numberCardColorListMap.get(number);
            if (cardColorList == null) {
                continue;
            }
            if (firstCompareNumber == -1) {
                firstCompareNumber = number;
            }
            if (secondCompareNumber == -1) {
                secondCompareNumber = number;
            }
            if (thirdCompareNumber == -1) {
                thirdCompareNumber = number;
            }
            if (fourthCompareNumber == -1) {
                fourthCompareNumber = number;
            }
            if (fifthCompareNumber == -1) {
                fifthCompareNumber = number;
            }
        }

        cardType = CARD_TYPE_HIGH_CARD;
    }

    public int compare(TexasHoldEmHandCardDO card) {
        if(cardType > card.getCardType()) {
            return 1;
        } else if(cardType < card.getCardType()) {
            return -1;
        }

        if (firstCompareNumber > card.getFirstCompareNumber()) {
            return 1;
        } else if (firstCompareNumber < card.getFirstCompareNumber()) {
            return -1;
        }
        if (secondCompareNumber > card.getSecondCompareNumber()) {
            return 1;
        } else if (secondCompareNumber < card.getSecondCompareNumber()) {
            return -1;
        }
        if (thirdCompareNumber > card.getThirdCompareNumber()) {
            return 1;
        } else if (thirdCompareNumber < card.getThirdCompareNumber()) {
            return -1;
        }
        if (fourthCompareNumber > card.getFourthCompareNumber()) {
            return 1;
        } else if (fourthCompareNumber < card.getFourthCompareNumber()) {
            return -1;
        }
        if (fifthCompareNumber > card.getFifthCompareNumber()) {
            return 1;
        } else if (fifthCompareNumber < card.getFifthCompareNumber()) {
            return -1;
        }
        return 0;
    }

    public static void main(String[] argv) {
        TexasHoldEmHandCardDO handCard = new TexasHoldEmHandCardDO();
        //high
        handCard.addHandCard(List.of(0, 1));
        handCard.addPublicCard(List.of(3, 17, 18, 19, 34));
        System.out.println(generateAllCardText(handCard.getAllCardList()));
        System.out.println(handCard.getCardType());

        //one pair
        handCard.getHandCardList().clear();
        handCard.getPublicCardList().clear();
        handCard.getAllCardList().clear();
        handCard.addHandCard(List.of(0, 13));
        handCard.addPublicCard(List.of(14, 15, 30, 31));
        System.out.println(generateAllCardText(handCard.getAllCardList()));
        System.out.println(new Gson().toJson(handCard.getNumberCardColorListMap()));
        System.out.println(handCard.getCardType());

        //two pair
        handCard.getHandCardList().clear();
        handCard.getPublicCardList().clear();
        handCard.getAllCardList().clear();
        handCard.addHandCard(List.of(0, 13));
        handCard.addPublicCard(List.of(1, 14, 16, 30, 44));
        System.out.println(generateAllCardText(handCard.getAllCardList()));
        System.out.println(handCard.getCardType());

        //three of kind
        handCard.getHandCardList().clear();
        handCard.getPublicCardList().clear();
        handCard.getAllCardList().clear();
        handCard.addHandCard(List.of(0, 13));
        handCard.addPublicCard(List.of(26, 27, 42, 43, 44));
        System.out.println(generateAllCardText(handCard.getAllCardList()));
        System.out.println(handCard.getCardType());

        //straight
        handCard.getHandCardList().clear();
        handCard.getPublicCardList().clear();
        handCard.getAllCardList().clear();
        handCard.addHandCard(List.of(1, 2));
        handCard.addPublicCard(List.of(16, 30, 43, 44, 45));
        System.out.println(generateAllCardText(handCard.getAllCardList()));
        System.out.println(handCard.getCardType());

        //straight
        handCard.getHandCardList().clear();
        handCard.getPublicCardList().clear();
        handCard.getAllCardList().clear();
        handCard.addHandCard(List.of(0, 1));
        handCard.addPublicCard(List.of(15, 29, 44, 45, 51));
        System.out.println(generateAllCardText(handCard.getAllCardList()));
        System.out.println(handCard.getCardType());

        //flush
        handCard.getHandCardList().clear();
        handCard.getPublicCardList().clear();
        handCard.getAllCardList().clear();
        handCard.addHandCard(List.of(0, 1));
        handCard.addPublicCard(List.of(2, 4, 5, 30, 44));
        System.out.println(generateAllCardText(handCard.getAllCardList()));
        System.out.println(handCard.getCardType());

        //full house
        handCard.getHandCardList().clear();
        handCard.getPublicCardList().clear();
        handCard.getAllCardList().clear();
        handCard.addHandCard(List.of(0, 13));
        handCard.addPublicCard(List.of(26, 1, 14, 2, 15));
        System.out.println(generateAllCardText(handCard.getAllCardList()));
        System.out.println(handCard.getCardType());

        //four of kind
        handCard.getHandCardList().clear();
        handCard.getPublicCardList().clear();
        handCard.getAllCardList().clear();
        handCard.addHandCard(List.of(12, 25));
        handCard.addPublicCard(List.of(38, 51, 1, 2, 3));
        System.out.println(generateAllCardText(handCard.getAllCardList()));
        System.out.println(handCard.getCardType());

        //straight flush
        handCard.getHandCardList().clear();
        handCard.getPublicCardList().clear();
        handCard.getAllCardList().clear();
        handCard.addHandCard(List.of(11, 10));
        handCard.addPublicCard(List.of(9, 8, 7, 30, 31));
        System.out.println(generateAllCardText(handCard.getAllCardList()));
        System.out.println(handCard.getCardType());

        //straight flush
        handCard.getHandCardList().clear();
        handCard.getPublicCardList().clear();
        handCard.getAllCardList().clear();
        handCard.addHandCard(List.of(25, 13));
        handCard.addPublicCard(List.of(14, 15, 16, 32, 46));
        System.out.println(generateAllCardText(handCard.getAllCardList()));
        System.out.println(handCard.getCardType());

        //royal straight flush
        handCard.getHandCardList().clear();
        handCard.getPublicCardList().clear();
        handCard.getAllCardList().clear();
        handCard.addHandCard(List.of(12, 11));
        handCard.addPublicCard(List.of(10, 9, 8, 20, 32));
        System.out.println(generateAllCardText(handCard.getAllCardList()));
        System.out.println(handCard.getCardType());
    }

    private static String generateAllCardText(List<Integer> allCardList) {
        List<String> list = new ArrayList<>();
        for (Integer card : allCardList) {
            String cardText = generateCardColorText(card) + generateCardNumberText(card);
            list.add(cardText);
        }
        return new Gson().toJson(list);
    }

    private static String generateCardColorText(int card) {
        int color = card / 13;
        return List.of("♣", "♦", "♥", "♠").get(color);
    }

    private static String generateCardNumberText(int card) {
        int number = card % 13;
        return List.of("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A").get(number);
    }
}

