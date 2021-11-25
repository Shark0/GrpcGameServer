package com.shark.game.entity.room.texasHoldem;

import com.google.gson.Gson;
import com.shark.game.entity.player.PlayerDO;
import com.shark.game.entity.room.BaseSeatRoomDO;
import com.shark.game.entity.room.SeatDO;
import com.shark.game.service.TexasHoldemGameService;
import io.grpc.stub.StreamObserver;

import java.util.*;

public class TexasHoldemGameRoomDO extends BaseSeatRoomDO {

    private long smallBlindBet;

    private int ROOM_STATUS_WAITING = 0 ,ROOM_STATUS_PRE_FLOP = 1, ROOM_STATUS_FLOP = 2, ROOM_STATUS_TURN = 3, ROOM_STATUS_RIVER = 4;

    private int  SEAT_STATUS_WAITING = 0, SEAT_STATUS_GAMING = 1;

    private int SEAT_OPERATION_CALL = 1, SEAT_OPERATION_RAISE = 2,  SEAT_OPERATION_FOLD = 3;

    private int RESPONSE_STATUS_ROOM_INFO = 0, RESPONSE_STATUS_CARD_INFO = 1 , RESPONSE_STATUS_START_OPERATION = 2;

    private Integer smallBlindSeatId = -1, bigBlindSeatId = -1, roundStartSeatId = -1, currentOperationSeatId = -1;

    private Integer roomStatus = ROOM_STATUS_WAITING;

    private List<Integer> cardList = new ArrayList<>();

    private List<Integer> publicCardList = new ArrayList<>();

    private Map<Integer, List<Integer>> seatIdCardListMap = new HashMap<>();

    private List<BetPoolDo> betPoolList = new ArrayList<>();

    private final int OPERATION_WAIT_TIME = 15000;

    private Timer timer;

    public TexasHoldemGameRoomDO(int agentId, int gameType, int minBet, int smallBlindBet, int maxQueueCount, int minQueueCount) {
        super(agentId, gameType, minBet, maxQueueCount, minQueueCount);
        this.smallBlindBet = smallBlindBet;
    }

    @Override
    protected synchronized void checkSeatLive() {
        notifyRoomInfo();
    }

    protected synchronized void notifyRoomInfo() {
        TexasHoldemRoomInfoResponseDO texasHoldemRoomInfoResponseDO = new TexasHoldemRoomInfoResponseDO();
        texasHoldemRoomInfoResponseDO.setBetPoolList(betPoolList);
        texasHoldemRoomInfoResponseDO.setPublicCardList(publicCardList);
        texasHoldemRoomInfoResponseDO.setSeatIdSeatMap(seatIdSeatMap);
        String message = new Gson().toJson(texasHoldemRoomInfoResponseDO);
        for(Integer seatId: seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if(seatDO != null) {
                Long playerId = seatDO.getPlayerId();
                boolean success = notifyStatusResponse(playerId, RESPONSE_STATUS_ROOM_INFO, message);
                if(!success && roomStatus == ROOM_STATUS_WAITING) {
                    seatIdSeatMap.remove(seatId);
                    playerIdStatusObserverMap.remove(playerId);
                }
            }
        }
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
        dealPlayerCard();
        notifyCardInfo();
        initBetPoolList();
        initSmallBlindSeatBet();
        initBigBlindSeatBet();
        initRoundStartSeatId();
        notifyRoomInfo();
        currentOperationSeatId = roundStartSeatId;
        notifySeatStartOperation();
        startWaitPositionOperation();
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
                notifyStatusResponse(playerId, RESPONSE_STATUS_CARD_INFO, message);
            }
        }
    }

    private void initBetPoolList() {
        betPoolList.clear();
        betPoolList.add(new BetPoolDo());
    }

    private synchronized void initSmallBlindSeatBet() {
        SeatDO seatDO = seatIdSeatMap.get(smallBlindSeatId);
        long bet = smallBlindBet;
        long money = seatDO.getMoney();
        money = money - bet;
        seatDO.setMoney(money);
        seatDO.setBetMoney(bet);
        BetPoolDo betPool = betPoolList.get(0);
        long betPoolBet = betPool.getBet() + bet;
        betPool.setBet(betPoolBet);
    }

    private synchronized void initBigBlindSeatBet() {
        SeatDO seatDO = seatIdSeatMap.get(bigBlindSeatId);
        long bet = smallBlindBet * 2;
        long money = seatDO.getMoney();
        money = money - bet;
        seatDO.setMoney(money);
        seatDO.setBetMoney(bet);
        BetPoolDo betPool = betPoolList.get(0);
        long betPoolBet = betPool.getBet() + bet;
        betPool.setBet(betPoolBet);
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

    private void notifySeatStartOperation() {
        //TODO
        notifyStatusResponse(currentOperationSeatId, RESPONSE_STATUS_START_OPERATION, null);
    }

    private void startWaitPositionOperation() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SeatDO seatDO = seatIdSeatMap.get(currentOperationSeatId);
                seatDO.setOperation(SEAT_OPERATION_FOLD);
                int countGamingSeatSize = countGamingSeatSize();
                if(countGamingSeatSize > 1) {
                    changeSeatPosition();
                } else {
                    //TODO
                }
            }
        }, OPERATION_WAIT_TIME);
    }

    private void changeSeatPosition() {
        currentOperationSeatId = currentOperationSeatId + 1;
        if (currentOperationSeatId >= roundStartSeatId) {
            //換下一回合
            //TODO
        } else {
            notifySeatStartOperation();
        }
    }

    private int countGamingSeatSize() {
        int count = 0;
        for(int i = 0; i < maxSeatCount; i ++) {
            SeatDO seatDO = seatIdSeatMap.get(i);
            if(seatDO.getStatus() == SEAT_STATUS_GAMING) {
                count = count + 1;
            }
        }
        return count;
    }

    private synchronized boolean notifyStatusResponse(long playerId, int status, String message) {
        StreamObserver streamObserver = playerIdStatusObserverMap.get(playerId);
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
