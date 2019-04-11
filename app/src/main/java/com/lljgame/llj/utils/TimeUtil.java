package com.lljgame.llj.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.lljgame.llj.log.MyLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by 98733 on 2018/5/8.
 */

public class TimeUtil {

    private static int getGapCount(Date startDate, Date endDate) {
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(startDate);

        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(endDate);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);

        return (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
    }

    public static boolean isExpire(Context context) {
        ShareUtil.init(context);
        long dateTime = ShareUtil.getSharedPreferences().getLong(ShareUtil.DATE, -1);
        if (dateTime == -1) {
            ShareUtil.write(ShareUtil.DATE,System.currentTimeMillis());
        } else {
            Date date = new Date(System.currentTimeMillis());

            Date old_date = new Date(dateTime);
            int day = getGapCount(old_date, date);
            //判断多少天清空
            MyLog.i("day: ->"+day);
            if(day==7||day>7){
                return true;
            }
        }
        return false;
    }

    public static long getMillFromDate(int year,int month,int day,int hour,int min,int sec){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day, hour, min, sec);
        return calendar.getTimeInMillis();
    }

    public static String LongToTimer(long mill){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        Date date=new Date(mill);
        return simpleDateFormat.format(date);
    }

}
