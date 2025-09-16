package com.example.loding.common

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class CommonViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val repository: CommonRepository by lazy {
        Log.d("TAG", ":2")
        CommonRepository()
    }
    val uploadLiveData: ResLiveData<List<String>> by lazy { ResLiveData() }

    // 专注于文件处理
    fun uploadFile(file: String) {
        request(
            uploadLiveData,
            object : LiveDataCallback<List<String>, List<String>> {
                override fun success(
                    emit: ResLiveData<List<String>>,
                    msg: String?,
                    data: List<String>?,
                ) {
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<List<String>>,
                    code: Int?,
                    msg: String?,
                    data: List<String>?,
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<List<String>>,
                    e: ErrorResponse,
                ) {
                    emit.error(e, null)
                }
            },
            {
                repository.uploadFile(file)
            },
        )
    }
}
