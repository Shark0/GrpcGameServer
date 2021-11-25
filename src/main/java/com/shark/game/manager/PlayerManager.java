package com.shark.game.manager;

import com.shark.game.entity.player.PlayerDO;

import java.util.HashMap;

public class PlayerManager {


    private static PlayerManager instance;

    private HashMap<Long, PlayerDO> playerIdPlayerMap = new HashMap<>();


    private PlayerManager() {}

    public void putPlayer(PlayerDO playerDo) {
        playerIdPlayerMap.put(playerDo.getId(), playerDo);
    }

    public void removePlayer(Long playerId) {
        playerIdPlayerMap.remove(playerId);
    }

    public static PlayerManager getInstance() {
        if(instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    public PlayerDO findById(Long id) {
        return playerIdPlayerMap.get(id);
    }
}
