package com.jxdx.classroom.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.jxdx.classroom.service.MediaProjectionService;
import com.jxdx.classroom.R;
import com.jxdx.classroom.widget.FloatingBallManager;

public class MainActivity extends AppCompatActivity implements MediaProjectionService.ServiceCallbacks, OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {

    // 建议使用本地测试服务器地址或已知可用的RTMP服务器
    // 本地测试推荐使用SRS、nginx-rtmp等搭建本地服务器
//    private final String url = "rtmp://192.168.2.104:1935/live/home";
    // 备用公共测试服务器（仅用于测试，不保证长期可用）
//     private final String url = "rtmp://192.168.2.104:1935/live/home";

    private final String baseUrl = "rtmp://121.41.176.238:1935/live/";
    private String url = "rtmp://121.41.176.238:1935/live/c8264c57-ddab-4430-acfe-5ab43ad9866a?userId=1&liveId=71";
    private static final String TAG = "MainActivity";
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private ScreenLive mScreenLive;
    private FFmpegScreenLive mFFmpegScreenLive; // 添加FFmpeg推流实例
    private MediaProjectionService mediaProjectionService;
    private boolean isServiceConnected = false;
    private Intent screenCaptureData; // 保存屏幕捕获Intent数据
    private int screenCaptureResultCode; // 保存屏幕捕获结果码
    private boolean useFFmpegLive = false; // 控制使用哪种推流方式，默认为Native
    private boolean isStreamKeyReady = false; // 推流码是否已获取
    private StreamKeyHelper streamKeyHelper; // 推流码获取助手
    private int liveId = 0; // 接收传递的liveId
    private FloatingBallManager floatingBallManager; // 悬浮球管理器

    // PDF相关变量
    private PDFView pdfView;
    private TextView tvPdfPlaceholder;
    private LinearLayout loadingLayout;
    private LinearLayout errorLayout;
    private TextView tvLoadingText;
    private TextView tvErrorMessage;
    private Button btnRetry;
    private Uri pdfUri;
    private String pdfFileName = "未选择文件";
    private int currentPage = 0;
    private int totalPages = 0;

    private final int REQUEST_CODE_SCREEN_CAPTURE = 100;

