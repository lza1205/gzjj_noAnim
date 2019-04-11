package com.lljgame.llj;

import com.lljgame.llj.device.DeviceDataCtrl;
import com.lljgame.llj.utils.Utils;

import java.io.ByteArrayOutputStream;

/**
 * Created by Davia.Li on 2017-07-31.
 */

public class DecodeBoard implements DeviceDataCtrl.DeviceDataCtrlListener {
    private DeviceDataCtrl mDeviceDataCtrl;
    private int mLastScanTime;
    private DecodeBoardListener mListener;

    public DecodeBoard(DecodeBoardListener l) {
        mDeviceDataCtrl = new DeviceDataCtrl(this, "ttyS4");
        mListener = l;
    }


    @Override
    public void onDataReceive(byte[] data, int length) {
        if(length < 5) {
            return;
        }
        int lim = length - 1;
        if(lim <= 0) {
            return;
        }
        byte sum = 0;
        for(int i = 0; i < lim; i++) {
            sum += data[i] & 0xFF;
        }

        if(sum != data[lim]) {
            return;
        }

        if(data[lim - 1] == (byte)0xCC) {
            handleScanData(data, length);
        }
    }

    public void send(byte[] data) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(0xC0);
        byte sum = 0;
        for(byte b : data) {
            sum += (b & 0xFF);
            if((b & 0xFF) == 0xC0) {
                bos.write(0xDB);
                bos.write(0xDC);
            } else if((b & 0xFF) == 0xDB) {
                bos.write(0xDB);
                bos.write(0xDD);
            } else {
                bos.write(b);
            }
        }
        if((sum & 0xFF) == 0xC0) {
            bos.write(0xDB);
            bos.write(0xDC);
        } else if((sum & 0xFF) == 0xDB) {
            bos.write(0xDB);
            bos.write(0xDD);
        } else {
            bos.write(sum);
        }
        bos.write(0xC0);
        byte[] sendData = bos.toByteArray();
        mDeviceDataCtrl.send(sendData);
    }

    private void handleScanData(byte[] data, int length) {
        synchronized (this) {
            int now = Utils.getCurrentTime();
            if(now - mLastScanTime < 5) {
                return;
            }
            mLastScanTime = now;
        }

        String qrcode = new String(data, 0, length-2).trim();
        if(mListener != null) {
            mListener.onQrcodeScan(qrcode);
        }
    }

    interface DecodeBoardListener {
        void onQrcodeScan(String qrcodeValue);
    }
}
