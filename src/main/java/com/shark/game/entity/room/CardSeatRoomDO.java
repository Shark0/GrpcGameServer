package com.shark.game.entity.room;


import com.shark.game.entity.player.PlayerDO;
import com.shark.game.manager.PlayerManager;
import com.shark.game.service.CardSeatGameService;
import io.grpc.stub.StreamObserver;

import java.util.*;

public class CardSeatRoomDO extends BaseSeatRoomDO {

    private final int OPERATION_AUTO_CALL = 1, OPERATION_CALL = 2, OPERATION_FOLD = 3, OPERATION_EXIT_GAME = 4;

    private final int ROUND_COUNT = 3;

    private final int OPERATION_WAIT_TIME = 15000;

    private int currentRound = 1;

    private int operationSeatPosition = 0;

    private Timer timer;

    private List<Integer> cardList = new ArrayList<>();

    public CardSeatRoomDO(int gameType) {
        super(gameType);
    }

    @Override
    protected int generateMaxCount() {
        return 6;
    }

    @Override
    protected int generateMinCount() {
        return 2;
    }

    @Override
    protected void gameStart() {
        System.out.println("CardSeatRoomDO gameStart()");
        createSeat();
        initCardList();
        dealCard();
        notifyCardInfo();
        startRoundOperation();
    }

    private void createSeat() {
        System.out.println("CardSeatRoomDO createSeat()");
        for (String token : queueTokenList) {
            PlayerDO playerDo = PlayerManager.getInstance().findByToken(token);
            CardSeatDO seatDo = new CardSeatDO();
            seatDo.setPlayerDO(playerDo);
            int money = playerDo.getMoney();
            money = money - 100;
            playerDo.setMoney(money);
            seatDo.setBetMoney(100);
            seatDo.setToken(token);
            cardSeatDOList.add(seatDo);
            tokenSeatDOMap.put(token, seatDo);
        }
    }

    private void initCardList() {
        System.out.println("CardSeatRoomDO initCardList()");
        for (int i = 1; i <= 24; i++) {
            cardList.add(i);
        }
    }

    private void dealCard() {
        Random random = new Random();
        for (CardSeatDO cardSeatDO : cardSeatDOList) {
            int cardIndex = random.nextInt(cardList.size());
            Integer card = cardList.get(cardIndex);
            cardSeatDO.getCardList().add(card);
            cardList.remove(cardIndex);
            System.out.println("CardSeatRoomDO dealCard() cardIndex = " + cardIndex + ", card = " + card + ", cardList.size() = " + cardList.size());
        }
    }

    private void notifyCardInfo() {
        System.out.println("CardSeatRoomDO notifyCardInfo()");
        for (int i = 0; i < cardSeatDOList.size(); i++) {
            CardSeatDO cardSeatI = cardSeatDOList.get(i);
            StringBuilder builder = new StringBuilder();

            for (int j = 0; j < cardSeatDOList.size(); j++) {
                CardSeatDO cardSeatJ = cardSeatDOList.get(j);

                builder.append("玩家: ").append(cardSeatJ.getPlayerDO().getName()).append(" 牌: [");

                for (int k = 0; k < cardSeatJ.getCardList().size(); k++) {
                    if (k != 0) {
                        builder.append(", ");
                    }
                    if(k == 0 && i != j) {
                        builder.append("*");
                    } else {
                        builder.append(cardSeatJ.getCardList().get(k));
                    }
                }
                builder.append("] 目前下注金額: ").append(cardSeatJ.getBetMoney()).append(" ");
            }
            String token = cardSeatI.getToken();
            String message = builder.toString();
            notifyMessage(token, 0, message);
        }
    }

    private void startRoundOperation() {
        System.out.println("CardSeatRoomDO startRoundOperation()");
        dealCard();
        notifyCardInfo();
        operationSeatPosition = 0;
        notifySeatOperation();
        startWaitPositionOperation();
    }

    private void notifySeatOperation() {
        String message = "目前玩家" + cardSeatDOList.get(operationSeatPosition).getPlayerDO().getName() + "操作中";
        System.out.println("CardSeatRoomDO startRoundOperation() message = " + message);
        notifyAll(0, message);
    }

