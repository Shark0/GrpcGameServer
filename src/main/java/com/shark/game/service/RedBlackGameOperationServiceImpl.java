package com.shark.game.service;

import com.shark.game.entity.room.RedBlackGameRoomDO;
import com.shark.game.manager.RoomManager;
import io.grpc.stub.StreamObserver;

public class RedBlackGameOperationServiceImpl extends ReadBlackGameOperationServiceGrpc.ReadBlackGameOperationServiceImplBase {

    final private int EXIT_GAME = 0, PLACE_BET = 1;

    @Override
    public void start(RedBlackGameOperationService.OperationRequest request,
                      StreamObserver<RedBlackGameOperationService.OperationResponse> responseObserver) {
        int operation = request.getOperation();
        switch (operation) {
            case EXIT_GAME:
                exitRoomOperation(responseObserver, request.getToken());
                break;
            case PLACE_BET:
                placeBetOperation(responseObserver, request.getToken(), request.getPosition(), request.getBet());
                break;
            default:
                sendOperationFailResponse(responseObserver);
                break;
        }
    }

    private void exitRoomOperation(StreamObserver<RedBlackGameOperationService.OperationResponse> responseObserver, String token) {
        RedBlackGameRoomDO room = (RedBlackGameRoomDO) RoomManager.getInstance().getRoomByToken(token);
        room.exitGame(token);
        RedBlackGameOperationService.OperationResponse response =
                RedBlackGameOperationService.OperationResponse.newBuilder()
                        .setStatus(-1).setMessage("離開遊戲").build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void placeBetOperation(StreamObserver responseObserver, String token, int position, int bet) {
        RedBlackGameRoomDO room = (RedBlackGameRoomDO) RoomManager.getInstance().getRoomByToken(token);
        room.placeBet(responseObserver, token, position, bet);
    }

    private void sendOperationFailResponse(StreamObserver responseObserver) {
        RedBlackGameOperationService.OperationResponse response =
                RedBlackGameOperationService.OperationResponse.newBuilder()
                        .setStatus(-1).setMessage("錯誤操作").build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
