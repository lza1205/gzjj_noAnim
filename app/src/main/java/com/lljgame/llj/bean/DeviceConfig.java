package com.lljgame.llj.bean;

/**
 * Created by Davia.Li on 2017-07-31.
 */

public class DeviceConfig {
    public static final String DEVICEID="deviceId";
    public static final String DEVICESECRET="deviceSecret";
    public String deviceId;
    public String deviceSecret;
    public int probability;
    public int gstrength;
    public int lstrength;
    public int launchCoin;

    public int xspeed;
    public int zspeed;
    public int gameTime;

    private boolean configDone;
    private boolean preferenceDone = true;

    public DeviceConfig(){}

    public DeviceConfig(String deviceId, String deviceSecret, int probability, int gstrength, int lstrength, int launchCoin, int xspeed, int zspeed, int gameTime) {
        this.deviceId = deviceId;
        this.deviceSecret = deviceSecret;
        this.probability = probability;
        this.gstrength = gstrength;
        this.lstrength = lstrength;
        this.launchCoin = launchCoin;
        this.xspeed = xspeed;
        this.zspeed = zspeed;
        this.gameTime = gameTime;
    }

    public boolean isInit() {
        return configDone && preferenceDone;
    }

    public void setConfig(String deviceId, String deviceSecret) {
        this.deviceId = deviceId;
        this.deviceSecret = deviceSecret;
        configDone = true;
    }

    public void setPreference(int probability, int gstrength, int lstrength, int launchCoin) {
        this.probability = probability;
        this.gstrength = gstrength;
        this.lstrength = lstrength;
        this.launchCoin = launchCoin;
        preferenceDone = true;
    }

    public void setPreferenceExtra(int xspeed, int zspeed, int gameTime) {
        this.xspeed = xspeed;
        this.zspeed = zspeed;
        this.gameTime = gameTime;
    }
}
