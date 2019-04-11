package com.lljgame.llj.process;



import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Davia.Li on 2017-11-16.
 */

public class ServerCallBackProcess implements Runnable {
    private Thread mThread;
    private boolean mRunning;
    private BlockingQueue<Runnable> mCallBackQueue;

    public ServerCallBackProcess() {
        mThread = new Thread(this);
        mCallBackQueue = new ArrayBlockingQueue<>(30);
    }

    public void start() {
        mRunning = true;
        mThread.start();
    }

    public void stop() {
        mRunning = false;
        mThread.interrupt();
    }

    public void addServerCallBack(Runnable runnable) {
        mCallBackQueue.offer(runnable);
    }


    @Override
    public void run() {
        while (mRunning) {
            try {
                Runnable task = mCallBackQueue.take();
                task.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
