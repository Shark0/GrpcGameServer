package com.shark.game.entity.scene.seat;

import com.shark.game.entity.scene.BaseAgentDO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class BaseSeatAgentDO<AgentAsset> extends BaseAgentDO<AgentAsset> {

    protected int seatId = -1;

    public BaseSeatAgentDO(long sceneId, String name, AgentAsset agentAsset) {
        this.sceneId = sceneId;
        this.name = name;
        this.agentAsset = agentAsset;
    }
}
