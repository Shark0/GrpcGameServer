package com.shark.game.service;

import com.shark.game.entity.scene.texasHoldEm.agent.TexasHoldEmGameAsset;
import com.shark.game.entity.scene.texasHoldEm.agent.TexasHoldEmUserAgentDO;
import com.shark.game.entity.scene.texasHoldEm.TexasHoldEmGameSceneDO;
import com.shark.game.entity.user.UserDO;
import com.shark.game.manager.AgentManager;
import com.shark.game.manager.SceneManager;
import com.shark.game.manager.UserManager;
import com.shark.game.util.TokenUtil;
import io.grpc.stub.StreamObserver;

public class TexasHoldemGameStatusServiceImpl extends TexasHoldemGameStatusServiceGrpc.TexasHoldemGameStatusServiceImplBase {

    @Override
    public void registerTexasHoldemGameStatus(
            TexasHoldemGameService.TexasHoldemGameStatusRequest request,
            StreamObserver<TexasHoldemGameService.TexasHoldemGameStatusResponse> responseObserver) {

        String token = request.getToken();
        long userId = TokenUtil.tokenToUserId(token);
        TexasHoldEmGameSceneDO texasHoldemGameSceneDO =
                (TexasHoldEmGameSceneDO) SceneManager.getInstance().findById(SceneManager.SCENE_TEXAS_HOLDEM);
        TexasHoldEmGameAsset asset = new TexasHoldEmGameAsset();
        UserDO userDO = UserManager.getInstance().findById(userId);
        long money = userDO.getMoney();
        asset.setMoney(money);
        userDO.setMoney(0);
        TexasHoldEmUserAgentDO agent = new TexasHoldEmUserAgentDO(SceneManager.SCENE_TEXAS_HOLDEM, userDO.getName(), asset, responseObserver);
        texasHoldemGameSceneDO.enter(agent);
        AgentManager.getInstance().getUserIdAgentMap().put(userId, agent);
    }
}
