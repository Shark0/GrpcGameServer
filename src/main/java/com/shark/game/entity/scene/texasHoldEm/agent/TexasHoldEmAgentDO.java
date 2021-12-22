package com.shark.game.entity.scene.texasHoldEm.agent;

import com.shark.game.entity.scene.BaseAgentDO;
import com.shark.game.entity.scene.seat.BaseSeatAgentDO;
import com.shark.game.entity.scene.texasHoldEm.TexasHoldEmGameActionListener;
import com.shark.game.entity.scene.texasHoldEm.TexasHoldEmGameStatusListener;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
public abstract class TexasHoldEmAgentDO extends BaseSeatAgentDO<TexasHoldEmGameAsset>
        implements TexasHoldEmGameStatusListener, Serializable {

    protected TexasHoldEmGameActionListener actionListener;

    public TexasHoldEmAgentDO(long sceneId, String name, TexasHoldEmGameAsset texasHoldEmGameAsset) {
        super(sceneId, name, texasHoldEmGameAsset);
    }


    public boolean standUp() {
        return actionListener.onActionStandUp(this);
    }

    public boolean sitDown() {
        return actionListener.onActionSitDown(this);
    }

    public boolean fold() {
        return actionListener.onActionFold(this);
    }

    public boolean raise(long bet) {
        return actionListener.onActionRaise(this, bet);
    }

    public boolean call(long bet) {
        return actionListener.onActionCall(this, bet);
    }

    public boolean exit() {
        return actionListener.onActionExit(this);
    }
}
