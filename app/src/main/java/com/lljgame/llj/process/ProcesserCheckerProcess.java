package com.lljgame.llj.process;

import android.content.Context;
import android.content.Intent;

import java.util.Date;

/**
 * Created by Davia.Li on 2017-02-13.
 */

//判断时间是否该打开应用
public class ProcesserCheckerProcess implements Runnable {
    private Thread mThread;
    private boolean mStarting;
    private Context mContext;

    public ProcesserCheckerProcess(Context context) {
        mContext = context;
        mThread = new Thread(this);
    }

    public void start() {
        mStarting = true;
        mThread.start();
    }

    public void stop() {
        mStarting = false;
        mThread.interrupt();
    }

    @Override
    public void run() {
        while (mStarting) {
            try {
                Thread.sleep(Common.HEART_BEAT_INTERVAL - 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long now = new Date().getTime();
            long last = Common.getLastHeartBeatTime();
            if((now - last) > (Common.HEART_BEAT_INTERVAL * 2 + 1)) {
                launchApp();
            }
        }
    }

    private void launchApp() {
        //System.out.println("launch " + Common.APP_PACKAGE_NAME);
        Intent i = mContext.getPackageManager().getLaunchIntentForPackage(Common.APP_PACKAGE_NAME);
        if(i != null) {
            mContext.startActivity(i);
        }

    }
}
