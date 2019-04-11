package com.lljgame.llj.process;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Davia.Li on 2017-02-13.
 */
//定时发送者，用来即使打开应用的
public class HeartBeatSenderProcess implements Runnable {
    private Thread mThread;
    private boolean mStarting;
    private DatagramSocket mSocket;

    public HeartBeatSenderProcess() {
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
            mSocket = new DatagramSocket();
            byte[] data = Common.HEART_BEAT_CMD.getBytes("UTF-8");
            InetAddress server = InetAddress.getByName("127.0.0.1");
            DatagramPacket packet = new DatagramPacket(data, data.length, server, Common.HEART_BEAT_SEND_PORT);
            while (mStarting) {
                mSocket.send(packet);
                try {
                    Thread.sleep(Common.HEART_BEAT_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
