package com.shark.game.entity.room;

import com.shark.game.manager.RoomManager;
import com.shark.game.service.CardSeatGameService;
import io.grpc.stub.StreamObserver;

import java.util.*;

public abstract class BaseSeatRoomDO extends BaseRoomDO {

    protected int gameType;

    protected List<String> queueTokenList = new ArrayList<>();

    protected int queueMaxCount, queueMinCount;

    protected final Map<String, StreamObserver> tokenStatusObserverMap = new HashMap<>();

    protected List<CardSeatDO> cardSeatDOList = new ArrayList<>();
    protected Map<String, CardSeatDO> tokenSeatDOMap = new HashMap<>();

    public BaseSeatRoomDO(int gameType) {
        this.gameType = gameType;
    }

    public void init() {
        queueMaxCount = generateMaxCount();
        queueMinCount = generateMinCount();
        checkQueueStatus();
    }

    protected abstract int generateMaxCount();

    protected abstract int generateMinCount();

    private void checkQueueStatus() {
        System.out.println("CardSeatRoomDO checkQueueStatus()");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for(String token: queueTokenList) {
                    StreamObserver observer = tokenStatusObserverMap.get(token);
                    try {
                        CardSeatGameService.CardSeatGameStatusResponse response
                                = CardSeatGameService.CardSeatGameStatusResponse.newBuilder()
                                .setStatus(0).setMessage("排隊等待中").build();
                        observer.onNext(response);
                    } catch (Exception e) {
                        queueTokenList.remove(token);
                        tokenStatusObserverMap.remove(token);
                    }
                }


                int queueCount = queueTokenList.size();
                if (queueCount >= queueMinCount) {
                    RoomManager.getInstance().removeRoomFromQueue(gameType, BaseSeatRoomDO.this);
                    gameStart();
                } else {
                    checkQueueStatus();
                }
            }
        }, 5000);
    }

    public boolean enterGame(StreamObserver observer, String token) {
        int queueCount = queueTokenList.size();
        if (queueCount < queueMaxCount) {
            queueTokenList.add(token);
            tokenStatusObserverMap.put(token, observer);
            return true;
        }
        return false;
    }

    public boolean isQueuing() {
        return queueTokenList.size() < queueMaxCount;
    }

    protected abstract void gameStart();


}
