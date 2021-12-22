package com.shark.game.service;


import com.shark.game.entity.user.UserDO;
import com.shark.game.manager.UserManager;
import com.shark.game.util.TokenUtil;
import io.grpc.stub.StreamObserver;

import java.util.Date;

public class LoginServiceImpl extends LoginServiceGrpc.LoginServiceImplBase {


    @Override
    public void start(LoginServiceOuterClass.LoginRequest request, StreamObserver<LoginServiceOuterClass.LoginResponse> responseObserver) {
        String playerName = request.getPlayerName();
        long playerId = new Date().getTime();
        UserDO player = findPlayer(playerId);
        player.setName(playerName);
        String token = TokenUtil.playerIdToToken(playerId);
        UserManager.getInstance().addPlayer(player);
        sendLoginSuccessResponse(responseObserver, token, player.getName(), player.getMoney());
    }

    private void sendLoginSuccessResponse(
            StreamObserver<LoginServiceOuterClass.LoginResponse> responseObserver,
            String token, String playerName, long playerMoney) {
        LoginServiceOuterClass.LoginResponse response =
                LoginServiceOuterClass.LoginResponse.newBuilder()
                        .setStatus(1).setToken(token).setName(playerName).setMoney(playerMoney)
                        .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private UserDO findPlayer(long playerId) {
        //FIXME load from db
        UserDO userDo = new UserDO();
        userDo.setId(playerId);
        userDo.setAgentId(1);
        userDo.setName("Player" + playerId);
        int money = 20000;
        userDo.setMoney(money);
        return userDo;
    }
}
