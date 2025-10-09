package com.jxdx.classroom.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.ViewModelProvider
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.SessionState
import com.example.corekit.common.BaseActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.jxdx.classroom.databinding.ClassActivityBinding
import com.jxdx.classroom.group.GroupSeatActivity


class ClassActivity : BaseActivity<ClassActivityBinding>() {
    //这个是日志标签，用来观察的
    private val TAG = "RTMPLiveViewer"
    // 这个用来控制直播播放
    private var currentSession: FFmpegSession? = null
    private var isPlaying = false
    private var subjectId=1
    private lateinit var player: ExoPlayer
    private lateinit var viewModel: ClassDetailsViewModel


    // 面板滑动相关变量
    private var isPanelOpen = false
    private val panelWidth = 400f // 面板宽度，与XML中的translationX值对应
    
    // 接收传递的参数
    private var liveId: Int = 0
    
    //地址：这个测试的地址是可以放的啊
    private var rtmpUrl = "rtmp://liteavapp.qcloud.com/live/liteavdemoplayerstreamid"
    //加载布局，初始化视图
    override fun bindLayout(): ClassActivityBinding {
        return ClassActivityBinding.inflate(layoutInflater)
    }
    //初始化视图
    override fun initView() {
        // 接收传递的参数
        liveId = intent.getIntExtra("liveId", 0)
        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[ClassDetailsViewModel::class.java]
        
        // 设置全屏横屏体验
        setupFullscreenLandscape()
        
        // 初始化面板滑动功能
        initSlidePanel()
        
        // 设置返回按钮点击事件
        setupBackButton()
    }

    override fun subscribeUi() {
        // 初始化ExoPlayer
        player = ExoPlayer.Builder(this).build()
        view.coursewareContainer.player = player
        
        // 监听网络请求结果
        viewModel.rtmpUrlLiveData.observe(this) { result ->
            result.onSuccess { rtmpUrlData ->
                if(rtmpUrlData != null){
                    val newUrl = rtmpUrlData.rtmpUrl
                    Log.d(TAG, "收到新的RTMP URL: $newUrl")
                    rtmpUrl = newUrl
                    // 更新播放器URL并重新播放
                    updatePlayerUrl(newUrl)
                }
            }
            result.onError { error, _ ->
                Log.e(TAG, "获取RTMP URL失败: ${error?.message}")
                // 使用默认URL播放
                updatePlayerUrl(rtmpUrl)
            }
        }
        
        // 发起网络请求获取RTMP URL
        viewModel.getRtmpUrl(liveId)
    }
    
