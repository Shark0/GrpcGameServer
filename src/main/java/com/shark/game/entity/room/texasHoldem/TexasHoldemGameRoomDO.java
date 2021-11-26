package com.shark.game.entity.room.texasHoldem;

import com.google.gson.Gson;
import com.shark.game.entity.room.BaseSeatRoomDO;
import com.shark.game.entity.room.SeatDO;
import com.shark.game.service.TexasHoldemGameService;
import io.grpc.stub.StreamObserver;

import java.util.*;

public class TexasHoldemGameRoomDO extends BaseSeatRoomDO {

    private long smallBlindBet, callBet;

    private final int ROOM_STATUS_WAITING = 0 ,ROOM_STATUS_PRE_FLOP = 1, ROOM_STATUS_FLOP = 2, ROOM_STATUS_TURN = 3, ROOM_STATUS_RIVER = 4, ROOM_STATUS_ALLOCATE_POT = 5;

    private Integer roomStatus = ROOM_STATUS_WAITING;

    private final int OPERATION_EXIT = 0, OPERATION_CALL = 1, OPERATION_RAISE = 2,  OPERATION_FOLD = 3;

    private final int RESPONSE_STATUS_ROOM_INFO = 0, RESPONSE_STATUS_SEAT_INFO = 1,
            RESPONSE_STATUS_CHANGE_STATUS = 2, RESPONSE_STATUS_PLAYER_CARD = 3,
            RESPONSE_STATUS_START_OPERATION = 4, RESPONSE_STATUS_PUBLIC_CARD = 5;

    private Integer smallBlindSeatId = -1, bigBlindSeatId = -1, roundStartSeatId = -1, currentOperationSeatId = -1;


    private List<Integer> cardList = new ArrayList<>();

    private List<Integer> publicCardList = new ArrayList<>();

    private Map<Integer, List<Integer>> seatIdCardListMap = new HashMap<>();

    private long pot;

    private final int WAIT_OPERATION_TIME = 15000;

    private Timer timer;

    public TexasHoldemGameRoomDO(int agentId, int gameType, int minBet, int smallBlindBet, int maxQueueCount, int minQueueCount) {
        super(agentId, gameType, minBet, maxQueueCount, minQueueCount);
        this.smallBlindBet = smallBlindBet;
    }

    @Override
    protected synchronized void checkSeatLive() {
        notifyRoomInfo();
    }

    @Override
    protected synchronized void startGameStatus() {
        System.out.println("TexasHoldemGameRoomDO gameStart()");
        changeSeatListStatus(SEAT_STATUS_GAMING);
        initBlindSeatId();
        initCardList();
        initAllSeatBetMoney();
        initAllSeatCardList();
        startPreFlopStatus();
    }

    private synchronized void initBlindSeatId() {
        List<Integer> playingSeatIdList = new ArrayList<>();
        for(int seatId = 0; seatId < maxSeatCount; seatId ++) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if(seatDO != null) {
                playingSeatIdList.add(seatId);
            }
        }
        if(smallBlindSeatId == - 1) {
            smallBlindSeatId = playingSeatIdList.get(new Random().nextInt(playingSeatIdList.size()));
        } else {
            SeatDO smallBlindSeat = null;
            while (smallBlindSeat == null) {
                smallBlindSeatId = (smallBlindSeatId + 1) % maxSeatCount;
                smallBlindSeat = seatIdSeatMap.get(smallBlindSeatId);
            }
        }

