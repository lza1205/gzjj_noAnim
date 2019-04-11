package com.lljgame.llj;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.widget.Toast;

import com.lljgame.llj.bean.DeviceConfig;
import com.lljgame.llj.bean.DeviceInfo;
import com.lljgame.llj.bean.LaunchInfo;
import com.lljgame.llj.device.DeviceDataCtrl;
import com.lljgame.llj.log.ConfigureLog4J;
import com.lljgame.llj.log.MyLog;
import com.lljgame.llj.process.DeviceCheckErrorProcess;
import com.lljgame.llj.process.ServerCallBackProcess;
import com.lljgame.llj.remote.RemoteApi;
import com.lljgame.llj.utils.ShareUtil;
import com.lljgame.llj.utils.Utils;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Davia.Li on 2017-07-31.
 */
//建立主板控制命令的类
public class MainBoard implements DeviceDataCtrl.DeviceDataCtrlListener {
    private DeviceDataCtrl mDeviceDataCtrl;
    private boolean mGameStarting;
    private int mDeviceErrCode;
    private LaunchInfo mLastLaunchInfo;
    private DeviceConfig mDeviceConfig;
    private DeviceCheckErrorProcess mErrorCheckProcess;

    private ServerCallBackProcess mServerCallBackProcess;


    private int mLastScanTime = 0;
    private boolean mIsRequesting;
    private Context context;
    private MainActivity.UIHandler uiHandler;
    private int winCount, failCount;
    //private boolean isDoubleOpen;
    private LaunchInfo info;
    private Logger log;
    private boolean isCanScan;

    private byte[] checklink_ok = {(byte) 0xAA, 0x03, 0x01, (byte) 0xFE, (byte) 0xFC, (byte) 0xDD};//检测连接的固定码


    public MainBoard(Context context, MainActivity.UIHandler uiHandler, Logger log) {
        this.context = context;
        this.uiHandler = uiHandler;
        mDeviceDataCtrl = new DeviceDataCtrl(this, "ttyS5");
        mDeviceConfig = new DeviceConfig();
        mServerCallBackProcess = new ServerCallBackProcess();
        mServerCallBackProcess.start();

        this.log = log;
        winCount = ShareUtil.getSharedPreferences().getInt(ShareUtil.WIN, 0);
        failCount = ShareUtil.getSharedPreferences().getInt(ShareUtil.FAIL, 0);
    }

    public ServerCallBackProcess getmServerCallBackProcess() {
        return mServerCallBackProcess;
    }

    public boolean isGameStarting() {
        return mGameStarting;
    }

    public boolean isDeviceError() {
        return mDeviceErrCode != 0;
    }

    public DeviceConfig getDeviceConfig() {
        return mDeviceConfig;
    }

    public void onQrcodeScan(String qrcode) {
        MyLog.i("查询到二维码");
        if (!mDeviceConfig.isInit()) {
            //正式时候记得开启
            return;
        }

        if (isGameStarting() || isDeviceError()) {
            return;
        }

        int now = Utils.getCurrentTime();//间隔
        if ((now - mLastScanTime < 2) || mIsRequesting) {
            return;
        }

        mIsRequesting = true;
        if (isCanScan) {
            info = RemoteApi.qrcodeScan(qrcode);
            isCanScan = false;
            if (info == null) isCanScan = true;
        }
        //放在外面是怕串口没收到命令就之后一直启动不到游戏的情况,但是会向主板再发一次,2s间隔应该差不多了
        if (info != null) {
            //判断是否二次启动
            launchDevice(info);
            MyLog.i("启动了游戏");
        }
        mLastScanTime = Utils.getCurrentTime();//间隔
        mIsRequesting = false;

    }

