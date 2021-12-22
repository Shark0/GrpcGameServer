package com.shark.game.entity.scene.texasHoldEm.agent;

import com.google.gson.Gson;
import com.shark.game.entity.scene.seat.BaseSeatSceneDO;
import com.shark.game.entity.scene.texasHoldEm.TexasHoldEmGameSceneDO;
import com.shark.game.entity.scene.texasHoldEm.TexasHoldEmHandCardDO;
import com.shark.game.entity.scene.texasHoldEm.response.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

@Data
@NoArgsConstructor
public class TexasHoldEmRobotAgentDO extends TexasHoldEmAgentDO implements Serializable {

    private int sceneStatus;
    private int seatId = -1;
    private long gameBet;
    private long bigBlindBet;
    private TexasHoldEmHandCardDO handCard;

    public TexasHoldEmRobotAgentDO(
            long sceneId, String name, TexasHoldEmGameAsset texasHoldEmGameAsset) {
        super(sceneId, name, texasHoldEmGameAsset);
        handCard = new TexasHoldEmHandCardDO();
    }

    @Override
    public void notifyEnterSceneInfo(int sceneStatus) {
        this.sceneStatus = sceneStatus;
    }

    @Override
    public void notifySitDown(int seatId) {
        //System.out.println("TexasHoldEmRobotAgentDO notifySitDown() seatId = " + seatId);
        this.seatId = seatId;
    }

    @Override
    public void notifyStandUp() {
        this.seatId = -1;
    }

    @Override
    public void notifyNoSeat() {
        this.seatId = -1;
    }

    @Override
    public void notifySeatInfo(TexasHoldEmSeatInfoResponseDO responseDO) {
    }

    @Override
    public void notifySceneInfo(TexasHoldEmSceneInfoResponseDO responseDO) {
    }

    @Override
    public boolean notifyCheckLive() {
        return true;
    }

    @Override
    public void notifyStatusChanged(int status) {
        this.sceneStatus = status;
        if (status == BaseSeatSceneDO.ROOM_STATUS_WAITING) {
            handCard = new TexasHoldEmHandCardDO();
        }
    }

    @Override
    public void notifySeatCard(Map<Integer, TexasHoldEmHandCardResponseDO> seatIdCardListMap) {
        TexasHoldEmHandCardResponseDO texasHoldEmHandCardResponseDO = seatIdCardListMap.get(seatId);
        if (texasHoldEmHandCardResponseDO == null) {
            return;
        }
        List<Integer> cardList = texasHoldEmHandCardResponseDO.getCardList();
        //System.out.println("TexasHoldEmRobotAgentDO notifySeatCard() seatId = " + seatId + ", cardList = " + new Gson().toJson(cardList));
        if (handCard.getHandCardList().size() == 0) {
            handCard.addHandCard(cardList);
        }
    }

