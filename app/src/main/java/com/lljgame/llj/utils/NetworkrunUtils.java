package com.lljgame.llj.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by aier on 2018/5/3.
 */

public class NetworkrunUtils implements Runnable{
    private boolean mRunning=true;
    private BlockingQueue<Runnable> mCallBackQueue;

    private static NetworkrunUtils networkrunUtils=new NetworkrunUtils();

    private NetworkrunUtils(){
        mCallBackQueue=new ArrayBlockingQueue<Runnable>(15);
        Thread thread =new Thread(this);
        thread.start();
    }

    public static NetworkrunUtils getInstance(){
        return networkrunUtils;
    }

    public void add(Runnable runnable){
        mCallBackQueue.offer(runnable);
    }

    @Override
    public void run() {
        while (mRunning){
            try {
                Runnable task= mCallBackQueue.take();
                task.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop(){
        mRunning=false;
    }
}
