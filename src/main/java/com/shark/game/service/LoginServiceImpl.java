package com.shark.game.service;


import com.shark.game.entity.player.PlayerDO;
import com.shark.game.manager.PlayerManager;
import com.shark.game.util.TokenUtil;
import io.grpc.stub.StreamObserver;

import java.util.Random;

public class LoginServiceImpl extends LoginServiceGrpc.LoginServiceImplBase {

    @Override
    public void start(LoginServiceOuterClass.LoginRequest request, StreamObserver<LoginServiceOuterClass.LoginResponse> responseObserver) {
        int playerId = request.getPlayerId();
        PlayerDO player = findPlayer(playerId);
        String token = TokenUtil.playerIdToToken(playerId);
        PlayerManager.getInstance().putPlayer(player);
        sendLoginSuccessResponse(responseObserver, token);
    }

    private void sendLoginSuccessResponse(StreamObserver<LoginServiceOuterClass.LoginResponse> responseObserver,
                                          String token) {
        LoginServiceOuterClass.LoginResponse response =
                LoginServiceOuterClass.LoginResponse.newBuilder()
                        .setStatus(0).setToken(token).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private PlayerDO findPlayer(long playerId) {
        //FIXME load from db
        PlayerDO playerDo = new PlayerDO();
        playerDo.setId(playerId);
        playerDo.setAgentId(1);
        playerDo.setName("Player" + playerId);
        int money = new Random().nextInt(10000) + 100000;
        playerDo.setMoney(money);
        return playerDo;
    }
}
