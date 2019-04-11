package com.lljgame.llj.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Davia.Li on 2017-08-01.
 */

public class HttpUtils {
    private static final String TAG = "HttpUtil";

    private static final int HTTP_TIME_OUT = 10 * 1000;

    public static String Get(String u) {
        try {
            boolean isHttps = u.startsWith("https://");
            URL url = new URL(u);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(HTTP_TIME_OUT);
            conn.setReadTimeout(HTTP_TIME_OUT);
            if(isHttps) {
                ((HttpsURLConnection)conn).setRequestMethod("GET");
            } else {
                ((HttpURLConnection)conn).setRequestMethod("GET");
            }

            conn.setUseCaches(false);
            conn.connect();

            int respCode;
            if(isHttps) {
                respCode =  ((HttpsURLConnection)conn).getResponseCode();
            } else {
                respCode = ((HttpURLConnection)conn).getResponseCode();
            }
            if(respCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            return readStream(conn.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String PostJson(String u, String json) {
        System.out.println("post json to:" + u);
        try {
            boolean isHttps = u.startsWith("https://");
            URL url = new URL(u);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(HTTP_TIME_OUT);
            conn.setReadTimeout(HTTP_TIME_OUT);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            if(isHttps) {
                ((HttpsURLConnection)conn).setRequestMethod("POST");
            } else {
                ((HttpURLConnection)conn).setRequestMethod("POST");
            }
            conn.setUseCaches(false);
            conn.connect();

            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(conn.getOutputStream()));
            out.writeBytes(json);
            out.close();

            int respCode;
            if(isHttps) {
                respCode =  ((HttpsURLConnection)conn).getResponseCode();
            } else {
                respCode = ((HttpURLConnection)conn).getResponseCode();
            }
            if(respCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            return readStream(conn.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String res = response.toString();
        System.out.println("http response:" + res);
        return res;
    }
}
