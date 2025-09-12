package com.example.loding.adapter

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class LikeDynamicViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val dynamicRepository: DynamicRepository by lazy {
        Log.d("TAG", ":2")
        DynamicRepository()
    }

    val likeDynamicLiveData: ResLiveData<Unit> by lazy { ResLiveData() }

    // 修复参数名：从commentId改为postId，与Repository保持一致
    fun likeDynamic(postId: Int) {
        request(
            likeDynamicLiveData,
            object : LiveDataCallback<Unit, Unit> {
                override fun success(
                    emit: ResLiveData<Unit>,
                    msg: String?,
                    data: Unit?,
                ) {
                    // 直接触发成功，不需要检查data是否为null
                    // 因为后端返回就是null，这小bug，哈哈哈
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
            { dynamicRepository.likeDynamic(postId) },
        )
    }
}
