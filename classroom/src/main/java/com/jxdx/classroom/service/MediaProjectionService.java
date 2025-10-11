package com.jxdx.classroom.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.jxdx.classroom.R;

public class MediaProjectionService extends Service {
    private static final String TAG = "MediaProjectionService";
    private static final String CHANNEL_ID = "media_projection_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private MediaProjection mediaProjection;
    private ServiceCallbacks callbacks;
    
    // 用于Activity绑定服务的Binder
    private final IBinder binder = new LocalBinder();
    
    public class LocalBinder extends Binder {
        public MediaProjectionService getService() {
            return MediaProjectionService.this;
        }
    }

    public interface ServiceCallbacks {
        void onServiceReady(MediaProjectionService service);
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        this.callbacks = callbacks;
        if (callbacks != null) {
            callbacks.onServiceReady(this);
        }
    }

    public void setMediaProjection(MediaProjection projection) {
        this.mediaProjection = projection;
    }

    public MediaProjection getMediaProjection() {
        return mediaProjection;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MediaProjectionService.onCreate开始");
        try {
            createNotificationChannel();
            Log.d(TAG, "通知渠道创建完成");
            startForeground(NOTIFICATION_ID, createNotification());
            Log.d(TAG, "前台服务启动完成");
        } catch (Exception e) {
            Log.e(TAG, "MediaProjectionService.onCreate异常", e);
            // 即使通知创建失败，也不要让服务崩溃
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "屏幕录制服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("用于支持屏幕录制功能的前台服务");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_media_play) // 使用系统图标，避免资源问题
                    .setContentTitle("正在录制屏幕")
                    .setContentText("正在进行屏幕录制和直播")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOngoing(true);
            
            return builder.build();
        } catch (Exception e) {
            Log.e(TAG, "创建通知失败", e);
            // 返回一个最简单的通知
            return new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .setContentTitle("屏幕录制服务")
                    .setContentText("服务运行中")
                    .build();
        }
    }
}