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
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jxdx.classroom.service.MediaProjectionService;
import com.jxdx.classroom.R;

public class MainActivity extends AppCompatActivity implements MediaProjectionService.ServiceCallbacks {

    // 建议使用本地测试服务器地址或已知可用的RTMP服务器
    // 本地测试推荐使用SRS、nginx-rtmp等搭建本地服务器
//    private final String url = "rtmp://192.168.2.104:1935/live/home";
    // 备用公共测试服务器（仅用于测试，不保证长期可用）
//     private final String url = "rtmp://192.168.2.104:1935/live/home";

    private final String url = "rtmp://121.41.176.238:1935/live/c5a5064a-88f2-449c-9ff3-a2fc72763648?userId=1&liveId=66";    private static final String TAG = "MainActivity";
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private ScreenLive mScreenLive;
    private FFmpegScreenLive mFFmpegScreenLive; // 添加FFmpeg推流实例
    private MediaProjectionService mediaProjectionService;
    private boolean isServiceConnected = false;
    private Intent screenCaptureData; // 保存屏幕捕获Intent数据
    private int screenCaptureResultCode; // 保存屏幕捕获结果码
    private boolean useFFmpegLive = false; // 控制使用哪种推流方式，默认为Native

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
        setContentView(R.layout.activity_main_updated); // 使用新的布局文件

        // 显示RTMP服务器地址
        TextView tvUrl = findViewById(R.id.tv_url);
        tvUrl.setText("RTMP服务器地址：" + url);

