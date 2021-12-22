package com.shark.game.entity.scene.texasHoldEm;

import com.shark.game.entity.scene.texasHoldEm.agent.TexasHoldEmAgentDO;

public interface TexasHoldEmGameActionListener {
    boolean onActionStandUp(TexasHoldEmAgentDO agent);
    boolean onActionSitDown(TexasHoldEmAgentDO agent);
    boolean onActionFold(TexasHoldEmAgentDO agent);
    boolean onActionRaise(TexasHoldEmAgentDO agent, long bet);
    boolean onActionCall(TexasHoldEmAgentDO agent, long bet);
    boolean onActionExit(TexasHoldEmAgentDO agent);
}
