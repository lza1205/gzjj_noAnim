package com.lljgame.llj.device;

import android.util.Log;

import com.lljgame.llj.log.MyLog;
import com.lljgame.llj.utils.Utils;

/**
 * Created by Davia.Li on 2017-07-31.
 */

public class DeviceDataCtrl implements SerialPortCtrl.SerialPortReceive {
    public static final int MAX_QR_LENGTH = 320;
    private DeviceDataCtrlListener mListener;
    private byte[] mReceiveData;
    private int mReceiveIndex;
    private static final byte SLIP_WRAP = (byte) 0xC0;
    private SerialPortCtrl mSerialPortCtrl;


    public DeviceDataCtrl(DeviceDataCtrlListener l, String port) {
        mSerialPortCtrl = new SerialPortCtrl(port, this);
        mReceiveData = new byte[MAX_QR_LENGTH];
        mReceiveIndex = 0;
        mListener = l;
    }

    public void send(byte[] data) {
        MyLog.i("发送数据： "+ Utils.byteArrayToHexString(data,data.length));
        mSerialPortCtrl.sendData(data);
    }

    //主板串口收到信息的回调，控制那块的
    private byte[] bs;
    @Override
    public void onDataReceived(byte[] buffer, int size) {
//        System.out.println("receive from device:" + Utils.byteArrayToHexString(buffer, size));
        MyLog.i("接收到了数据： "+ Utils.byteArrayToHexString(buffer,size));

        if(mListener != null) {
            bs=new byte[size];
            System.arraycopy(buffer,0,bs,0,size);
            mListener.onDataReceive(bs, size);
        }
   /*     for(int i = 0; i < size; i++) {
            mReceiveData[mReceiveIndex++] = buffer[i];
            if(mReceiveData[0] != SLIP_WRAP) {
                mReceiveIndex = 0;
                continue;
            }
            if(mReceiveData[mReceiveIndex - 1] == SLIP_WRAP && mReceiveIndex > 2) {
                byte[] data = new byte[mReceiveIndex - 2];
                int dataIndex = 0;
                for(int j = 1; j < mReceiveIndex; j++) {
                    if(mReceiveData[j] == (byte)0xDB || mReceiveData[j] == (byte)0xC0) {
                        continue;
                    }
                    if(mReceiveData[j] == (byte)0xDC && mReceiveData[j-1] == (byte)0xDB ) {
                        data[dataIndex++] = (byte) 0xC0;
                    } else if(mReceiveData[j] == (byte)0xDD && mReceiveData[j-1] == (byte)0xDB) {
                        data[dataIndex++] = (byte) 0xDB;
                    } else {
                        data[dataIndex++] = mReceiveData[j];
                    }
                }
                mReceiveIndex = 0;
                if(mListener != null) {
                    mListener.onDataReceive(data, dataIndex);
                }
            }
            if(mReceiveIndex == MAX_QR_LENGTH) {
                mReceiveIndex = 0;
            }
        }*/
    }

    public interface DeviceDataCtrlListener {
        void onDataReceive(byte[] data, int length);
    }
}
