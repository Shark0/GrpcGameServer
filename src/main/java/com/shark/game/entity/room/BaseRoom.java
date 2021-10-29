package com.shark.game.entity.room;

import com.shark.game.entity.PlayerDo;
import com.shark.game.manager.PlayerManager;

public class BaseRoom {

    protected PlayerDo findPlayerByToken(String token) {
        PlayerDo playerDo = PlayerManager.getInstance().findByToken(token);
        return playerDo;
    }

    protected boolean isBetEnough(PlayerDo playerDo, int bet) {
        return playerDo.getMoney() >= bet;
    }
}
