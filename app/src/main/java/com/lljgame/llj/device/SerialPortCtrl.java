package com.lljgame.llj.device;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

/**
 * Created by Davia.Li on 2017-07-31.
 */

public class SerialPortCtrl {
    private SerialPortReceive mPortReceive;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public SerialPortCtrl() {
        try {
            //mSerialPort = new SerialPort(new File("/dev/ttyS3"), 9600, 0);
            //SerialPort serialPort = new SerialPort(new File("/dev/ttyS2"), 115200, 0);
            //sx
            SerialPort serialPort = new SerialPort(new File("/dev/ttyS4"), 115200, 0);

            mOutputStream = serialPort.getOutputStream();
            mInputStream = serialPort.getInputStream();

            new ReadThread().start();
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public SerialPortCtrl(String port, SerialPortReceive r) {
        try {
            SerialPort serialPort = new SerialPort(new File("/dev/" + port), 19200, 0);

            mOutputStream = serialPort.getOutputStream();
            mInputStream = serialPort.getInputStream();
            mPortReceive = r;
            new ReadThread().start();
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public SerialPortCtrl(String port, int baudrate, SerialPortReceive r) {
        try {
            SerialPort serialPort = new SerialPort(new File("/dev/" + port), baudrate, 0);

            mOutputStream = serialPort.getOutputStream();
            mInputStream = serialPort.getInputStream();
            mPortReceive = r;
            new ReadThread().start();
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(byte[] buff) {
        sendData(buff, 0, buff.length);
    }

    public void sendData(byte[] buff, int offset, int count) {
        if(mOutputStream != null) {
            try {
                mOutputStream.write(buff, offset, count);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            if(mPortReceive == null) {
                throw new RuntimeException("null port receive");
            }
            int size;
            while(!isInterrupted()) {
                try {
                    byte[] buffer = new byte[256];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        mPortReceive.onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface SerialPortReceive {
        void onDataReceived(byte[] buffer, int size);
    }
}
