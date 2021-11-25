package com.shark.game.entity.room;

import lombok.Data;

@Data
public class SeatDO {

    private long playerId;

    private String playerName;

    private long money;

    private long betMoney = 0;

    private int status;

    private int operation;
}
