package com.shark.game.service;

import com.shark.game.entity.room.texasHoldEm.TexasHoldEmGameRoomDO;
import com.shark.game.manager.RoomManager;
import com.shark.game.util.TokenUtil;
import io.grpc.stub.StreamObserver;

public class TexasHoldemGameOperationServiceImpl extends TexasHoldemOperationServiceGrpc.TexasHoldemOperationServiceImplBase {

    @Override
    public void sendTexasHoldemGameOperation(
            TexasHoldemGameService.TexasHoldemGameOperationRequest request,
            StreamObserver<TexasHoldemGameService.TexasHoldemGameOperationResponse> responseObserver) {
        String token = request.getToken();
        long playerId = TokenUtil.tokenToPlayerId(token);
        Integer operation = request.getOperation();
        Long bet = request.getBet();
        TexasHoldEmGameRoomDO texasHoldemGameRoomDO = (TexasHoldEmGameRoomDO) RoomManager.getInstance().getRoom(playerId);
        TexasHoldemGameService.TexasHoldemGameOperationResponse response;
        if(texasHoldemGameRoomDO == null) {
            response = TexasHoldemGameService.TexasHoldemGameOperationResponse.newBuilder().setStatus(1).build();
        } else {
            boolean success = texasHoldemGameRoomDO.operation(playerId, operation, bet);
            if(success) {
                response = TexasHoldemGameService.TexasHoldemGameOperationResponse.newBuilder().setStatus(1).build();
            } else {
                response = TexasHoldemGameService.TexasHoldemGameOperationResponse.newBuilder().setStatus(-1).build();
            }
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
