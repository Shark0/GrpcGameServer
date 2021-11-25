package com.shark.game.entity.player;

import lombok.Data;

@Data
public class PlayerDO {
    private long id;

    private int agentId;

    private String name;

    private long money;
}
