package com.lljgame.llj.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by 98733 on 2018/7/30.
 */

public class ShareUtil {
    private static SharedPreferences sharedPreferences;
    public static final String Expire="Expire";
    public static String DATE="date";
    public static String WIN="winCount";
    public static String FAIL="failCount";

    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences(Expire,Context.MODE_PRIVATE);
    }

    public  static SharedPreferences getSharedPreferences(){
        return sharedPreferences;
    }

    public static void write(String date, long l) {
        SharedPreferences.Editor ed= sharedPreferences.edit();
        ed.putLong(date, l);
        ed.commit();
    }

    public static void write(String date, int l) {
        SharedPreferences.Editor ed= sharedPreferences.edit();
        ed.putInt(date, l);
        ed.commit();
    }

    public static void reset(){
        SharedPreferences.Editor ed=sharedPreferences.edit();
        ed.putLong(DATE,-1);
        ed.putInt(WIN,0);
        ed.putInt(FAIL,0);
        ed.commit();
    }
}
