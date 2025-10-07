package com.jxdx.square.friend

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class AddFriendToChatViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val friendRepository: FriendRepository by lazy {
        Log.d("AddFriendToChatViewModel", "初始化FriendRepository")
        FriendRepository()
    }
    
    val addFriendToChatLiveData: ResLiveData<String> by lazy {
        ResLiveData()
    }

    fun addFriendToChat(friendId: Int) {
        Log.d("AddFriendToChatViewModel", "开始添加好友到聊天，friendId: $friendId")
        request(
            addFriendToChatLiveData,
            object : LiveDataCallback<String, String> {
                override fun success(
                    emit: ResLiveData<String>,
                    msg: String?,
                    data: String?,
                ) {
                    Log.d("AddFriendToChatViewModel", "添加好友到聊天成功: $msg")
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<String>,
                    code: Int?,
                    msg: String?,
                    data: String?,
                ) {
                    Log.e("AddFriendToChatViewModel", "添加好友到聊天失败，其他错误码: $code, 消息: $msg")
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<String>,
                    e: ErrorResponse,
                ) {
                    Log.e("AddFriendToChatViewModel", "添加好友到聊天失败", e)
                    emit.error(e, null)
                }
            },
            {
                friendRepository.addFriendToChat(friendId)
            },
        )
    }
}
