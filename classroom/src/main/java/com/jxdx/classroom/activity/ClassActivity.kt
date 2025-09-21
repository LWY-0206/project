package com.jxdx.classroom.activity

import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder

import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import com.example.corekit.common.BaseActivity
import com.jxdx.mylibrary.databinding.ClassActivityBinding
import com.arthenica.ffmpegkit.*

class ClassActivity : BaseActivity<ClassActivityBinding>(), SurfaceHolder.Callback {
    //这个是日志标签，用来观察的
    private val TAG = "RTMPLiveViewer"
    //这个是SurfaceHolder对象，用于显示直播画面
    private var surfaceHolder: SurfaceHolder? = null
    // 这个用来控制直播播放
    private var currentSession: FFmpegSession? = null

    private var isPlaying = false
    private var isSurfaceReady = false
    //地址：这个测试的地址是可以放的啊
    private val rtmpUrl = "rtmp://liteavapp.qcloud.com/live/liteavdemoplayerstreamid"
    //加载布局，初始化视图
    override fun bindLayout(): ClassActivityBinding {
        return ClassActivityBinding.inflate(layoutInflater)
    }
    //初始化视图获取 SurfaceHolder 并设置回调监听
    override fun initView() {
        surfaceHolder = view.coursewareContainer.holder
        surfaceHolder?.addCallback(this)
    }

    override fun subscribeUi() {}
    //启动直播播放
    private fun startRtmpPlay() {
        val surface = surfaceHolder?.surface ?: return
        if (!surface.isValid) {
            Log.e(TAG, "Surface 未准备就绪")
            return
        }

        // 正确的命令参数列表：确保各选项与参数格式正确，无多余字符
        val ffmpegCommand = "-i $rtmpUrl -fflags nobuffer+fastseek -vcodec copy -acodec copy -f android_view_surface ${surface.javaClass.name}@${surface.hashCode()}"


        Log.d(TAG, "执行命令：${ffmpegCommand}")

        // 异步执行命令
        currentSession = FFmpegKit.executeAsync(
            ffmpegCommand,
            { session -> handleSessionComplete(session) },
            { log -> Log.d(TAG, "FFmpeg 日志：${log.message}") },
            null
        )
    }

    private fun handleSessionComplete(session: FFmpegSession) {
        runOnUiThread {
            isPlaying = false

            when {
                ReturnCode.isSuccess(session.returnCode) ->
                    Toast.makeText(this, "直播已结束（主播已下播）", Toast.LENGTH_SHORT).show()
                ReturnCode.isCancel(session.returnCode) ->
                    Toast.makeText(this, "直播播放已取消", Toast.LENGTH_SHORT).show()
                else -> {
                    val errorMsg = session.failStackTrace ?: "未知错误（网络断开或地址无效）"
                    val exitCode = session.returnCode?.value ?: -1
                    val output = session.output
                    Toast.makeText(this, "直播播放失败: $errorMsg", Toast.LENGTH_LONG).show()
                    Log.e(TAG, String.format("命令执行失败，状态: %s, 退出码: %s. %s\n输出: %s",
                            session.state, session.returnCode, errorMsg, output))
                }
            }
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

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "直播画面载体已创建")
        isSurfaceReady = true
        // 自动开始拉流播放
        startRtmpPlay()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "直播画面载体已销毁")
        stopPlay()
        isSurfaceReady = false
        surfaceHolder?.removeCallback(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlay()
    }
}