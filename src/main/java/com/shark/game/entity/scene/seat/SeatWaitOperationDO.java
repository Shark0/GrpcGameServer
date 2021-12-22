package com.shark.game.entity.scene.seat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class SeatWaitOperationDO implements Runnable {

    private int seatId;
    protected boolean isOperation;
    private long lastOperationTime;


    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(isOperation) {
            return;
        } else {
            lastOperationTime = lastOperationTime - 1000;
            if(lastOperationTime <= 0) {
                startFoldOperation(seatId);
            } else {
                waitOperation(seatId, lastOperationTime);
                run();
            }
        }
    }

    public abstract void waitOperation(int seatId, long lastOperationTime);

    public abstract void startFoldOperation(int seatId);
}
