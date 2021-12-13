package com.shark.game.service;

import com.shark.game.entity.room.texasHoldEm.TexasHoldEmGameRoomDO;
import com.shark.game.manager.RoomManager;
import com.shark.game.util.TokenUtil;
import io.grpc.stub.StreamObserver;

public class TexasHoldemGameStatusServiceImpl extends TexasHoldemGameStatusServiceGrpc.TexasHoldemGameStatusServiceImplBase {

    @Override
    public void registerTexasHoldemGameStatus(TexasHoldemGameService.TexasHoldemGameStatusRequest request, StreamObserver<TexasHoldemGameService.TexasHoldemGameStatusResponse> responseObserver) {
        String token = request.getToken();
        long playerId = TokenUtil.tokenToPlayerId(token);
        TexasHoldEmGameRoomDO texasHoldemGameRoomDO = (TexasHoldEmGameRoomDO) RoomManager.getInstance().getRoom(playerId);
        texasHoldemGameRoomDO.enterGame(responseObserver, playerId);
    }
}
