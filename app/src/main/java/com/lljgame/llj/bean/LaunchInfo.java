package com.lljgame.llj.bean;

/**
 * Created by Davia.Li on 2017-07-31.
 */

public class LaunchInfo implements Cloneable{
    public int userId;//用户id
    public int tradeNo;//交易号
    public int probability;//几率
    public int max;//最大（强抓力）
    public int min;//最小(弱抓力)
    public int time;//时间
    public boolean isTicket;//是否有购票

    public LaunchInfo(int userId, int probability, int tradeNo, int max, int min, int time, boolean isTicket) {
        this.userId = userId;
        this.probability = probability;
        this.tradeNo = tradeNo;
        this.max = max;
        this.min = min;
        this.time = time;
        this.isTicket = isTicket;
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
