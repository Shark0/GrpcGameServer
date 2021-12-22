package com.shark.game.manager;

import com.shark.game.entity.scene.BaseAgentDO;
import lombok.Data;

import java.util.HashMap;

@Data
public class AgentManager {

    private static AgentManager instance;

    private final HashMap<Long, BaseAgentDO> userIdAgentMap = new HashMap<>();

    private AgentManager() {}

    public static AgentManager getInstance() {
        if(instance == null) {
            instance = new AgentManager();
        }
        return instance;
    }
}
