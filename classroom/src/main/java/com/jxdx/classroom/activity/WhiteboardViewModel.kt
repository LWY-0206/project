package com.jxdx.classroom.activity

import android.app.Application
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.classroom.repository.WhiteboardRepository

class WhiteboardViewModel(application: Application) : BaseViewModel(application) {
    private val repository: WhiteboardRepository by lazy {
        WhiteboardRepository()
    }
    
    val uploadLiveData: ResLiveData<List<String>> by lazy { 
        ResLiveData() 
    }

    fun uploadWhiteboardImage(file: String) {
        request(
            uploadLiveData,
            object : LiveDataCallback<List<String>, List<String>> {
                override fun success(
                    emit: ResLiveData<List<String>>,
                    msg: String?,
                    data: List<String>?
                ) {
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<List<String>>,
                    code: Int?,
                    msg: String?,
                    data: List<String>?
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<List<String>>,
                    e: ErrorResponse
                ) {
                    emit.error(e, null)
                }
            }
        ) {
            repository.uploadWhiteboardImage(file)
        }
    }
}
