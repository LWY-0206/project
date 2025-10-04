package com.jxdx.square.friend

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class DeleteFriendViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val friendRepository: FriendRepository by lazy {
        Log.d("DeleteFriendViewModel", "初始化FriendRepository")
        FriendRepository()
    }
    
    val deleteFriendLiveData: ResLiveData<String> by lazy {
        ResLiveData()
    }

    fun deleteFriend(friendId: Int) {
        request(
            deleteFriendLiveData,
            object : LiveDataCallback<String, String> {
                override fun success(
                    emit: ResLiveData<String>,
                    msg: String?,
                    data: String?,
                ) {
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
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<String>,
                    e: ErrorResponse,
                ) {
                    emit.error(e, null)
                }
            },
            {
                friendRepository.deleteFriend(friendId)
            },
        )
    }
}