    // 文件选择结果处理
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) {
                    pdfUri = uri;
                    displayPdfFromUri(uri);
                }
            }
        }
    );

    // 权限请求处理
    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(),
        isGranted -> {
            if (isGranted) {
                launchFilePicker();
            } else {
                Toast.makeText(this, "需要存储权限来选择PDF文件", Toast.LENGTH_SHORT).show();
            }
        }
    );

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

        // 接收传递的参数
        String subjectName = "";
        if (getIntent() != null) {
            subjectName = getIntent().getStringExtra("subjectName");
            this.liveId = getIntent().getIntExtra("liveId", 0);
            Log.d(TAG, "接收到subjectName: " + subjectName + ", liveId: " + this.liveId);
        }

        // 初始化PDF相关组件
        initPdfViews();
        
        // 更新标题显示subjectName
        TextView titleView = findViewById(R.id.title);
        if (subjectName != null && !subjectName.isEmpty()) {
            titleView.setText(subjectName);
        }
        
        // 记录当前屏幕方向
        Log.d(TAG, "MainActivity创建完成 - 屏幕方向: " + 
            (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? "横屏" : "竖屏"));


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
        
        // 初始化推流码获取助手
        streamKeyHelper = new StreamKeyHelper(this);
        streamKeyHelper.init();
        
        // 初始化悬浮球管理器
        floatingBallManager = new FloatingBallManager(this);
        
        // 绑定按钮点击事件
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        findViewById(R.id.btn_select_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndPickFile();
            }
        });
        
        findViewById(R.id.btn_start_live).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScreenCapture(v);
            }
        });
        
        findViewById(R.id.btn_end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLive(v);
            }
        });
        
        // 功能按钮
        findViewById(R.id.btn_function).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFloatingBall();
            }
        });
        
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFloatingBall();
            }
        });
        
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pdfUri != null) {
                    displayPdfFromUri(pdfUri);
                } else {
                    checkPermissionAndPickFile();
                }
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
        
        // 如果有liveId，先获取推流码
        if (liveId > 0) {
            Log.d(TAG, "开始获取推流码，liveId: " + liveId);
            streamKeyHelper.getStreamKey(liveId, new kotlin.jvm.functions.Function1<String, kotlin.Unit>() {
                @Override
                public kotlin.Unit invoke(String streamKey) {
                    // 更新推流地址
                    if (streamKey != null && !streamKey.isEmpty()) {
                        url = baseUrl + streamKey;
                        Log.d(TAG, "更新推流地址: " + url);
                        
                        updateStreamStatus("推流地址已更新，准备开始推流");
                        
                        // 推流地址更新后，开始屏幕捕获
                        startScreenCaptureInternal();
                    } else {
                        Log.w(TAG, "获取推流码失败，使用默认地址");
                        // 即使获取推流码失败，也要开始推流
                        startScreenCaptureInternal();
                    }
                    return kotlin.Unit.INSTANCE;
                }
            });
        } else {
            // 没有liveId，直接开始屏幕捕获
            startScreenCaptureInternal();
        }
    }
    
    /**
     * 内部开始屏幕捕获方法
     */
    private void startScreenCaptureInternal() {
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
                // 状态信息通过日志输出，不再显示在界面上
                Log.i(TAG, "推流状态：" + status);
                // 可以通过Toast显示重要状态信息
                if (status.contains("失败") || status.contains("错误")) {
                    Toast.makeText(MainActivity.this, "推流状态：" + status, Toast.LENGTH_SHORT).show();
                }
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
        
        // 隐藏悬浮球
        hideFloatingBall();
    }
    
    /**
     * 显示悬浮球
     */
    private void showFloatingBall() {
        if (floatingBallManager != null) {
            // 使用liveId作为roomId
            String roomId = String.valueOf(this.liveId);
            floatingBallManager.showFloatingBall(roomId);
        }
    }
    
    /**
     * 隐藏悬浮球
     */
    private void hideFloatingBall() {
        if (floatingBallManager != null) {
            floatingBallManager.hideFloatingBall();
        }
    }
    
    /**
     * 初始化PDF相关视图
     */
    private void initPdfViews() {
        pdfView = findViewById(R.id.pdfView);
        tvPdfPlaceholder = findViewById(R.id.tv_pdf_placeholder);
        loadingLayout = findViewById(R.id.loading_layout);
        errorLayout = findViewById(R.id.error_layout);
        tvLoadingText = findViewById(R.id.tv_loading_text);
        tvErrorMessage = findViewById(R.id.tv_error_message);
        btnRetry = findViewById(R.id.btn_retry);
        
        // 设置PDFView背景色
        pdfView.setBackgroundColor(Color.LTGRAY);
        
        // 为横屏模式优化PDFView设置
        setupPdfViewForLandscape();
        
        // 显示默认提示
        showPdfPlaceholder();
    }
    
    /**
     * 为横屏模式优化PDFView设置
     */
    private void setupPdfViewForLandscape() {
        // 确保PDFView能够正确处理横屏布局
        pdfView.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT
        ));
        
        // 设置最小尺寸，确保在横屏模式下有足够的显示空间
        pdfView.setMinimumWidth(400);
        pdfView.setMinimumHeight(300);
        
        // 添加调试信息
        Log.d(TAG, "PDFView设置完成 - 当前屏幕方向: " + 
            (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? "横屏" : "竖屏"));
    }
    
    /**
     * 检查权限并选择文件
     */
    private void checkPermissionAndPickFile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED) {
            launchFilePicker();
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }
    
    /**
     * 启动文件选择器
     */
    private void launchFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            filePickerLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "未找到文件管理器", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 从URI显示PDF
     */
    private void displayPdfFromUri(Uri uri) {
        showLoading(true);
        hideError();
        
        pdfFileName = getFileName(uri);
        
        Log.d(TAG, "开始加载PDF文件: " + pdfFileName + ", URI: " + uri);
        
        try {
            // 确保PDFView可见
            pdfView.setVisibility(View.VISIBLE);
            
            // 在横屏模式下，使用更适合的页面适配策略
            FitPolicy fitPolicy = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE 
                ? FitPolicy.WIDTH : FitPolicy.BOTH;
            
            pdfView.fromUri(uri)
                .defaultPage(currentPage)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10)
                .onPageError(this)
                .pageFitPolicy(fitPolicy)
                .load();
            Log.d(TAG, "PDF加载请求已发送，适配策略: " + fitPolicy);
        } catch (Exception e) {
            Log.e(TAG, "PDF加载失败", e);
            showError("PDF加载失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件名
     */
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result != null ? result : "未知文件";
    }
    
    /**
     * 显示加载状态
     */
    private void showLoading(boolean show) {
        loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    /**
     * 显示错误信息
     */
    private void showError(String message) {
        errorLayout.setVisibility(View.VISIBLE);
        tvErrorMessage.setText(message);
        showLoading(false);
    }
    
    /**
     * 隐藏错误信息
     */
    private void hideError() {
        errorLayout.setVisibility(View.GONE);
    }
    
    /**
     * 显示PDF占位符
     */
    private void showPdfPlaceholder() {
        tvPdfPlaceholder.setVisibility(View.VISIBLE);
        pdfView.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
    }
    
    // OnPageChangeListener
    @Override
    public void onPageChanged(int page, int pageCount) {
        currentPage = page;
        totalPages = pageCount;
        Log.d(TAG, "页面改变: " + page + " / " + pageCount);
    }
    
    // OnLoadCompleteListener
    @Override
    public void loadComplete(int nbPages) {
        totalPages = nbPages;
        currentPage = 0;
        showLoading(false);
        hideError();
        tvPdfPlaceholder.setVisibility(View.GONE);
        pdfView.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "PDF加载完成，总页数: " + nbPages);
        
        // 在横屏模式下，确保PDF正确显示
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "横屏模式下PDF加载完成，确保正确显示");
            // 延迟一帧确保布局完成
            pdfView.post(new Runnable() {
                @Override
                public void run() {
                    // 强制重新绘制PDFView
                    pdfView.invalidate();
                }
            });
        }
    }
    
    // OnPageErrorListener
    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "无法加载第 " + page + " 页", t);
        showError("无法加载第 " + (page + 1) + " 页");
    }
    
    /**
     * 处理屏幕方向变化
     * 当屏幕方向改变时，重新加载PDF以确保正确显示
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "屏幕方向发生变化");
        
        // 如果当前有PDF正在显示，重新加载以确保在横屏模式下正确渲染
        if (pdfUri != null && pdfView.getVisibility() == View.VISIBLE) {
            Log.d(TAG, "重新加载PDF以适应新的屏幕方向");
            // 延迟重新加载，确保布局已经完成
            pdfView.post(new Runnable() {
                @Override
                public void run() {
                    displayPdfFromUri(pdfUri);
                }
            });
        }
    }
}
