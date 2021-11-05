package com.shark.game.manager;

import com.shark.game.entity.player.PlayerDO;

import java.util.HashMap;

public class PlayerManager {


    private static PlayerManager instance;

    private HashMap<String, PlayerDO> tokenPlayerMap = new HashMap<>();

    private HashMap<Integer, Integer> playerIdRoomIdMap = new HashMap<>();

    private PlayerManager() {}

    public void putPlayer(String token, PlayerDO playerDo) {
        tokenPlayerMap.put(token, playerDo);
    }

    public void removePlayer(String token) {
        tokenPlayerMap.remove(token);
    }

    public static PlayerManager getInstance() {
        if(instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    public PlayerDO findByToken(String token) {
        return tokenPlayerMap.get(token);
    }
}