    private void startWaitPositionOperation() {
        System.out.println("CardSeatRoomDO startWaitPositionOperation()");
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                CardSeatDO cardSeatDO = cardSeatDOList.get(operationSeatPosition);
                cardSeatDO.setFold(true);
                int liveSeatCount = 0;
                for(CardSeatDO liveSeat: cardSeatDOList) {
                    if(!liveSeat.isFold()) {
                        liveSeatCount = liveSeatCount +1;
                    }
                }
                if(liveSeatCount > 1) {
                    changeSeatPosition();
                } else {
                    endGame();
                }
            }
        }, OPERATION_WAIT_TIME);
    }

    private void changeSeatPosition() {
        operationSeatPosition = operationSeatPosition + 1;
        if (operationSeatPosition >= cardSeatDOList.size()) {
            //換下一回合
            currentRound = currentRound + 1;
            if (currentRound >= ROUND_COUNT) {
                endGame();
            } else {
                startRoundOperation();
            }
        } else {
            //換座位
            notifySeatOperation();
            CardSeatDO cardSeatDO = cardSeatDOList.get(operationSeatPosition);
            if(cardSeatDO.isAutoCall()) {
                //自動跟注
                PlayerDO playerDO = cardSeatDO.getPlayerDO();
                int money = playerDO.getMoney() - 100;
                playerDO.setMoney(money);
                int betMoney = cardSeatDO.getBetMoney() + 100;
                cardSeatDO.setBetMoney(betMoney);
                String message = "玩家: " + playerDO.getName() + " 自動跟柱";
                notifyAll(0, message);
            } else {
                notifySeatOperation();
                startWaitPositionOperation();
            }
        }
    }

    private void endGame() {
        CardSeatDO winnerSeatDO = null;
        int winBetMoney = 0;
        for(CardSeatDO cardSeatDO: cardSeatDOList) {
            winBetMoney = winBetMoney + cardSeatDO.getBetMoney();
            if(cardSeatDO.isFold()) {
                continue;
            }
            if(winnerSeatDO == null) {
                winnerSeatDO = cardSeatDO;
            } else {
                if(isWinWinnerSeat(winnerSeatDO, cardSeatDO)) {
                    winnerSeatDO = cardSeatDO;
                }
            }
        }
        String winnerName = winnerSeatDO.getPlayerDO().getName();

        for(CardSeatDO cardSeatDO: cardSeatDOList) {
            String token = cardSeatDO.getToken();
            String message = "贏家: " + winnerName + ", 總獎金: " + winBetMoney + ", 你這場下注總金額: " + cardSeatDO.getBetMoney();
            notifyMessage(token, 0, message);
        }
    }

    private boolean isWinWinnerSeat(CardSeatDO winnerSeatDO, CardSeatDO currentCardSeatDO) {
        int winCardSum = 0;
        for(Integer card: winnerSeatDO.getCardList()) {
            winCardSum = winCardSum + card;
        }
        int cardSeatSum = 0;
        for(Integer card: currentCardSeatDO.getCardList()) {
            cardSeatSum = cardSeatSum + card;
        }
        return cardSeatSum > winCardSum;
    }

    public void operation(StreamObserver<CardSeatGameService.CardSeatGameOperationResponse> responseObserver, String token, int operation) {
        int position = queueTokenList.indexOf(token);
        if(position == operationSeatPosition) {
            timer.cancel();
        }

        if(operation == OPERATION_AUTO_CALL || operation == OPERATION_EXIT_GAME) {
            switch (operation) {
                case OPERATION_AUTO_CALL:
                    autoCall(responseObserver, token);
                    break;
                case OPERATION_EXIT_GAME:
                    exitGame(responseObserver, token);
                    break;
            }
        } else {
            if (operationSeatPosition != position) {
                CardSeatGameService.CardSeatGameOperationResponse response =
                        CardSeatGameService.CardSeatGameOperationResponse.newBuilder()
                                .setStatus(-1).setMessage("不在可操作回合中做操作").build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }
            switch (operation) {
                case OPERATION_CALL:
                    call(responseObserver, token);
                    break;
                case OPERATION_FOLD:
                    fold(responseObserver, token);
                    break;
            }
        }
    }

    private void autoCall(StreamObserver<CardSeatGameService.CardSeatGameOperationResponse> responseObserver, String token) {
        System.out.println("CardSeatRoom autoCall()");
        CardSeatDO cardSeatDO = tokenSeatDOMap.get(token);
        cardSeatDO.setAutoCall(true);
        CardSeatGameService.CardSeatGameOperationResponse operationResponse =
                CardSeatGameService.CardSeatGameOperationResponse.newBuilder().setStatus(0).setMessage("Success").build();
        responseObserver.onNext(operationResponse);
        responseObserver.onCompleted();
        String message = "玩家 " + cardSeatDO.getPlayerDO().getName() + " 設定自動加注";
        notifyAll(0, message);

        if(operationSeatPosition == cardSeatDOList.indexOf(cardSeatDO)) {
            int money = cardSeatDO.getPlayerDO().getMoney();
            money = money - 100;
            cardSeatDO.getPlayerDO().setMoney(money);
            int betMoney = cardSeatDO.getBetMoney();
            betMoney = betMoney + 100;
            cardSeatDO.setBetMoney(betMoney);
            changeSeatPosition();
        }
    }

    private void call(StreamObserver<CardSeatGameService.CardSeatGameOperationResponse> responseObserver, String token) {
        System.out.println("CardSeatRoom call()");
        CardSeatDO cardSeatDO = tokenSeatDOMap.get(token);
        int money = cardSeatDO.getPlayerDO().getMoney();
        money = money - 100;
        cardSeatDO.getPlayerDO().setMoney(money);
        int betMoney = cardSeatDO.getBetMoney();
        betMoney = betMoney + 100;
        cardSeatDO.setBetMoney(betMoney);
        CardSeatGameService.CardSeatGameOperationResponse operationResponse =
                CardSeatGameService.CardSeatGameOperationResponse.newBuilder().setStatus(0).setMessage("Success").build();
        responseObserver.onNext(operationResponse);
        responseObserver.onCompleted();
        String message = "玩家 " + cardSeatDO.getPlayerDO().getName() + " 加注100";
        notifyAll(0, message);
        changeSeatPosition();
    }

    private void fold(StreamObserver<CardSeatGameService.CardSeatGameOperationResponse> responseObserver, String token) {
        System.out.println("CardSeatRoom fold()");
        CardSeatDO cardSeatDO = tokenSeatDOMap.get(token);
        cardSeatDO.setFold(true);
        CardSeatGameService.CardSeatGameOperationResponse operationResponse =
                CardSeatGameService.CardSeatGameOperationResponse.newBuilder().setStatus(0).setMessage("Success").build();
        responseObserver.onNext(operationResponse);
        responseObserver.onCompleted();
        String message = "玩家 " + cardSeatDO.getPlayerDO().getName() + " 棄牌";
        notifyAll(0, message);
        changeSeatPosition();
    }

    private void exitGame(StreamObserver<CardSeatGameService.CardSeatGameOperationResponse> responseObserver, String token) {
        System.out.println("CardSeatRoom exitGame()");
        CardSeatDO cardSeatDO = tokenSeatDOMap.get(token);
        cardSeatDO.setFold(true);
        CardSeatGameService.CardSeatGameOperationResponse operationResponse =
                CardSeatGameService.CardSeatGameOperationResponse.newBuilder().setStatus(0).setMessage("Success").build();
        responseObserver.onNext(operationResponse);
        responseObserver.onCompleted();
        String message = "玩家 " + cardSeatDO.getPlayerDO().getName() + " 離開房間";
        notifyAll(0, message);

        StreamObserver streamObserver = tokenStatusObserverMap.get(token);
        tokenStatusObserverMap.remove(token);
        streamObserver.onCompleted();

        if(operationSeatPosition == cardSeatDOList.indexOf(cardSeatDO)) {
            changeSeatPosition();
        }
    }

    private void notifyAll(int status, String message) {
        for(String token: tokenStatusObserverMap.keySet()) {
            notifyMessage(token, status, message);
        }
    }

    private void notifyMessage(String token, int status, String message) {
        //TODO 這些message以後可以替換成Json
        System.out.println("CardSeatRoom notifyMessage() token = " + token + ", status = " + status + ", message = " + message);
        StreamObserver streamObserver = tokenStatusObserverMap.get(token);
        if(streamObserver == null) {
            return;
        }
        CardSeatGameService.CardSeatGameStatusResponse response =
                CardSeatGameService.CardSeatGameStatusResponse.newBuilder()
                        .setStatus(status).setMessage(message)
                        .build();
        streamObserver.onNext(response);
    }
}