        // 设置推流方式选择监听
        RadioGroup radioGroup = findViewById(R.id.radio_group_stream_type);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_ffmpeg_stream) {
                    useFFmpegLive = true;
                    Log.i(TAG, "已切换为FFmpeg推流模式");
                } else {
                    useFFmpegLive = false;
                    Log.i(TAG, "已切换为Native推流模式");
                }
                
                // 如果当前正在推流，提示用户需要重启推流才能生效
                if ((mScreenLive != null || mFFmpegScreenLive != null) && isStreaming()) {
                    updateStreamStatus("提示：请停止并重新开始推流以应用新的推流方式");
                }
            }
        });

        // 先启动媒体投影前台服务
        Intent serviceIntent = new Intent(this, MediaProjectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        // 绑定到服务
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        
        // 初始化MediaProjectionManager
        this.mediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        
        // 绑定按钮点击事件
        findViewById(R.id.btn_start_screen_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScreenCapture(v);
            }
        });
        
        findViewById(R.id.btn_stop_live).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLive(v);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Log.i(TAG, "用户同意屏幕捕获权限");
                // 保存权限结果，等待服务绑定后再开始推流
                this.screenCaptureResultCode = resultCode;
                this.screenCaptureData = data;
                
                // 如果服务已经绑定，立即开始推流
                startLiveIfPossible();
            } else {
                Log.e(TAG, "用户拒绝屏幕捕获权限或操作取消");
            }
        }
    }
    
    @Override
    public void onServiceReady(MediaProjectionService service) {
        this.mediaProjectionService = service;
        Log.i(TAG, "媒体投影服务已准备就绪");
        
        // 服务就绪后，如果已经获取了屏幕捕获权限，立即开始推流
        startLiveIfPossible();
    }
    
    /**
     * 当服务已绑定且屏幕捕获权限已获取时，开始推流
     * 根据useFFmpegLive标志决定使用哪种推流方式
     */
    private void startLiveIfPossible() {
        if (mediaProjectionService != null && screenCaptureData != null) {
            try {
                // 获取MediaProjection
                mediaProjection = mediaProjectionManager.getMediaProjection(screenCaptureResultCode, screenCaptureData);
                if (mediaProjection == null) {
                    Log.e(TAG, "获取MediaProjection失败");
                    return;
                }
                
                // 设置到服务中
                mediaProjectionService.setMediaProjection(mediaProjection);
                
                // 开始直播，根据选择的推流方式使用不同的实现
                if (useFFmpegLive) {
                    // 使用FFmpeg推流
                    mFFmpegScreenLive = new FFmpegScreenLive();
                    // 可选：设置自定义参数
                    mFFmpegScreenLive.setStreamParameters(1280, 720, 30, 2000000);
                    boolean started = mFFmpegScreenLive.startLive(url, mediaProjection);
                    if (started) {
                        Log.i(TAG, "开始FFmpeg推流到地址：" + url);
                        updateStreamStatus("FFmpeg推流已开始");
                    } else {
                        Log.e(TAG, "FFmpeg推流启动失败");
                        updateStreamStatus("FFmpeg推流启动失败");
                    }
                } else {
                    // 使用原生推流
                    mScreenLive = new ScreenLive();
                    mScreenLive.startLive(url, mediaProjection);
                    Log.i(TAG, "开始Native推流到地址：" + url);
                    updateStreamStatus("Native推流已开始");
                }
            } catch (SecurityException e) {
                Log.e(TAG, "开始推流时安全异常", e);
                updateStreamStatus("推流启动失败：安全异常");
            } catch (Exception e) {
                Log.e(TAG, "开始推流时异常", e);
                updateStreamStatus("推流启动失败：" + e.getMessage());
            }
        }
    }

    /**
     * 停止录屏，停止当前正在使用的推流实例
     * @param view
     */
    public void stopLive(View view) {
        Log.i(TAG, "用户触发停止推流");
        // 1. 首先停止ScreenLive或FFmpegScreenLive，确保资源正确释放
        if (useFFmpegLive && mFFmpegScreenLive != null) {
            mFFmpegScreenLive.stopLive();
            mFFmpegScreenLive = null;
        } else if (mScreenLive != null) {
            mScreenLive.stopLive();
            mScreenLive = null;
        }
        
        // 2. 然后停止MediaProjection
        if (mediaProjection != null) {
            try {
                mediaProjection.stop();
                mediaProjection = null;
            } catch (Exception e) {
                Log.e(TAG, "停止MediaProjection异常", e);
            }
        }
        
        // 重置状态，允许重新开始推流
        screenCaptureData = null;
        updateStreamStatus("推流已停止");
    }
    
    /**
     * 开始屏幕捕获（可通过按钮触发）
     * @param view
     */
    public void startScreenCapture(View view) {
        Log.i(TAG, "用户触发开始屏幕捕获");
        if (mediaProjectionManager != null) {
            Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_CAPTURE);
        } else {
            Log.e(TAG, "MediaProjectionManager未初始化");
        }
    }
    
    /**
     * 检查当前是否正在推流
     */
    private boolean isStreaming() {
        if (useFFmpegLive && mFFmpegScreenLive != null) {
            return mFFmpegScreenLive.isStreaming();
        } else if (mScreenLive != null) {
            // 注意：原ScreenLive类没有isStreaming方法，这里假设它有一个isLiving属性或方法
            // 如果没有，可以考虑添加这个方法到ScreenLive类
            return false; // 这里只是一个占位符，实际需要根据ScreenLive的实现来修改
        }
        return false;
    }
    
    /**
     * 更新推流状态显示
     */
    private void updateStreamStatus(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvStatus = findViewById(R.id.tv_stream_status);
                tvStatus.setText("推流状态：" + status);
            }
        });
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
        
        // 确保所有资源都被释放
        if (useFFmpegLive && mFFmpegScreenLive != null) {
            mFFmpegScreenLive.stopLive();
            mFFmpegScreenLive = null;
        } else if (mScreenLive != null) {
            mScreenLive.stopLive();
            mScreenLive = null;
        }
        
        if (mediaProjection != null) {
            try {
                mediaProjection.stop();
                mediaProjection = null;
            } catch (Exception e) {
                Log.e(TAG, "停止MediaProjection异常", e);
            }
        }
        
        screenCaptureData = null;
    }
}
