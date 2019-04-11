package com.lljgame.llj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lljgame.llj.bean.DeviceConfig;
import com.lljgame.llj.bean.LaunchInfo;
import com.lljgame.llj.log.ConfigureLog4J;
import com.lljgame.llj.log.MyLog;
import com.lljgame.llj.process.DownloadAndInstallApkProcess;
import com.lljgame.llj.process.HeartBeatListenerProcess;
import com.lljgame.llj.process.HeartBeatSenderProcess;
import com.lljgame.llj.process.MainBoardConfigCheckProcess;
import com.lljgame.llj.process.ProcesserCheckerProcess;
import com.lljgame.llj.remote.DevicesServer;
import com.lljgame.llj.remote.RemoteApi;
import com.lljgame.llj.utils.NetworkrunUtils;
import com.lljgame.llj.utils.ScanUtil;
import com.lljgame.llj.utils.VideoAcitivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements DecodeBoard.DecodeBoardListener,
        MainBoardConfigCheckProcess.MainBoardConfigCheckListener, DevicesServer.DevicesServerListener,
        Scanner.ScannerListener {
    private MainBoard mMainBoard;
    private DevicesServer mDeviceServer;
    private int mRetryConnectTimeWait = Config.MIN_RETRY_CONNECT_TIME;

    private EditText mDeviceId;
    private EditText mDeviceSecret;
    private Button mLoginBtn;
    private TextView mLoginStatus;

    public static final int REQUEST_CODE = 111;

    private TextView mMaxPower;
    private TextView mMinPower;
    private TextView mProbability;
    private TextView mPriceThreshold;
    private TextView mWaitingTime;
    // private TextView mDeviceStatus;//设备状态
    // private TextView mDeviceWin;//结果
    //private Button mQrScanBtn;//扫码按钮

    private View mLoginArea;
    private View mCtrlArea;

    //-----------------------
    MainBoardConfigCheckProcess mainBoardConfigCheckProcess;
    private View infoGet;

    UIHandler uiHandler;
    private TextView mUserId;
    private TextView mUserTradeNo;
    private TextView mIsTicket;
    private TextView result;
    private TextView mXSpeed;
    private TextView mZspeed;
    private static final String TAG = "tag";
    private TextView textsend;
    private TextView text_receiver;
    private Button mTestPic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化log4j
        ConfigureLog4J configureLog4J = new ConfigureLog4J();
        configureLog4J.configure(this);

        new MyAsynTask().execute();//执行读取文件内容和连接串口操作
        InitView();//初始化界面的view
        initUdpCheckAPK();//用udp检测应用是否退出

        //==============================
        Intent intent = new Intent(MainActivity.this, VideoAcitivity.class);

        startActivity(intent);
        //==============================
    }

    private Logger log;

    //初始化控制的串口 Serialport
    private void initAllSerialPort() {
        log = Logger.getLogger(this.getClass());
        mMainBoard = new MainBoard(this, uiHandler, log);//连接监听控制的串口

        new Scanner(this, "ttyS4", 115200);//监听扫码器的串口

        initMainBoardConfigCheckProcess();//一开始就一直判断是否和主板初始化成功.如果成功才会有以后的操作
    }

    private void initUdpCheckAPK() {
        //下面那三个是一起发挥作用的,定时监听该应用是否还活着
        new HeartBeatListenerProcess().start();
        new HeartBeatSenderProcess().start();
        new ProcesserCheckerProcess(this).start();

    }

    private void initMainBoardConfigCheckProcess() {
        if (mainBoardConfigCheckProcess == null || mainBoardConfigCheckProcess.mStop) {
            mainBoardConfigCheckProcess = new MainBoardConfigCheckProcess(mMainBoard, this, uiHandler);
            mainBoardConfigCheckProcess.start();
        }
    }

    //扫到二位码Scanner触发的回掉
    @Override
    public void onQrcodeScan(String qrcodeValue) {
        mMainBoard.onQrcodeScan(qrcodeValue);
    }

    //和主板初始化成功之后回掉
    @Override
    public void onMainBoardInit() {
        uiHandler.sendEmptyMessage(UIHandler.CONTROLL_SUCESS);
        DeviceConfig cfg = mMainBoard.getDeviceConfig();
        //把信息存进去之后的发码有用
        RemoteApi.init(cfg.deviceId, cfg.deviceSecret, Config.AUTH_PASSWORD);
        log.debug("deviceId: " + cfg.deviceId + "  deviceSecret: " + cfg.deviceSecret + "\r\n");
        String appSecret = null;
        mMainBoard.sendLog("linking server... id: "+ cfg.deviceId+"  secret: "+cfg.deviceSecret);
        while (appSecret == null) {
            appSecret = RemoteApi.getAccountSecret();
            log.debug("appSecret: " + appSecret + "  address: " + RemoteApi.DEVICE_SERVER_BASE_URL);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //连接完服务器之后向主板发送安卓板可以开始工作了
        mMainBoard.sendRData();
        //这个就是服务端检测设备是否在线的tcp连接
        connectToServer();
    }

    @Override
    public void onMessage(byte[] msg) {
        System.out.println("onMessage" + new String(msg));
        try {
            JSONObject json = new JSONObject(new String(msg));
            String t = json.getString("type");
            if (t.equals("launch")) {
//                LaunchInfo info = new LaunchInfo(json.getInt("userId"),
//                        json.getInt("probability"), json.getInt("tradeNo"));
//                mMainBoard.launchDevice(info);
            } else if (t.equals("preference")) {
                int gstrength = json.getInt("max");
                int lstrength = json.getInt("min");
                int time = json.getInt("time");
                mMainBoard.setDevicePreference(gstrength, lstrength , time);
                Message message = Message.obtain();
                message.obj = mMainBoard.getDeviceConfig();
                message.what = UIHandler.DEV_DETAILS;
                uiHandler.sendMessage(message);


            } else if (t.equals("upgrade")) {
                String url = json.getString("url");
                new DownloadAndInstallApkProcess(this, url).start();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //未登陆的界面
    public void showPage1() {
        mLoginArea.setVisibility(View.VISIBLE);
        mCtrlArea.setVisibility(View.GONE);
    }

    //登录界面
    public void showPage2() {
        mLoginArea.setVisibility(View.GONE);
        mCtrlArea.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDisconnect() {
        System.out.println("onDisconnect");

        uiHandler.sendEmptyMessage(UIHandler.SERVER_FAIL);
        try {
            Thread.sleep(mRetryConnectTimeWait);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mDeviceServer.stop();
        mRetryConnectTimeWait *= 2;
        if (mRetryConnectTimeWait > Config.MAX_RETRY_CONNECT_TIME) {
            mRetryConnectTimeWait = Config.MAX_RETRY_CONNECT_TIME;
        }
        connectToServer();
    }

    //tcp连接成功之后回掉，connectToServer
    @Override
    public void onConnect() {
        System.out.println("onConnect");
        uiHandler.sendEmptyMessage(UIHandler.SERVER_SUCESS);
        mRetryConnectTimeWait = Config.MIN_RETRY_CONNECT_TIME;
    }


    //回退键，当显示详情信息回退到登录界面，否则执行父类方法
    @Override
    public void onBackPressed() {
        if (mCtrlArea.getVisibility() == View.GONE) {
            super.onBackPressed();
        } else {
            showPage1();
        }
    }

    private void connectToServer() {
        DeviceConfig cfg = mMainBoard.getDeviceConfig();
        mDeviceServer = new DevicesServer(this, cfg.deviceId, cfg.deviceSecret,
                Config.SERVER_URL, Config.SERVER_TCP_PORT, Config.AUTH_PASSWORD);

        log.debug("SERVER_URL: " + Config.SERVER_URL + "  TCP_PORT: " + Config.SERVER_TCP_PORT + "\r\n");

        mDeviceServer.setDeviceServerListener(this);
        mDeviceServer.start();
    }

    private static final int RIGHT = 0;
    private static final int NOCONTROLL = 1;
    private static final int INFOFAIL = 2;

    private void InitView() {
        uiHandler = new UIHandler();

        mLoginArea = findViewById(R.id.login_area);
        mCtrlArea = findViewById(R.id.ctrl_area);


        mLoginStatus = (TextView) findViewById(R.id.device_login_status);
        mDeviceId = (EditText) findViewById(R.id.device_id);
        mDeviceSecret = (EditText) findViewById(R.id.device_secret);
        mLoginBtn = (Button) findViewById(R.id.device_login);
        //点击登陆的情况下，不能进行对设备的重新配置，只能透过网络请求获取设备的抓力信息
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //1.获取editText信息
                //2.判断信息是不是当前的设备信息
                String deviceId = mDeviceId.getText().toString().replaceAll(" ", "");
                String deviceSecret = mDeviceSecret.getText().toString().replaceAll(" ", "");
                switch (isSure(deviceId, deviceSecret)) {
                    case RIGHT:
                        //成功
                        //1.打开详情界面
                        showPage2();
                        //开始连接获取抓力值
                        mMainBoard.getmServerCallBackProcess().addServerCallBack(new Runnable() {
                            @Override
                            public void run() {
                                mMainBoard.sendPData();
                            }
                        });

                        break;
                    case NOCONTROLL:
                        //与主板连接获取设备信息失败，请检查主板是否与程序进行初始话配置成功
                        Toast.makeText(MainActivity.this, "与主板连接获取设备信息失败，请检查主板是否与程序进行初始话配置成功", Toast.LENGTH_SHORT).show();
                        break;
                    case INFOFAIL:
                        //设备信息填写错误
                        Toast.makeText(MainActivity.this, "设备信息填写错误", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });
        infoGet = ((View) findViewById(R.id.device_get));
        infoGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*    SharedPreferences pf = getSharedPreferences(Config.SP_DEVICE, Context.MODE_PRIVATE);
                mDeviceId.setText(pf.getString(DeviceConfig.DEVICEID, "null"));
                mDeviceSecret.setText(pf.getString(DeviceConfig.DEVICESECRET, "null"));*/
                mDeviceId.setText(Config.DEVICEID);
                mDeviceSecret.setText(Config.DEVICEPSD);
            }
        });


        mMaxPower = (TextView) findViewById(R.id.device_max_power);
        mMinPower = (TextView) findViewById(R.id.device_min_power);
        mProbability = (TextView) findViewById(R.id.device_probability);
        mPriceThreshold = (TextView) findViewById(R.id.device_price_threshold);
        mWaitingTime = (TextView) findViewById(R.id.device_waitting_time);
        mUserId = ((TextView) findViewById(R.id.user_id));
        mUserTradeNo = ((TextView) findViewById(R.id.user_tradeNo));
        mIsTicket = ((TextView) findViewById(R.id.user_isTicket));
        result = ((TextView) findViewById(R.id.result));
        mXSpeed = ((TextView) findViewById(R.id.device_xspeed));
        mZspeed = ((TextView) findViewById(R.id.device_zspeed));
        textsend = ((TextView) findViewById(R.id.text_send));
        text_receiver = ((TextView) findViewById(R.id.text_receiver));

    }

    private int isSure(String deviceId, String deviceSecret) {
        if (!TextUtils.isEmpty(deviceId) && deviceId.length() == 6 && !TextUtils.isEmpty(deviceSecret) && deviceSecret.length() == 8) {
            SharedPreferences preferences = getSharedPreferences(Config.SP_DEVICE, MODE_PRIVATE);
            String sp_deviceId = preferences.getString(DeviceConfig.DEVICEID, "null");
            String sp_deviceSecret = preferences.getString(DeviceConfig.DEVICESECRET, "null");
            if ("null".equals(sp_deviceId) || "null".equals(sp_deviceSecret)) {
                return NOCONTROLL;
            } else if (!deviceId.equals(sp_deviceId) || !deviceSecret.equals(sp_deviceSecret)) {
                return INFOFAIL;
            }
            return RIGHT;
        }
        return INFOFAIL;
    }

    private void debugDisplayDeviceConfig(DeviceConfig config) {
        mMaxPower.setText(config.gstrength + "");
        mMinPower.setText(config.lstrength + "");
        mProbability.setText(config.probability + "");
        mPriceThreshold.setText(config.launchCoin + "");
        mXSpeed.setText(config.xspeed + "");
        mZspeed.setText(config.zspeed + "");
    }

    private void debugDisplayLaunchInfo(LaunchInfo info) {
        mUserId.setText(info.userId + "");
        mUserTradeNo.setText(info.tradeNo + "");
        mMaxPower.setText(info.max + "");
        mMinPower.setText(info.min + "");
        mProbability.setText(info.probability + "");
        //  mWaitingTime.setText(info.time + "");
        if (info.isTicket) {
            mIsTicket.setText("是");
        } else {
            mIsTicket.setText("否");
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(MainActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    @Override
    public void onQRScan(String QRCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "接收到扫码数据", Toast.LENGTH_SHORT).show();
            }
        });
        mMainBoard.onQrcodeScan(QRCode);
    }

    @Override
    public void onWIFIScan(String name, String password) {
        ScanUtil scUtil = new ScanUtil(this);
        scUtil.addNetWork(scUtil.createWifiInfo(name, password, password.length() == 0 ? 1 : 3));
    }

    @Override
    public void onIPChange(final String ip, final String port, final String rule) {
        NetworkrunUtils.getInstance().add(new Runnable() {
            @Override
            public void run() {
                File pathfile = new File(MApplication.IPCONFIG_Path);
                if (!pathfile.exists()) {
                    pathfile.mkdirs();
                }
                try {
                    //"config.json"
                    File file = new File(pathfile, Config.configFileName);
                    JSONObject js = new JSONObject();
                    js.put(Config.IP, ip);
                    js.put(Config.Port, port);
                    js.put(Config.Rule, rule);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(js.toString().getBytes(), 0, js.toString().getBytes().length);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                uiHandler.sendEmptyMessage(UIHandler.FINISH);
            }
        });
    }

    @Override
    public void onDeviceSet(final String deviceId, final String devicePsd, final boolean isYes) {
        NetworkrunUtils.getInstance().add(new Runnable() {
            @Override
            public void run() {
                File pathfile = new File(MApplication.IPCONFIG_Path);
                if (!pathfile.exists()) {
                    pathfile.mkdirs();
                }

                if(isYes){
                    try {
                        File file = new File(pathfile, Config.deviceFileName);
                        JSONObject js = new JSONObject();
                        js.put(Config.DEVICEID_Key, deviceId);
                        js.put(Config.DEVICEPSD_Key, devicePsd);
                        MyLog.i("回调拿到了设备id："+deviceId+"   devicePsd: "+devicePsd);
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(js.toString().getBytes(), 0, js.toString().getBytes().length);
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        uiHandler.sendEmptyMessage(UIHandler.FINISH);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }



            }
        });
    }


    public class UIHandler extends Handler {
        public static final int DEV_DETAILS = 0;
        public static final int CONTROLL_INIT = 1;
        public static final int CONTROLL_SUCESS = 2;
        public static final int SERVER_SUCESS = 3;
        public static final int SERVER_FAIL = 4;
        public static final int DEV_REFRESH_QR = 5;
        public static final int WIN = 6;
        public static final int FAIL = 7;
        public static final int SEND = 8;
        public static final int RECEIVER = 9;
        public static final int FINISH = 10;
        public static final int TAKEPIC = 11;
        public static final int TEST = 12;
        private int waitTime = -1;
        Timer timer;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DEV_DETAILS:
                    if (mCtrlArea.getVisibility() == View.VISIBLE) {
                        DeviceConfig deviceConfig = (DeviceConfig) msg.obj;
                        if (timer == null) {
                            mWaitingTime.setText(deviceConfig.gameTime + "");
                        }
                        debugDisplayDeviceConfig(deviceConfig);
                    }
                    break;
                case CONTROLL_INIT:
                    mLoginStatus.setText("与主板建立连接中");
                    break;
                case CONTROLL_SUCESS:
                    mLoginStatus.setText("与主板建立连接成功，正在登录服务器，请稍后...");
                    break;
                case SERVER_SUCESS:
                    mLoginStatus.setText("登录成功....");
                    // showPage2();
                    //debugDisplayDeviceConfig(mMainBoard.getDeviceConfig());
                    break;
                case SERVER_FAIL:
                    //mLoginStatus.setText("连接失败....");
                    showPage1();
                    break;
                case DEV_REFRESH_QR:
                    //开启计时器进行倒计时
                    LaunchInfo launchInfo = ((LaunchInfo) msg.obj);
                    if (timer == null) {
                        waitTime = launchInfo.time;
                        if (mCtrlArea.getVisibility() == View.VISIBLE) {
                            mWaitingTime.setText("" + waitTime);
                        }
                        //定时器用来动态更新时间
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                waitTime--;
                                uiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCtrlArea.getVisibility() == View.VISIBLE) {
                                            mWaitingTime.setText("" + waitTime);
                                        }
                                    }
                                });

                                if (waitTime == 0) {
                                    timer.cancel();
                                    timer = null;
                                }
                            }
                        }, 5000, 1000);
                    }

                    if (mCtrlArea.getVisibility() == View.VISIBLE) {
                        launchInfo = ((LaunchInfo) msg.obj);
                        debugDisplayLaunchInfo(launchInfo);
                    }
                    break;
                case WIN:
                    break;
                case FAIL:
                    if (mCtrlArea.getVisibility() == View.VISIBLE) {
                        result.setText("恭喜你，捡到宝了 ");
                    }
                    if (timer != null) {
                        timer.cancel();
                    }
                    break;
                case SEND:
                    textsend.setText(msg.obj + "");
                    break;
                case RECEIVER:
                    text_receiver.setText(msg.obj + "");
                    break;
                case FINISH:
                    finish();
                    System.exit(0);
                    break;
                case TAKEPIC:
                    break;
                case TEST:
                    Toast.makeText(MainActivity.this, "扫码请求返回成功", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    class MyAsynTask extends AsyncTask<Void,Void,Void>{


        @Override
        protected Void doInBackground(Void... params) {
            //读取配置文件的数据
            RemoteApi.init();
            MyLog.i("执行完了耗时操作");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //进行串口的连接
            initAllSerialPort();//初始化所有的串口
            MyLog.i("进行串口的连接");
        }
    }
}
