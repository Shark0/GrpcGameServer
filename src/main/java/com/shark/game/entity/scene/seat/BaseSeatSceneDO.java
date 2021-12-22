package com.shark.game.entity.scene.seat;

import com.google.gson.Gson;
import com.shark.game.entity.scene.BaseAgentDO;
import com.shark.game.entity.scene.BaseSceneDO;

import java.util.*;

public abstract class BaseSeatSceneDO<Agent> extends BaseSceneDO<Agent> {

    public static final int ROOM_STATUS_WAITING = 0;

    protected int sceneStatus = ROOM_STATUS_WAITING;

    protected int maxSeatCount, minSeatCount;

    protected final int SEAT_STATUS_WAITING = 0, SEAT_STATUS_GAMING = 1, SEAT_STATUS_EXIT = 2, SEAT_STATUS_STAND_UP = 3;

    protected final HashMap<Integer, Agent> seatIdAgentMap = new HashMap<>();

    protected final HashMap<Integer, SeatDO> seatIdSeatMap = new HashMap<>();

    public BaseSeatSceneDO(int gameType, int maxSeatCount, int minSeatCount) {
        super(gameType);
        this.maxSeatCount = maxSeatCount;
        this.minSeatCount = minSeatCount;
        startWaitingStatus();
    }

    protected void startWaitingStatus() {
        removeStanUpAndExitSeat();
        removeDeadAgent();
        notifyAllAgentSceneInfo();
        changeAllSeatStatus(SEAT_STATUS_WAITING);
        checkStartGameSeatSize();
    }

    protected abstract void removeDeadAgent();

    private void removeStanUpAndExitSeat() {
        List<Integer> removeSeatIdList = new ArrayList<>();
        for (Integer seatId : seatIdSeatMap.keySet()) {
            SeatDO seatDO = seatIdSeatMap.get(seatId);
            if (seatDO != null && (seatDO.getStatus() == SEAT_STATUS_STAND_UP || seatDO.getStatus() == SEAT_STATUS_EXIT)) {
                removeSeatIdList.add(seatId);
            }
        }
        for (Integer seatId : removeSeatIdList) {
            seatIdSeatMap.remove(seatId);
            seatIdAgentMap.remove(seatId);
        }
    }

    protected void checkStartGameSeatSize() {
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                int seatSize = countSeatSize();
                if (seatSize >= minSeatCount) {
                    startGame();
                } else {
                    if (robotAgentList.size() < (maxSeatCount / 2)) {
                        addRobot();
                    } else if (agentList.size() > (maxSeatCount / 2)) {
                        removeRobot();
                    }

                    checkStartGameSeatSize();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    protected abstract void addRobot();

    protected abstract void removeRobot();

    public synchronized void enter(Agent agentDO) {
//        System.out.println("CardSeatRoomDO enterGame()");
        agentList.add(agentDO);
        registerSceneStatus(agentDO);
        notifyEnterScene(agentDO);
        removeDeadAgent();
        List<Integer> emptySeatIdList = findEmptySeatIdList();
        if (emptySeatIdList.size() > 0) {
            int emptySeatId = new Random().nextInt(emptySeatIdList.size());
            sitDown(agentDO, emptySeatIdList.get(emptySeatId));
            notifyAllAgentSceneInfo();
        } else {
            notifyNoSeat(agentDO);
            notifyAgentSceneInfo(agentDO);
        }
    }

    protected abstract void registerSceneStatus(Agent agentDO);

    protected abstract void notifyEnterScene(Agent agentDO);

    protected abstract void notifyAgentSceneInfo(Agent agent);

    protected abstract void sitDown(Agent agent, int seatId);

    protected abstract void notifyNoSeat(Agent agent);

    protected abstract void notifyAllAgentSceneInfo();

    protected synchronized void exit(BaseAgentDO agent) {
//        System.out.println("BaseSeatDO exitGame()");
        agentList.remove(agent);
    }

    protected void allocateAgentSeat(Agent agent, int seatId) {
//        System.out.println("BaseSeatDO allocateAgentSeat()");
        SeatDO seat = new SeatDO();
        seat.setStatus(SEAT_STATUS_WAITING);
        seat.setId(seatId);
        seatIdSeatMap.put(seatId, seat);
        seatIdAgentMap.put(seat.getId(), agent);
    }

    protected List<Integer> findEmptySeatIdList() {
        List<Integer> emptySeatIdList = new ArrayList<>();
        for (int seatId = 0; seatId < maxSeatCount; seatId++) {
            if (seatIdSeatMap.get(seatId) == null) {
                emptySeatIdList.add(seatId);
            }
        }
        return emptySeatIdList;

    }

    protected abstract void startGame();

    protected int countSeatSize() {
        int size = 0;
        for (Integer seatId : seatIdSeatMap.keySet()) {
            if (seatIdSeatMap.get(seatId) != null) {
                size++;
            }
        }
        return size;
    }

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

