package com.lljgame.llj.log;

import android.content.Context;
import android.os.Environment;

import com.lljgame.llj.utils.TimeUtil;

import org.apache.log4j.Level;

import java.io.File;

import de.mindpipe.android.logging.log4j.LogConfigurator;

import static com.lljgame.llj.utils.FileUtil.delAllFile;


/**
 * Created by 98733 on 2018/7/30.
 */

public class ConfigureLog4J {
    static LogConfigurator logConfigurator;

    static String fileName = Environment.getExternalStorageDirectory()
            + File.separator + "log"
            + File.separator + "test.log";

    public static void configure(Context context) {
        //执行七天自动重置数据
       if (TimeUtil.isExpire(context)) {
            File file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + "log");
            if (file.exists()) {
                delAllFile(file.getAbsolutePath());
            }
        }
        logConfigurator = new LogConfigurator();
        // Date nowtime = new Date();
        // String needWriteMessage = myLogSdf.format(nowtime);
        //日志文件路径地址:SD卡下myc文件夹log文件夹的test文件

        //设置文件名
        logConfigurator.setFileName(fileName);
        //设置root日志输出级别 默认为DEBUG
        logConfigurator.setRootLevel(Level.DEBUG);
        // 设置日志输出级别
        logConfigurator.setLevel("org.apache", Level.INFO);
        //设置 输出到日志文件的文字格式 默认 %d %-5p [%c{2}]-[%L] %m%n
        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        //设置输出到控制台的文字格式 默认%m%n
        logConfigurator.setLogCatPattern("%m%n");
        //设置总文件大小
        logConfigurator.setMaxFileSize(1024 * 1024 * 5);
        //设置最大产生的文件个数
        logConfigurator.setMaxBackupSize(7);
        //设置所有消息是否被立刻输出 默认为true,false 不输出
        logConfigurator.setImmediateFlush(true);
        //是否本地控制台打印输出 默认为true ，false不输出
        logConfigurator.setUseLogCatAppender(true);
        //设置是否启用文件附加,默认为true。false为覆盖文件
        logConfigurator.setUseFileAppender(true);
        //设置是否重置配置文件，默认为true
        logConfigurator.setResetConfiguration(true);
        //是否显示内部初始化日志,默认为false
        logConfigurator.setInternalDebugging(false);
        logConfigurator.configure();
    }

    static String fileCountName = Environment.getExternalStorageDirectory()
            + File.separator + "log"
            + File.separator + "count.log";

    public static void ChangeToCount() {
        logConfigurator.setFileName(fileCountName);
        //设置最大产生的文件个数
        logConfigurator.configure();
    }

    public static void ChangeToTital() {
        logConfigurator.setFileName(fileName);
        logConfigurator.configure();
    }
}
