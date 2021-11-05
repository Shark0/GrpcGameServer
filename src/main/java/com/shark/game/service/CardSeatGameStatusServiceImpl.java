package com.shark.game.service;

import com.shark.game.entity.room.CardSeatRoomDO;
import com.shark.game.manager.RoomManager;
import io.grpc.stub.StreamObserver;

public class CardSeatGameStatusServiceImpl extends
        CardSeatGameStatusServiceGrpc.CardSeatGameStatusServiceImplBase {

    @Override
    public void registerCardSeatGameStatus(
            CardSeatGameService.CardSeatGameStatusRequest request,
            StreamObserver<CardSeatGameService.CardSeatGameStatusResponse> responseObserver) {
        String token = request.getToken();
        CardSeatRoomDO room = (CardSeatRoomDO) RoomManager.getInstance().getRoomByToken(token);
        room.enterGame(responseObserver, token);
    }
}
