package com.shark.game.entity.user;

import lombok.Data;

@Data
public class UserDO {
    private long id;

    private int agentId;

    private String name;

    private long money;
}
