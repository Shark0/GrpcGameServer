package com.shark.game.entity.room;

import com.shark.game.entity.player.PlayerDO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CardSeatDO {

    private String token;

    private PlayerDO playerDO;

    private List<Integer> cardList = new ArrayList<>();

    private int betMoney = 0;

    private boolean isAutoCall;

    private boolean isFold;

    private boolean isExist;
}
