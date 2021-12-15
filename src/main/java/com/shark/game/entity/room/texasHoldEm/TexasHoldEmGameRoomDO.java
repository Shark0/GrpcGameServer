package com.shark.game.entity.room.texasHoldEm;

import com.google.gson.Gson;
import com.shark.game.entity.player.PlayerDO;
import com.shark.game.entity.room.seat.BaseSeatRoomDO;
import com.shark.game.entity.room.seat.SeatDO;
import com.shark.game.entity.room.seat.SeatWaitOperationDO;
import com.shark.game.manager.PlayerManager;
import com.shark.game.service.TexasHoldemGameService;
import io.grpc.netty.shaded.io.netty.util.internal.StringUtil;
import io.grpc.stub.StreamObserver;

import java.util.*;

public class TexasHoldEmGameRoomDO extends BaseSeatRoomDO {

    private final int ROOM_STATUS_PRE_FLOP = 1, ROOM_STATUS_FLOP = 2, ROOM_STATUS_TURN = 3, ROOM_STATUS_RIVER = 4,
            ROOM_STATUS_OPEN_CARD = 5, ROOM_STATUS_ALLOCATE_POT = 6;

    private final int OPERATION_NONE = -1, OPERATION_EXIT = 0, OPERATION_CALL = 1, OPERATION_RAISE = 2, OPERATION_ALL_IN = 3,
            OPERATION_FOLD = 4, OPERATION_STAND_UP = 5, OPERATION_SIT_DOWN = 6;

    public static final int RESPONSE_STATUS_SIT_DOWN = 0, RESPONSE_STATUS_NO_SEAT = 1, RESPONSE_STATUS_SEAT_INFO = 2,
            RESPONSE_STATUS_ENTER_ROOM_INFO = 3, RESPONSE_STATUS_ROOM_INFO = 4, RESPONSE_STATUS_CHECK_SEAT_LIVE = 5,
            RESPONSE_STATUS_CHANGE_STATUS = 6, RESPONSE_STATUS_SEAT_CARD = 7, RESPONSE_STATUS_START_OPERATION = 8,
            RESPONSE_STATUS_WAIT_SEAT_OPERATION = 9, RESPONSE_STATUS_SEAT_OPERATION = 10, RESPONSE_STATUS_PUBLIC_CARD = 11,
            RESPONSE_STATUS_WIN_POT_BET = 12;

    private final int OPERATION_WAIT_TIME = 20000;

    private final long smallBlindBet;

    private long roomGameBet;
    private long roundCallBet;
    private long roundRaiseBet = 100;

    private int smallBlindSeatId = -1, bigBlindSeatId = -1, roundStartSeatId = -1, currentOperationSeatId = -1;

    private final List<Integer> cardList = new ArrayList<>();

    private final List<Integer> publicCardList = new ArrayList<>();

    private final Map<Integer, TexasHoldEmHandCardDO> seatIdHandCardMap = new HashMap<>();

    private SeatWaitOperationDO seatWaitOperation;

    public TexasHoldEmGameRoomDO(int agentId, int gameType, int minBet, int smallBlindBet, int maxQueueCount, int minQueueCount) {
        super(agentId, gameType, minBet, maxQueueCount, minQueueCount);
        this.smallBlindBet = smallBlindBet;
    }

    @Override
    protected void sitDown(long playerId, int seatId) {
        allocatePlayerSeat(playerId, seatId);
        notifySitDown(playerId, seatId);
        notifySeatInfo(seatId);
    }

