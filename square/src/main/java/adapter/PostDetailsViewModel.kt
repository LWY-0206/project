package com.example.loding.adapter

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.example.loding.entity.DynamicDetail

class PostDetailsViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val repository: DynamicDetailRepository by lazy {
        Log.d("TAG", ":2 ")
        DynamicDetailRepository()
    }
    val dynamicDetailLiveData: ResLiveData<ArrayList<DynamicDetail>> by lazy { ResLiveData() }

    fun getPostDetails(id: Int) {
        request(
            dynamicDetailLiveData,
            object : LiveDataCallback<ArrayList<DynamicDetail>, DynamicDetail> {
                override fun success(
                    emit: ResLiveData<ArrayList<DynamicDetail>>,
                    msg: String?,
                    data: DynamicDetail?,
                ) {
                    val list = ArrayList<DynamicDetail>()
                    data?.let {
                        list.add(it)
                    }
                    // 即使data为null，也会发射一个空列表而不是null
                    emit.success(list)
                }

                override fun otherCode(
                    emit: ResLiveData<ArrayList<DynamicDetail>>,
                    code: Int?,
                    msg: String?,
                    data: DynamicDetail?,
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<ArrayList<DynamicDetail>>,
                    e: ErrorResponse,
                ) {
                    emit.error(e, null)
                }
            },
            {
                repository.getDPostDetails(id)
            },
        )
    }
}
