package com.lljgame.llj.remote;

import android.content.Context;
import android.util.Log;

import com.lljgame.llj.MainActivity;
import com.lljgame.llj.R;
import com.lljgame.llj.utils.CipherUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

/**
 * Created by Davia.Li on 2017-07-31.
 */
//这个是为了和服务器检测长连接的，并且对应服务器返回数据执行相应操作
public class DevicesServer implements Runnable {
    private static final int COMMUNICATE_TIMEOUT = 25 * 1000 * 2;
    private static final int FRAME_TYPE_RESPONSE = 0;
    private static final int FRAME_TYPE_ERROR = 1;
    private static final int FRAME_TYPE_MESSAGE = 2;
    private static final char NL = '\n';
    private  Context context;

    private String mDeviceId;
    private String mDeviceSecret;
    private String mServerUrl;
    private int mServerPort;
    private Thread mThread;
    private DevicesServerListener mListener;
    private boolean mServiceRunning;
    private Socket mSocket;

    private DataInputStream mInputStream;
    private DataOutputStream mOutputStream;

    private String mAuthPassword;

    private enum pushState {
        connected,closed
    }

    private pushState mState;

    public DevicesServer(Context context, String deviceId, String deviceSecret, String serverUrl,
                         int serverPort, String authPassword) {
        this.context=context;
        mDeviceId = deviceId;
        mDeviceSecret = deviceSecret;
        mServerUrl = serverUrl;
        mServerPort = serverPort;
        mAuthPassword = authPassword;
        mThread = new Thread(this);
        mSocket = new Socket();
    }

    public void setDeviceServerListener(DevicesServerListener l) {
        mListener = l;
    }

    public void start() {
        mServiceRunning = true;
        mThread.start();
    }

    public void stop() {
        mServiceRunning = false;
        mThread.interrupt();
    }

    @Override
    public void run() {
        try {
            connect();
        } catch (IOException e) {
            close();
        }
        if (mState != pushState.connected) {
            return;
        }


        mListener.onConnect();

        while(mState != pushState.closed && mServiceRunning) {
            try {
                byte[] response = readResponse();
                handleResponse(response);
            } catch (IOException e) {
                e.printStackTrace();
                close();
                break;
            }
        }
    }

    private byte[] readResponse() throws IOException{
        int size = mInputStream.readInt();
        byte[] data = new byte[size];
        mInputStream.read(data, 0, size);

        return data;
    }

    private void connect() throws IOException {
        mSocket.setSoTimeout(COMMUNICATE_TIMEOUT);
        mSocket.connect(new InetSocketAddress(mServerUrl, mServerPort),
                COMMUNICATE_TIMEOUT);
        mOutputStream = new DataOutputStream(new BufferedOutputStream(mSocket.getOutputStream()));
        mInputStream = new DataInputStream(new BufferedInputStream(mSocket.getInputStream()));
        mOutputStream.writeBytes("  V1");
        mOutputStream.flush();
        auth();
        mState = pushState.connected;
    }

    private void auth() throws IOException {
        String time = new Date().getTime() + "";
        send(command("AUTH", mDeviceId, time, CipherUtils.sha1(mDeviceId + mDeviceSecret + time + mAuthPassword),context.getResources().getString(R.string.version)));
    }

    private void nul() throws IOException{
        send(command("NUL"));
    }

    private void send(String command) throws IOException {
        mOutputStream.writeBytes(command);
        mOutputStream.flush();
    }

    private static String command(String cmd, String... params){
        StringBuilder commandBuilder = new StringBuilder(cmd);
        for(String param : params){
            commandBuilder.append(" ");
            commandBuilder.append(param);
        }
        commandBuilder.append(NL);

        return commandBuilder.toString();
    }

    private void handleResponse(byte[] response) throws IOException {
        DataInputStream ds = new DataInputStream(new ByteArrayInputStream(response));
        int frameType = ds.readInt();
        switch (frameType) {
            case FRAME_TYPE_RESPONSE:
                String s = new String(response).trim();
                String[] responseArray = s.split(" ");
                if(responseArray.length == 2 && responseArray[0].equals("AUTH") &&
                        responseArray[1].equals("OK")) {
                    break;
                }
                if(responseArray.length == 1 && responseArray[0].equals("_heartbeat_")) {
                    nul();
                    break;
                }
                break;
            case FRAME_TYPE_MESSAGE:
                byte[] msg = new byte[response.length - 4];
                ds.read(msg);
                if(mListener != null) {
                    mListener.onMessage(msg);
                }
                break;
            case FRAME_TYPE_ERROR:
            default:
        }
    }

    private void close() {
        mState = pushState.closed;
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(mListener != null) {
            mListener.onDisconnect();
        }
    }

    public interface DevicesServerListener {
        void onMessage(byte[] msg);
        void onDisconnect();
        void onConnect();
    }
}
