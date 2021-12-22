package com.shark.game.service;

import com.shark.game.entity.scene.texasHoldEm.TexasHoldEmGameSceneDO;
import com.shark.game.entity.scene.texasHoldEm.agent.TexasHoldEmUserAgentDO;
import com.shark.game.manager.AgentManager;
import com.shark.game.util.TokenUtil;
import io.grpc.stub.StreamObserver;

public class TexasHoldemGameOperationServiceImpl extends TexasHoldemOperationServiceGrpc.TexasHoldemOperationServiceImplBase {

    @Override
    public void sendTexasHoldemGameOperation(
            TexasHoldemGameService.TexasHoldemGameOperationRequest request,
            StreamObserver<TexasHoldemGameService.TexasHoldemGameOperationResponse> responseObserver) {
        String token = request.getToken();
        long userId = TokenUtil.tokenToUserId(token);

        TexasHoldEmUserAgentDO agentDO = (TexasHoldEmUserAgentDO) AgentManager.getInstance().getUserIdAgentMap().get(userId);
        TexasHoldemGameService.TexasHoldemGameOperationResponse response;
        boolean success = false;
        if(agentDO != null) {
            Integer operation = request.getOperation();
            switch (operation) {
                case TexasHoldEmGameSceneDO.OPERATION_EXIT:
                    success = agentDO.exit();
                    break;
                case TexasHoldEmGameSceneDO.OPERATION_STAND_UP:
                    success = agentDO.standUp();
                    break;
                case TexasHoldEmGameSceneDO.OPERATION_SIT_DOWN:
                    success = agentDO.sitDown();
                    break;
                case TexasHoldEmGameSceneDO.OPERATION_CALL:
                    success = agentDO.call(request.getBet());
                    break;
                case TexasHoldEmGameSceneDO.OPERATION_RAISE:
                    success = agentDO.raise(request.getBet());
                    break;
                case TexasHoldEmGameSceneDO.OPERATION_FOLD:
                    success = agentDO.fold();
                    break;
            }
        }

        if(success) {
            response = TexasHoldemGameService.TexasHoldemGameOperationResponse.newBuilder().setStatus(1).build();
        } else {
            response = TexasHoldemGameService.TexasHoldemGameOperationResponse.newBuilder().setStatus(-1).build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
