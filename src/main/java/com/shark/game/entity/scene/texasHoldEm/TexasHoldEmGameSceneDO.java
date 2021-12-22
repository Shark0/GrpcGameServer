package com.shark.game.entity.scene.texasHoldEm;

import com.shark.game.entity.scene.seat.BaseSeatSceneDO;
import com.shark.game.entity.scene.seat.SeatDO;
import com.shark.game.entity.scene.seat.SeatWaitOperationDO;
import com.shark.game.entity.scene.texasHoldEm.agent.TexasHoldEmAgentDO;
import com.shark.game.entity.scene.texasHoldEm.agent.TexasHoldEmGameAsset;
import com.shark.game.entity.scene.texasHoldEm.agent.TexasHoldEmRobotAgentDO;
import com.shark.game.entity.scene.texasHoldEm.response.*;
import com.shark.game.manager.SceneManager;

import java.util.*;

public class TexasHoldEmGameSceneDO extends BaseSeatSceneDO<TexasHoldEmAgentDO> implements TexasHoldEmGameActionListener {

    public static final int ROOM_STATUS_PRE_FLOP = 1, ROOM_STATUS_FLOP = 2, ROOM_STATUS_TURN = 3, ROOM_STATUS_RIVER = 4,
            ROOM_STATUS_OPEN_CARD = 5, ROOM_STATUS_ALLOCATE_POT = 6;

    public static final int OPERATION_NONE = -1, OPERATION_EXIT = 0, OPERATION_CALL = 1, OPERATION_RAISE = 2, OPERATION_ALL_IN = 3,
            OPERATION_FOLD = 4, OPERATION_STAND_UP = 5, OPERATION_SIT_DOWN = 6;

    private final int OPERATION_WAIT_TIME = 20000;

    private final long smallBlindBet;

    private long sceneGameBet, roundCallBet, roundRaiseBet;

    private int smallBlindSeatId = -1, bigBlindSeatId = -1, roundStartSeatId = -1, currentOperationSeatId = -1;

    private final List<Integer> cardList = new ArrayList<>();

    private final List<Integer> publicCardList = new ArrayList<>();

    private final Map<Integer, TexasHoldEmHandCardDO> seatIdHandCardMap = new HashMap<>();

    private SeatWaitOperationDO seatWaitOperation;

    private Set<Integer> hasActionSeatSet = new HashSet<>();

    public TexasHoldEmGameSceneDO(int gameType, int smallBlindBet, int maxQueueCount, int minQueueCount) {
        super(gameType, maxQueueCount, minQueueCount);
        this.smallBlindBet = smallBlindBet;
    }

    @Override
    protected void removeDeadAgent() {
        //System.out.println("TexasHoldemGameSceneDO removeDeadAgent()");
        List<TexasHoldEmAgentDO> deadAgentList = new ArrayList<>();
        for(TexasHoldEmAgentDO agentDO: agentList) {
            boolean success = agentDO.notifyCheckLive();
            if (!success) {
                deadAgentList.add(agentDO);
            }
        }

        for(TexasHoldEmAgentDO agentDO: deadAgentList) {
            int seatId = agentDO.getSeatId();
            seatIdSeatMap.remove(seatId);
            seatIdAgentMap.remove(seatId);
            exit(agentDO);
        }
    }

