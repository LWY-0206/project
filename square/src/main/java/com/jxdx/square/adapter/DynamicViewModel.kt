package com.jxdx.square.adapter

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.square.entity.Dynamic

class DynamicViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val repository: DynamicRepository by lazy {
        Log.d("TAG", ":2 ")
        DynamicRepository()
    }
    val dynamicLiveData: ResLiveData<ArrayList<Dynamic>> by lazy { ResLiveData() }

    fun getDynamics(
        page: Int? = null,
        size: Int,
    ) {
        request(
            dynamicLiveData,
            object : LiveDataCallback<ArrayList<Dynamic>, List<Dynamic>> {
                override fun success(
                    emit: ResLiveData<ArrayList<Dynamic>>,
                    msg: String?,
                    data: List<Dynamic>?,
                ) {
                    val dynamicList = ArrayList<Dynamic>()
                    data?.let { dynamicList.addAll(it) }
                    emit.success(dynamicList)
                }

                override fun otherCode(
                    emit: ResLiveData<ArrayList<Dynamic>>,
                    code: Int?,
                    msg: String?,
                    data: List<Dynamic>?,
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<ArrayList<Dynamic>>,
                    e: ErrorResponse,
                ) {
                    emit.error(e, null)
                }
            },
            block = { repository.getDynamics(page, size) },
        )
    }
}
