package com.shark.game.entity.scene.texasHoldEm.response;

import lombok.Data;

import java.util.List;

@Data
public class TexasHoldEmHandCardResponseDO {
    protected int cardType;
    protected List<Integer> cardList;
}
