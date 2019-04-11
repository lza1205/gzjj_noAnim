package com.lljgame.llj.process;

import android.os.Handler;
import android.util.Log;

import com.lljgame.llj.MainActivity;
import com.lljgame.llj.MainBoard;
import com.lljgame.llj.log.MyLog;

/**
 * Created by Davia.Li on 2017-04-01.
 */

//就是一开始判断与主板串口的初始化线程
public class MainBoardConfigCheckProcess implements Runnable {
    private Thread mThread;
    private MainBoard mMainBoard;
    public boolean mStop;
    private MainBoardConfigCheckListener mListener;
    private MainActivity.UIHandler uiHandler;
    public MainBoardConfigCheckProcess(MainBoard mainBoard, MainBoardConfigCheckListener l,MainActivity.UIHandler uiHandler) {
        this.uiHandler=uiHandler;
        mMainBoard = mainBoard;
        mThread = new Thread(this);
        mListener = l;
    }

    public void start() {
        mStop = false;
        mThread.start();
    }

    public void stop() {
        mStop = true;
        mThread.interrupt();
    }

    @Override
    public void run() {
        try {
            uiHandler.sendEmptyMessage(MainActivity.UIHandler.CONTROLL_INIT);
            while (!mStop) {
                if(mMainBoard.getDeviceConfig().isInit()) {
                    mListener.onMainBoardInit();
                    MyLog.i("主板初始化成功");
                    break;
                }
                mMainBoard.sendWData();
//                Thread.sleep(2 * 1000);
//                mMainBoard.sendPData();
                Thread.sleep(6* 1000);
                MyLog.i("主板初始化中...");
            }
            mStop=true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public interface MainBoardConfigCheckListener {
        void onMainBoardInit();
    }
}
