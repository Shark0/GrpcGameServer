package com.shark.game.entity.room.seat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class SeatWaitOperationDO implements Runnable {

    private int seatId;
    protected boolean isOperation;
    private long waitTime;


    @Override
    public void run() {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!isOperation) {
            startFoldOperation(seatId);
        }
    }

    public abstract void startFoldOperation(int seatId);
}
