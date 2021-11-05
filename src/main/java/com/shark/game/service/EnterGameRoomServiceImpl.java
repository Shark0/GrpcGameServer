package com.shark.game.service;

import com.shark.game.entity.room.CardSeatRoomDO;
import com.shark.game.entity.room.RedBlackGameRoomDO;
import com.shark.game.entity.room.RockPaperScissorsGameRoomDO;
import com.shark.game.manager.RoomManager;
import io.grpc.stub.StreamObserver;

import static com.shark.game.manager.RoomManager.*;

public class EnterGameRoomServiceImpl extends EnterGameRoomServiceGrpc.EnterGameRoomServiceImplBase{

    @Override
    public void start(EnterGameRoomServiceOuterClass.EnterGameRoomRequest request,
                      StreamObserver<EnterGameRoomServiceOuterClass.EnterGameRoomResponse> responseObserver) {
        String token = request.getToken();
        int roomType = request.getRoomType();

        switch (roomType) {
            case 1: //猜拳
                RockPaperScissorsGameRoomDO rockPaperScissorsGameRoom = new RockPaperScissorsGameRoomDO();
                RoomManager.getInstance().putRoomByToken(token, rockPaperScissorsGameRoom);
                sendEnterRoomResponse(responseObserver);
                break;
            case 2:
                RedBlackGameRoomDO redBlackGameRoom =
                        (RedBlackGameRoomDO) RoomManager.getInstance().findRoomByType(RED_BLACK_ROOM_TYPE);
                RoomManager.getInstance().putRoomByToken(token, redBlackGameRoom);
                sendEnterRoomResponse(responseObserver);
                break;
            case 3:
                CardSeatRoomDO cardSeatRoomDo =
                        (CardSeatRoomDO) RoomManager.getInstance().findRoomByType(CARD_SEAT_ROOM_TYPE);
                RoomManager.getInstance().putRoomByToken(token, cardSeatRoomDo);
                sendEnterRoomResponse(responseObserver);
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