    @Override
    protected void removeDeadSeat() {
        System.out.println("TexasHoldemGameRoomDO removeDeadSeat()");
        List<Integer> deadGameSeatIdList = new ArrayList<>();
        for (Integer seatId : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO != null) {
                long playerId = seatDO.getPlayerId();
                boolean success = notifyStatusResponse(playerId, RESPONSE_STATUS_CHECK_SEAT_LIVE, "");
                if (!success) {
                    deadGameSeatIdList.add(seatId);
                }
            }
        }
        for (Integer seatId : deadGameSeatIdList) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            exitGame(seatDO.getPlayerId());
            seatIdSeatMap.remove(seatId);
        }
    }

    private void standUpAllNoMoneySeat() {
        System.out.println("TexasHoldemGameRoomDO removeNoMoneySeat()");
        List<Integer> noMoneySeatIdIdList = new ArrayList<>();
        for (Integer seatId : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO != null && seatDO.getMoney() <= 0) {
                noMoneySeatIdIdList.add(seatDO.getId());
            }
        }

        for (Integer seatId : noMoneySeatIdIdList) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            PlayerDO playerDO = PlayerManager.getInstance().findById(seatDO.getPlayerId());
            playerDO.setMoney(playerDO.getMoney() + seatDO.getMoney());
            seatIdSeatMap.remove(seatId);
        }
    }

    @Override
    protected void startWaitingStatus() {
        System.out.println("TexasHoldemGameRoomDO startWaitingStatus()");
        roomStatus = ROOM_STATUS_WAITING;
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_CHANGE_STATUS, String.valueOf(roomStatus));

        standUpAllNoMoneySeat();
        super.startWaitingStatus();
    }

    @Override
    protected void startGameStatus() {
        startPreFlopStatus();
    }

    private void initBlindSeatId() {
        System.out.println("TexasHoldemGameRoomDO initBlindSeatId()");
        List<Integer> playingSeatIdList = new ArrayList<>();
        for (int seatId = 0; seatId < maxSeatCount; seatId++) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO != null) {
                playingSeatIdList.add(seatId);
            }
        }
        if (smallBlindSeatId == -1) {
            smallBlindSeatId = playingSeatIdList.get(new Random().nextInt(playingSeatIdList.size()));
        } else {
            SeatDO smallBlindSeat = null;
            while (smallBlindSeat == null) {
                smallBlindSeatId = (smallBlindSeatId + 1) % maxSeatCount;
                smallBlindSeat = seatIdSeatMap.get(smallBlindSeatId);
            }
        }

        bigBlindSeatId = (smallBlindSeatId + 1) % maxSeatCount;
        SeatDO bigBlindSeat = seatIdSeatMap.get(bigBlindSeatId);
        while (bigBlindSeat == null) {
            bigBlindSeatId = (bigBlindSeatId + 1) % maxSeatCount;
            bigBlindSeat = seatIdSeatMap.get(bigBlindSeatId);
        }
    }

    private void initCardList() {
        System.out.println("TexasHoldemGameRoomDO initCardList()");
        cardList.clear();
        for (int i = 0; i < 52; i++) {
            cardList.add(i);
        }
    }

    private void initAllSeat() {
        System.out.println("TexasHoldemGameRoomDO initAllSeatBetMoney()");
        for (Integer key : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(key);
            if (seatDO == null) {
                continue;
            }
            seatDO.setOperation(OPERATION_NONE);
            seatDO.setTotalBet(0);
            seatDO.setRoundBet(0);
        }
    }

    private void initAllSeatCardList() {
        System.out.println("TexasHoldemGameRoomDO initAllSeatCardList()");
        for (int i = 0; i < maxSeatCount; i++) {
            seatIdHandCardMap.put(i, new TexasHoldEmHandCardDO());
        }
    }

    private void startPreFlopStatus() {
        System.out.println("TexasHoldemGameRoomDO startPreFlopStatus()");
        roomStatus = ROOM_STATUS_PRE_FLOP;
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_CHANGE_STATUS, String.valueOf(roomStatus));

        changeAllSeatStatus(SEAT_STATUS_GAMING);

        initCardList();
        publicCardList.clear();
        initAllSeatCardList();
        initAllSeat();
        initBlindSeatId();
        initRoundStartSeatId();
        currentOperationSeatId = roundStartSeatId;

        roomGameBet = 0;
        roundCallBet = smallBlindBet * 2;
        roundRaiseBet = 100;
        dealPlayerCard();
        notifySeatCardInfo();
        initSmallBlindSeatBet();
        initBigBlindSeatBet();

        notifyRoomInfo();
        notifySeatStartOperation();
        startWaitPositionOperation();
    }

    private void startFlopStatus() {
        System.out.println("TexasHoldemGameRoomDO startFlopStatus()");
        roomStatus = ROOM_STATUS_FLOP;
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_CHANGE_STATUS, String.valueOf(roomStatus));

        clearAllSeatRoundBet();
        roundCallBet = 0;
        roundRaiseBet = 100;

        List<Integer> dealCardList = dealPublicCard(3);
        publicCardList.addAll(dealCardList);
        addPublicCardToAllSeatHandCard(dealCardList);
        notifyPublicCardToAllSeat(dealCardList);
        notifyAllSeatSelfCardType();
        notifySeatStartOperation();
        startWaitPositionOperation();
    }

    private void startTurnStatus() {
        System.out.println("TexasHoldemGameRoomDO startTurnStatus()");
        roomStatus = ROOM_STATUS_TURN;
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_CHANGE_STATUS, String.valueOf(roomStatus));

        clearAllSeatRoundBet();
        roundCallBet = 0;
        roundRaiseBet = 100;

        List<Integer> dealCardList = dealPublicCard(1);
        publicCardList.addAll(dealCardList);
        addPublicCardToAllSeatHandCard(dealCardList);
        notifyPublicCardToAllSeat(dealCardList);
        notifyAllSeatSelfCardType();
        notifySeatStartOperation();
        startWaitPositionOperation();
    }

    private void startRiverStatus() {
        System.out.println("TexasHoldemGameRoomDO startRiverStatus()");
        roomStatus = ROOM_STATUS_RIVER;
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_CHANGE_STATUS, String.valueOf(roomStatus));

        clearAllSeatRoundBet();
        roundCallBet = 0;
        roundRaiseBet = 100;

        List<Integer> dealCardList = dealPublicCard(1);
        publicCardList.addAll(dealCardList);
        addPublicCardToAllSeatHandCard(dealCardList);
        notifyPublicCardToAllSeat(dealCardList);
        notifyAllSeatSelfCardType();
        notifySeatStartOperation();
        startWaitPositionOperation();
    }

    private void startOpenAllSeatCard() {
        System.out.println("TexasHoldemGameRoomDO openAllSeatCard()");
        currentOperationSeatId = -1;
        roomStatus = ROOM_STATUS_OPEN_CARD;
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_CHANGE_STATUS, String.valueOf(roomStatus));

        int liveCount = 0;
        HashMap<Integer, TexasHoldEmHandCardResponseDO> map = new HashMap<>();
        for (Integer seatId : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO.getOperation() == OPERATION_CALL || seatDO.getOperation() == OPERATION_RAISE || seatDO.getOperation() == OPERATION_ALL_IN) {
                liveCount = liveCount + 1;
            }
        }

        if (liveCount == 1) {
            return;
        }

        for (Integer seatId : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO.getOperation() == OPERATION_CALL || seatDO.getOperation() == OPERATION_RAISE || seatDO.getOperation() == OPERATION_ALL_IN) {
                TexasHoldEmHandCardDO texasHoldemHandCardDO = seatIdHandCardMap.get(seatId);
                TexasHoldEmHandCardResponseDO texasHoldemHandCardResponseDO = new TexasHoldEmHandCardResponseDO();
                texasHoldemHandCardResponseDO.setCardList(texasHoldemHandCardDO.getHandCardList());
                texasHoldemHandCardResponseDO.setCardType(texasHoldemHandCardDO.getCardType());
                map.put(seatId, texasHoldemHandCardResponseDO);
            }
        }


        String message = new Gson().toJson(map);
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_SEAT_CARD, message);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startAllocatePot();
    }

    private void startAllocatePot() {
        System.out.println("TexasHoldemGameRoomDO startAllocatePot()");
        roomStatus = ROOM_STATUS_ALLOCATE_POT;
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_CHANGE_STATUS, String.valueOf(roomStatus));

        currentOperationSeatId = -1;
        List<TexasHoldEmPotDo> potPoolList = createPotPoolList();
        List<TexasHoldEmWinPotBetResponseDO> winPotBetResponseDOList = allocateWinnerPotBetToSeat(potPoolList);
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_WIN_POT_BET, new Gson().toJson(winPotBetResponseDOList));

        try {
            Thread.sleep((winPotBetResponseDOList.size() * 1000) + 2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startWaitingStatus();
    }

    private void clearAllSeatRoundBet() {
        System.out.println("TexasHoldemGameRoomDO clearAllSeatRoundBet()");
        for (int seatId = 0; seatId < maxSeatCount; seatId++) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO == null || seatDO.getStatus() != SEAT_STATUS_GAMING) {
                continue;
            }
            seatDO.setRoundBet(0);
        }
    }

    private void dealPlayerCard() {
        System.out.println("TexasHoldemGameRoomDO dealPlayerCard()");
        Random random = new Random();
        for (int seatId = 0; seatId < maxSeatCount; seatId++) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO == null || seatDO.getStatus() != SEAT_STATUS_GAMING) {
                continue;
            }
            List<Integer> handCard = new ArrayList<>();
            int count = 0;
            while (count != 2) {
                int cardIndex = random.nextInt(cardList.size());
                Integer card = cardList.get(cardIndex);
                handCard.add(card);
                cardList.remove(cardIndex);
                count++;
            }
            seatIdHandCardMap.get(seatId).addHandCard(handCard);
        }
    }

    private void notifySeatCardInfo() {
        System.out.println("TexasHoldemGameRoomDO notifyCardInfo()");
        for (Integer seatId : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO != null) {
                long playerId = seatDO.getPlayerId();
                TexasHoldEmHandCardDO texasHoldemHandCardDO = seatIdHandCardMap.get(seatId);
                TexasHoldEmHandCardResponseDO texasHoldemHandCardResponseDO = new TexasHoldEmHandCardResponseDO();
                texasHoldemHandCardResponseDO.setCardType(texasHoldemHandCardDO.getCardType());
                texasHoldemHandCardResponseDO.setCardList(texasHoldemHandCardDO.getHandCardList());
                HashMap<Integer, TexasHoldEmHandCardResponseDO> seatIdHandCardMap = new HashMap<>();
                seatIdHandCardMap.put(seatId, texasHoldemHandCardResponseDO);
                String message = new Gson().toJson(seatIdHandCardMap);
                notifyStatusResponse(playerId, RESPONSE_STATUS_SEAT_CARD, message);
            }
        }
    }

    private void initSmallBlindSeatBet() {
        System.out.println("TexasHoldemGameRoomDO initSmallBlindSeatBet()");
        SeatDO seatDO = seatIdSeatMap.get(smallBlindSeatId);
        long bet = smallBlindBet;
        seatDO.setMoney(seatDO.getMoney() - bet);
        seatDO.setTotalBet(bet);
        seatDO.setRoundBet(bet);
        roomGameBet = roomGameBet + bet;
    }


    private void initBigBlindSeatBet() {
        System.out.println("TexasHoldemGameRoomDO initBigBlindSeatBet()");
        SeatDO seatDO = seatIdSeatMap.get(bigBlindSeatId);
        long bet = smallBlindBet * 2;
        seatDO.setMoney(seatDO.getMoney() - bet);
        seatDO.setTotalBet(bet);
        seatDO.setRoundBet(bet);
        roomGameBet = roomGameBet + bet;
    }

    private void initRoundStartSeatId() {
        System.out.println("TexasHoldemGameRoomDO initRoundStartSeatId()");
        roundStartSeatId = (bigBlindSeatId + 1) % maxSeatCount;
        SeatDO seatDO = seatIdSeatMap.get(roundStartSeatId);
        while (seatDO == null || seatDO.getStatus() == SEAT_STATUS_WAITING) {
            roundStartSeatId = (roundStartSeatId + 1) % maxSeatCount;
            seatDO = seatIdSeatMap.get(roundStartSeatId);
        }
    }

    private void startWaitPositionOperation() {
        System.out.println("TexasHoldemGameRoomDO startWaitPositionOperation()");
        seatWaitOperation = new SeatWaitOperationDO(currentOperationSeatId, false, OPERATION_WAIT_TIME) {
            @Override
            public void waitOperation(int seatId, long lastOperationTime) {
                notifyWaitOperation(seatId, lastOperationTime);
            }

            @Override
            public void startFoldOperation(int seatId) {
                operationFold(seatIdSeatMap.get(seatId).getPlayerId());
            }
        };
        seatWaitOperation.run();
    }


    private void changeSeatPosition() {
        System.out.println("TexasHoldemGameRoomDO changeSeatPosition()");
        int canBetSeatCount = countGamingSeatSize();
        System.out.println("TexasHoldemGameRoomDO changeSeatPosition(): canBetSeatCount = " + canBetSeatCount);
        if (canBetSeatCount <= 1) {
            //處理蓋排、離開遊戲房間
            startAllocatePot();
            return;
        }

        boolean isAllSeatAllIn = isAllSeatAllIn();
        if (isAllSeatAllIn) {
            //處理All In
            int dealPublicCardCount = 0;
            if (roomStatus == ROOM_STATUS_PRE_FLOP) {
                dealPublicCardCount = 5;
            } else if (roomStatus == ROOM_STATUS_FLOP) {
                dealPublicCardCount = 2;
            } else if (roomStatus == ROOM_STATUS_TURN) {
                dealPublicCardCount = 1;
            }
            if (dealPublicCardCount > 0) {
                List<Integer> dealCardList = dealPublicCard(dealPublicCardCount);
                publicCardList.addAll(dealCardList);
                addPublicCardToAllSeatHandCard(dealCardList);
                notifyPublicCardToAllSeat(dealCardList);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            startOpenAllSeatCard();
            return;
        }

        currentOperationSeatId = currentOperationSeatId + 1 % maxSeatCount;
        SeatDO seatDO = seatIdSeatMap.get(currentOperationSeatId);
        while (seatDO == null ||
                seatDO.getStatus() == SEAT_STATUS_WAITING ||
                seatDO.getStatus() == SEAT_STATUS_EXIT ||
                seatDO.getStatus() == SEAT_STATUS_STAND_UP ||
                seatDO.getOperation() == OPERATION_ALL_IN) {
            currentOperationSeatId = (currentOperationSeatId + 1) % maxSeatCount;
            seatDO = seatIdSeatMap.get(currentOperationSeatId);
        }

        if (currentOperationSeatId == roundStartSeatId) {
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
                    startOpenAllSeatCard();
                    break;
            }
        } else {
            notifySeatStartOperation();
            startWaitPositionOperation();
        }
    }

    private boolean isAllSeatAllIn() {
        int gamingSeatSize = 0;
        int allInSeatSize = 0;
        int roundBeatSeatSize = 0;
        for (Integer seatId : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO != null && seatDO.getStatus() == SEAT_STATUS_GAMING) {
                gamingSeatSize = gamingSeatSize + 1;
                if (seatDO.getOperation() == OPERATION_ALL_IN) {
                    allInSeatSize = allInSeatSize + 1;
                } else {
                    if (seatDO.getRoundBet() == roundCallBet) {
                        roundBeatSeatSize = roundBeatSeatSize + 1;
                    }
                }
            }
        }

        if (roundBeatSeatSize > 1) {
            //還有兩位有錢人在下注
            return false;
        }

        return gamingSeatSize == allInSeatSize + roundBeatSeatSize;
    }

    private int countCanBetSeatSize() {
        System.out.println("TexasHoldemGameRoomDO countBetSeatSize()");
        int size = 0;
        for (int i = 0; i < maxSeatCount; i++) {
            SeatDO seatDO = seatIdSeatMap.get(i);
            if (seatDO != null && seatDO.getStatus() == SEAT_STATUS_GAMING &&
                    (seatDO.getOperation() == OPERATION_NONE || seatDO.getOperation() == OPERATION_CALL || seatDO.getOperation() == OPERATION_RAISE)) {
                size = size + 1;
            }
        }
        return size;
    }

    private int countGamingSeatSize() {
        System.out.println("TexasHoldemGameRoomDO countGamingSeatSize()");
        int size = 0;
        for (int i = 0; i < maxSeatCount; i++) {
            SeatDO seatDO = seatIdSeatMap.get(i);
            if (seatDO != null && seatDO.getStatus() == SEAT_STATUS_GAMING) {
                size = size + 1;
            }
        }
        return size;
    }

    private List<Integer> dealPublicCard(int dealCardSize) {
        System.out.println("TexasHoldemGameRoomDO dealPublicCard()");
        List<Integer> dealCardList = new ArrayList<>();
        int i = 0;
        while (i < dealCardSize) {
            int dealCardIndex = new Random().nextInt(cardList.size());
            Integer card = cardList.get(dealCardIndex);
            cardList.remove(dealCardIndex);
            dealCardList.add(card);
            i = i + 1;
        }
        return dealCardList;
    }

    private void addPublicCardToAllSeatHandCard(List<Integer> publicCardList) {
        System.out.println("TexasHoldemGameRoomDO addPublicCardToAllSeatHandCard()");
        for (Integer seatId : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO.getStatus() == SEAT_STATUS_GAMING) {
                TexasHoldEmHandCardDO texasHoldemHandCardDO = seatIdHandCardMap.get(seatId);
                texasHoldemHandCardDO.addPublicCard(publicCardList);
            }
        }
    }

    private List<TexasHoldEmPotDo> createPotPoolList() {
        System.out.println("TexasHoldemGameRoomDO createPotPoolList()");
        Set<Long> seatBetSet = new HashSet<>();
        for (Integer seatId : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            long seatBet = seatDO.getTotalBet();
            if (seatBet > 0) {
                seatBetSet.add(seatDO.getTotalBet());
            }
        }

        List<Long> seatBetList = new ArrayList<>(seatBetSet);
        seatBetList.sort((bet1, bet2) -> {
            if (bet1 > bet2) {
                return 1;
            } else if (bet1.equals(bet2)) {
                return 0;
            } else {
                return -1;
            }
        });

        List<TexasHoldEmPotDo> potDoList = new ArrayList<>();
        for (Long bet : seatBetList) {
            TexasHoldEmPotDo potDo = new TexasHoldEmPotDo();
            potDo.setPotBet(bet);
            potDoList.add(potDo);

            for (Integer seatId : seatIdSeatMap.keySet()) {
                SeatDO seatDO = seatIdSeatMap.get(seatId);
                if (seatDO.getTotalBet() >= bet) {
                    potDo.setBetPlayerSize(potDo.getBetPlayerSize() + 1);
                    if (seatDO.getStatus() == SEAT_STATUS_GAMING) {
                        potDo.addHandCard(seatId, seatIdHandCardMap.get(seatId));
                    }
                }
            }
        }

        for (int i = 0; i < potDoList.size(); i++) {
            TexasHoldEmPotDo potDo = potDoList.get(i);
            long bet = seatBetList.get(i);
            long preBet = 0;
            if (i > 0) {
                preBet = seatBetList.get(i - 1);
            }
            potDo.setBetMoney((bet - preBet) * potDo.getBetPlayerSize());
        }

        return potDoList;
    }

    private List<TexasHoldEmWinPotBetResponseDO> allocateWinnerPotBetToSeat(List<TexasHoldEmPotDo> potList) {
        System.out.println("TexasHoldemGameRoomDO allocatePotBetToSeat()");
        List<TexasHoldEmWinPotBetResponseDO> list = new ArrayList<>();
        for (TexasHoldEmPotDo potDo : potList) {
            List<Integer> winnerSeatIdList = potDo.getWinnerSeatIdList();
            if (winnerSeatIdList.size() == 0) {
                //最大注的人離開遊戲
                continue;
            }

            long potWinnerBet = potDo.getBetMoney() / winnerSeatIdList.size();
            TexasHoldEmWinPotBetResponseDO winPotResponseDO = new TexasHoldEmWinPotBetResponseDO();
            winPotResponseDO.setWinnerSeatIdList(winnerSeatIdList);
            winPotResponseDO.setWinBet(potWinnerBet);
            list.add(winPotResponseDO);

            for (Integer seatId : winnerSeatIdList) {
                SeatDO seatDO = seatIdSeatMap.get(seatId);
                seatDO.setMoney(seatDO.getMoney() + potWinnerBet);
            }
        }
        return list;
    }

    public synchronized boolean operation(long playerId, int operation, long bet) {
        System.out.println("TexasHoldemGameRoomDO operation(): playerId = " + playerId + ", operation = " + operation + ", bet = " + bet);

        switch (operation) {
            case OPERATION_EXIT:
                return operationExit(playerId);
            case OPERATION_CALL:
                return operationCall(playerId, bet);
            case OPERATION_RAISE:
                return operationRaise(playerId, bet);
            case OPERATION_FOLD:
                return operationFold(playerId);
            case OPERATION_STAND_UP:
                return operationStanUp(playerId);
            case OPERATION_SIT_DOWN:
                return operationSitDown(playerId);
        }
        return false;
    }

    private boolean operationExit(long playerId) {
        System.out.println("TexasHoldemGameRoomDO operationExit()");
        Integer seatId = playerIdSeatIdMap.get(playerId);
        if (seatId == null) {
            return false;
        }

        if (seatId == currentOperationSeatId) {
            seatWaitOperation.setOperation(true);
        }
        new Thread(() -> {
            exitGame(playerId);
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if(seatDO.getStatus() == SEAT_STATUS_GAMING) {
                //最後計算排持要用到，所以保留
                seatDO.setPlayerName("");
                seatDO.setStatus(SEAT_STATUS_EXIT);
                seatDO.setOperation(OPERATION_EXIT);
                notifySeatOperation(seatId, OPERATION_EXIT, 0, 0);
                notifySeatInfo(seatId);
            } else {
                seatIdSeatMap.remove(seatId);
            }
            notifyRoomInfo();
            if (roomStatus == ROOM_STATUS_PRE_FLOP ||
                    roomStatus == ROOM_STATUS_FLOP ||
                    roomStatus == ROOM_STATUS_TURN ||
                    roomStatus == ROOM_STATUS_RIVER) {
                int canBetSeatSize = countCanBetSeatSize();
                if (canBetSeatSize == 1) {
                    seatWaitOperation.setOperation(true);
                    startAllocatePot();
                } else {
                    if (seatId == currentOperationSeatId) {
                        changeSeatPosition();
                    }
                }
            }
        }).start();
        return true;
    }

    private boolean operationCall(long playerId, long bet) {
        System.out.println("TexasHoldemGameRoomDO operationCall()");
        Integer seatId = playerIdSeatIdMap.get(playerId);
        if (seatId == null) {
            return false;
        }
        if (seatId != currentOperationSeatId) {
            return false;
        }
        seatWaitOperation.setOperation(true);
        new Thread(() -> {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            PlayerDO playerDO = PlayerManager.getInstance().findById(playerId);
            long playerMoney = playerDO.getMoney() - bet;
            playerDO.setMoney(playerMoney);

            seatDO.setTotalBet(seatDO.getTotalBet() + bet);
            seatDO.setRoundBet(seatDO.getRoundBet() + bet);

            long seatMoney = seatDO.getMoney() - bet;
            seatDO.setMoney(seatMoney);

            if (seatMoney == 0) {
                seatDO.setOperation(OPERATION_ALL_IN);
                notifySeatOperation(seatId, OPERATION_ALL_IN, seatDO.getMoney(), seatDO.getRoundBet());
            } else {
                seatDO.setOperation(OPERATION_CALL);
                notifySeatOperation(seatId, OPERATION_CALL, seatDO.getMoney(), seatDO.getRoundBet());
            }

            this.roomGameBet = this.roomGameBet + bet;
            notifySeatInfo(seatId);
            changeSeatPosition();
        }).start();
        return true;
    }

    private boolean operationRaise(long playerId, long bet) {
        System.out.println("TexasHoldemGameRoomDO operationCall()");
        Integer seatId = playerIdSeatIdMap.get(playerId);
        if (seatId == null) {
            return false;
        }
        if (seatId != currentOperationSeatId) {
            return false;
        }
        seatWaitOperation.setOperation(true);
        new Thread(() -> {
            this.roundStartSeatId = seatId;
            SeatDO seatDO = seatIdSeatMap.get(seatId);

            PlayerDO playerDO = PlayerManager.getInstance().findById(playerId);
            long playerMoney = playerDO.getMoney() - bet;
            playerDO.setMoney(playerMoney);

            seatDO.setTotalBet(seatDO.getTotalBet() + bet);
            seatDO.setRoundBet(seatDO.getRoundBet() + bet);

            long seatMoney = seatDO.getMoney() - bet;
            seatDO.setMoney(seatMoney);

            long seatRoundBet = seatDO.getRoundBet();
            if (seatRoundBet > this.roundCallBet) {
                this.roundCallBet = seatRoundBet;
            }
            this.roomGameBet = this.roomGameBet + bet;
            this.roundRaiseBet = bet * 2;

            System.out.println("TexasHoldemGameRoomDO operationCall(): this.roundRaiseBet = " + this.roundRaiseBet);
            if (seatMoney == 0) {
                seatDO.setOperation(OPERATION_ALL_IN);
                notifySeatOperation(seatId, OPERATION_ALL_IN, seatDO.getMoney(), seatDO.getRoundBet());
            } else {
                seatDO.setOperation(OPERATION_RAISE);
                notifySeatOperation(seatId, OPERATION_RAISE, seatDO.getMoney(), seatDO.getRoundBet());
            }

            notifySeatInfo(seatId);
            changeSeatPosition();
        }).start();
        return true;
    }

    private boolean operationFold(long playerId) {
        System.out.println("TexasHoldemGameRoomDO operationFold()");
        Integer seatId = playerIdSeatIdMap.get(playerId);
        if (seatId == null) {
            return false;
        }

        if (seatId != currentOperationSeatId) {
            System.out.println("TexasHoldemGameRoomDO operationFold(): seatId != currentOperationSeatId");
            return false;
        }
        if (roomStatus == ROOM_STATUS_WAITING ||
                roomStatus == ROOM_STATUS_ALLOCATE_POT) {
            return false;
        }

        seatWaitOperation.setOperation(true);
        new Thread(() -> {
            notifySeatOperation(seatId, OPERATION_FOLD, 0,0);
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            seatDO.setStatus(SEAT_STATUS_WAITING);
            seatDO.setOperation(OPERATION_FOLD);
            notifySeatInfo(seatId);
            changeSeatPosition();
        }).start();
        return true;
    }

    private boolean operationStanUp(long playerId) {
        System.out.println("TexasHoldemGameRoomDO operationStanUp()");
        Integer seatId = playerIdSeatIdMap.get(playerId);
        if (seatId == null) {
            return false;
        }

        if (seatId == currentOperationSeatId) {
            seatWaitOperation.setOperation(true);
        }
        new Thread(() -> {
            playerIdSeatIdMap.remove(playerId);

            SeatDO seatDO = seatIdSeatMap.get(seatId);
            PlayerDO playerDO = PlayerManager.getInstance().findById(seatDO.getPlayerId());
            playerDO.setMoney(playerDO.getMoney() + seatDO.getMoney());
            if(seatDO.getStatus() == SEAT_STATUS_GAMING) {
                //最後計算排持要用到，所以保留
                seatDO.setPlayerName("");
                seatDO.setStatus(SEAT_STATUS_STAND_UP);
                seatDO.setOperation(OPERATION_STAND_UP);
                notifySeatOperation(seatId, OPERATION_STAND_UP, 0,0);
                notifySeatInfo(seatId);
            } else {
                seatIdSeatMap.remove(seatId);
            }
            notifyRoomInfo();
            if (roomStatus == ROOM_STATUS_PRE_FLOP ||
                    roomStatus == ROOM_STATUS_FLOP ||
                    roomStatus == ROOM_STATUS_TURN ||
                    roomStatus == ROOM_STATUS_RIVER) {
                int canBetSeatSize = countCanBetSeatSize();
                if (canBetSeatSize == 1) {
                    seatWaitOperation.setOperation(true);
                    startAllocatePot();
                } else {
                    if (seatId == currentOperationSeatId) {
                        changeSeatPosition();
                    }
                }
            }
        }).start();
        return true;
    }

    private boolean operationSitDown(long playerId) {
        List<Integer> emptySeatIdList = findEmptySeatIdList();
        if (emptySeatIdList.size() > 0) {
            int emptySeatId = new Random().nextInt(emptySeatIdList.size());
            sitDown(playerId, emptySeatIdList.get(emptySeatId));
        } else {
            notifyNoSeat(playerId);
        }
        return true;
    }

    @Override
    protected void notifyRoomInfo(long playerId) {
        TexasHoldEmRoomInfoResponseDO texasHoldemRoomInfoResponseDO = new TexasHoldEmRoomInfoResponseDO();
        texasHoldemRoomInfoResponseDO.setRoomStatus(roomStatus);
        texasHoldemRoomInfoResponseDO.setSmallBlindSeatId(smallBlindSeatId);
        texasHoldemRoomInfoResponseDO.setBigBlindSeatId(bigBlindSeatId);
        texasHoldemRoomInfoResponseDO.setCurrentOperationSeatId(currentOperationSeatId);
        texasHoldemRoomInfoResponseDO.setRoomBet(roomGameBet);
        texasHoldemRoomInfoResponseDO.setPublicCardList(publicCardList);
        texasHoldemRoomInfoResponseDO.setSeatIdSeatMap(seatIdSeatMap);
        String message = new Gson().toJson(texasHoldemRoomInfoResponseDO);
        notifyStatusResponse(playerId, RESPONSE_STATUS_ENTER_ROOM_INFO, message);
    }

    @Override
    protected void notifySitDown(long playerId, int seatId) {
        notifyStatusResponse(playerId, RESPONSE_STATUS_SIT_DOWN, String.valueOf(seatId));
    }

    protected void notifySeatInfo(int seatId) {
        System.out.println("TexasHoldemGameRoomDO notifySeatInfo()");
        SeatDO seatDO = seatIdSeatMap.get(seatId);
        String message = new Gson().toJson(seatDO);
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_SEAT_INFO, message);
    }

    protected void notifyRoomInfo() {
        System.out.println("TexasHoldemGameRoomDO notifyRoomInfo()");
        TexasHoldEmRoomInfoResponseDO texasHoldemRoomInfoResponseDO = new TexasHoldEmRoomInfoResponseDO();
        texasHoldemRoomInfoResponseDO.setRoomStatus(roomStatus);
        texasHoldemRoomInfoResponseDO.setSmallBlindSeatId(smallBlindSeatId);
        texasHoldemRoomInfoResponseDO.setBigBlindSeatId(bigBlindSeatId);
        texasHoldemRoomInfoResponseDO.setCurrentOperationSeatId(currentOperationSeatId);
        texasHoldemRoomInfoResponseDO.setRoomBet(roomGameBet);
        texasHoldemRoomInfoResponseDO.setPublicCardList(publicCardList);
        texasHoldemRoomInfoResponseDO.setSeatIdSeatMap(seatIdSeatMap);
        String message = new Gson().toJson(texasHoldemRoomInfoResponseDO);
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_ROOM_INFO, message);
    }

    @Override
    protected void notifyNoSeat(long playerId) {
        TexasHoldemGameService.TexasHoldemGameStatusResponse.Builder builder =
                TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder();
        builder.setStatus(TexasHoldEmGameRoomDO.RESPONSE_STATUS_NO_SEAT);
        TexasHoldemGameService.TexasHoldemGameStatusResponse response = builder.build();
        playerIdObserverMap.get(playerId).onNext(response);
    }

    private void notifySeatStartOperation() {
        System.out.println("TexasHoldemGameRoomDO notifySeatStartOperation()");
        SeatDO seatDO = seatIdSeatMap.get(currentOperationSeatId);
        TexasHoldEmStartOperationResponseDO startOperationResponseDO = new TexasHoldEmStartOperationResponseDO();
        startOperationResponseDO.setSeatId(currentOperationSeatId);

        if ((seatDO.getMoney() + seatDO.getRoundBet()) > this.roundCallBet) {
            startOperationResponseDO.setCanRaise(true);
            if (seatDO.getMoney() >= roundRaiseBet) {
                startOperationResponseDO.setMinRaiseBet(roundRaiseBet);
            } else {
                startOperationResponseDO.setMinRaiseBet(seatDO.getMoney());
            }
            startOperationResponseDO.setMaxRaiseBet(seatDO.getMoney());
            startOperationResponseDO.setCallBet(roundCallBet - seatDO.getRoundBet());
        } else {
            startOperationResponseDO.setCanRaise(false);
            startOperationResponseDO.setCallBet(seatDO.getMoney());
        }

        startOperationResponseDO.setLastOperationTime(OPERATION_WAIT_TIME);
        String message = new Gson().toJson(startOperationResponseDO);
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_START_OPERATION, message);
    }

    private void notifyWaitOperation(int seatId, long lastOperationTime) {
        TexasHoldEmWaitOperationResponseDO waitOperationResponseDO = new TexasHoldEmWaitOperationResponseDO();
        waitOperationResponseDO.setSeatId(seatId);
        waitOperationResponseDO.setLastOperationTime(lastOperationTime);
        String message = new Gson().toJson(waitOperationResponseDO);
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_WAIT_SEAT_OPERATION, message);
    }

    private void notifySeatOperation(int seatId, int operation, long money, long bet) {
        System.out.println("TexasHoldemGameRoomDO notifySeatStartOperation()");
        TexasHoldEmSeatOperationResponseDO texasHoldEmSeatOperationDO = new TexasHoldEmSeatOperationResponseDO();
        texasHoldEmSeatOperationDO.setSeatId(seatId);
        texasHoldEmSeatOperationDO.setOperation(operation);
        texasHoldEmSeatOperationDO.setBet(bet);
        texasHoldEmSeatOperationDO.setRoomGameBet(this.roomGameBet);
        String message = new Gson().toJson(texasHoldEmSeatOperationDO);
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_SEAT_OPERATION, message);
    }

    private void notifyPublicCardToAllSeat(List<Integer> dealCardList) {
        System.out.println("TexasHoldemGameRoomDO notifyPublicCardToAllSeat()");
        Gson gson = new Gson();
        String message = gson.toJson(dealCardList);
        notifyAllPlayerStatusResponse(RESPONSE_STATUS_PUBLIC_CARD, message);
    }

    private void notifyAllSeatSelfCardType() {
        System.out.println("TexasHoldemGameRoomDO notifyAllSeatSelfCardType()");
        for (Integer seatId : seatIdSeatMap.keySet()) {
            Map<Integer, TexasHoldEmHandCardResponseDO> map = new HashMap<>();
            TexasHoldEmHandCardDO handCardDO = seatIdHandCardMap.get(seatId);
            TexasHoldEmHandCardResponseDO texasHoldEmHandCardResponseDO = new TexasHoldEmHandCardResponseDO();
            texasHoldEmHandCardResponseDO.setCardType(handCardDO.getCardType());
            texasHoldEmHandCardResponseDO.setCardList(handCardDO.getHandCardList());

            map.put(seatId, texasHoldEmHandCardResponseDO);
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            String message = new Gson().toJson(map);
            notifyStatusResponse(seatDO.getPlayerId(), RESPONSE_STATUS_SEAT_CARD, message);
        }
    }

    private void notifyAllPlayerStatusResponse(int status, String message) {
        for (Long playerId : playerIdObserverMap.keySet()) {
            notifyStatusResponse(playerId, status, message);
        }
    }


    private synchronized boolean notifyStatusResponse(long playerId, int status, String message) {
//        System.out.println("TexasHoldEmGameRoomDO notifyStatusResponse(): playerId = " + playerId + ", status = " + status + ", message = " + message);
        StreamObserver streamObserver = playerIdObserverMap.get(playerId);
        if (streamObserver == null) {
            return false;
        }
        TexasHoldemGameService.TexasHoldemGameStatusResponse.Builder builder =
                TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder();
        builder.setStatus(status);
        if (!StringUtil.isNullOrEmpty(message)) {
            builder.setMessage(message);
        }
        TexasHoldemGameService.TexasHoldemGameStatusResponse response = builder.build();
        try {
            streamObserver.onNext(response);
            return true;
        } catch (Exception e) {
            System.out.println("TexasHoldEmGameRoomDO notifyStatusResponse() e: " + e.getMessage());
//            e.printStackTrace();
            return false;
        }
    }

}
