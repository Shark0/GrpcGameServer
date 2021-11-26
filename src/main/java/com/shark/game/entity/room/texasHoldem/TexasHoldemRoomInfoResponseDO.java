package com.shark.game.entity.room.texasHoldem;

import com.shark.game.entity.room.SeatDO;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TexasHoldemRoomInfoResponseDO {
    private long pot;
    private List<Integer> publicCardList;
    private Map<Integer, SeatDO> seatIdSeatMap;
    private int smallBlindPosition;
    private int bigBlindPosition;
    private int roomStatus;
}
