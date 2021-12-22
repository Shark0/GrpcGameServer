package com.shark.game.manager;

import com.shark.game.entity.scene.BaseSceneDO;
import com.shark.game.entity.scene.texasHoldEm.TexasHoldEmGameSceneDO;

import java.util.HashMap;
import java.util.Map;

public class SceneManager {

    public static final int SCENE_TEXAS_HOLDEM = 1;

    private static SceneManager instance;

    private final Map<Integer, BaseSceneDO> sceneIdSceneMap = new HashMap<>();



    private SceneManager() {}

    public void init() {
        TexasHoldEmGameSceneDO texasHoldemGameSceneDO =
                new TexasHoldEmGameSceneDO(SCENE_TEXAS_HOLDEM, 50, 12, 2);
        sceneIdSceneMap.put(SCENE_TEXAS_HOLDEM, texasHoldemGameSceneDO);
    }

    public synchronized BaseSceneDO findById(int sceneId) {
        switch (sceneId) {
            case SCENE_TEXAS_HOLDEM:
                return sceneIdSceneMap.get(sceneId);
        }
        return null;
    }


    public static SceneManager getInstance() {
        if(instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

}
