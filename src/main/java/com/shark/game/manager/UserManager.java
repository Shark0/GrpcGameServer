package com.shark.game.manager;

import com.shark.game.entity.user.UserDO;

import java.util.HashMap;

public class UserManager {


    private static UserManager instance;

    private HashMap<Long, UserDO> playerIdPlayerMap = new HashMap<>();


    private UserManager() {}

    public void addPlayer(UserDO userDo) {
        playerIdPlayerMap.put(userDo.getId(), userDo);
    }

    public void removePlayer(Long playerId) {
        playerIdPlayerMap.remove(playerId);
    }

    public static UserManager getInstance() {
        if(instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public UserDO findById(Long id) {
        return playerIdPlayerMap.get(id);
    }
}
