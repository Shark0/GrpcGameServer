package com.shark.game.entity.room.texasHoldEm;

import lombok.Data;

import java.util.List;

@Data
public class TexasHoldEmWinPotBetResponseDO {
    private List<Integer> winnerSeatIdList;
    private long winBet;
}
