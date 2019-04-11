package com.lljgame.llj.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.lljgame.llj.MainActivity;
import com.lljgame.llj.R;

/**
 * Created by Administrator on 2019/4/10.
 */

public class VideoAcitivity extends Activity {
    private VideoView videoView;

    private String TAG = "llj";

    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        setContentView(R.layout.video_layout_main);

        videoView = (VideoView) findViewById(R.id.video_view);

        initVideoPath();

        HttpDownloader httpDownloader = new HttpDownloader("http://172.30.254.74:8080/my_file/", "zhangcaoyanzi/video.mp4", "test.mp4");
        httpDownloader.startDownLoad();
    }


    private void initVideoPath() {

        if(ContextCompat.checkSelfPermission(VideoAcitivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "WRITE_EXTERNAL_STORAGE");
            ActivityCompat.requestPermissions(VideoAcitivity.this, new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

            return;
        }

        Log.d(TAG, "initVideoPath");

        Uri mUri;
        mUri = Uri.parse(Environment.getExternalStorageDirectory() + "/Movies/test.mp4");
        //设置视频控制器
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(mUri);
        videoView.requestFocus();

        //开始播放视频
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });
    }

}
