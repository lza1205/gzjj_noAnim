package com.lljgame.llj;

import android.app.Application;
import android.os.Environment;

import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import java.io.File;

/**
 * Created by Davia.Li on 2017-08-25.
 */

public class MApplication extends Application {
    public static final String IPCONFIG_Path= Environment.getExternalStorageDirectory()+ File.separator+"ipconfig";
    @Override
    public void onCreate() {
        super.onCreate();

      //  ZXingLibrary.initDisplayOpinion(this);
    }
}