    @Override
    public void notifySeatStartOperation(TexasHoldEmStartOperationResponseDO responseDO) {
        System.out.println("TexasHoldEmRobotAgentDO notifySeatStartOperation() seatId = " + seatId + ", callBet = " + responseDO.getCallBet());
        if (this.seatId != responseDO.getSeatId()) {
            return;
        }
        new Thread(() -> {
            try {
                Thread.sleep(new Random().nextInt((int) responseDO.getLastOperationTime() * 1 / 2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            switch (sceneStatus) {
                case TexasHoldEmGameSceneDO.ROOM_STATUS_PRE_FLOP:
                    handlePreFlopStatusOperation(responseDO);
                    break;
                case TexasHoldEmGameSceneDO.ROOM_STATUS_FLOP:
                case TexasHoldEmGameSceneDO.ROOM_STATUS_TURN:
                case TexasHoldEmGameSceneDO.ROOM_STATUS_RIVER:
                    handleOperation(responseDO);
                    break;
            }
        }).start();
    }

    private void handlePreFlopStatusOperation(TexasHoldEmStartOperationResponseDO responseDO) {
        boolean isBigCall = responseDO.getCallBet() >= (bigBlindBet * 4);
        switch (handCard.getCardType()) {
            case TexasHoldEmHandCardDO.CARD_TYPE_ONE_PAIR:
                if (handCard.getFirstCompareNumber() >= 10) {
                    //大對
                    if (responseDO.isCanRaise()) {
                        long bet = agentAsset.getMoney() * (new Random().nextInt(3)) / 3;
                        actionListener.onActionRaise(TexasHoldEmRobotAgentDO.this, Math.max(bet, responseDO.getMinRaiseBet()));
                        return;
                    }
                } else {
                    //小對
                    if (isBigCall) {
                        //對方可能有大對跟雙高牌
                        boolean isRandomFold = (new Random().nextInt(3) == 0);
                        if (isRandomFold) {
                            actionListener.onActionFold(this);
                            return;
                        }
                    }
                }
                actionListener.onActionCall(this, responseDO.getCallBet());
                break;
            case TexasHoldEmHandCardDO.CARD_TYPE_HIGH_CARD:
                if (handCard.getFirstCompareNumber() >= 10 && handCard.getSecondCompareNumber() >= 10) {
                    //雙高牌
                    if (responseDO.isCanRaise()) {
                        actionListener.onActionRaise(TexasHoldEmRobotAgentDO.this, Math.max(bigBlindBet * 5, responseDO.getMinRaiseBet()));
                        return;
                    }
                } else if (handCard.getFirstCompareNumber() >= 10) {
                    //單高牌
                    if (isBigCall) {
                        //對方可能有大對跟雙高牌
                        boolean isRandomFold = (new Random().nextInt(5) == 0);
                        if (isRandomFold) {
                            actionListener.onActionFold(this);
                            return;
                        }
                    }
                } else {
                    //小牌
                    boolean isNoStraightChance = (handCard.getFirstCompareNumber() - handCard.getSecondCompareNumber()) > 4;
                    if (isBigCall || isNoStraightChance) {
                        actionListener.onActionFold(this);
                        return;
                    }
                }
                actionListener.onActionCall(this, responseDO.getCallBet());
                break;
        }
    }

    private void handleOperation(TexasHoldEmStartOperationResponseDO responseDO) {
        //System.out.println("TexasHoldEmRobotAgentDO handleFlopStatusOperation()");
        boolean isBigCall = (responseDO.getCallBet() >= (this.gameBet / 3));
        switch (handCard.getCardType()) {
            case TexasHoldEmHandCardDO.CARD_TYPE_ROYAL_STRAIGHT_FLUSH:
            case TexasHoldEmHandCardDO.CARD_TYPE_STRAIGHT_FLUSH:
            case TexasHoldEmHandCardDO.CARD_TYPE_FOUR_OF_KIND:
                //好牌直接硬幹
                if (responseDO.isCanRaise()) {
                    long bet = agentAsset.getMoney() * (new Random().nextInt(2) + 1) / 3;
                    actionListener.onActionRaise(this, Math.max(bet, responseDO.getMinRaiseBet()));
                    return;
                }
                actionListener.onActionCall(this, responseDO.getCallBet());
                break;
            case TexasHoldEmHandCardDO.CARD_TYPE_FULL_HOUSE:
                boolean isFirstCompareNumberInHand = isFirstCompareNumberInHand();
                boolean isFirstCompareBigThenSecondCompare = handCard.getFirstCompareNumber() > handCard.getSecondCompareNumber();
                if (isFirstCompareNumberInHand && isFirstCompareBigThenSecondCompare) {
                    //最大的三條在手上，且第三條比一對大
                    if (responseDO.isCanRaise()) {
                        long bet = agentAsset.getMoney() * (new Random().nextInt(2) + 1) / 3;
                        actionListener.onActionRaise(this, Math.max(bet, responseDO.getMinRaiseBet()));
                        return;
                    }
                }
                actionListener.onActionCall(this, responseDO.getCallBet());
                break;
            case TexasHoldEmHandCardDO.CARD_TYPE_FLUSH:
                boolean isHandCardHasHighCard = isHandCardHasHighCard();
                if (isHandCardHasHighCard) {
                    if (responseDO.isCanRaise()) {
                        long bet = agentAsset.getMoney() * (new Random().nextInt(2) + 1) / 3;
                        actionListener.onActionRaise(this, Math.max(bet, responseDO.getMinRaiseBet()));
                        return;
                    }
                }
                actionListener.onActionCall(this, responseDO.getCallBet());
                break;
            case TexasHoldEmHandCardDO.CARD_TYPE_STRAIGHT:
                boolean hasFlushChance = hasFlushChance();
                if (!hasFlushChance && responseDO.isCanRaise()) {
                    //好牌且對手沒有桐花機會，直接硬幹
                    long bet = agentAsset.getMoney() * (new Random().nextInt(2) + 1) / 3;
                    actionListener.onActionRaise(this, Math.max(bet, responseDO.getMinRaiseBet()));
                    return;
                }
                actionListener.onActionCall(this, responseDO.getCallBet());
                break;
            case TexasHoldEmHandCardDO.CARD_TYPE_THREE_OF_KIND:
                isFirstCompareNumberInHand = isFirstCompareNumberInHand();
                isFirstCompareBigThenSecondCompare = handCard.getFirstCompareNumber() > handCard.getSecondCompareNumber();
                if (isFirstCompareNumberInHand && isFirstCompareBigThenSecondCompare) {
                    //最大的三條在手上，且是目前最大數
                    if (responseDO.isCanRaise()) {
                        long bet = agentAsset.getMoney() * (new Random().nextInt(2) + 1) / 3;
                        actionListener.onActionRaise(this, Math.max(bet, responseDO.getMinRaiseBet()));
                        return;
                    }
                }
                if (isBigCall) {
                    actionListener.onActionFold(this);
                    return;
                }
                actionListener.onActionCall(this, responseDO.getCallBet());
                break;
            case TexasHoldEmHandCardDO.CARD_TYPE_TWO_PAIR:
                isFirstCompareNumberInHand= isFirstCompareNumberInHand();
                boolean isSecondCompareNumberInHand = isSecondCompareNumberInHand();
                boolean isFirstCompareBigThenThirdCompare = handCard.getFirstCompareNumber() > handCard.getThirdCompareNumber();
                hasFlushChance = hasFlushChance();
                boolean hasStraightChance = hasStraightChance();
                if (isFirstCompareNumberInHand && isSecondCompareNumberInHand && isFirstCompareBigThenThirdCompare &&
                        !hasFlushChance && !hasStraightChance) {
                    //兩對都在手上，且第一對是目前最大數
                    if (responseDO.isCanRaise()) {
                        long bet = agentAsset.getMoney() * (new Random().nextInt(2) + 1) / 3;
                        actionListener.onActionRaise(this, Math.max(bet, responseDO.getMinRaiseBet()));
                        return;
                    }
                }
                if (isBigCall) {
                    actionListener.onActionFold(this);
                    return;
                }
                actionListener.onActionCall(this, responseDO.getCallBet());
                break;
            case TexasHoldEmHandCardDO.CARD_TYPE_ONE_PAIR:
                isFirstCompareNumberInHand= isFirstCompareNumberInHand();
                isFirstCompareBigThenSecondCompare = handCard.getFirstCompareNumber() > handCard.getSecondCompareNumber();
                hasFlushChance = hasFlushChance();
                hasStraightChance = hasStraightChance();
                if (isFirstCompareNumberInHand && isFirstCompareBigThenSecondCompare &&
                        !hasFlushChance && !hasStraightChance) {
                    //一對在手上，且第一對是目前最大數
                    if (responseDO.isCanRaise()) {
                        actionListener.onActionRaise(this, Math.max(bigBlindBet * 5, responseDO.getMinRaiseBet()));
                        return;
                    }
                }
                if (isBigCall) {
                    actionListener.onActionFold(this);
                    return;
                }
                actionListener.onActionCall(this, responseDO.getCallBet());
                break;
            default:
                if (isBigCall) {
                    actionListener.onActionFold(this);
                    return;
                }
                actionListener.onActionCall(this, responseDO.getCallBet());
                break;
        }
    }


    private boolean isFirstCompareNumberInHand() {
        for (Integer card : handCard.getHandCardList()) {
            int number = card % 13;
            if (number == handCard.getFirstCompareNumber()) {
                return true;
            }
        }
        return false;
    }

    private boolean isSecondCompareNumberInHand() {
        for (Integer card : handCard.getHandCardList()) {
            int number = card % 13;
            if (number == handCard.getSecondCompareNumber()) {
                return true;
            }
        }
        return false;
    }

    private boolean isHandCardHasHighCard() {
        for (int card : handCard.getHandCardList()) {
            int number = card % 13;
            if (number >= 10) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFlushChance() {
        for (int color : handCard.getColorCardNumberListMap().keySet()) {
            List<Integer> numberList = handCard.getColorCardNumberListMap().get(color);
            if (numberList != null && numberList.size() >= 3) {
                return true;
            }
        }
        return false;
    }

    private boolean hasStraightChance() {
        int count;
        for (int number = 0; number < 9; number++) {
            count = 0;
            for (int i = 0; i < 5; i++) {
                Integer cardNumber = number + i;
                List<Integer> colorList = handCard.getNumberCardColorListMap().get(cardNumber);
                if (colorList != null && colorList.size() > 0) {
                    count = count + 1;
                }
            }
            if (count >= 3) {
                return true;
            }
        }
        //12345
        count = 0;
        for (int number = 0; number < 4; number++) {
            List<Integer> colorList = handCard.getNumberCardColorListMap().get(number);
            if (colorList != null && colorList.size() > 0) {
                count = count + 1;
            }
        }
        List<Integer> colorList = handCard.getNumberCardColorListMap().get(12);
        if (colorList != null && colorList.size() > 0) {
            count = count + 1;
        }
        if (count >= 3) {
            return true;
        }
        return false;
    }

    @Override
    public void notifyWaitingSeatOperation(TexasHoldEmWaitOperationResponseDO responseDO) {
    }

    @Override
    public void notifySeatOperation(TexasHoldEmSeatOperationResponseDO responseDO) {
        this.gameBet = responseDO.getSceneGameBet();
    }

    @Override
    public void notifyDealPublicCard(List<Integer> publicCardList) {
        //System.out.println("TexasHoldEmRobotAgentDO notifyDealPublicCard() publicCardList = " + new Gson().toJson(publicCardList));
        handCard.addPublicCard(publicCardList);
    }

    @Override
    public void notifyWinPotBet(List<TexasHoldEmWinPotBetResponseDO> winPotBetResponseDOList) {
    }
}
