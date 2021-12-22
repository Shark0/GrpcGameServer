package com.shark.game.entity.scene.texasHoldEm.agent;

import com.google.gson.Gson;
import com.shark.game.entity.scene.texasHoldEm.response.*;
import com.shark.game.service.TexasHoldemGameService;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class TexasHoldEmUserAgentDO extends TexasHoldEmAgentDO implements Serializable {

    public static final int RESPONSE_STATUS_SIT_DOWN = 0, RESPONSE_STATUS_STAND_UP = 1, RESPONSE_STATUS_NO_SEAT = 2,
            RESPONSE_STATUS_SEAT_INFO = 3, RESPONSE_STATUS_ENTER_SCENE_INFO = 4, RESPONSE_STATUS_SCENE_INFO = 5,
            RESPONSE_STATUS_CHECK_LIVE = 6, RESPONSE_STATUS_STATUS_CHANGED = 7, RESPONSE_STATUS_SEAT_CARD = 8,
            RESPONSE_STATUS_SEAT_START_ACTION = 9, RESPONSE_STATUS_WAIT_SEAT_ACTION = 10, RESPONSE_STATUS_SEAT_ACTION = 11,
            RESPONSE_STATUS_PUBLIC_CARD = 12, RESPONSE_STATUS_WIN_POT_BET = 13;


    private StreamObserver<TexasHoldemGameService.TexasHoldemGameStatusResponse> streamObserver;

    public TexasHoldEmUserAgentDO(
            long sceneId, String name, TexasHoldEmGameAsset texasHoldEmGameAsset,
            StreamObserver<TexasHoldemGameService.TexasHoldemGameStatusResponse> streamObserver) {
        super(sceneId, name, texasHoldEmGameAsset);
        this.streamObserver = streamObserver;
    }

    @Override
    public void notifyEnterSceneInfo(int roomStatus) {
        try {
            TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                    TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                            .setStatus(RESPONSE_STATUS_ENTER_SCENE_INFO)
                            .setMessage(String.valueOf(roomStatus))
                            .build();
            streamObserver.onNext(response);
        } catch (Exception ignored) {}

    }

    @Override
    public void notifySitDown(int seatId) {
        this.seatId = seatId;
        try {
            TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                    TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                            .setStatus(RESPONSE_STATUS_SIT_DOWN)
                            .setMessage(String.valueOf(seatId))
                            .build();
            streamObserver.onNext(response);
        } catch (Exception ignored) {}
    }

    @Override
    public void notifyStandUp()
    {
        this.seatId = -1;
        try {
            TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                    TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                            .setStatus(RESPONSE_STATUS_STAND_UP)
                            .build();
            streamObserver.onNext(response);
        } catch (Exception ignored) {}
    }

    @Override
    public void notifyNoSeat() {
        try {
            TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                    TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                            .setStatus(RESPONSE_STATUS_NO_SEAT)
                            .build();
            streamObserver.onNext(response);
        } catch (Exception ignored) {}

    }

    @Override
    public void notifySeatInfo(TexasHoldEmSeatInfoResponseDO responseDO) {
        try {
            TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                    TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                            .setStatus(RESPONSE_STATUS_SEAT_INFO)
                            .setMessage(new Gson().toJson(responseDO))
                            .build();
            streamObserver.onNext(response);
        } catch (Exception ignored) {}

    }

    @Override
    public void notifySceneInfo(TexasHoldEmSceneInfoResponseDO responseDO) {
        try {
            TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                    TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                            .setStatus(RESPONSE_STATUS_SCENE_INFO)
                            .setMessage(new Gson().toJson(responseDO))
                            .build();
            streamObserver.onNext(response);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean notifyCheckLive() {
        try {
            TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                    TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                            .setStatus(RESPONSE_STATUS_CHECK_LIVE)
                            .build();
            streamObserver.onNext(response);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void notifyStatusChanged(int status) {
        try {
            TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                    TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                            .setStatus(RESPONSE_STATUS_STATUS_CHANGED)
                            .setMessage(String.valueOf(status))
                            .build();
            streamObserver.onNext(response);
        } catch (Exception ignored) {}
    }

    @Override
    public void notifySeatCard(Map<Integer, TexasHoldEmHandCardResponseDO> seatIdCardListMap) {
        try {
            TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                    TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                            .setStatus(RESPONSE_STATUS_SEAT_CARD)
                            .setMessage(new Gson().toJson(seatIdCardListMap))
                            .build();
            streamObserver.onNext(response);
        } catch (Exception ignored) {}
    }

    @Override
    public void notifySeatStartOperation(TexasHoldEmStartOperationResponseDO responseDO) {
        try {
            TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                    TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                            .setStatus(RESPONSE_STATUS_SEAT_START_ACTION)
                            .setMessage(new Gson().toJson(responseDO))
                            .build();
            streamObserver.onNext(response);
        } catch (Exception ignored) {}
    }

    @Override
    public void notifyWaitingSeatOperation(TexasHoldEmWaitOperationResponseDO responseDO) {
        try {
            TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                    TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                            .setStatus(RESPONSE_STATUS_WAIT_SEAT_ACTION)
                            .setMessage(new Gson().toJson(responseDO))
                            .build();
            streamObserver.onNext(response);
        } catch (Exception ignored) {}
    }

    @Override
    public void notifySeatOperation(TexasHoldEmSeatOperationResponseDO responseDO) {
        try {
            TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                    TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                            .setStatus(RESPONSE_STATUS_SEAT_ACTION)
                            .setMessage(new Gson().toJson(responseDO))
                            .build();
            streamObserver.onNext(response);
        } catch (Exception ignored) {}
    }

    @Override
    public void notifyDealPublicCard(List<Integer> publicCardList) {
        try {
            TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                    TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                            .setStatus(RESPONSE_STATUS_PUBLIC_CARD)
                            .setMessage(new Gson().toJson(publicCardList))
                            .build();
            streamObserver.onNext(response);
        } catch (Exception ignored) {}
    }

    @Override
    public void notifyWinPotBet(List<TexasHoldEmWinPotBetResponseDO> winPotBetResponseDOList) {
        try {
            TexasHoldemGameService.TexasHoldemGameStatusResponse response =
                    TexasHoldemGameService.TexasHoldemGameStatusResponse.newBuilder()
                            .setStatus(RESPONSE_STATUS_WIN_POT_BET)
                            .setMessage(new Gson().toJson(winPotBetResponseDOList))
                            .build();
            streamObserver.onNext(response);
        } catch (Exception ignored) {}

    }
}
