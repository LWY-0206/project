package com.jxdx.square.friend

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.square.entity.ApplicationMessage

class FriendMessageViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val repository: FriendRepository by lazy {
        Log.d("TAG", ":2")
        FriendRepository()
    }

    val applicationLiveData: ResLiveData<List<ApplicationMessage>> by lazy { ResLiveData() }
    val acceptFriendLiveData: ResLiveData<String> by lazy { ResLiveData() }
    val rejectFriendLiveData: ResLiveData<String> by lazy { ResLiveData() }

    fun getApplications() {
        request(
            applicationLiveData,
            object : LiveDataCallback<List<ApplicationMessage>, List<ApplicationMessage>> {
                override fun success(
                    emit: ResLiveData<List<ApplicationMessage>>,
                    msg: String?,
                    data: List<ApplicationMessage>?,
                ) {
                    data?.let {
                        emit.success(ArrayList(it))
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<List<ApplicationMessage>>,
                    code: Int?,
                    msg: String?,
                    data: List<ApplicationMessage>?,
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<List<ApplicationMessage>>,
                    e: ErrorResponse,
                ) {
                    emit.error(e, null)
                }
            },
            {
                repository.getApplications()
            },
        )
    }

    fun acceptFriend(applicationId: Int) {
        request(
            acceptFriendLiveData,
            object : LiveDataCallback<String, String> {
                override fun success(
                    emit: ResLiveData<String>,
                    msg: String?,
                    data: String?,
                ) {
                    emit.success(data ?: "接受成功")
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
        ) {
            repository.acceptFriend(applicationId)
        }
    }

    fun rejectFriend(applicationId: Int) {
        request(
            rejectFriendLiveData,
            object : LiveDataCallback<String, String> {
                override fun success(
                    emit: ResLiveData<String>,
                    msg: String?,
                    data: String?,
                ) {
                    emit.success(data ?: "拒绝成功")
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
        ) {
            repository.rejectFriend(applicationId)
        }
    }
}
