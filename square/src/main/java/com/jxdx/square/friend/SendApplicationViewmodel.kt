package com.jxdx.square.friend

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
class SendApplicationViewmodel(
    application: Application,
) : BaseViewModel(application) {
    private val repository: FriendRepository by lazy {
        Log.d("TAG", ":2")
        FriendRepository()
    }
    val sendApplicationLiveData: ResLiveData<Unit> by lazy {
        ResLiveData()
    }

    fun addFriend(applicationBody: ApplicationBody) {
        request(
            sendApplicationLiveData,
            object : LiveDataCallback<Unit, String> {
                override fun success(
                    emit: ResLiveData<Unit>,
                    msg: String?,
                    data: String?,
                ) {
                    emit.success(Unit)
                }

                override fun otherCode(
                    emit: ResLiveData<Unit>,
                    code: Int?,
                    msg: String?,
                    data: String?,
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<Unit>,
                    e: ErrorResponse,
                ) {
                    emit.error(e, null)
                }
            },
            {
                repository.addFriend(applicationBody)
            },
        )
    }
}
