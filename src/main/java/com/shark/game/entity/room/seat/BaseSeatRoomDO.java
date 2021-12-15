package com.shark.game.entity.room.seat;

import com.google.gson.Gson;
import com.shark.game.entity.player.PlayerDO;
import com.shark.game.entity.room.BaseRoomDO;
import com.shark.game.manager.PlayerManager;
import io.grpc.stub.StreamObserver;

import java.util.*;

public abstract class BaseSeatRoomDO extends BaseRoomDO {

    protected final int ROOM_STATUS_WAITING = 0;

    protected int roomStatus = ROOM_STATUS_WAITING;

    protected int maxSeatCount, minSeatCount;

    protected final int SEAT_STATUS_WAITING = 0, SEAT_STATUS_GAMING = 1, SEAT_STATUS_EXIT = 2, SEAT_STATUS_STAND_UP = 3;

    protected final Map<Long, Integer> playerIdSeatIdMap = new HashMap<>();

    protected final Map<Integer, SeatDO> seatIdSeatMap = new HashMap<>();

    public BaseSeatRoomDO(int agentId, int gameType, int minBet, int maxSeatCount, int minSeatCount) {
        super(agentId, gameType, minBet);
        this.maxSeatCount = maxSeatCount;
        this.minSeatCount = minSeatCount;
        startWaitingStatus();
    }

    protected void startWaitingStatus() {
        removeStanUpSeat();
        removeDeadSeat();
        notifyRoomInfo();
        changeAllSeatStatus(SEAT_STATUS_WAITING);
        checkStartGameSeatSize();
    }

    protected abstract void removeDeadSeat();

    private void removeStanUpSeat() {
        List<Integer> standUpSeatIdList = new ArrayList<>();
        for (Integer seatId : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO != null && seatDO.getStatus() == SEAT_STATUS_STAND_UP) {
                standUpSeatIdList.add(seatId);
            }
        }
        for (Integer seatId : standUpSeatIdList) {
            seatIdSeatMap.remove(seatId);
        }
    }

    protected void checkStartGameSeatSize() {
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                int seatSize = countSeatSize();
                if (seatSize >= minSeatCount) {
                    startGameStatus();
                } else {
                    checkStartGameSeatSize();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public synchronized void enterGame(StreamObserver observer, long playerId) {
        System.out.println("CardSeatRoomDO enterGame()");
        removeDeadSeat();
        playerIdObserverMap.put(playerId, observer);
        List<Integer> emptySeatIdList = findEmptySeatIdList();
        if (emptySeatIdList.size() > 0) {
            int emptySeatId = new Random().nextInt(emptySeatIdList.size());
            notifyRoomInfo(playerId);
            sitDown(playerId, emptySeatIdList.get(emptySeatId));
        } else {
            notifyNoSeat(playerId);
            notifyRoomInfo();
        }
    }

    protected abstract void notifyRoomInfo(long playerId);

    protected abstract void sitDown(long playerId, int seatId);

    protected abstract void notifyNoSeat(long playerId);

    protected abstract void notifyRoomInfo();

    protected void exitGame(long playerId) {
        System.out.println("BaseSeatDO exitGame(): playerId = " + playerId);
        StreamObserver streamObserver = playerIdObserverMap.get(playerId);
        if(streamObserver != null) {
            streamObserver.onCompleted();
            System.out.println("BaseSeatDO exitGame(): streamObserver.onCompleted()");
        }
        playerIdObserverMap.remove(playerId);
        playerIdSeatIdMap.remove(playerId);
    }

    protected void allocatePlayerSeat(long playerId, int seatId) {
        System.out.println("CardSeatRoomDO addPlayerToRandomSeat()");
        PlayerDO playerDO = PlayerManager.getInstance().findById(playerId);
        long money = playerDO.getMoney();
        playerDO.setMoney(0);
        SeatDO seatDO = new SeatDO();
        seatDO.setPlayerId(playerDO.getId());
        seatDO.setPlayerName(playerDO.getName());
        seatDO.setMoney(money);
        seatDO.setStatus(SEAT_STATUS_WAITING);
        seatDO.setId(seatId);
        seatIdSeatMap.put(seatId, seatDO);
        playerIdSeatIdMap.put(playerId, seatId);

        System.out.println("CardSeatRoomDO addPlayerToRandomSeat(): playerIdSeatIdMap = " + new Gson().toJson(playerIdSeatIdMap));
    }

    protected List<Integer> findEmptySeatIdList() {
        List<Integer> emptySeatIdList = new ArrayList<>();
        for (int seatId = 0; seatId < maxSeatCount; seatId ++) {
            if (seatIdSeatMap.get(seatId) == null) {
                emptySeatIdList.add(seatId);
            }
        }
        return emptySeatIdList;

    }

    protected abstract void notifySitDown(long playerId, int seatId);

    protected abstract void startGameStatus();

    protected int countSeatSize() {
        int size = 0;
        for(Integer seatId: seatIdSeatMap.keySet()) {
            if(seatIdSeatMap.get(seatId) != null) {
                size ++;
            }
        }
        return size;
    };

    protected void changeAllSeatStatus(int status) {
        for (Integer key : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(key);
            if (seatDO == null) {
                continue;
            }
            seatDO.setStatus(status);
        }
    }
}

