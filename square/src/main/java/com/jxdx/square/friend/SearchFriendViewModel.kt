package com.jxdx.square.friend

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.square.entity.UserInfo

class SearchFriendViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val friendRepository: FriendRepository by lazy {
        Log.d("TAG", ":2")
        FriendRepository()
    }
    val friendDetailsLiveData: ResLiveData<UserInfo> by lazy {
        ResLiveData()
    }

    fun getUserInfo(userId: String) {
        request(
            friendDetailsLiveData,
            object : LiveDataCallback<UserInfo, UserInfo> {
                override fun success(
                    emit: ResLiveData<UserInfo>,
                    msg: String?,
                    data: UserInfo?,
                ) {
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<UserInfo>,
                    code: Int?,
                    msg: String?,
                    data: UserInfo?,
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<UserInfo>,
                    e: ErrorResponse,
                ) {
                    emit.error(e, null)
                }
            },
            {
                friendRepository.getUserInfo(userId)
            },
        )
    }
}
