package com.shark.game.service;

import com.shark.game.entity.player.PlayerDO;
import com.shark.game.entity.room.RedBlackGameRoomDO;
import com.shark.game.manager.PlayerManager;
import com.shark.game.manager.RoomManager;
import com.shark.game.util.TokenUtil;
import io.grpc.stub.StreamObserver;

public class RedBlackGameStatusServiceImpl extends
        ReadBlackGameStatusServiceGrpc.ReadBlackGameStatusServiceImplBase {

    @Override
    public void start(RedBlackGameStatusService.StatusRequest request,
                      StreamObserver<RedBlackGameStatusService.StatusResponse> responseObserver) {
        String token = request.getToken();
        long playerId = TokenUtil.tokenToPlayerId(token);
        PlayerDO playerDO = PlayerManager.getInstance().findById(playerId);

        RedBlackGameRoomDO room = (RedBlackGameRoomDO) RoomManager.getInstance().findRoomByAgentIdAndRoomType(
                playerDO.getAgentId(), RoomManager.RED_BLACK_ROOM_TYPE);

        room.enterGame(responseObserver, playerId);
    }
}