    //回掉处理。向服务器发信息做事情
    @Override
    public void onDataReceive(byte[] data, int length) {
        MyLog.i("MainBoard 接收到了数据： " + Utils.byteArrayToHexString(data, length) + " len: " + length);
        if (data[0] == (byte) 0xAA) {
            int len = data[1];
            if (len + 2 <= length && data[len + 2] == (byte) 0xDD) {
                if (Utils.getcheck(data) == (byte) data[length - 2]) {
                    switch (data[3]) {
                        case (byte) 0xFE:
                            //主板初始化检测
                            DeviceInfo.CurrentDevice = data[4];
                            DeviceInfo.CurrentVersion = data[5] + "." + data[6] + "." + data[7];
                            MyLog.i("主板初始化成功： " + DeviceInfo.CurrentDevice + "  版本号: " + DeviceInfo.CurrentVersion);

                            handleDeviceConfig();
                            break;
                        case (byte) 0xFD:
                            //游戏开始接受
                            if (data[4] == 0x00) {
                                handleGameStart();
                            } else {
                                //游戏开始失败
                                handleErr();
                            }
                            break;
                        case (byte) 0xFC:
                            //游戏结果返回
                           /* if (data[4] == 0x00) {
                                MyLog.i("游戏结束");
                                handleGameOver();
                            } else if (data[4] == 0x01) {
                                MyLog.i("游戏胜利");
                                handleWin();
                                handleGameOver();//add
                            }*/
                            if((DeviceInfo.CurrentDevice)==DeviceInfo.Device_A){
                                if (data[4] == 0x00) {
                                    handleGameOver();
                                } else if (data[4] == 0x01) {
                                    handleWin();
                                    handleGameOver();//add
                                }
                            }else{
                                if (data[4] == 0x00) {
                                    if(DeviceInfo.CurrentDevice==DeviceInfo.Device_ND){
                                        //扭蛋机
                                        handleWin(data[5]);
                                    }else{
                                        handleWin();
                                    }
                                    handleGameOver();//add

                                } else if (data[4] == 0x01) {
                                    handleGameOver();
                                }
                            }

                            break;
                        case (byte) 0xFB:
                            //收到文本接受成功返回
                            break;
                        case (byte) 0xED:
                            //游戏准备就绪，用户可以扫码
                            isCanScan = true;
                            break;
                        case (byte) 0xEF:
                            //得到所有的报错数据
                         /*  int len2 = len - 3;
                            for(int i=0;i<len2;i++){
                                MyLog.i("数据: "+data[4+i]);
                            }*/
                            handleErr();
                            MyLog.i("检测到了故障");
                            break;
                        default:
                            MyLog.i("onDataReceive: 异常");
                            break;
                    }
                } else {
                    MyLog.i("这一包的校验码信息不对");
                }

            }
        }

    }

    private void increatment(boolean isWin) {
        if (isWin) {
            winCount++;
            ShareUtil.write(ShareUtil.WIN, winCount);
        } else {
            failCount++;
            ShareUtil.write(ShareUtil.FAIL, failCount);
        }
        RemoteApi.debugTrace("totalCoint_Win: " + winCount + "  totalCoint_Fail: " + failCount + "\r\n");
        log.debug("totalCoint_Win: " + winCount + "  totalCoint_Fail: " + failCount + "\r\n");
        ConfigureLog4J.ChangeToCount();
        log.info("totalCoint_Win: " + winCount + "  totalCoint_Fail: " + failCount + "\r\n");
        ConfigureLog4J.ChangeToTital();
    }


    public void launchDevice(LaunchInfo info) {
        mLastLaunchInfo = (LaunchInfo) info.clone();
        //   sendLauchDevice(info, 1.0f);
        sendNewLauchDevice(info, 1.0f);
    }

    public void sendLauchDevice(LaunchInfo info, float a) {
        Random random = new Random();
        int sum = random.nextInt(5) + 1;
        if (info.max != info.min) {
            info.max = (int) (info.max * a);
            info.min = (int) (info.min * a);
        }
        sendToDevice(new byte[]{(byte) info.max, (byte) info.min, (byte) info.time, (byte) sum, (byte) 0xDE});
    }

