package com.lljgame.llj.process;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by Davia.Li on 2017-04-07.
 */

public class DownloadAndInstallApkProcess implements Runnable {
    private static final int DOWNLOAD_TIMEOUT = 15 * 1000;
    private Thread mThread;
    private Context mContext;
    private String mUrl;

    public DownloadAndInstallApkProcess(Context context, String url) {
        mContext = context;
        mThread = new Thread(this);
        mUrl = url;
    }

    public void start() {
        mThread.start();
    }

    @Override
    public void run() {
        try {
            String fileName = mUrl.substring(mUrl.lastIndexOf("/"));
            URL url = new URL(mUrl);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setConnectTimeout(DOWNLOAD_TIMEOUT);
            c.setReadTimeout(DOWNLOAD_TIMEOUT);

            int responseCode = c.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                return;
            }

            String path = Environment.getExternalStorageDirectory() + "/download/";
            File file = new File(path);
            file.mkdirs();
            File outputFile = new File(file, fileName);
            FileOutputStream fos = new FileOutputStream(outputFile);

            InputStream is = c.getInputStream();

            byte[] buffer = new byte[256];
            int len;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            is.close();
            System.out.println("Download success");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(outputFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
