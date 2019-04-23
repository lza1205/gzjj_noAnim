package com.lljgame.llj;

/**
 * Created by Davia.Li on 2017-08-01.
 */

public class Config {
    public static final String AUTH_PASSWORD = "OgKh7Z1JaiqdsUpR2dMI";

    public static volatile String SERVER_PROTOCOL = "https://";
    // public static  String SERVER_PROTOCOL = "http://";
     public static volatile String SERVER_URL = "api.lljgame.com";
    //public static volatile String SERVER_URL = "api-debug.lljgame.com";
    //public static volatile String SERVER_URL = "jz.lljgame.com";
    // public static final String SERVER_URL = "47.106.224.80";
    // public static String SERVER_URL = "192.168.0.109";
    public static final int SERVER_TCP_PORT = 17723;
    public static volatile int SERVER_HTTP_PORT = 443;
    //public static final int SERVER_HTTP_PORT = 17722;
    public static final String SP_DEVICE = "device";

    public static final int MIN_RETRY_CONNECT_TIME = 5 * 1000;
    public static final int MAX_RETRY_CONNECT_TIME = 120 * 1000;

    public static final String VERSION = "1.2.1";

    public static final String IP = "IP";
    public static final String Port = "Port";
    public static final String WifiName = "WifiName";
    public static final String WifiPwd = "WifiPwd";
    public static final String Rule = "Rule";
    public static final String DEVICEID_Key = "deviceId";
    public static final String DEVICEPSD_Key = "devicePsd";
    public static final String ISYES = "isYes";
    public static String deviceFileName = "device.json";
    public static String configFileName = "config.json";

    public static String DEVICEID = "";
    public static  String DEVICEPSD = "";
}
