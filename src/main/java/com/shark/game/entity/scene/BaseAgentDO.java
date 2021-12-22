package com.shark.game.entity.scene;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class BaseAgentDO<AgentAsset> {

    protected long sceneId;

    protected String name;

    protected AgentAsset agentAsset;

    public BaseAgentDO(long sceneId, String name, AgentAsset agentAsset) {
        this.sceneId = sceneId;
        this.name = name;
        this.agentAsset = agentAsset;
    }
}
