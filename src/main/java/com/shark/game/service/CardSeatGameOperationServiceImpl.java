package com.shark.game.service;

import com.shark.game.entity.room.CardSeatRoomDO;
import com.shark.game.manager.RoomManager;
import io.grpc.stub.StreamObserver;


public class CardSeatGameOperationServiceImpl extends
        CardSeatGameOperationServiceGrpc.CardSeatGameOperationServiceImplBase {

    @Override
    public void sendCardSeatGameOperation(
            CardSeatGameService.CardSeatGameOperationRequest request,
            StreamObserver<CardSeatGameService.CardSeatGameOperationResponse> responseObserver) {
        String token = request.getToken();
        int operation = request.getOperation();
        System.out.println("CardSeatGameOperationServiceImpl token: " + token + ", operation = " + operation);
        CardSeatRoomDO cardSeatRoomDo = (CardSeatRoomDO) RoomManager.getInstance().getRoomByToken(token);
        cardSeatRoomDo.operation(responseObserver, token, operation);
    }
}
