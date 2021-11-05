package com.shark.game.entity.room;

import com.shark.game.entity.player.PlayerDO;
import com.shark.game.manager.PlayerManager;

public class BaseRoomDO {

    protected PlayerDO findPlayerByToken(String token) {
        PlayerDO playerDo = PlayerManager.getInstance().findByToken(token);
        return playerDo;
    }

    protected boolean isBetEnough(PlayerDO playerDo, int bet) {
        return playerDo.getMoney() >= bet;
    }
}
