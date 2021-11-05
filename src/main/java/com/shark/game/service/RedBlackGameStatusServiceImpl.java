package com.shark.game.service;

import com.shark.game.entity.room.RedBlackGameRoomDO;
import com.shark.game.manager.RoomManager;
import io.grpc.stub.StreamObserver;

public class RedBlackGameStatusServiceImpl extends ReadBlackGameStatusServiceGrpc.ReadBlackGameStatusServiceImplBase {

    @Override
    public void start(RedBlackGameStatusService.StatusRequest request, StreamObserver<RedBlackGameStatusService.StatusResponse> responseObserver) {
        RedBlackGameRoomDO room = (RedBlackGameRoomDO) RoomManager.getInstance().findRoomByType(2);
        String token = request.getToken();
        room.enterGame(responseObserver, token);
    }
}
