package com.shark.game.entity.room.texasHoldEm;

import lombok.Data;

@Data
public class TexasHoldEmSeatOperationResponseDO {
    private int seatId;
    private int operation;
    private long money;
    private long bet;
    private long roomGameBet;
}
