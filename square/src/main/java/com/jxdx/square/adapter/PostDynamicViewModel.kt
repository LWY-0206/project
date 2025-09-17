package com.jxdx.square.adapter

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class PostDynamicViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val repository: DynamicRepository by lazy {
        Log.d("TAG", ":2 ")
        DynamicRepository()
    }

    // 需要注意的是这里的泛型不是我们监听的数据，我们监听的是一个Resource<T>类型的
    val postDynamicLiveData: ResLiveData<Unit> by lazy { ResLiveData() }

    fun postDynamic(dynamicBody: DynamicBody) {
        request(
            postDynamicLiveData,
            object : LiveDataCallback<Unit, Unit> {
                override fun success(
                    emit: ResLiveData<Unit>,
                    msg: String?,
                    data: Unit?,
                ) {
                    emit.success(Unit)
                }

                override fun otherCode(
                    emit: ResLiveData<Unit>,
                    code: Int?,
                    msg: String?,
                    data: Unit?,
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
                repository.postDynamic(dynamicBody)
            },
        )
    }
}
