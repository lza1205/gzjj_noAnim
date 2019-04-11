package com.lljgame.llj.utils;


import android.util.Log;

import com.lljgame.llj.log.MyLog;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import java.util.Random;

/**
 * Created by Davia.Li on 2017-07-31.
 */

public class Utils {

    private static Random _random = new Random(getCurrentTimeMillis());

    public static int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static int nextInt(int n) {
        return _random.nextInt(n);
    }

    public static String byteArrayToHexString(byte[] byteBuffer) {
        return byteArrayToHexString(byteBuffer, byteBuffer.length);
    }

    public static String byteArrayToHexString(byte[] byteBuffer, int size) {
        StringBuilder strHexString = new StringBuilder();
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(0xff & byteBuffer[i]);
            if (hex.length() == 1) {
                strHexString.append('0');
            }
            strHexString.append(hex);
        }

        return strHexString.toString();
    }

    public static final String byte2hex(byte b[]) {
        if (b == null) {
            throw new IllegalArgumentException(
                    "Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }


    //获得不重复的6位密码
    public static String unRepeatSixCode() {
        String sixChar = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        Date date = new Date();
        String time = sdf.format(date);
        for (int i = 0; i < time.length() / 2; i++) {
            String singleChar;
            String x = time.substring(i * 2, (i + 1) * 2);
            int b = Integer.parseInt(x);
            if (b < 10) {
                singleChar = Integer.toHexString(Integer.parseInt(x));
            } else if (b >= 10 && b < 36) {
                singleChar = String.valueOf((char) (Integer.parseInt(x) + 55));
            } else {
                singleChar = String.valueOf((char) (Integer.parseInt(x) + 61));
            }
            sixChar = sixChar + singleChar;

        }
        System.out.println("生成一个6位不可重复的字符编码是：" + sixChar);
        return sixChar;
    }

    //获得不重复的8位密码
    public static String unRepeatEightCode() {
        String sixChar = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSSS");
        Date date = new Date();
        String time = sdf.format(date);
        for (int i = 0; i < time.length() / 2; i++) {
            String singleChar;
            String x = time.substring(i * 2, (i + 1) * 2);
            int b = Integer.parseInt(x);
            if (b < 10) {
                singleChar = Integer.toHexString(Integer.parseInt(x));
            } else if (b >= 10 && b < 36) {
                singleChar = String.valueOf((char) (Integer.parseInt(x) + 55));
                //A--Z
            } else if ((b > 61&&b<=64)) {
                singleChar = String.valueOf((char) (Integer.parseInt(x) + 20));
            } else if ((b >64&&b<90)) {
                singleChar = String.valueOf((char) (Integer.parseInt(x)));
            } else if ((b >=90)) {
                singleChar = String.valueOf((char) ((Integer.parseInt(x)-10)));
            } else {
                //18年
                singleChar = String.valueOf((char) (Integer.parseInt(x) + 61));
            }
            sixChar = sixChar + singleChar;

        }
        return sixChar;
    }

    //异或校验
    public static byte getcheck(byte[] bs) {
        byte temp = bs[1];
        for (int i = 2; i < bs.length - 2; i++) {
            temp ^= bs[i];
        }
        return temp;
    }

    public static byte[] addBytes(byte[] srcs,byte[] dests,int srcIndex,int destIndex){
        MyLog.i("src: "+ Arrays.toString(srcs)+"  dest: "+Arrays.toString(dests));
        System.arraycopy(srcs,srcIndex,dests,destIndex, srcs.length);
        MyLog.i("src2: "+ Arrays.toString(srcs)+"  dest2: "+Arrays.toString(dests));
        return dests;
    }
}
