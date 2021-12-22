package com.shark.game.entity.scene.texasHoldEm;

import com.shark.game.entity.scene.texasHoldEm.response.*;

import java.util.List;
import java.util.Map;

public interface TexasHoldEmGameStatusListener {
    void notifyEnterSceneInfo(int sceneStatus);
    void notifySitDown(int seatId);
    void notifyStandUp();
    void notifyNoSeat();
    void notifySeatInfo(TexasHoldEmSeatInfoResponseDO responseDO);
    void notifySceneInfo(TexasHoldEmSceneInfoResponseDO responseDO);
    boolean notifyCheckLive();
    void notifyStatusChanged(int status);
    void notifySeatCard(Map<Integer, TexasHoldEmHandCardResponseDO> seatIdCardMap);
    void notifySeatStartOperation(TexasHoldEmStartOperationResponseDO responseDO);
    void notifyWaitingSeatOperation(TexasHoldEmWaitOperationResponseDO responseDO);
    void notifySeatOperation(TexasHoldEmSeatOperationResponseDO responseDO);
    void notifyDealPublicCard(List<Integer> publicCardList);
    void notifyWinPotBet(List<TexasHoldEmWinPotBetResponseDO> winPotBetResponseDOList);
}
