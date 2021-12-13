package com.shark.game.entity.room.texasHoldEm;

import com.shark.game.entity.room.seat.SeatDO;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TexasHoldEmRoomInfoResponseDO {
    private long roomBet;
    private List<Integer> publicCardList;
    private Map<Integer, SeatDO> seatIdSeatMap;
    private int smallBlindSeatId;
    private int bigBlindSeatId;
    private int currentOperationSeatId;
    private int roomStatus;
}
