package com.shark.game.service;

import com.shark.game.entity.player.PlayerDO;
import com.shark.game.entity.room.RedBlackGameRoomDO;
import com.shark.game.entity.room.RockPaperScissorsGameRoomDO;
import com.shark.game.manager.PlayerManager;
import com.shark.game.manager.RoomManager;
import io.grpc.stub.StreamObserver;

import static com.shark.game.manager.RoomManager.RED_BLACK_ROOM_TYPE;
import static com.shark.game.manager.RoomManager.ROCK_PAPER_SCISSORS_ROOM_TYPE;

public class EnterGameRoomServiceImpl extends EnterGameRoomServiceGrpc.EnterGameRoomServiceImplBase{

    @Override
    public void start(EnterGameRoomServiceOuterClass.EnterGameRoomRequest request,
                      StreamObserver<EnterGameRoomServiceOuterClass.EnterGameRoomResponse> responseObserver) {
        String token = request.getToken();
        long playerId = Long.parseLong(token);
        PlayerDO playerDO = PlayerManager.getInstance().findById(playerId);
        int roomType = request.getRoomType();

        switch (roomType) {
            case 1: //猜拳
                RockPaperScissorsGameRoomDO rockPaperScissorsGameRoom = new RockPaperScissorsGameRoomDO(
                        playerDO.getAgentId(), ROCK_PAPER_SCISSORS_ROOM_TYPE, 10);
                RoomManager.getInstance().putRoomByToken(token, rockPaperScissorsGameRoom);
                sendEnterRoomResponse(responseObserver);
                break;
            case 2:
                RedBlackGameRoomDO redBlackGameRoom =
                        (RedBlackGameRoomDO) RoomManager.getInstance().findRoomByAgentIdAndRoomType(playerDO.getAgentId(), RED_BLACK_ROOM_TYPE);
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