    @Override
    protected void addRobot() {
        TexasHoldEmGameAsset asset = new TexasHoldEmGameAsset();
        asset.setMoney(new Random().nextInt(16) * 1000 + 4000);
        StringBuilder nameStringBuilder = new StringBuilder();
        int nameSize = new Random().nextInt(10) + 3;
        String[] nameArray = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
                "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        for(int i = 0; i < nameSize; i ++) {
            nameStringBuilder.append(nameArray[new Random().nextInt(nameArray.length)]);
        }
        String name =  nameStringBuilder.toString();
        TexasHoldEmRobotAgentDO agentDO =
                new TexasHoldEmRobotAgentDO(SceneManager.SCENE_TEXAS_HOLDEM, name, asset);
        agentDO.setBigBlindBet(smallBlindBet * 2);
        agentDO.setActionListener(this);
        robotAgentList.add(agentDO);
        enter(agentDO);
    }

    @Override
    protected void removeRobot() {
        if(robotAgentList.isEmpty()) {
            return;
        }
        TexasHoldEmRobotAgentDO agentDO = (TexasHoldEmRobotAgentDO) robotAgentList.get(new Random().nextInt(robotAgentList.size()));
        exit(agentDO);
        robotAgentList.remove(agentDO);
    }

    @Override
    protected void registerSceneStatus(TexasHoldEmAgentDO agentDO) {
        agentDO.setActionListener(this);
    }

    @Override
    protected void notifyEnterScene(TexasHoldEmAgentDO agentDO) {
        agentDO.notifyEnterSceneInfo(sceneStatus);
    }

    protected void notifyAgentSceneInfo(TexasHoldEmAgentDO agent) {
        TexasHoldEmSceneInfoResponseDO responseDO = new TexasHoldEmSceneInfoResponseDO();
        responseDO.setSceneStatus(sceneStatus);
        responseDO.setSmallBlindSeatId(smallBlindSeatId);
        responseDO.setBigBlindSeatId(bigBlindSeatId);
        responseDO.setBigBlindBet(smallBlindBet * 2);
        responseDO.setCurrentOperationSeatId(currentOperationSeatId);
        responseDO.setRoomBet(sceneGameBet);
        responseDO.setPublicCardList(publicCardList);
        HashMap<Integer, TexasHoldEmSeatInfoResponseDO> seatIdSeatMap = new HashMap<>();
        for(Integer seatId: this.seatIdSeatMap.keySet()) {
            SeatDO seatDO = this.seatIdSeatMap.get(seatId);
            TexasHoldEmSeatInfoResponseDO seatInfo = new TexasHoldEmSeatInfoResponseDO();
            seatInfo.setId(seatId);
            seatInfo.setStatus(seatDO.getStatus());
            seatInfo.setAction(seatDO.getAction());
            seatInfo.setRoundBet(seatInfo.getRoundBet());
            TexasHoldEmAgentDO agentDO = seatIdAgentMap.get(seatDO.getId());
            if(agentDO != null) {
                seatInfo.setName(agentDO.getName());
                seatInfo.setMoney(agentDO.getAgentAsset().getMoney());
            }
            seatIdSeatMap.put(seatId, seatInfo);
        }
        responseDO.setSeatIdSeatMap(seatIdSeatMap);
        agent.notifySceneInfo(responseDO);
    }

    @Override
    protected void sitDown(TexasHoldEmAgentDO agent, int seatId) {
        //System.out.println("TexasHoldemGameSceneDO sitDown()");
        allocateAgentSeat(agent, seatId);
        agent.notifySitDown(seatId);
        notifyAllAgentSeatInfo(seatId);
    }

    @Override
    protected void notifyNoSeat(TexasHoldEmAgentDO agent) {
        agent.notifyNoSeat();
    }

    private void standUpAllNoMoneySeat() {
        //System.out.println("TexasHoldemGameSceneDO standUpAllNoMoneySeat()");
        List<Integer> noMoneySeatIdIdList = new ArrayList<>();
        for (Integer seatId : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            TexasHoldEmAgentDO agentDO = seatIdAgentMap.get(seatDO.getId());
            if (agentDO != null && agentDO.getAgentAsset().getMoney() <= 0) {
                noMoneySeatIdIdList.add(seatDO.getId());
            }
        }

        for (Integer seatId : noMoneySeatIdIdList) {
            TexasHoldEmAgentDO agentDO = seatIdAgentMap.get(seatId);
            seatIdAgentMap.remove(seatId);
            seatIdSeatMap.remove(seatId);
            agentDO.standUp();
        }
    }

    @Override
    protected void startWaitingStatus() {
        //System.out.println("TexasHoldemGameSceneDO startWaitingStatus()");
        sceneStatus = ROOM_STATUS_WAITING;
        notifyAllAgentStatusChanged(sceneStatus);
        standUpAllNoMoneySeat();
        super.startWaitingStatus();
    }

    @Override
    protected void startGame() {
        startPreFlopStatus();
    }

    private void initBlindSeatId() {
        //System.out.println("TexasHoldemGameSceneDO initBlindSeatId()");
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
        //System.out.println("TexasHoldemGameSceneDO initCardList()");
        cardList.clear();
        for (int i = 0; i < 52; i++) {
            cardList.add(i);
        }
    }

    private void initAllSeat() {
        //System.out.println("TexasHoldemGameSceneDO initAllSeatBetMoney()");
        for (Integer key : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(key);
            if (seatDO == null) {
                continue;
            }
            seatDO.setAction(OPERATION_NONE);
            seatDO.setTotalBet(0);
            seatDO.setRoundBet(0);
        }
    }

    private void initAllSeatCardList() {
        //System.out.println("TexasHoldemGameSceneDO initAllSeatCardList()");
        for (int i = 0; i < maxSeatCount; i++) {
            seatIdHandCardMap.put(i, new TexasHoldEmHandCardDO());
        }
    }

    private void startPreFlopStatus() {
        //System.out.println("TexasHoldemGameSceneDO startPreFlopStatus()");
        sceneStatus = ROOM_STATUS_PRE_FLOP;
        notifyAllAgentStatusChanged(sceneStatus);

        changeAllSeatStatus(SEAT_STATUS_GAMING);

        initCardList();
        publicCardList.clear();
        initAllSeatCardList();
        initAllSeat();
        initBlindSeatId();
        initRoundStartSeatId();
        currentOperationSeatId = roundStartSeatId;

        sceneGameBet = 0;
        roundCallBet = smallBlindBet * 2;
        roundRaiseBet = smallBlindBet * 2;

        dealPlayerCard();
        notifySeatSelfCardInfo();
        initSmallBlindSeatBet();
        initBigBlindSeatBet();

        notifyAllAgentSceneInfo();
        hasActionSeatSet.clear();
        hasActionSeatSet.add(currentOperationSeatId);
        notifyAllAgentSeatStartOperation();
        startWaitPositionOperation();
    }

    private void startFlopStatus() {
        //System.out.println("TexasHoldemGameSceneDO startFlopStatus()");
        sceneStatus = ROOM_STATUS_FLOP;
        notifyAllAgentStatusChanged(sceneStatus);

        clearAllSeatRoundBet();
        roundCallBet = 0;
        roundRaiseBet = 100;

        List<Integer> dealCardList = dealPublicCard(3);
        publicCardList.addAll(dealCardList);
        addPublicCardToAllSeatHandCard(dealCardList);
        notifyAllAgentPublicCard(dealCardList);
        notifyAllSeatSelfCardType();
        hasActionSeatSet.clear();
        hasActionSeatSet.add(currentOperationSeatId);
        notifyAllAgentSeatStartOperation();
        startWaitPositionOperation();
    }

    private void startTurnStatus() {
        //System.out.println("TexasHoldemGameSceneDO startTurnStatus()");
        sceneStatus = ROOM_STATUS_TURN;
        notifyAllAgentStatusChanged(sceneStatus);

        clearAllSeatRoundBet();
        roundCallBet = 0;
        roundRaiseBet = 100;
        List<Integer> dealCardList = dealPublicCard(1);
        publicCardList.addAll(dealCardList);
        addPublicCardToAllSeatHandCard(dealCardList);
        notifyAllAgentPublicCard(dealCardList);
        notifyAllSeatSelfCardType();
        hasActionSeatSet.clear();
        hasActionSeatSet.add(currentOperationSeatId);
        notifyAllAgentSeatStartOperation();
        startWaitPositionOperation();
    }

    private void startRiverStatus() {
        //System.out.println("TexasHoldemGameSceneDO startRiverStatus()");
        sceneStatus = ROOM_STATUS_RIVER;
        notifyAllAgentStatusChanged(sceneStatus);

        clearAllSeatRoundBet();
        roundCallBet = 0;
        roundRaiseBet = 100;

        List<Integer> dealCardList = dealPublicCard(1);
        publicCardList.addAll(dealCardList);
        addPublicCardToAllSeatHandCard(dealCardList);
        notifyAllAgentPublicCard(dealCardList);
        notifyAllSeatSelfCardType();
        hasActionSeatSet.clear();
        hasActionSeatSet.add(currentOperationSeatId);
        notifyAllAgentSeatStartOperation();
        startWaitPositionOperation();
    }

    private void startOpenAllSeatCard() {
        //System.out.println("TexasHoldemGameSceneDO openAllSeatCard()");
        currentOperationSeatId = -1;
        sceneStatus = ROOM_STATUS_OPEN_CARD;
        notifyAllAgentStatusChanged(sceneStatus);

        HashMap<Integer, TexasHoldEmHandCardResponseDO> seatIdCardMap = new HashMap<>();
        for (Integer seatId : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO.getAction() == OPERATION_CALL || seatDO.getAction() == OPERATION_RAISE || seatDO.getAction() == OPERATION_ALL_IN) {
                TexasHoldEmHandCardDO texasHoldemHandCardDO = seatIdHandCardMap.get(seatId);
                TexasHoldEmHandCardResponseDO texasHoldemHandCardResponseDO = new TexasHoldEmHandCardResponseDO();
                texasHoldemHandCardResponseDO.setCardList(texasHoldemHandCardDO.getHandCardList());
                texasHoldemHandCardResponseDO.setCardType(texasHoldemHandCardDO.getCardType());
                seatIdCardMap.put(seatId, texasHoldemHandCardResponseDO);
            }
        }

        for(TexasHoldEmAgentDO agent: agentList) {
            agent.notifySeatCard(seatIdCardMap);
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startAllocatePot();
    }

    private void startAllocatePot() {
        //System.out.println("TexasHoldemGameSceneDO startAllocatePot()");
        sceneStatus = ROOM_STATUS_ALLOCATE_POT;
        notifyAllAgentStatusChanged(sceneStatus);

        currentOperationSeatId = -1;
        List<TexasHoldEmPotDo> potPoolList = createPotPoolList();
        List<TexasHoldEmWinPotBetResponseDO> winPotBetResponseDOList = allocateWinnerPotBetToSeat(potPoolList);
        for(TexasHoldEmAgentDO agent: agentList) {
            agent.notifyWinPotBet(winPotBetResponseDOList);
        }

        try {
            Thread.sleep((winPotBetResponseDOList.size() * 1000L) + 2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startWaitingStatus();
    }

    private void clearAllSeatRoundBet() {
        //System.out.println("TexasHoldemGameSceneDO clearAllSeatRoundBet()");
        for (int seatId = 0; seatId < maxSeatCount; seatId++) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO == null || seatDO.getStatus() != SEAT_STATUS_GAMING) {
                continue;
            }
            seatDO.setRoundBet(0);
        }
    }

    private void dealPlayerCard() {
        //System.out.println("TexasHoldemGameSceneDO dealPlayerCard()");
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

    private void notifySeatSelfCardInfo() {
        //System.out.println("TexasHoldemGameSceneDO notifySeatSelfCardInfo()");
        for (Integer seatId : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO != null) {
                TexasHoldEmHandCardDO texasHoldemHandCardDO = seatIdHandCardMap.get(seatId);
                TexasHoldEmHandCardResponseDO texasHoldemHandCardResponseDO = new TexasHoldEmHandCardResponseDO();
                texasHoldemHandCardResponseDO.setCardType(texasHoldemHandCardDO.getCardType());
                texasHoldemHandCardResponseDO.setCardList(texasHoldemHandCardDO.getHandCardList());
                HashMap<Integer, TexasHoldEmHandCardResponseDO> seatIdHandCardMap = new HashMap<>();
                seatIdHandCardMap.put(seatId, texasHoldemHandCardResponseDO);

                TexasHoldEmAgentDO agentDO = seatIdAgentMap.get(seatDO.getId());
                agentDO.notifySeatCard(seatIdHandCardMap);

            }
        }
    }

    private void initSmallBlindSeatBet() {
        //System.out.println("TexasHoldemGameSceneDO initSmallBlindSeatBet()");
        SeatDO seatDO = seatIdSeatMap.get(smallBlindSeatId);
        TexasHoldEmAgentDO agentDO = seatIdAgentMap.get(seatDO.getId());
        long bet = smallBlindBet;

        agentDO.getAgentAsset().setMoney(agentDO.getAgentAsset().getMoney() - bet);
        seatDO.setTotalBet(bet);
        seatDO.setRoundBet(bet);
        sceneGameBet = sceneGameBet + bet;
    }


    private void initBigBlindSeatBet() {
        //System.out.println("TexasHoldemGameSceneDO initBigBlindSeatBet()");
        SeatDO seatDO = seatIdSeatMap.get(bigBlindSeatId);
        TexasHoldEmAgentDO agentDO = seatIdAgentMap.get(seatDO.getId());
        long bet = smallBlindBet * 2;
        agentDO.getAgentAsset().setMoney(agentDO.getAgentAsset().getMoney() - bet);
        seatDO.setTotalBet(bet);
        seatDO.setRoundBet(bet);
        sceneGameBet = sceneGameBet + bet;
    }

    private void initRoundStartSeatId() {
        //System.out.println("TexasHoldemGameSceneDO initRoundStartSeatId()");
        roundStartSeatId = (bigBlindSeatId + 1) % maxSeatCount;
        SeatDO seatDO = seatIdSeatMap.get(roundStartSeatId);
        while (seatDO == null || seatDO.getStatus() == SEAT_STATUS_WAITING) {
            roundStartSeatId = (roundStartSeatId + 1) % maxSeatCount;
            seatDO = seatIdSeatMap.get(roundStartSeatId);
        }
    }

    private void startWaitPositionOperation() {
        //System.out.println("TexasHoldemGameSceneDO startWaitPositionOperation()");
        seatWaitOperation = new SeatWaitOperationDO(currentOperationSeatId, false, OPERATION_WAIT_TIME) {
            @Override
            public void waitOperation(int seatId, long lastOperationTime) {
                notifyAllAgentWaitingSeatOperation(seatId, lastOperationTime);
            }

            @Override
            public void startFoldOperation(int seatId) {
                SeatDO seatDO = seatIdSeatMap.get(seatId);
                TexasHoldEmAgentDO agentDO = seatIdAgentMap.get(seatDO.getId());
                agentDO.fold();
            }
        };
        seatWaitOperation.run();
    }

    private void changeSeatPosition() {
        //System.out.println("TexasHoldemGameSceneDO changeSeatPosition()");
        int canBetSeatCount = countGamingSeatSize();
        if (canBetSeatCount <= 1) {
            //處理蓋排、離開遊戲房間
            startAllocatePot();
            return;
        }

        boolean isAllSeatAllIn = isAllSeatAllIn();
        if (isAllSeatAllIn) {
            //處理All In
            int dealPublicCardCount = 0;
            if (sceneStatus == ROOM_STATUS_PRE_FLOP) {
                dealPublicCardCount = 5;
            } else if (sceneStatus == ROOM_STATUS_FLOP) {
                dealPublicCardCount = 2;
            } else if (sceneStatus == ROOM_STATUS_TURN) {
                dealPublicCardCount = 1;
            }
            if (dealPublicCardCount > 0) {
                List<Integer> dealCardList = dealPublicCard(dealPublicCardCount);
                publicCardList.addAll(dealCardList);
                addPublicCardToAllSeatHandCard(dealCardList);
                notifyAllAgentPublicCard(dealCardList);
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
                OPERATION_ALL_IN == seatDO.getAction()) {
            currentOperationSeatId = (currentOperationSeatId + 1) % maxSeatCount;
            seatDO = seatIdSeatMap.get(currentOperationSeatId);
        }

        if (hasActionSeatSet.contains(currentOperationSeatId)) {
            //換下一回合
            switch (sceneStatus) {
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
            hasActionSeatSet.add(currentOperationSeatId);
            notifyAllAgentSeatStartOperation();
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
                if (seatDO.getAction() == OPERATION_ALL_IN) {
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
        //System.out.println("TexasHoldemGameSceneDO countBetSeatSize()");
        int size = 0;
        for (int i = 0; i < maxSeatCount; i++) {
            SeatDO seatDO = seatIdSeatMap.get(i);
            if (seatDO != null && seatDO.getStatus() == SEAT_STATUS_GAMING &&
                    (seatDO.getAction() == OPERATION_NONE || seatDO.getAction() == OPERATION_CALL || seatDO.getAction() == OPERATION_RAISE)) {
                size = size + 1;
            }
        }
        return size;
    }

    private int countGamingSeatSize() {
        //System.out.println("TexasHoldemGameSceneDO countGamingSeatSize()");
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
        //System.out.println("TexasHoldemGameSceneDO dealPublicCard()");
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
        //System.out.println("TexasHoldemGameSceneDO addPublicCardToAllSeatHandCard()");
        for (Integer seatId : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO.getStatus() == SEAT_STATUS_GAMING) {
                TexasHoldEmHandCardDO texasHoldemHandCardDO = seatIdHandCardMap.get(seatId);
                texasHoldemHandCardDO.addPublicCard(publicCardList);
            }
        }
    }

    private List<TexasHoldEmPotDo> createPotPoolList() {
        //System.out.println("TexasHoldemGameSceneDO createPotPoolList()");
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
        //System.out.println("TexasHoldemGameSceneDO allocatePotBetToSeat()");
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
                TexasHoldEmAgentDO agentDO = seatIdAgentMap.get(seatDO.getId());
                agentDO.getAgentAsset().setMoney(agentDO.getAgentAsset().getMoney() + potWinnerBet);
            }
        }
        return list;
    }

    @Override
    public boolean onActionStandUp(TexasHoldEmAgentDO agent) {
        //System.out.println("TexasHoldemGameSceneDO onActionStandUp()");
        SeatDO seatDO = seatIdSeatMap.get(agent.getSeatId());
        if (seatDO == null) {
            return false;
        }

        if (seatDO.getId() == currentOperationSeatId) {
            seatWaitOperation.setOperation(true);
        }
        new Thread(() -> {
            seatIdAgentMap.remove(seatDO.getId());
            if(seatDO.getStatus() == SEAT_STATUS_GAMING) {
                //最後計算排持要用到，所以保留
                seatDO.setStatus(SEAT_STATUS_STAND_UP);
                seatDO.setAction(OPERATION_STAND_UP);
                notifyAllAgentSeatOperation(seatDO.getId(), OPERATION_STAND_UP, 0,0);
                notifyAllAgentSeatInfo(seatDO.getId());
            } else {
                seatIdSeatMap.remove(seatDO.getId());
            }

            agent.notifyStandUp();
            notifyAllAgentSceneInfo();
            if (sceneStatus == ROOM_STATUS_PRE_FLOP ||
                    sceneStatus == ROOM_STATUS_FLOP ||
                    sceneStatus == ROOM_STATUS_TURN ||
                    sceneStatus == ROOM_STATUS_RIVER) {
                int canBetSeatSize = countCanBetSeatSize();
                if (canBetSeatSize == 1) {
                    seatWaitOperation.setOperation(true);
                    startAllocatePot();
                } else {
                    if (seatDO.getId() == currentOperationSeatId) {
                        changeSeatPosition();
                    }
                }
            }
        }).start();
        return true;
    }

    @Override
    public boolean onActionSitDown(TexasHoldEmAgentDO agent) {
        //System.out.println("TexasHoldemGameSceneDO onActionSitDown()");
        List<Integer> emptySeatIdList = findEmptySeatIdList();
        if (emptySeatIdList.size() > 0) {
            int emptySeatId = new Random().nextInt(emptySeatIdList.size());
            sitDown(agent, emptySeatIdList.get(emptySeatId));
        } else {
            notifyNoSeat(agent);
        }
        return true;
    }

    @Override
    public boolean onActionFold(TexasHoldEmAgentDO agent) {
        SeatDO seatDO = seatIdSeatMap.get(agent.getSeatId());
        if (seatDO == null || seatDO.getId() != currentOperationSeatId) {
            return false;
        }

        if (sceneStatus == ROOM_STATUS_WAITING ||
                sceneStatus == ROOM_STATUS_ALLOCATE_POT) {
            return false;
        }

        seatWaitOperation.setOperation(true);
        new Thread(() -> {
            seatDO.setStatus(SEAT_STATUS_WAITING);
            seatDO.setAction(OPERATION_FOLD);
            notifyAllAgentSeatOperation(seatDO.getId(), OPERATION_FOLD, 0,0);
            changeSeatPosition();
        }).start();
        return true;
    }

    @Override
    public boolean onActionRaise(TexasHoldEmAgentDO agent, long bet) {
//        System.out.println("TexasHoldemGameSceneDO onActionRaise() bet = " + bet);
        SeatDO seatDO = seatIdSeatMap.get(agent.getSeatId());
        if (seatDO == null || seatDO.getId() != currentOperationSeatId) {
            return false;
        }
        seatWaitOperation.setOperation(true);
        new Thread(() -> {
            this.roundStartSeatId = seatDO.getId();
            hasActionSeatSet.clear();
            hasActionSeatSet.add(this.roundStartSeatId);

            long money = agent.getAgentAsset().getMoney() - bet;
            agent.getAgentAsset().setMoney(money);

            seatDO.setTotalBet(seatDO.getTotalBet() + bet);
            seatDO.setRoundBet(seatDO.getRoundBet() + bet);

            long seatRoundBet = seatDO.getRoundBet();
            if (seatRoundBet >= this.roundCallBet) {
                this.roundCallBet = seatRoundBet;
            }
            this.sceneGameBet = this.sceneGameBet + bet;
            this.roundRaiseBet = bet * 2;

            if (money == 0) {
                seatDO.setAction(OPERATION_ALL_IN);
                notifyAllAgentSeatOperation(seatDO.getId(), OPERATION_ALL_IN, money, seatDO.getRoundBet());
            } else {
                seatDO.setAction(OPERATION_RAISE);
                notifyAllAgentSeatOperation(seatDO.getId(), OPERATION_RAISE, money, seatDO.getRoundBet());
            }

            changeSeatPosition();
        }).start();
        return true;
    }

    @Override
    public boolean onActionCall(TexasHoldEmAgentDO agent, long bet) {
        System.out.println("TexasHoldemGameSceneDO onActionCall()");
        SeatDO seatDO = seatIdSeatMap.get(agent.getSeatId());
        if (seatDO == null || seatDO.getId() != currentOperationSeatId) {
            return false;
        }

        seatWaitOperation.setOperation(true);
        new Thread(() -> {

            long money = agent.getAgentAsset().getMoney() - bet;
            agent.getAgentAsset().setMoney(money);

            seatDO.setTotalBet(seatDO.getTotalBet() + bet);
            seatDO.setRoundBet(seatDO.getRoundBet() + bet);

            if (money == 0) {
                seatDO.setAction(OPERATION_ALL_IN);
                notifyAllAgentSeatOperation(seatDO.getId(), OPERATION_ALL_IN, money, seatDO.getRoundBet());
            } else {
                seatDO.setAction(OPERATION_CALL);
                notifyAllAgentSeatOperation(seatDO.getId(), OPERATION_CALL, money, seatDO.getRoundBet());
            }

            this.sceneGameBet = this.sceneGameBet + bet;
            changeSeatPosition();
        }).start();
        return true;
    }

    @Override
    public boolean onActionExit(TexasHoldEmAgentDO agent) {
        //System.out.println("TexasHoldemGameSceneDO onActionExit()");
        SeatDO seatDO = seatIdSeatMap.get(agent.getSeatId());
        if (seatDO != null && seatDO.getId() == currentOperationSeatId) {
            seatWaitOperation.setOperation(true);
        }
        new Thread(() -> {
            exit(agent);
            if (seatDO != null) {
                seatIdAgentMap.remove(seatDO.getId());
                if(seatDO.getStatus() == SEAT_STATUS_GAMING) {
                    //最後計算排持要用到，所以保留
                    seatDO.setStatus(SEAT_STATUS_EXIT);
                    seatDO.setAction(OPERATION_EXIT);
                    notifyAllAgentSeatOperation(seatDO.getId(), OPERATION_EXIT, 0, 0);
                } else {
                    seatIdSeatMap.remove(seatDO.getId());
                }
                notifyAllAgentSceneInfo();
                if (sceneStatus == ROOM_STATUS_PRE_FLOP ||
                        sceneStatus == ROOM_STATUS_FLOP ||
                        sceneStatus == ROOM_STATUS_TURN ||
                        sceneStatus == ROOM_STATUS_RIVER) {
                    int canBetSeatSize = countCanBetSeatSize();
                    if (canBetSeatSize == 1) {
                        seatWaitOperation.setOperation(true);
                        startAllocatePot();
                    } else {
                        if (seatDO.getId() == currentOperationSeatId) {
                            changeSeatPosition();
                        }
                    }
                }
            }

        }).start();
        return true;
    }

    protected void notifyAllAgentSeatInfo(int seatId) {
        //System.out.println("TexasHoldemGameSceneDO notifySeatInfo()");
        SeatDO seatDO = seatIdSeatMap.get(seatId);
        TexasHoldEmAgentDO agentDO = seatIdAgentMap.get(seatDO.getId());
        TexasHoldEmSeatInfoResponseDO responseDO = new TexasHoldEmSeatInfoResponseDO();
        responseDO.setId(seatId);
        responseDO.setStatus(seatDO.getStatus());
        responseDO.setAction(seatDO.getAction());
        responseDO.setRoundBet(seatDO.getRoundBet());
        if(agentDO != null) {
            responseDO.setName(agentDO.getName());
            responseDO.setMoney(agentDO.getAgentAsset().getMoney());
        }
        for(TexasHoldEmAgentDO agent: agentList) {
            agent.notifySeatInfo(responseDO);
        }
    }

    protected void notifyAllAgentSceneInfo() {
        //System.out.println("TexasHoldemGameSceneDO notifyAllAgentSceneInfo()");
        TexasHoldEmSceneInfoResponseDO responseDO = new TexasHoldEmSceneInfoResponseDO();
        responseDO.setSceneStatus(sceneStatus);
        responseDO.setSmallBlindSeatId(smallBlindSeatId);
        responseDO.setBigBlindSeatId(bigBlindSeatId);
        responseDO.setCurrentOperationSeatId(currentOperationSeatId);
        responseDO.setRoomBet(sceneGameBet);
        responseDO.setPublicCardList(publicCardList);
        HashMap<Integer, TexasHoldEmSeatInfoResponseDO> seatIdSeatMap = new HashMap<>();
        for(Integer seatId: this.seatIdSeatMap.keySet()) {
            SeatDO seatDO = this.seatIdSeatMap.get(seatId);
            TexasHoldEmSeatInfoResponseDO seatInfo = new TexasHoldEmSeatInfoResponseDO();
            seatInfo.setId(seatId);
            seatInfo.setStatus(seatDO.getStatus());
            seatInfo.setAction(seatDO.getAction());
            seatInfo.setRoundBet(seatDO.getRoundBet());
            TexasHoldEmAgentDO agentDO = seatIdAgentMap.get(seatDO.getId());
            if(agentDO != null) {
                seatInfo.setName(agentDO.getName());
                seatInfo.setMoney(agentDO.getAgentAsset().getMoney());
            }
            seatIdSeatMap.put(seatId, seatInfo);
        }
        responseDO.setSeatIdSeatMap(seatIdSeatMap);

        for(TexasHoldEmAgentDO agent: agentList) {
            agent.notifySceneInfo(responseDO);
        }
    }

    private void notifyAllAgentSeatStartOperation() {
//        System.out.println("TexasHoldemGameSceneDO notifyAllAgentSeatStartOperation()");
        SeatDO seatDO = seatIdSeatMap.get(currentOperationSeatId);
        TexasHoldEmStartOperationResponseDO responseDO = new TexasHoldEmStartOperationResponseDO();
        responseDO.setSeatId(currentOperationSeatId);
        TexasHoldEmAgentDO agentDO = seatIdAgentMap.get(seatDO.getId());
        long agentMoney = agentDO.getAgentAsset().getMoney();

        if ((agentMoney + seatDO.getRoundBet()) > roundCallBet) {
            responseDO.setCanRaise(true);
            responseDO.setMinRaiseBet(Math.min(agentMoney, roundRaiseBet));
            responseDO.setMaxRaiseBet(agentMoney);
            responseDO.setCallBet(roundCallBet - seatDO.getRoundBet());
        } else {
            responseDO.setCanRaise(false);
            responseDO.setCallBet(agentMoney);
        }

        responseDO.setLastOperationTime(OPERATION_WAIT_TIME);
        for(TexasHoldEmAgentDO agent: agentList) {
            agent.notifySeatStartOperation(responseDO);
        }
    }

    private void notifyAllAgentWaitingSeatOperation(int seatId, long lastOperationTime) {
        TexasHoldEmWaitOperationResponseDO responseDO = new TexasHoldEmWaitOperationResponseDO();
        responseDO.setSeatId(seatId);
        responseDO.setLastOperationTime(lastOperationTime);
        for(TexasHoldEmAgentDO agent: agentList) {
            agent.notifyWaitingSeatOperation(responseDO);
        }
    }

    private void notifyAllAgentSeatOperation(int seatId, int operation, long money, long bet) {
        //System.out.println("TexasHoldemGameSceneDO notifyAllAgentSeatOperation()");
        TexasHoldEmSeatOperationResponseDO responseDO = new TexasHoldEmSeatOperationResponseDO();
        responseDO.setSeatId(seatId);
        responseDO.setOperation(operation);
        responseDO.setMoney(money);
        responseDO.setBet(bet);
        responseDO.setSceneGameBet(this.sceneGameBet);
        for(TexasHoldEmAgentDO agent: agentList) {
            agent.notifySeatOperation(responseDO);
        }
    }


    private void notifyAllAgentPublicCard(List<Integer> dealCardList) {
        //System.out.println("TexasHoldemGameSceneDO notifyPublicCardToAllSeat()");
        for(TexasHoldEmAgentDO agent: agentList) {
            agent.notifyDealPublicCard(dealCardList);
        }
    }

    private void notifyAllSeatSelfCardType() {
        //System.out.println("TexasHoldemGameSceneDO notifyAllSeatSelfCardType()");
        for (Integer seatId : seatIdSeatMap.keySet()) {
            //System.out.println("TexasHoldemGameSceneDO notifyAllSeatSelfCardType() seatId = " + seatId);
            Map<Integer, TexasHoldEmHandCardResponseDO> seatIdCardMap = new HashMap<>();
            TexasHoldEmHandCardDO handCardDO = seatIdHandCardMap.get(seatId);
            TexasHoldEmHandCardResponseDO texasHoldEmHandCardResponseDO = new TexasHoldEmHandCardResponseDO();
            texasHoldEmHandCardResponseDO.setCardType(handCardDO.getCardType());
            texasHoldEmHandCardResponseDO.setCardList(handCardDO.getHandCardList());
            seatIdCardMap.put(seatId, texasHoldEmHandCardResponseDO);
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            TexasHoldEmAgentDO agentDO = seatIdAgentMap.get(seatDO.getId());
            agentDO.notifySeatCard(seatIdCardMap);
        }
    }

    private void notifyAllAgentStatusChanged(int status) {
        for(TexasHoldEmAgentDO agent: agentList) {
            agent.notifyStatusChanged(status);
        }
    }
}
