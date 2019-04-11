package com.lljgame.llj.log;

import android.util.Log;

/**
 * Created by 98733 on 2018/7/24.
 */

public class MyLog {
    public static String TAG = "tag";
    private static boolean debug = true;

    public static void i(String msg) {
        if (debug)
            Log.i(TAG, msg);
    }
}
