package com.shark.game.entity.room;

import com.shark.game.entity.player.PlayerDO;
import com.shark.game.manager.PlayerManager;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;

public class BaseRoomDO {

    protected int agentId, gameType, minBet;

    protected final Map<Long, StreamObserver> playerIdObserverMap = new HashMap<>();

    public BaseRoomDO(int agentId, int gameType, int minBet) {
        this.agentId = agentId;
        this.gameType = gameType;
        this.minBet = minBet;
    }


    protected boolean isBetEnough(PlayerDO playerDo, int bet) {
        return playerDo.getMoney() >= bet;
    }
}
