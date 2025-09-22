package com.jxdx.classroom.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.jxdx.classroom.service.MediaProjectionService;
import com.jxdx.classroom.R;

public class MainActivity extends AppCompatActivity implements MediaProjectionService.ServiceCallbacks {

//    private final String url = "rtmp://192.168.2.104:1935/live/home";
    private final String url = "rtmp://121.41.176.238:1935/live/77630d7d-79bb-4ea4-9064-3f1e97ff8897?userId=6&liveId=14";

    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private ScreenLive mScreenLive;
    private MediaProjectionService mediaProjectionService;
    private boolean isServiceConnected = false;

    private final int REQUEST_CODE_SCREEN_CAPTURE = 100;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 服务连接成功，通过Binder获取服务实例
            MediaProjectionService.LocalBinder binder = (MediaProjectionService.LocalBinder) service;
            mediaProjectionService = binder.getService();
            isServiceConnected = true;
            mediaProjectionService.setCallbacks(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 服务断开连接
            isServiceConnected = false;
            mediaProjectionService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 先启动媒体投影前台服务
        Intent serviceIntent = new Intent(this, MediaProjectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        // 绑定到服务
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        
        // 请求开始录屏
        this.mediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_CAPTURE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑服务
        if (isServiceConnected) {
            unbindService(serviceConnection);
            isServiceConnected = false;
        }
        // 停止服务
        Intent serviceIntent = new Intent(this, MediaProjectionService.class);
        stopService(serviceIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE && resultCode == Activity.RESULT_OK) {
            // 用户同意开始采集屏幕
            try {
                // 使用媒体投影服务获取MediaProjection
                if (mediaProjectionService != null) {
                    mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
                    mediaProjectionService.setMediaProjection(mediaProjection);
                    
                    // 开始直播
                    mScreenLive = new ScreenLive();
                    mScreenLive.startLive(url, mediaProjection);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onServiceReady(MediaProjectionService service) {
        this.mediaProjectionService = service;
    }

    /**
     * 停止录屏
     * @param view
     */
    public void stopLive(View view) {
        if (mediaProjection != null){
            this.mediaProjection.stop();
        }
    }
    public void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_CAPTURE);
    }
}