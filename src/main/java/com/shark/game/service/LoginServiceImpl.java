package com.shark.game.service;


import com.shark.game.entity.player.PlayerDO;
import com.shark.game.manager.PlayerManager;
import io.grpc.stub.StreamObserver;

import java.util.Random;

public class LoginServiceImpl extends LoginServiceGrpc.LoginServiceImplBase {

    @Override
    public void start(LoginServiceOuterClass.LoginRequest request, StreamObserver<LoginServiceOuterClass.LoginResponse> responseObserver) {
        int playerId = request.getPlayerId();
        PlayerDO player = findPlayer(playerId);
        if(player == null) {
            sendLoginFailResponse(responseObserver);
        }
        String token = generateToken(player);
        PlayerManager.getInstance().putPlayer(token, player);
        sendLoginSuccessResponse(responseObserver, token);
    }

    private void sendLoginFailResponse(StreamObserver<LoginServiceOuterClass.LoginResponse> responseObserver) {
        LoginServiceOuterClass.LoginResponse response =
                LoginServiceOuterClass.LoginResponse.newBuilder()
                .setStatus(-1).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void sendLoginSuccessResponse(StreamObserver<LoginServiceOuterClass.LoginResponse> responseObserver,
                                          String token) {
        LoginServiceOuterClass.LoginResponse response =
                LoginServiceOuterClass.LoginResponse.newBuilder()
                        .setStatus(0).setToken(token).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private PlayerDO findPlayer(int playerId) {
        //FIXME load from db
        PlayerDO playerDo = new PlayerDO();
        playerDo.setId(playerId);
        playerDo.setName("Player" + playerId);
        int money = new Random().nextInt(10000) + 100000;
        playerDo.setMoney(money);
        return playerDo;
    }

    private String generateToken(PlayerDO player) {
        //FIXME use jwt
        String token = String.valueOf(player.getId());
        return token;
    }
}
