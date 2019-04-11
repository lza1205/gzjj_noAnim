package com.lljgame.llj.remote;

import android.text.TextUtils;

import com.lljgame.llj.Config;
import com.lljgame.llj.MApplication;
import com.lljgame.llj.bean.DeviceConfig;
import com.lljgame.llj.bean.LaunchInfo;
import com.lljgame.llj.log.MyLog;
import com.lljgame.llj.utils.CipherUtils;
import com.lljgame.llj.utils.HttpUtils;
import com.lljgame.llj.utils.NetworkrunUtils;
import com.lljgame.llj.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.lljgame.llj.utils.HttpUtils.PostJson;

/**
 * Created by Davia.Li on 2017-07-31.
 */

public class RemoteApi {
    public static String DEVICE_SERVER_BASE_URL = Config.SERVER_PROTOCOL + Config.SERVER_URL + ":" + Config.SERVER_HTTP_PORT;
    private static String GET_ACCOUNT_SECRET_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/getAccountSecret";
    private static  String DEVICE_QRCODE_SCAN_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/qrcodeScan";
    private static  String PRODUCT_WIN_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/productWin";
    private static  String DEVICE_ERROR_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/deviceError";
    private static  String GAME_START_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/gameStart";
    private static  String GAME_END_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/gameEnd";
    private static  String UPDTE_DEVICE_PREFERENCE_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/updateDevicePreference";
    private static  String DEBUG_TRACE_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/debugTrace";//debugTrace

    private static final String DEVICE_ID_STRING = "deviceId";
    private static final String SIGN_STRING = "sign";
    private static final String TIME_STAMP_STRING = "timeStamp";
    private static final String ERR_CODE_STRING = "errcode";
    private static final String ACCOUNT_SECRET_STRING = "accountSecret";
    private static final String QRCODE_STRING = "qrcode";
    private static final String USER_ID_STRING = "userId";
    private static final String PROBABILITY_STRING = "probability";
    private static final String TRADE_NO_STRING = "tradeNo";
    private static final String ACCOUNT_QRCODE_TTL_STRING = "accountQrcodeTTL";
    private static final String PRICE_THRESHOLD_STRING = "priceThreshold";
    private static final String MIN_POWER_STRING = "minPower";
    private static final String MAX_POWER_STRING = "maxPower";
    private static final String WAITTING_TIME_STRING = "waitingTime";
    private static final String VERSION_STRING = "version";
    private static final String MAX = "max";
    private static final String MIN = "min";
    private static final String TIME = "time";
    private static final String IS_TICKET = "isTicket";
    private static final String COUNT = "count";

    private static String deviceId;
    private static String deviceSecret;
    private static String authPassword;
    private static String accountKey;

    private static int qrcodeTTL;

    public static void init(String deviceId, String deviceSecret, String authPassword) {
        RemoteApi.deviceId = deviceId;
        RemoteApi.deviceSecret = deviceSecret;
        RemoteApi.authPassword = authPassword;
    }

    public static void init() {
        //读取配置文件的数据（默认还是有地址的）
        LoadConfigFile();
        LoadDeviceFile();
        DEVICE_SERVER_BASE_URL = Config.SERVER_PROTOCOL + Config.SERVER_URL + ":" + Config.SERVER_HTTP_PORT;
        GET_ACCOUNT_SECRET_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/getAccountSecret";
        DEVICE_QRCODE_SCAN_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/qrcodeScan";
        PRODUCT_WIN_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/productWin";
        DEVICE_ERROR_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/deviceError";
        GAME_START_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/gameStart";
        GAME_END_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/gameEnd";
        UPDTE_DEVICE_PREFERENCE_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/updateDevicePreference";
        DEBUG_TRACE_URL = DEVICE_SERVER_BASE_URL + "/deviceServer/device/debugTrace";//debugTrace

    }