    public void sendNewLauchDevice(LaunchInfo info, float a) {
        if (info.max != info.min) {
            info.max = (int) (info.max * a);
            info.min = (int) (info.min * a);
        }
        //发送游戏开始的命令
        switch (DeviceInfo.CurrentDevice) {
            case DeviceInfo.Device_A:
                sendNewToDevice(new byte[]{0x08, 0x01, (byte) 0xFD, (byte) info.max, (byte) info.min, 0x32, (byte) info.time, 0x32}, true);

                break;
            case DeviceInfo.Device_C:
                byte b1 = (byte) (info.max == info.min ? 0x00 : 0x01);
                sendNewToDevice(new byte[]{0x07, 0x01, (byte) 0xFD, (byte) info.max, (byte) info.min, (byte) info.time, b1}, true);
                break;
            case DeviceInfo.Device_ND:
                sendNewToDevice(new byte[]{0x04,0x01, (byte) 0xFD,0x01},true);
                break;
            case DeviceInfo.Device_LZJ:
                byte b2 = (byte) (info.max == info.min ? 0x00 : 0x01);
                sendNewToDevice(new byte[]{0x07, 0x01, (byte) 0xFD, (byte) info.max, (byte) info.min, 0x01, b2}, true);
                break;
        }
        // sendNewToDevice(new byte[]{0x04, 0x01, (byte) 0xFB,0x01}, true);
    }

    public void sendNewToDevice(byte[] data, boolean isState) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(0xAA);

