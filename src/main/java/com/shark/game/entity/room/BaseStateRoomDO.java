package com.shark.game.entity.room;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public abstract class BaseStateRoomDO extends BaseRoomDO {

    protected final int STATUS_START = 0;
    protected int status = 0;
    protected Map<Integer, Integer> statusTimeMap;
    protected int statusTime = 0;

    public void init() {
        statusTimeMap = generateTimeStateList();
        status = STATUS_START;
        statusTime = statusTimeMap.get(status);
        initCheckState();
    }

    protected abstract Map<Integer, Integer> generateTimeStateList();

    protected void initCheckState() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                statusTime = statusTime - 1000;
                if(statusTime <= 0) {
                    changeState();
                    statusTime = statusTimeMap.get(status);
                } else {
                    notifyState();
                }
                initCheckState();
            }
        }, 1000);
    }

    protected abstract void notifyState();

    protected abstract void changeState();
}