    private static void LoadDeviceFile() {
        File file = new File(MApplication.IPCONFIG_Path, Config.deviceFileName);
        if (!file.exists()) {
            return;
        }
        StringBuffer str = new StringBuffer();
        try {
            FileInputStream Fis = new FileInputStream(file);
            if (Fis != null) {
                InputStreamReader inputreader = new InputStreamReader(Fis);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                //分行读取
                while ((line = buffreader.readLine()) != null) {
                    str.append(line);
                }
                buffreader.close();
                JSONObject jsonObject = new JSONObject(str.toString());
                String deviceId = jsonObject.optString(Config.DEVICEID_Key);
                String devicePsd = jsonObject.optString(Config.DEVICEPSD_Key);
                Config.DEVICEID=deviceId;
                Config.DEVICEPSD=devicePsd;
                MyLog.i("LoadDeviceFile:   deviceId："+ Config.DEVICEID+"  DEVICEPSD: "+Config.DEVICEPSD);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void LoadConfigFile() {
        File file = new File(MApplication.IPCONFIG_Path, Config.configFileName);
        if (!file.exists()) {
            return;
        }
        StringBuffer str = new StringBuffer();
        try {
            FileInputStream Fis = new FileInputStream(file);
            if (Fis != null) {
                InputStreamReader inputreader = new InputStreamReader(Fis);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                //分行读取
                while ((line = buffreader.readLine()) != null) {
                    str.append(line);
                }
                buffreader.close();
                JSONObject jsonObject = new JSONObject(str.toString());
                String IP = jsonObject.optString(Config.IP);
                String Port = jsonObject.optString(Config.Port);
                String Rule = jsonObject.optString(Config.Rule);
                MyLog.i("ip: " + IP + " Port: " + Port + "  Rule: " + Rule);
                Config.SERVER_URL = IP;
                Config.SERVER_HTTP_PORT = Integer.valueOf(Port);
                Config.SERVER_PROTOCOL = Rule + "://";
                MyLog.i("rule: " + Config.SERVER_PROTOCOL + "  server_url: " + Config.SERVER_URL + "  Port: " + Config.SERVER_HTTP_PORT);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static LaunchInfo qrcodeScan(String qrcode) {
        if (accountKey == null) {
            return null;
        }

        byte[] scanData = CipherUtils.base64Decode(qrcode);
        byte[] key = CipherUtils.base64Decode(accountKey + "=");
        String scanString = CipherUtils.aesDecrypt(scanData, key);
        if (scanString == null) {
            return null;
        }
        System.out.println(scanString);
        String[] vals = scanString.split(",");
        try {

            long time = Long.parseLong(vals[vals.length - 1]);
            int now = Utils.getCurrentTime();
            if (now - time > qrcodeTTL || time - now > qrcodeTTL) {
                //return null;
            }

            JSONObject jsonReq = new JSONObject();
            jsonReq.put(DEVICE_ID_STRING, deviceId);
            jsonReq.put(SIGN_STRING, getSign(now));
            jsonReq.put(TIME_STAMP_STRING, now);
            jsonReq.put(QRCODE_STRING, qrcode);

            String jsonStr = PostJson(DEVICE_QRCODE_SCAN_URL, jsonReq.toString());
            JSONObject jsonRes = new JSONObject(jsonStr);

            int errCode = jsonRes.getInt(ERR_CODE_STRING);
            if (errCode != 0) {
                return null;
            }

            return new LaunchInfo(jsonRes.getInt(USER_ID_STRING),
                    jsonRes.getInt(PROBABILITY_STRING),
                    jsonRes.getInt(TRADE_NO_STRING),
                    jsonRes.getInt(MAX),
                    jsonRes.getInt(MIN),
                    jsonRes.getInt(TIME),
                    jsonRes.getBoolean(IS_TICKET));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static LaunchInfo getDeviceDetails(String qrcode) throws JSONException {
        int now = Utils.getCurrentTime();
        JSONObject jsonReq = new JSONObject();
        jsonReq.put(DEVICE_ID_STRING, deviceId);
        jsonReq.put(SIGN_STRING, getSign(now));
        jsonReq.put(TIME_STAMP_STRING, now);
        jsonReq.put(QRCODE_STRING, qrcode);

        String jsonStr = PostJson(DEVICE_QRCODE_SCAN_URL, jsonReq.toString());
        JSONObject jsonRes = new JSONObject(jsonStr);
        int errCode = jsonRes.getInt(ERR_CODE_STRING);
        if (errCode != 0) {
            return null;
        }
        return new LaunchInfo(jsonRes.getInt(USER_ID_STRING),
                jsonRes.getInt(PROBABILITY_STRING),
                jsonRes.getInt(TRADE_NO_STRING),
                jsonRes.getInt(MAX),
                jsonRes.getInt(MIN),
                jsonRes.getInt(TIME),
                jsonRes.getBoolean(IS_TICKET));
    }

    public static boolean productWin(int tradeNo, int userId, boolean isTicket) {
        try {
            int now = Utils.getCurrentTime();
            JSONObject jsonReq = new JSONObject();
            jsonReq.put(DEVICE_ID_STRING, deviceId);
            jsonReq.put(SIGN_STRING, getSign(now));
            jsonReq.put(TIME_STAMP_STRING, now);
            jsonReq.put(TRADE_NO_STRING, tradeNo);
            jsonReq.put(USER_ID_STRING, userId);
            jsonReq.put(IS_TICKET, isTicket);
            String jsonStr = HttpUtils.PostJson(PRODUCT_WIN_URL, jsonReq.toString());
            if (jsonStr == null) {
                return false;
            }

            JSONObject json = new JSONObject(jsonStr);
            return json.getInt(ERR_CODE_STRING) == 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean productWin(int tradeNo, int userId, boolean isTicket,int wincount) {
        try {
            int now = Utils.getCurrentTime();
            JSONObject jsonReq = new JSONObject();
            jsonReq.put(DEVICE_ID_STRING, deviceId);
            jsonReq.put(SIGN_STRING, getSign(now));
            jsonReq.put(TIME_STAMP_STRING, now);
            jsonReq.put(TRADE_NO_STRING, tradeNo);
            jsonReq.put(USER_ID_STRING, userId);
            jsonReq.put(COUNT,wincount);
            jsonReq.put(IS_TICKET, isTicket);
            String jsonStr = HttpUtils.PostJson(PRODUCT_WIN_URL, jsonReq.toString());
            if (jsonStr == null) {
                return false;
            }

            JSONObject json = new JSONObject(jsonStr);
            return json.getInt(ERR_CODE_STRING) == 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean gameStart(int tradeNo, int userId, boolean isTicket) {
        try {
            int now = Utils.getCurrentTime();
            JSONObject jsonReq = new JSONObject();
            jsonReq.put(DEVICE_ID_STRING, deviceId);
            jsonReq.put(SIGN_STRING, getSign(now));
            jsonReq.put(TIME_STAMP_STRING, now);
            jsonReq.put(TRADE_NO_STRING, tradeNo);
            jsonReq.put(USER_ID_STRING, userId);
            jsonReq.put(IS_TICKET, isTicket);
            String jsonStr = HttpUtils.PostJson(GAME_START_URL, jsonReq.toString());
            if (jsonStr == null) {
                return false;
            }

            JSONObject json = new JSONObject(jsonStr);
            return json.getInt(ERR_CODE_STRING) == 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean gameEnd(int tradeNo, int userId, boolean isTicket) {
        try {
            int now = Utils.getCurrentTime();
            JSONObject jsonReq = new JSONObject();
            jsonReq.put(DEVICE_ID_STRING, deviceId);
            jsonReq.put(SIGN_STRING, getSign(now));
            jsonReq.put(TIME_STAMP_STRING, now);
            jsonReq.put(TRADE_NO_STRING, tradeNo);
            jsonReq.put(USER_ID_STRING, userId);
            jsonReq.put(IS_TICKET, isTicket);
            String jsonStr = HttpUtils.PostJson(GAME_END_URL, jsonReq.toString());
            if (jsonStr == null) {
                return false;
            }

            JSONObject json = new JSONObject(jsonStr);
            return json.getInt(ERR_CODE_STRING) == 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean deviceError(int errCode) {
        try {
            int now = Utils.getCurrentTime();
            JSONObject jsonReq = new JSONObject();
            jsonReq.put(DEVICE_ID_STRING, deviceId);
            jsonReq.put(SIGN_STRING, getSign(now));
            jsonReq.put(TIME_STAMP_STRING, now);
            jsonReq.put(ERR_CODE_STRING, errCode);

            String jsonStr = HttpUtils.PostJson(DEVICE_ERROR_URL, jsonReq.toString());
            if (jsonStr == null) {
                return false;
            }

            JSONObject json = new JSONObject(jsonStr);
            return json.getInt(ERR_CODE_STRING) == 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean updatePreference(DeviceConfig config) {
        try {
            int now = Utils.getCurrentTime();
            JSONObject jsonReq = new JSONObject();
            jsonReq.put(DEVICE_ID_STRING, deviceId);
            jsonReq.put(SIGN_STRING, getSign(now));
            jsonReq.put(TIME_STAMP_STRING, now);
            jsonReq.put(PRICE_THRESHOLD_STRING, config.launchCoin);
            jsonReq.put(PROBABILITY_STRING, config.probability);
            jsonReq.put(MIN_POWER_STRING, config.lstrength);
            jsonReq.put(MAX_POWER_STRING, config.gstrength);
            jsonReq.put(WAITTING_TIME_STRING, config.gameTime);
            jsonReq.put(VERSION_STRING, Config.VERSION);

            String jsonStr = HttpUtils.PostJson(UPDTE_DEVICE_PREFERENCE_URL, jsonReq.toString());
            if (jsonStr == null) {
                return false;
            }

            JSONObject json = new JSONObject(jsonStr);
            return json.getInt(ERR_CODE_STRING) == 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getAccountSecret() {
        if (deviceId == null || deviceSecret == null) {
            return null;
        }

        try {
            int now = Utils.getCurrentTime();
            JSONObject jsonReq = new JSONObject();
            MyLog.i("RemoteApi deviceId: "+deviceId);
            jsonReq.put(DEVICE_ID_STRING, deviceId);
            jsonReq.put(SIGN_STRING, getSign(now));
            jsonReq.put(TIME_STAMP_STRING, now);
            String jsonString = PostJson(GET_ACCOUNT_SECRET_URL, jsonReq.toString());
            if (jsonString == null) {
                return null;
            }
            JSONObject jsonRes = new JSONObject(jsonString);
            int errcode = jsonRes.getInt(ERR_CODE_STRING);
            if (errcode != 0) {
                return null;
            }
            qrcodeTTL = jsonRes.getInt(ACCOUNT_QRCODE_TTL_STRING);
            accountKey = jsonRes.getString(ACCOUNT_SECRET_STRING);
            return accountKey;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 调试trace
    public static void debugTrace(final String traceNo) {
        NetworkrunUtils.getInstance().add(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonReq = new JSONObject();
                    jsonReq.put("k", traceNo);
                    HttpUtils.PostJson(DEBUG_TRACE_URL, jsonReq.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static String getSign(int timeStamp) {
        return CipherUtils.sha1(deviceId + deviceSecret + timeStamp + authPassword);
    }
}
