package com.shark.game.entity.room;

import com.shark.game.entity.PlayerDo;
import com.shark.game.service.RockPaperScissorsGameServiceOuterClass;
import io.grpc.stub.StreamObserver;

public class RockPaperScissorsGameRoom extends BaseRoom {

    public void gameStart(RockPaperScissorsGameServiceOuterClass.GameRequest request,
                          StreamObserver<RockPaperScissorsGameServiceOuterClass.GameResponse> responseObserver) {
        String token = request.getToken();
        PlayerDo playerDo = findPlayerByToken(token);
        if(playerDo == null) {
            sendFailResponse(responseObserver, -1, "用戶不存在");
            return;
        }
        int userOperation = request.getOperation();
        int computerOperation = (int) (Math.random() * 3);
        int bet = request.getBet();
        if(!isBetEnough(playerDo, bet)) {
            sendFailResponse(responseObserver, -2, "遊戲幣不足");
            return;
        }
        int result = generateResult(userOperation, computerOperation, bet);
        int playerMoney = playerDo.getMoney() + result;
        System.out.println("playerMoney = " + playerMoney + ", result = " + result);
        playerDo.setMoney(playerMoney);

        sendSuccessResponse(responseObserver, computerOperation, result, playerMoney);
    }

    private void sendFailResponse(
            StreamObserver<RockPaperScissorsGameServiceOuterClass.GameResponse> responseObserver, int status, String message) {
        RockPaperScissorsGameServiceOuterClass.GameResponse response = RockPaperScissorsGameServiceOuterClass.GameResponse.newBuilder()
                .setStatus(status)
                .setMessage(message)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private int generateResult(int userOperation, int computerOperation, int bet) {
        int result = 0;
        switch (computerOperation) {
            case 0:
                switch (userOperation) {
                    case 2:
                        result = bet * -1;
                        break;
                    case 1:
                        result = bet;
                        break;
                }
                break;
            case 1:
                switch (userOperation) {
                    case 0:
                        result = bet * -1;
                        break;
                    case 2:
                        result = bet;
                        break;
                }
                break;
            case 2:
                switch (userOperation) {
                    case 1:
                        result = bet * -1;
                        break;
                    case 0:
                        result = bet;
                        break;
                }
                break;
        }
        return result;
    }

    private void sendSuccessResponse(StreamObserver<RockPaperScissorsGameServiceOuterClass.GameResponse> responseObserver, int computerOperation, int result, int playerMoney) {
        RockPaperScissorsGameServiceOuterClass.GameResponse response = RockPaperScissorsGameServiceOuterClass.GameResponse.newBuilder()
                .setStatus(0)
                .setResult(result)
                .setComputerOperation(computerOperation)
                .setPlayerMoney(playerMoney)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
