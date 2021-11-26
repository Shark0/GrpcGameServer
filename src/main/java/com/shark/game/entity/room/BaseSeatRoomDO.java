package com.shark.game.entity.room;

import com.shark.game.entity.player.PlayerDO;
import com.shark.game.manager.PlayerManager;
import io.grpc.stub.StreamObserver;

import java.util.*;

public abstract class BaseSeatRoomDO extends BaseRoomDO {

    protected int maxSeatCount, minSeatCount;

    protected final int SEAT_STATUS_WAITING = 0, SEAT_STATUS_GAMING = 1;

    protected final Map<Long, Integer> playerIdSeatIdMap = new HashMap<>();
    protected final Map<Integer, SeatDO> seatIdSeatMap = new HashMap<>();

    public BaseSeatRoomDO(int agentId, int gameType, int minBet, int maxSeatCount, int minSeatCount) {
        super(agentId, gameType, minBet);
        this.maxSeatCount = maxSeatCount;
        this.minSeatCount = minSeatCount;
        init();
    }

    protected void initSeatMap() {
        for(int i = 0; i < maxSeatCount; i ++) {
            seatIdSeatMap.put(i, null);
        }
    }

    private void init() {
        initSeatMap();
        startWaitingStatus();
    }

    private void startWaitingStatus() {
        System.out.println("CardSeatRoomDO startWaitingStatus()");
        changeSeatListStatus(SEAT_STATUS_WAITING);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                checkSeatLive();
                int emptySeatCount = findEmptySeatIdList().size();
                if ((maxSeatCount - emptySeatCount) >= minSeatCount) {
                    startGameStatus();
                } else {
                    startWaitingStatus();
                }
            }
        }, 1000);
    }

    protected abstract void checkSeatLive();

    public synchronized boolean enterGame(StreamObserver observer, long playerId) {
        List<Integer> emptySeatIdList = findEmptySeatIdList();
        if (emptySeatIdList.size() < maxSeatCount) {
            playerIdObserverMap.put(playerId, observer);
            addPlayerToRandomSeat(playerId, emptySeatIdList);
            return true;
        }
        return false;
    }

    public synchronized void exitGame(long playerId) {
        Integer seatId = playerIdSeatIdMap.get(playerId);
        seatIdSeatMap.remove(seatId);
        playerIdObserverMap.remove(playerId);
        playerIdSeatIdMap.remove(playerId);
    }

    protected void addPlayerToRandomSeat(long playerId, List<Integer> emptySeatIdList) {
        PlayerDO playerDO = PlayerManager.getInstance().findById(playerId);
        SeatDO seatDO = new SeatDO();
        seatDO.setPlayerId(playerDO.getId());
        seatDO.setPlayerName(playerDO.getName());
        seatDO.setStatus(SEAT_STATUS_WAITING);
        Random random = new Random();
        Integer randomEmptySeatId = emptySeatIdList.get(random.nextInt(emptySeatIdList.size()));
        seatIdSeatMap.put(randomEmptySeatId, seatDO);
    }

    protected List<Integer> findEmptySeatIdList() {
        List<Integer> emptySeatIdList = new ArrayList<>();
        for(Integer key: seatIdSeatMap.keySet()) {
            if(seatIdSeatMap.get(key) == null) {
                emptySeatIdList.add(key);
            }
        }
        return emptySeatIdList;
    }

    protected abstract void startGameStatus();

    protected void changeSeatListStatus(int status) {
        for(Integer key: seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(key);
            if(seatDO == null) {
                continue;
            }
            seatDO.setStatus(status);
        }
    }
}
