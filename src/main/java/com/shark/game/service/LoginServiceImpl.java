package com.shark.game.service;


import com.shark.game.entity.player.PlayerDO;
import com.shark.game.manager.PlayerManager;
import com.shark.game.util.TokenUtil;
import io.grpc.stub.StreamObserver;

import java.util.Date;

public class LoginServiceImpl extends LoginServiceGrpc.LoginServiceImplBase {


    @Override
    public void start(LoginServiceOuterClass.LoginRequest request, StreamObserver<LoginServiceOuterClass.LoginResponse> responseObserver) {
        String playerName = request.getPlayerName();
        long playerId = new Date().getTime();
        PlayerDO player = findPlayer(playerId);
        player.setName(playerName);
        String token = TokenUtil.playerIdToToken(playerId);
        PlayerManager.getInstance().putPlayer(player);
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

    private PlayerDO findPlayer(long playerId) {
        //FIXME load from db
        PlayerDO playerDo = new PlayerDO();
        playerDo.setId(playerId);
        playerDo.setAgentId(1);
        playerDo.setName("Player" + playerId);
        int money = 20000;
        playerDo.setMoney(money);
        return playerDo;
    }
}
