package com.shark.game.entity.scene.texasHoldEm;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TexasHoldEmPotDo {

    private Map<Integer, TexasHoldEmHandCardDO> seatIdCardMap = new HashMap<>();

    private Long potBet;

    private long betMoney;

    private int betPlayerSize;

    public void addHandCard(Integer seatId, TexasHoldEmHandCardDO handCardDO) {
        seatIdCardMap.put(seatId, handCardDO);
    }

    public List<Integer> getWinnerSeatIdList() {
        List<Integer> winnerSeatIdList = new ArrayList<>();
        for(Integer seatId: seatIdCardMap.keySet()) {
            if(winnerSeatIdList.size() == 0) {
                winnerSeatIdList.add(seatId);
            } else {
                TexasHoldEmHandCardDO winnerHandCard = seatIdCardMap.get(winnerSeatIdList.get(0));
                TexasHoldEmHandCardDO seatHandCard = seatIdCardMap.get(seatId);
                int compareResult = seatHandCard.compare(winnerHandCard);
                if(compareResult == 1) {
                    winnerSeatIdList.clear();
                    winnerSeatIdList.add(seatId);
                } else if (compareResult == 0) {
                    winnerSeatIdList.add(seatId);
                }
            }
        }
        return winnerSeatIdList;
    }

}
