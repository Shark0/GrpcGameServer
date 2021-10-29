package com.shark.game.service;

import com.shark.game.entity.room.RedBlackGameRoom;
import com.shark.game.entity.room.RockPaperScissorsGameRoom;
import com.shark.game.manager.RoomManager;
import io.grpc.stub.StreamObserver;

import static com.shark.game.manager.RoomManager.*;

public class EnterGameRoomServiceImpl extends EnterGameRoomServiceGrpc.EnterGameRoomServiceImplBase{

    @Override
    public void start(EnterGameRoomServiceOuterClass.EnterGameRoomRequest request,
                      StreamObserver<EnterGameRoomServiceOuterClass.EnterGameRoomResponse> responseObserver) {
        String token = request.getToken();
        int roomId = request.getRoomId();

        switch (roomId) {
            case 1: //猜拳
                RockPaperScissorsGameRoom rockPaperScissorsGameRoom = new RockPaperScissorsGameRoom();
                RoomManager.getInstance().putRoomByToken(token, rockPaperScissorsGameRoom);
                sendEnterRoomResponse(responseObserver);
                break;
            case 2:
                RedBlackGameRoom redBlackGameRoom = (RedBlackGameRoom)
                        RoomManager.getInstance().findRoomById(RED_BLACK_ROOM_ID);
                RoomManager.getInstance().putRoomByToken(token, redBlackGameRoom);
                sendEnterRoomResponse(responseObserver);
                break;
            case 3:
                //TODO
                break;
            default:
                sendNotFindRoomResponse(responseObserver);
                break;
        }
    }

    private void sendNotFindRoomResponse(StreamObserver<EnterGameRoomServiceOuterClass.EnterGameRoomResponse> responseObserver) {
        EnterGameRoomServiceOuterClass.EnterGameRoomResponse response =
                EnterGameRoomServiceOuterClass.EnterGameRoomResponse.newBuilder()
                        .setStatus(-1).setMessage("找不到配對房間").build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void sendEnterRoomResponse(StreamObserver<EnterGameRoomServiceOuterClass.EnterGameRoomResponse> responseObserver) {
        EnterGameRoomServiceOuterClass.EnterGameRoomResponse response =
                EnterGameRoomServiceOuterClass.EnterGameRoomResponse.newBuilder()
                        .setStatus(0).setMessage("進房成功").build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
