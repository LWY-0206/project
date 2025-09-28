package com.jxdx.classroom.activity

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.SessionState
import com.example.corekit.common.BaseActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.jxdx.classroom.databinding.ClassActivityBinding


class ClassActivity : BaseActivity<ClassActivityBinding>() {
    //这个是日志标签，用来观察的
    private val TAG = "RTMPLiveViewer"
    // 这个用来控制直播播放
    private var currentSession: FFmpegSession? = null
    private var isPlaying = false
    private lateinit var player: ExoPlayer
    private lateinit var viewModel: ClassDetailsViewModel

    
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

//    private fun handleSessionComplete(session: FFmpegSession) {
//        runOnUiThread {
//            isPlaying = false
//
//            when {
//                ReturnCode.isSuccess(session.returnCode) ->
//                    Toast.makeText(this, "直播已结束（主播已下播）", Toast.LENGTH_SHORT).show()
//                ReturnCode.isCancel(session.returnCode) ->
//                    Toast.makeText(this, "直播播放已取消", Toast.LENGTH_SHORT).show()
//                else -> {
//                    val errorMsg = session.failStackTrace ?: "未知错误（网络断开或地址无效）"
//                    val exitCode = session.returnCode?.value ?: -1
//                    val output = session.output
//                    Toast.makeText(this, "直播播放失败: $errorMsg", Toast.LENGTH_LONG).show()
//                    Log.e(TAG, String.format("命令执行失败，状态: %s, 退出码: %s. %s\n输出: %s",
//                            session.state, session.returnCode, errorMsg, output))
//                }
//            }
//        }
//    }

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