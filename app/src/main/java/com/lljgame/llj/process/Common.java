package com.lljgame.llj.process;

import java.util.Date;

/**
 * Created by Davia.Li on 2017-02-13.
 */

public class Common {
    public static final int HEART_BEAT_LISTEN_PORT = 9000;
    public static final int HEART_BEAT_SEND_PORT = 9001;
    public static final int HEART_BEAT_INTERVAL = 3 * 1000;
    public static final String HEART_BEAT_CMD = "__heart_beat__";
    private static long lastHeartBeatTime = 0;

    public static final String APP_PACKAGE_NAME = "com.xintian.jizhuadaemon";

    public static void setHeartBeat() {
        lastHeartBeatTime = new Date().getTime();
    }

    public static long getLastHeartBeatTime() {
        return lastHeartBeatTime;
    }
}
