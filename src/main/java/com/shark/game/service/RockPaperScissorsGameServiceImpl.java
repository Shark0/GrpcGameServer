package com.shark.game.service;


import com.shark.game.entity.room.RockPaperScissorsGameRoom;
import com.shark.game.manager.RoomManager;
import io.grpc.stub.StreamObserver;


public class RockPaperScissorsGameServiceImpl extends RockPaperScissorsGameServiceGrpc.RockPaperScissorsGameServiceImplBase {
    @Override
    public void start(RockPaperScissorsGameServiceOuterClass.GameRequest request, StreamObserver<RockPaperScissorsGameServiceOuterClass.GameResponse> responseObserver) {
        String token = request.getToken();
        RockPaperScissorsGameRoom room = (RockPaperScissorsGameRoom) RoomManager.getInstance().getRoomByToken(token);
        if(room == null) {
            sendNotFindRoomResponse(responseObserver);
            return;
        }

        room.gameStart(request, responseObserver);
    }

    private void sendNotFindRoomResponse(StreamObserver<RockPaperScissorsGameServiceOuterClass.GameResponse> responseObserver) {
        RockPaperScissorsGameServiceOuterClass.GameResponse response =
                RockPaperScissorsGameServiceOuterClass.GameResponse.newBuilder().setStatus(-1).setMessage("找不到配對房間").build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
