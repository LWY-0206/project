package com.jxdx.classroom.activity

import android.app.Application
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.classroom.response.ClassUrlRepository
import com.jxdx.classroom.entity.RtmpUrl

class ClassDetailsViewModel(application: Application):BaseViewModel(application) {
    private val repository: ClassUrlRepository by lazy {
        ClassUrlRepository()
    }
    val rtmpUrlLiveData: ResLiveData<RtmpUrl> by lazy {
        ResLiveData()
    }
    fun getRtmpUrl(liveId: Int) {
        request(
            rtmpUrlLiveData,
            object:LiveDataCallback<RtmpUrl,RtmpUrl> {
                override fun success(
                    emit: ResLiveData<RtmpUrl>,
                    msg: String?,
                    data: RtmpUrl?
                ) {
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<RtmpUrl>,
                    code: Int?,
                    msg: String?,
                    data: RtmpUrl?
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(emit: ResLiveData<RtmpUrl>, e: ErrorResponse) {
                    emit.error(e, null)
                }
            }
        ){
            repository.getRtmpUrl(liveId)
        }
    }
}