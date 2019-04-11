package com.lljgame.llj.process;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by Davia.Li on 2017-02-13.
 */
//心跳机制，用来更新时间，用来定时判断打开应用的d
public class HeartBeatListenerProcess implements Runnable {
    private Thread mThread;
    private boolean mStarting;
    private DatagramSocket mSocket;

    public HeartBeatListenerProcess() {
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
        try {
            byte[] buffer = new byte[128];
            mSocket = new DatagramSocket(Common.HEART_BEAT_LISTEN_PORT);
            DatagramPacket pack = new DatagramPacket(buffer, buffer.length);
            while (mStarting) {
                mSocket.receive(pack);
                String heartBeat = new String(pack.getData(), pack.getOffset(), pack.getLength(), "UTF-8");
                if(heartBeat.equals(Common.HEART_BEAT_CMD)) {
                    Common.setHeartBeat();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
