package com.lljgame.llj.bean;

import android.util.Log;

/**
 * Created by Davia.Li on 2017-07-31.
 */

public class LaunchInfo implements Cloneable{

    private String TAG = "LLJ";

    public int userId;//用户id
    public int tradeNo;//交易号
    public int probability;//几率
    public int max;//最大（强抓力）
    public int min;//最小(弱抓力)
    public int time;//时间
    public boolean isTicket;//是否有购票

    public boolean isTwoClaws = false; //判断是否有TwoClaws
    public int gamePower;   //游戏合爪力度
    public int hitPower;    //送奖合爪力度
    public int zHeight; //z轴终点位置
    public int dropDown;    //爪子返回时先到达一个位置张开爪子
    public int initPosition;    //爪子归位位置: 0:爪子归位左前方 1:归位右前方

    public LaunchInfo(int userId, int probability, int tradeNo, int max, int min, int time, boolean isTicket) {
        this.userId = userId;
        this.probability = probability;
        this.tradeNo = tradeNo;
        this.max = max;
        this.min = min;
        this.time = time;
        this.isTicket = isTicket;
    }

    public void setTwoClaws(int gamePower, int hitPower, int zHeight, int dropDown, int initPosition)
    {
        this.gamePower = gamePower;
        this.hitPower = hitPower;
        this.zHeight = zHeight;
        this.dropDown = dropDown;
        this.initPosition = initPosition;

        this.isTwoClaws = true;
        Log.d(TAG, "setTwoClaws :" + this.gamePower + " " + this.hitPower+ " " + this.zHeight+ " " + this.dropDown+ " " + this.initPosition);
    }

    public Object clone()
    {
        Object o=null;
        try
        {
            o=(LaunchInfo)super.clone();//Object 中的clone()识别出你要复制的是哪一个对象。
        }
        catch(CloneNotSupportedException e)
        {
            System.out.println(e.toString());
        }
        return o;
    }
}