        bigBlindSeatId = smallBlindSeatId;
        SeatDO bigBlindSeat = null;
        while (bigBlindSeat == null) {
            bigBlindSeatId = (smallBlindSeatId + 1) % maxSeatCount;
            bigBlindSeat = seatIdSeatMap.get(bigBlindSeatId);
        }
    }

    private synchronized void initCardList() {
        System.out.println("TexasHoldemGameRoomDO initCardList()");
        cardList.clear();
        for (int i = 0; i < 52; i++) {
            cardList.add(i);
        }
    }

    private synchronized void initAllSeatBetMoney() {
        for(Integer key: seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(key);
            if(seatDO == null) {
                continue;
            }
            seatDO.setBetMoney(0);
        }
    }

    private synchronized void initAllSeatCardList() {
        for(int i = 0; i < maxSeatCount; i ++) {
            seatIdCardListMap.put(i, new ArrayList<>());
        }
    }

    private synchronized void startPreFlopStatus() {
        roomStatus = ROOM_STATUS_PRE_FLOP;
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_CHANGE_STATUS, String.valueOf(roomStatus));
        callBet = smallBlindBet * 2;
        pot = 0;
        dealPlayerCard();
        notifyCardInfo();
        initSmallBlindSeatBet();
        initBigBlindSeatBet();
        initRoundStartSeatId();
        currentOperationSeatId = roundStartSeatId;
        notifyRoomInfo();
        notifySeatStartOperation();
        startWaitPositionOperation();
    }

    private synchronized void startFlopStatus() {
        roomStatus = ROOM_STATUS_FLOP;
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_CHANGE_STATUS, String.valueOf(roomStatus));
        List<Integer> dealCardList = dealPublicCard(3);
        publicCardList.addAll(dealCardList);
        notifyPublicCardToAllSeat(dealCardList);
        notifySeatStartOperation();
        startWaitPositionOperation();
    }

    private synchronized void startTurnStatus() {
        roomStatus = ROOM_STATUS_TURN;
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_CHANGE_STATUS, String.valueOf(roomStatus));
        List<Integer> dealCardList = dealPublicCard(1);
        publicCardList.addAll(dealCardList);
        notifyPublicCardToAllSeat(dealCardList);
        notifySeatStartOperation();
        startWaitPositionOperation();
    }

    private synchronized void startRiverStatus() {
        roomStatus = ROOM_STATUS_RIVER;
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_CHANGE_STATUS, String.valueOf(roomStatus));
        List<Integer> dealCardList = dealPublicCard(1);
        publicCardList.addAll(dealCardList);
        notifyPublicCardToAllSeat(dealCardList);
        notifySeatStartOperation();
        startWaitPositionOperation();
    }

    private void startAllocatePot() {
        roomStatus = ROOM_STATUS_ALLOCATE_POT;
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_CHANGE_STATUS, String.valueOf(roomStatus));
        //TODO
    }

    private synchronized void dealPlayerCard() {
        System.out.println("TexasHoldemGameRoomDO dealCard()");
        Random random = new Random();
        for(int i = 0; i < 2; i ++) {
            for (int seatId = 0; seatId < maxSeatCount; seatId ++) {
                SeatDO seatDO = seatIdSeatMap.get(seatId);
                if(seatDO == null || seatDO.getStatus() != SEAT_STATUS_GAMING) {
                    continue;
                }
                int cardIndex = random.nextInt(cardList.size());
                Integer card = cardList.get(cardIndex);
                seatIdCardListMap.get(seatId).add(card);
                cardList.remove(cardIndex);
            }
        }
    }

    private synchronized void notifyCardInfo() {
        System.out.println("TexasHoldemGameRoomDO notifyCardInfo()");
        for(Integer seatId: seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if(seatDO != null) {
                long playerId = seatDO.getPlayerId();
                String message = new Gson().toJson(seatDO);
                notifyStatusResponse(playerId, RESPONSE_STATUS_PLAYER_CARD, message);
            }
        }
    }

    private synchronized void initSmallBlindSeatBet() {
        SeatDO seatDO = seatIdSeatMap.get(smallBlindSeatId);
        long bet = smallBlindBet;
        long money = seatDO.getMoney();
        money = money - bet;
        seatDO.setMoney(money);
        seatDO.setBetMoney(bet);
        pot = pot +bet;
    }

    private synchronized void initBigBlindSeatBet() {
        SeatDO seatDO = seatIdSeatMap.get(bigBlindSeatId);
        long bet = smallBlindBet * 2;
        long money = seatDO.getMoney();
        money = money - bet;
        seatDO.setMoney(money);
        seatDO.setBetMoney(bet);
        pot = pot +bet;
    }

    private synchronized void initRoundStartSeatId() {
        int seatId = bigBlindSeatId  + 1;
        while (true) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if(seatDO != null && seatDO.getStatus() == SEAT_STATUS_GAMING) {
                roundStartSeatId = seatId;
            }
            seatId = seatId + 1;
        }
    }

    private synchronized void startWaitPositionOperation() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SeatDO seatDO = seatIdSeatMap.get(currentOperationSeatId);
                seatDO.setOperation(OPERATION_FOLD);
                int countGamingSeatSize = countGamingSeatSize();
                if(countGamingSeatSize > 1) {
                    changeSeatPosition();
                } else {
                    startAllocatePot();
                }
            }
        }, WAIT_OPERATION_TIME);
    }

    private synchronized void changeSeatPosition() {
        currentOperationSeatId = currentOperationSeatId + 1;
        if (currentOperationSeatId >= roundStartSeatId) {
            //換下一回合
            switch (roomStatus) {
                case ROOM_STATUS_PRE_FLOP:
                    startFlopStatus();
                    break;
                case ROOM_STATUS_FLOP:
                    startTurnStatus();
                    break;
                case ROOM_STATUS_TURN:
                    startRiverStatus();
                    break;
                case ROOM_STATUS_RIVER:
                    startAllocatePot();
                    break;
            }
        } else {
            notifySeatStartOperation();
        }
    }

    private synchronized int countGamingSeatSize() {
        int count = 0;
        for(int i = 0; i < maxSeatCount; i ++) {
            SeatDO seatDO = seatIdSeatMap.get(i);
            if(seatDO.getStatus() == SEAT_STATUS_GAMING) {
                count = count + 1;
            }
        }
        return count;
    }

    private synchronized List<Integer> dealPublicCard(int count) {
        List<Integer> dealCardList = new ArrayList<>();
        for(int i = 0; i < count; i ++) {
            int dealCardIndex = new Random().nextInt(cardList.size());
            Integer card = cardList.get(dealCardIndex);
            cardList.remove(dealCardIndex);
            dealCardList.add(card);
        }
        return dealCardList;
    }

    public void operation(long playerId, int operation, long bet) {
        int seatId = playerIdSeatIdMap.get(playerId);
        if(seatId == currentOperationSeatId) {
            timer.cancel();
        }
        switch (operation) {
            case OPERATION_EXIT:
                break;
            case OPERATION_CALL:
                break;
            case OPERATION_RAISE:
                break;
            case OPERATION_FOLD:
                operationFold(seatId);
                break;
        }
    }

    private void operationFold(int seatId) {
        if(seatId != currentOperationSeatId) {
            return;
        }
        SeatDO seatDO = seatIdSeatMap.get(seatId);
        seatDO.setOperation(OPERATION_FOLD);
        notifySeatInfo(seatDO);
        changeSeatPosition();
    }

    private synchronized void notifySeatStartOperation() {
        SeatDO seatDO = seatIdSeatMap.get(currentOperationSeatId);
        TexasHoldemStartOperationResponseDO startOperationResponseDO = new TexasHoldemStartOperationResponseDO();
        startOperationResponseDO.setPlayerId(seatDO.getPlayerId());
        startOperationResponseDO.setCallBet(callBet - seatDO.getBetMoney());
        String message = new Gson().toJson(startOperationResponseDO);
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_START_OPERATION, message);
    }

    protected synchronized void notifyRoomInfo() {
        TexasHoldemRoomInfoResponseDO texasHoldemRoomInfoResponseDO = new TexasHoldemRoomInfoResponseDO();
        texasHoldemRoomInfoResponseDO.setPot(pot);
        texasHoldemRoomInfoResponseDO.setPublicCardList(publicCardList);
        texasHoldemRoomInfoResponseDO.setSeatIdSeatMap(seatIdSeatMap);
        String message = new Gson().toJson(texasHoldemRoomInfoResponseDO);
        for(Integer seatId: seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if(seatDO != null) {
                Long playerId = seatDO.getPlayerId();
                boolean success = notifyStatusResponse(playerId, RESPONSE_STATUS_ROOM_INFO, message);
                if(!success && roomStatus == ROOM_STATUS_WAITING) {
                    exitGame(playerId);
                }
            }
        }
    }

    private void notifySeatInfo(SeatDO seatDO) {
        String message = new Gson().toJson(seatDO);
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_SEAT_INFO, message);
    }

    private synchronized void notifyPublicCardToAllSeat(List<Integer> dealCardList) {
        Gson gson = new Gson();
        String message = gson.toJson(dealCardList);
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_PUBLIC_CARD, message);
    }

    private synchronized void notifyAllPlayerStatusResponse(int status, String message) {
        for(Long playerId: playerIdObserverMap.keySet()) {
            notifyStatusResponse(playerId, status, message);
        }
    }

    private synchronized boolean notifyStatusResponse(long playerId, int status, String message) {
        StreamObserver streamObserver = playerIdObserverMap.get(playerId);
        if(streamObserver == null) {
            return false;
        }
        TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                        .setMessage(message).setStatus(status).build();
        try {
            streamObserver.onNext(response);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
