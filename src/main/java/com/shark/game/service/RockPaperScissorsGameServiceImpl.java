package com.shark.game.service;


import com.shark.game.entity.player.PlayerDO;
import com.shark.game.entity.room.RockPaperScissorsGameRoomDO;
import com.shark.game.manager.PlayerManager;
import com.shark.game.manager.RoomManager;
import com.shark.game.util.TokenUtil;
import io.grpc.stub.StreamObserver;

public class RockPaperScissorsGameServiceImpl extends RockPaperScissorsGameServiceGrpc.RockPaperScissorsGameServiceImplBase {

    @Override
    public void start(RockPaperScissorsGameServiceOuterClass.GameRequest request, StreamObserver<RockPaperScissorsGameServiceOuterClass.GameResponse> responseObserver) {
        String token = request.getToken();
        PlayerDO playerDO = PlayerManager.getInstance().findById(TokenUtil.tokenToPlayerId(token));
        RockPaperScissorsGameRoomDO room = (RockPaperScissorsGameRoomDO)
                RoomManager.getInstance().findRoomByAgentIdAndRoomType(playerDO.getAgentId(), RoomManager.ROCK_PAPER_SCISSORS_ROOM_TYPE);
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
