package com.lljgame.llj;


import android.text.TextUtils;

import com.lljgame.llj.device.SerialPortCtrl;
import com.lljgame.llj.log.MyLog;

import org.json.JSONException;
import org.json.JSONObject;

//接受二维码信息处理的类
public class Scanner implements SerialPortCtrl.SerialPortReceive {
    private static final int MAX_DATA_LENGTH = 512;
    private ScannerListener mListener;
    private byte[] mReceiveData;
    private int mReceiveIndex;

    public Scanner(ScannerListener l, String port, int baudrate) {
        mListener = l;
        new SerialPortCtrl(port, baudrate, this);
        mReceiveData = new byte[MAX_DATA_LENGTH];
        mReceiveIndex = 0;
    }

    private StringBuffer stringBuffer=new StringBuffer();

    @Override
    public void onDataReceived(byte[] buffer, int size) {
//        System.out.println("onDataReceived:" + Utils.bytes2HexString(buffer, size));
        String info=new String(buffer,0,size);

        if(stringBuffer.length()>0){
            if(stringBuffer.indexOf("{")!=0){
                stringBuffer.delete(0,stringBuffer.length());
            }else {
                if(stringBuffer.indexOf("}")!=-1){
                    String json= stringBuffer.substring(0,stringBuffer.indexOf("}")+1);
                    try {
                        MyLog.i("接收到配置数据了: "+json);
                        JSONObject jsonObject = new JSONObject(json);
                        String wifiName = jsonObject.optString(Config.WifiName);
                        String wifPwd = jsonObject.optString(Config.WifiPwd);
                        String IP = jsonObject.optString(Config.IP);
                        String Port = jsonObject.optString(Config.Port);
                        String Rule = jsonObject.optString(Config.Rule);

                        //设备6位id，设备8位密码，是否强制更改
                        String deviceId = jsonObject.optString(Config.DEVICEID_Key);
                        String devicePsd = jsonObject.optString(Config.DEVICEPSD_Key);
                        boolean isYes=jsonObject.optBoolean(Config.ISYES);

                        if (!TextUtils.isEmpty(wifiName) && !TextUtils.isEmpty(wifPwd)) {
                            mListener.onWIFIScan(wifiName, wifPwd);
                        }

                        if (!TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(devicePsd)) {
                            mListener.onDeviceSet(deviceId, devicePsd, isYes);
                        }

                        if (!TextUtils.isEmpty(IP) && !TextUtils.isEmpty(Port) && !TextUtils.isEmpty(Rule)) {
                            mListener.onIPChange(IP, Port, Rule);
                        }
                        stringBuffer.delete(0,stringBuffer.length());
                    } catch (JSONException e) {
                        MyLog.i("出异常了: "+stringBuffer.toString());
                        stringBuffer.delete(0,stringBuffer.length());
                    }
                }else{
                    stringBuffer.append(info);
                }
            }
        }else{
            if(info.indexOf("{")!=-1){
                stringBuffer.append(info,info.indexOf("{"),size);
            }
        }





        for (int i = 0; i < size; i++) {
            mReceiveData[mReceiveIndex++] = buffer[i];
            if (mReceiveData[mReceiveIndex - 1] == 13 || mReceiveData[mReceiveIndex - 1] == 10) {
                String qr = new String(mReceiveData, 0, mReceiveIndex).trim();
                mReceiveIndex = 0;
                mListener.onQRScan(qr);
            }
            if (mReceiveIndex == MAX_DATA_LENGTH) {
                mReceiveIndex = 0;
            }
        }
    }

    interface ScannerListener {
        void onQRScan(String QRCode);

        void onWIFIScan(String name, String password);

        void onIPChange(String ip, String port, String rule);

        void onDeviceSet(String deviceId, String devicePsd, boolean isYes);
    }
}