    private fun updatePlayerUrl(url: String) {
        Log.d(TAG, "更新播放器URL: $url")
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    /**
     * 设置全屏横屏体验
     */
    private fun setupFullscreenLandscape() {
        // 隐藏状态栏和导航栏，实现沉浸式体验
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 使用新的 WindowInsetsController API
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android 11 以下使用传统方法
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
        
        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * 初始化面板滑动功能
     */
    private fun initSlidePanel() {
        // 设置互动按钮点击监听器
        view.interactionButton.setOnClickListener {
            toggleSlidePanel()
        }
        
        // 设置遮罩层点击监听器
        view.overlay.setOnClickListener {
            closeSlidePanel()
        }
        
        // 设置功能按钮点击监听器
        setupFunctionButtons()
    }
    
    /**
     * 切换面板显示状态
     */
    private fun toggleSlidePanel() {
        if (isPanelOpen) {
            closeSlidePanel()
        } else {
            openSlidePanel()
        }
    }
    
    /**
     * 打开面板
     */
    private fun openSlidePanel() {
        if (isPanelOpen) return
        
        isPanelOpen = true
        
        // 显示面板和遮罩
        view.slidePanel.visibility = View.VISIBLE
        view.overlay.visibility = View.VISIBLE
        
        // 面板滑入动画
        val panelAnimator = ObjectAnimator.ofFloat(view.slidePanel, "translationX", panelWidth, 0f)
        panelAnimator.duration = 300
        panelAnimator.interpolator = AccelerateDecelerateInterpolator()
        
        // 遮罩淡入动画
        val overlayAnimator = ObjectAnimator.ofFloat(view.overlay, "alpha", 0f, 1f)
        overlayAnimator.duration = 300
        
        // 互动按钮旋转动画
        val buttonAnimator = ObjectAnimator.ofFloat(view.interactionButton, "rotation", 0f, 180f)
        buttonAnimator.duration = 300
        
        // 同时执行动画
        panelAnimator.start()
        overlayAnimator.start()
        buttonAnimator.start()
    }
    
    /**
     * 关闭面板
     */
    private fun closeSlidePanel() {
        if (!isPanelOpen) return
        
        isPanelOpen = false
        
        // 面板滑出动画
        val panelAnimator = ObjectAnimator.ofFloat(view.slidePanel, "translationX", 0f, panelWidth)
        panelAnimator.duration = 300
        panelAnimator.interpolator = AccelerateDecelerateInterpolator()
        
        // 遮罩淡出动画
        val overlayAnimator = ObjectAnimator.ofFloat(view.overlay, "alpha", 1f, 0f)
        overlayAnimator.duration = 300
        
        // 互动按钮旋转动画
        val buttonAnimator = ObjectAnimator.ofFloat(view.interactionButton, "rotation", 180f, 0f)
        buttonAnimator.duration = 300
        
        // 动画完成后隐藏面板和遮罩
        panelAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.slidePanel.visibility = View.GONE
                view.overlay.visibility = View.GONE
            }
        })
        
        // 同时执行动画
        panelAnimator.start()
        overlayAnimator.start()
        buttonAnimator.start()
    }
    
    /**
     * 设置返回按钮点击事件
     */
    private fun setupBackButton() {
        view.btnBack.setOnClickListener {
            Log.d(TAG, "返回按钮被点击")
            // 停止播放
            stopPlay()
            // 释放播放器资源
            if (::player.isInitialized) {
                player.release()
            }
            // 关闭当前Activity
            finish()
        }
    }
    
    /**
     * 设置功能按钮点击监听器
     */
    private fun setupFunctionButtons() {
        // 举手按钮
        view.btnRaiseHand.setOnClickListener {
            // TODO: 实现举手功能
            Log.d(TAG, "举手按钮被点击")
        }
        
        // 懂了按钮
        view.btnUnderstood.setOnClickListener {
            // TODO: 实现懂了功能
            Log.d(TAG, "懂了按钮被点击")
        }
        
        // 不懂按钮
        view.btnConfused.setOnClickListener {
            // TODO: 实现不懂功能
            Log.d(TAG, "不懂按钮被点击")
        }
        
        // 小组讨论按钮
        view.btnTaolun.setOnClickListener {
            val intent = Intent(this, GroupSeatActivity::class.java)
            intent.putExtra("subjectid", subjectId.toString())
            startActivity(intent)
            Log.d(TAG, "小组讨论被点击")
        }
        
        // 白板按钮
        view.btnWhiteboard.setOnClickListener {
            Log.d(TAG, "白板按钮被点击")
            // 跳转到白板绘制界面，传递roomId
            val intent = android.content.Intent(this, WhiteboardActivity::class.java)
            intent.putExtra("roomId", liveId.toString()) // 将liveId作为roomId传递
            startActivity(intent)
        }
        
        // 答题按钮
        view.btnAnswerSheet.setOnClickListener {

            Log.d(TAG, "答题按钮被点击")
        }
    }

    private fun stopPlay() {
        isPlaying = false
        currentSession?.let {
            if (it.state == SessionState.RUNNING ) {
                FFmpegKit.cancel(it.sessionId)
            }
        }
        currentSession = null
    }


    override fun onDestroy() {
        super.onDestroy()
        stopPlay()
        // 释放ExoPlayer资源
        if (::player.isInitialized) {
            player.release()
        }
    }
}