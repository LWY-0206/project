package com.example.loding.friend

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.example.loding.entity.Friend

class FriendViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val repository: FriendRepository by lazy {
        Log.d("TAG", ":2")
        FriendRepository()
    }
    val friendLiveData: ResLiveData<List<Friend>> by lazy {
        ResLiveData()
    }

    fun getFriends() {
        request(
            friendLiveData,
            object : LiveDataCallback<List<Friend>, List<Friend>> {
                override fun success(
                    emit: ResLiveData<List<Friend>>,
                    msg: String?,
                    data: List<Friend>?,
                ) {
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<List<Friend>>,
                    code: Int?,
                    msg: String?,
                    data: List<Friend>?,
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<List<Friend>>,
                    e: ErrorResponse,
                ) {
                    emit.error(e, null)
                }
            },
            {
                repository.getFriends()
            },
        )
    }
}
