package com.shark.game.service;

import com.shark.game.entity.room.RedBlackGameRoom;
import com.shark.game.manager.RoomManager;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

public class RedBlackGameStatusServiceImpl extends ReadBlackGameStatusServiceGrpc.ReadBlackGameStatusServiceImplBase {

    @Override
    public void start(RedBlackGameStatusService.StatusRequest request, StreamObserver<RedBlackGameStatusService.StatusResponse> responseObserver) {
        RedBlackGameRoom room = (RedBlackGameRoom) RoomManager.getInstance().findRoomById(2);
        String token = request.getToken();
        room.enterGame(responseObserver, token);
    }
}
