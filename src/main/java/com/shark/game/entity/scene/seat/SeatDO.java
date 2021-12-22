package com.shark.game.entity.scene.seat;

import lombok.Data;

@Data
public class SeatDO {

    private int id;

    private long totalBet = 0;

    private long roundBet = 0;

    private int status = -1;

    private int action = -1;
}
