package com.shark.game.entity.room.seat;

import lombok.Data;

@Data
public class SeatDO {

    private int id;

    private long playerId;

    private String playerName;

    private long money = 0;

    private long totalBet = 0;

    private long roundBet = 0;

    private int status = -1;

    private int operation = -1;
}
