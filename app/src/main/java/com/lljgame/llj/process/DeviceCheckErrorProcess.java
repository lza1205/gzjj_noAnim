package com.lljgame.llj.process;

import com.lljgame.llj.MainBoard;

/**
 * Created by Davia.Li on 2017-08-09.
 */

public class DeviceCheckErrorProcess implements Runnable {
    private boolean mRunning;
    private Thread mThread;
    private MainBoard mMainBoard;

    public DeviceCheckErrorProcess(MainBoard mainBoard) {
        mThread = new Thread(this);
        mMainBoard = mainBoard;
    }

    public void start() {
        mRunning = true;
        mThread.start();
    }

    public void stop() {
        mRunning = false;
        mThread.interrupt();
    }

    public boolean isRunning() {
        return mRunning;
    }


    @Override
    public void run() {
        while (mRunning) {
            try {
                Thread.sleep(300 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mMainBoard.sendRData();
        }
    }
}
