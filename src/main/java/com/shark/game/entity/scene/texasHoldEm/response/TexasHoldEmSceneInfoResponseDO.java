package com.shark.game.entity.scene.texasHoldEm.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TexasHoldEmSceneInfoResponseDO {
    private long roomBet;
    private List<Integer> publicCardList;
    private Map<Integer, TexasHoldEmSeatInfoResponseDO> seatIdSeatMap;
    private long bigBlindBet;
    private int smallBlindSeatId;
    private int bigBlindSeatId;
    private int currentOperationSeatId;
    private int sceneStatus;
}
