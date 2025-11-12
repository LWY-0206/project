package com.jxdx.classroom.activity

import android.app.Application
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.classroom.response.StreamKeyRepository

class StreamKeyViewModel(application: Application): BaseViewModel(application) {
    private val repository: StreamKeyRepository by lazy {
        StreamKeyRepository()
    }
    
    val streamKeyData: ResLiveData<String> by lazy {
        ResLiveData()
    }
    
    fun getStreamKey(liveId: Int) {
        request(
            streamKeyData,
            object : LiveDataCallback<String, String> {
                override fun success(
                    emit: ResLiveData<String>,
                    msg: String?,
                    data: String?
                ) {
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<String>,
                    code: Int?,
                    msg: String?,
                    data: String?
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(emit: ResLiveData<String>, e: ErrorResponse) {
                    emit.error(e, null)
                }
            }
        ) {
            repository.getStreamKey(liveId)
        }
    }
}
