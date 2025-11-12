package com.jxdx.classroom.activity

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.jxdx.classroom.activity.StreamKeyViewModel

class StreamKeyHelper(private val activity: Activity) {
    private lateinit var viewModel: StreamKeyViewModel
    private var onStreamKeyReceived: ((String) -> Unit)? = null
    
    fun init() {
        viewModel = ViewModelProvider(activity as androidx.lifecycle.ViewModelStoreOwner)[StreamKeyViewModel::class.java]
        
        // 观察推流码获取结果
        viewModel.streamKeyData.observe(activity as androidx.lifecycle.LifecycleOwner) { result ->
            result.onSuccess { streamKey ->
                Log.d("StreamKeyHelper", "获取推流码成功: $streamKey")
                onStreamKeyReceived?.invoke(streamKey ?: "")
            }
            result.onError { error, _ ->
                Log.e("StreamKeyHelper", "获取推流码失败: $error")
                // 使用默认推流码
                onStreamKeyReceived?.invoke("")
            }
        }
    }
    
    fun getStreamKey(liveId: Int, callback: (String) -> Unit) {
        onStreamKeyReceived = callback
        viewModel.getStreamKey(liveId)
    }
}
