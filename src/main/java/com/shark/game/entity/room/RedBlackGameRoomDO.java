package com.shark.game.entity.room;

import com.shark.game.entity.player.PlayerDO;
import com.shark.game.manager.PlayerManager;
import com.shark.game.service.RedBlackGameStatusService;
import io.grpc.stub.StreamObserver;

import java.util.*;

public class RedBlackGameRoomDO extends BaseStateRoomDO {

    private final int STATUS_OPEN_RESULT = 1, STATUS_SEND_RESULT = 2;

    private final Integer RED_POSITION = 0, BLACK_POSITION = 1;

    private final Map<Long, Map<Integer, Integer>> playerIdBetMap = new HashMap<>();

    private int redCard;

    private int blackCard;

    public RedBlackGameRoomDO(int agentId, int gameType, int minBet) {
        super(agentId, gameType, minBet);
    }

    @Override
    protected Map<Integer, Integer> generateTimeStateList() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(STATUS_START, 10000);
        map.put(STATUS_OPEN_RESULT, 4000);
        map.put(STATUS_SEND_RESULT, 4000);
        return map;
    }

    @Override
    protected void notifyState() {
        String message = "";
        switch (status) {
            case STATUS_START:
                message = "開始下注, " + (statusTime / 1000) + "秒後開獎";
                break;
            case STATUS_OPEN_RESULT:
                message = "開獎中, " + (statusTime / 1000) + "秒後發送獎勵";
                break;
            case STATUS_SEND_RESULT:
                message = "發獎中, " + (statusTime / 1000) + "秒後開始下注";
                break;
        }
        for(Long playerId: playerIdObserverMap.keySet()) {
            sendStatusResponse(playerId, message);
        }
    }

    protected void changeState() {
        switch (status) {
            case STATUS_START:
                status = STATUS_OPEN_RESULT;
                break;
            case STATUS_OPEN_RESULT:
                status = STATUS_SEND_RESULT;
                break;
            case STATUS_SEND_RESULT:
                status = STATUS_START;
                break;
        }
        stateFunction();
    }

    protected void stateFunction() {
        switch (status) {
            case 0:
                startBet();
                break;
            case 1:
                openResult();
                break;
            case 2:
                sendResult();
                break;
        }
    }

    private void startBet() {
        playerIdBetMap.clear();
        String message = "開始下注, " + (statusTime / 1000) + "秒後開獎";
        for(Long playerId: playerIdObserverMap.keySet()) {
            sendStatusResponse(playerId, message);
        }
    }

    private void openResult() {
        List<Integer> cardList = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        int redChoose = new Random().nextInt(cardList.size() - 1);
        redCard = cardList.get(redChoose);
        cardList.remove(redChoose);
        int blackChoose = new Random().nextInt(cardList.size() - 1);
        blackCard = cardList.get(blackChoose);
        String message = "開獎結果, 紅: " + redCard + ", 黑: " + blackCard + ((redCard > blackCard)? ", 紅贏, ": ", 黑贏, ") + "4秒後發送獎勵";
        for(Long playerId: playerIdObserverMap.keySet()) {
            sendStatusResponse(playerId, message);
        }
    }

    private void sendResult() {
        for(Long playerId: playerIdObserverMap.keySet()) {
            PlayerDO playerDo = PlayerManager.getInstance().findById(playerId);
            Map<Integer, Integer> betMap = playerIdBetMap.get(playerId);
            if(betMap != null) {
                long money = playerDo.getMoney();
                int totalBet = 0;
                int win = 0;
                Integer redBet = betMap.get(RED_POSITION);
                totalBet = totalBet + redBet;
                if(redCard > blackCard) {
                    win = win + redBet * 2;
                }
                Integer blackBet = betMap.get(BLACK_POSITION);
                totalBet = totalBet + blackBet;
                if(blackCard > redCard) {
                    win = win + blackBet * 2;
                }
                money = money + win;
                playerDo.setMoney(money);
                String message = "這場總下注金額: " + totalBet + ", 下紅金額: " + redBet + ", 下黑金額: " + blackBet +
                        ", 贏: " + win + ", 玩家剩餘金額: " + playerDo.getMoney()  + ", 4秒後發送獎勵";
                sendStatusResponse(playerId, message);
            } else {
                String message = "這場沒下注, 4秒後發送獎勵";
                sendStatusResponse(playerId, message);
            }
        }
    }

    private void sendStatusResponse(Long playerId, String message) {
        System.out.println("sendStatusResponse playerId = " + playerId + ", message = " + message);
        StreamObserver observer = playerIdObserverMap.get(playerId);
        RedBlackGameStatusService.StatusResponse gameStatusResponse;
        gameStatusResponse = RedBlackGameStatusService.StatusResponse.newBuilder()
                .setStatus(status).setMessage(message).build();
        try {
            observer.onNext(gameStatusResponse);
        } catch (Exception e) {
            e.printStackTrace();
            playerIdObserverMap.remove(playerId);
        }
    }

    public void placeBet(StreamObserver observer, Long playerId, int position, int bet) {
        Map<Integer, Integer> betMap = playerIdBetMap.get(playerId);
        if(betMap == null) {
            betMap = new HashMap<>();
            betMap.put(RED_POSITION, 0);
            betMap.put(BLACK_POSITION, 0);
            playerIdBetMap.put(playerId, betMap);
        }
        PlayerDO playerDo = PlayerManager.getInstance().findById(playerId);
        if(status != STATUS_START) {
            RedBlackGameStatusService.StatusResponse response = RedBlackGameStatusService.StatusResponse.newBuilder()
                    .setStatus(-1).setMessage("現在非下注階段").build();
            observer.onNext(response);
            return;
        }
        if(!isBetEnough(playerDo, bet)) {
            RedBlackGameStatusService.StatusResponse response = RedBlackGameStatusService.StatusResponse.newBuilder()
                    .setStatus(-1).setMessage("遊戲幣不足").build();
            observer.onNext(response);
            return;
        }
        if(position != RED_POSITION && position != BLACK_POSITION) {
            RedBlackGameStatusService.StatusResponse response = RedBlackGameStatusService.StatusResponse.newBuilder()
                    .setStatus(-1).setMessage("操作錯誤").build();
            observer.onNext(response);
            return;
        }
        Integer redBet = betMap.get(RED_POSITION);
        Integer blackBet = betMap.get(BLACK_POSITION);
        long money = playerDo.getMoney();
        money = money - bet;
        playerDo.setMoney(money);
        if(position == RED_POSITION) {
            redBet = redBet + bet;
            betMap.put(RED_POSITION, redBet);
        } else {
            blackBet = blackBet + bet;
            betMap.put(BLACK_POSITION, blackBet);
        }
        Integer totalBet = redBet + blackBet;
        String message = "這場總下注金額: " + totalBet + ", 下紅金額: " + redBet + ", 下黑金額: " + blackBet +
                ", 剩餘金額: " + playerDo.getMoney();
        RedBlackGameStatusService.StatusResponse betResponse = RedBlackGameStatusService.StatusResponse.newBuilder()
                .setStatus(0).setMessage(message).build();
        observer.onNext(betResponse);
        observer.onCompleted();
    }

    public void enterGame(StreamObserver observer, Long playerId) {
        playerIdObserverMap.put(playerId, observer);
    }

    public void exitGame(String token) {
        System.out.println("RedBlackGameRoom exitGame(): token = " + token);
        StreamObserver observer = playerIdObserverMap.get(token);
        if(observer != null) {
            playerIdObserverMap.remove(token);
            observer.onCompleted();
        }
    }
}