        for (byte b : data) {
            bos.write(b);
        }
        bos.write(0x00);//校验位
        bos.write(0xDD);//结束位
        byte[] sendData = bos.toByteArray();
        sendData[sendData.length - 2] = Utils.getcheck(sendData);
        mDeviceDataCtrl.send(sendData);
    }


    public void handleGameStart() {
        mGameStarting = true;
        isCanScan = true;
        if (mLastLaunchInfo == null) {
            return;
        }
        final int tradeNo = mLastLaunchInfo.tradeNo;
        final int userID = mLastLaunchInfo.userId;
        final boolean isTicket = mLastLaunchInfo.isTicket;
        mServerCallBackProcess.addServerCallBack(new Runnable() {
            @Override
            public void run() {
                boolean success = RemoteApi.gameStart(tradeNo, userID, isTicket);
                if (!success) {
                    RemoteApi.gameStart(tradeNo, userID, isTicket);
                }
            }
        });
    }

    public void handleGameOver() {
        mGameStarting = false;
        if (mLastLaunchInfo == null) {
            return;
        }
        final int tradeNo = mLastLaunchInfo.tradeNo;
        final int userID = mLastLaunchInfo.userId;
        final boolean isTicket = mLastLaunchInfo.isTicket;
        mServerCallBackProcess.addServerCallBack(new Runnable() {
            @Override
            public void run() {
                boolean success = RemoteApi.gameEnd(tradeNo, userID, isTicket);
                if (!success) {
                    RemoteApi.gameEnd(tradeNo, userID, isTicket);
                }
            }
        });
    }

    public void handleWin() {
        if (mLastLaunchInfo == null) {
            return;
        }
        final int tradeNo = mLastLaunchInfo.tradeNo;
        final int userID = mLastLaunchInfo.userId;
        final boolean isTicket = mLastLaunchInfo.isTicket;
        mServerCallBackProcess.addServerCallBack(new Runnable() {
            @Override
            public void run() {
                boolean success = RemoteApi.productWin(tradeNo, userID, isTicket);
                if (!success) {
                    RemoteApi.productWin(tradeNo, userID, isTicket);
                }
            }
        });
    }

    public void handleWin(final int winCount) {
        if (mLastLaunchInfo == null) {
            return;
        }
        final int tradeNo = mLastLaunchInfo.tradeNo;
        final int userID = mLastLaunchInfo.userId;
        final boolean isTicket = mLastLaunchInfo.isTicket;
        mServerCallBackProcess.addServerCallBack(new Runnable() {
            @Override
            public void run() {
                boolean success = RemoteApi.productWin(tradeNo, userID, isTicket,winCount);
                if (!success) {
                    RemoteApi.productWin(tradeNo, userID, isTicket,winCount);
                }
            }
        });
    }

    public void handleErr() {
        mServerCallBackProcess.addServerCallBack(new Runnable() {
            @Override
            public void run() {
                boolean success = RemoteApi.deviceError(-1);
                if (!success) {
                    RemoteApi.deviceError(-1);
                }
            }
        });
    }

    private void handleDeviceData(byte[] data) {
        ByteBuffer bf = ByteBuffer.wrap(data);

        final int errCode = bf.get(30) & 0xFF;
        log.debug("errCode==================errCode: " + errCode + "\r\n");
        RemoteApi.debugTrace("errCode==================errCode: " + errCode + "\r\n");
        if (errCode != mDeviceErrCode) {
            mServerCallBackProcess.addServerCallBack(new Runnable() {
                @Override
                public void run() {
                    boolean success = RemoteApi.deviceError(errCode);
                    if (!success) {
                        RemoteApi.deviceError(errCode);
                    }
                }
            });

        }

        if (errCode != 0 && (mErrorCheckProcess == null || !mErrorCheckProcess.isRunning())) {
            mErrorCheckProcess = new DeviceCheckErrorProcess(this);
            mErrorCheckProcess.start();
        }

        if (errCode == 0 && mErrorCheckProcess != null && mErrorCheckProcess.isRunning()) {
            mErrorCheckProcess.stop();
        }

        mDeviceErrCode = errCode;

        if (mDeviceErrCode != 0) {
            mGameStarting = false;
        }
    }


    private void handleDeviceConfig() {
        MyLog.i("handleDeviceConfig：ID " + Config.DEVICEID + "   PSD： " + Config.DEVICEPSD);
        mDeviceConfig.setConfig(Config.DEVICEID, Config.DEVICEPSD);
    }

    //更新抓力的值
    public void setDevicePreference(int gstrength, int lstrength, int time) {
        mDeviceConfig.gstrength = gstrength;
        mDeviceConfig.lstrength = lstrength;
        mDeviceConfig.gameTime = time;

        sendNewToDevice(new byte[]{0x01, (byte) 0xFC, (byte) mDeviceConfig.gstrength,
                (byte) mDeviceConfig.lstrength,
                (byte) mDeviceConfig.gameTime,
        }, false);
    }


    //游戏的连接状态检测
    public void sendWData() {
        MyLog.i("check: " + Utils.byte2hex(new byte[]{Utils.getcheck(checklink_ok)}));
        mDeviceDataCtrl.send(checklink_ok);
    }

    //试下这个
    public void sendPData() {
        sendToDevice(new byte[]{'P', (byte) 0xDC});
    }

    //游戏准备就绪
    public void sendRData() {
        sendNewToDevice(new byte[]{0x03, 0x01, (byte) 0xED}, false);
    }

    public void sendToDevice(byte[] data) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(0xC0);
        byte sum = 0;
        for (byte b : data) {
            sum += (b & 0xFF);
            if ((b & 0xFF) == 0xC0) {
                bos.write(0xDB);
                bos.write(0xDC);
            } else if ((b & 0xFF) == 0xDB) {
                bos.write(0xDB);
                bos.write(0xDD);
            } else {
                bos.write(b);
            }
        }
        if ((sum & 0xFF) == 0xC0) {
            bos.write(0xDB);
            bos.write(0xDC);
        } else if ((sum & 0xFF) == 0xDB) {
            bos.write(0xDB);
            bos.write(0xDD);
        } else {
            bos.write(sum);
        }
        bos.write(0xC0);
        byte[] sendData = bos.toByteArray();
        mDeviceDataCtrl.send(sendData);
    }


    private byte[] as = {(byte) 0xAA, 0x04, 0x01, (byte) 0xFB, 0x30, (byte) 0xCE, (byte) 0xDD};

    public void sendLog(String info) {
        byte[] bs = info.getBytes();
        byte[] newbs = new byte[bs.length + 3];
        newbs[0] = (byte) (bs.length + 3);
        newbs[1] = 0x01;
        newbs[2] = (byte) 0xFB;
        newbs = Utils.addBytes(bs, newbs, 0, 3);
        //MyLog.i("显示newbs: " + Arrays.toString(newbs));
        sendNewToDevice(newbs, false);
        //mDeviceDataCtrl.send(as);
    }
}
