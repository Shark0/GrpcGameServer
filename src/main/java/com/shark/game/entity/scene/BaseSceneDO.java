package com.shark.game.entity.scene;

import java.util.ArrayList;
import java.util.List;

public class BaseSceneDO<Agent> {

    protected int sceneId;

    protected final List<Agent> agentList = new ArrayList<>();
    protected final List<Agent> robotAgentList = new ArrayList<>();

    public BaseSceneDO(int sceneId) {
        this.sceneId = sceneId;
    }
}
