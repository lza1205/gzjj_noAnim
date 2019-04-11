package com.lljgame.llj.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Administrator on 2019/4/10.
 */

public class HttpDownloader {

    private String TAG = "HttpDownloader";
    private Context context;
    private String ServerUrl = null;
    private String dowmUrl = null;
    private String fileName = null;

    public  HttpDownloader(String ServerUrl)
    {
        this.context = context;
        this.ServerUrl = ServerUrl;
    }

    public  HttpDownloader(String ServerUrl, String dowmUrl, String fileName)
    {
        this.context = context;
        this.ServerUrl = ServerUrl;
        this.dowmUrl = dowmUrl;
        this.fileName = fileName;
    }

    public void startDownLoad()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                download();
            }
        }).start();
    }

    public void setDownloaderUrl(String dowmUrl)
    {
        this.dowmUrl = dowmUrl;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }



    //下载具体操作
    private boolean download() {
        try {
            URL url = new URL(this.ServerUrl + this.dowmUrl);
            //打开连接
            URLConnection conn = url.openConnection();
            //打开输入流
            InputStream is = conn.getInputStream();
            //获得长度
            int contentLength = conn.getContentLength();
            Log.d(TAG, "文件长度 = " + contentLength);
            //创建文件夹 MyDownLoad，在存储卡下
            String dirName = Environment.getExternalStorageDirectory() + "/Movies/";
            //下载后的文件名
            String fileName = dirName + this.fileName;
            File file1 = new File(fileName);
            /*
            if (file1.exists()) {
                Log.d(TAG, "文件已经存在！");
                return fileIsExists(fileName);
            } else
            */
            {
                //创建字节流
                byte[] bs = new byte[1024];
                int len;
                OutputStream os = new FileOutputStream(fileName);
                //写数据
                while ((len = is.read(bs)) != -1) {
                    os.write(bs, 0, len);
                }
                //完成后关闭流
                Log.d(TAG, "下载成功！");
                os.close();
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //判断文件是否存在
    public boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }

}
