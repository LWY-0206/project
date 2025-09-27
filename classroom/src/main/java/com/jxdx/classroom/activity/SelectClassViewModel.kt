package com.jxdx.classroom.activity

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.classroom.entity.SelectClass
import com.jxdx.classroom.response.SelectClassRepository

class SelectClassViewModel(application:Application):BaseViewModel(application) {
    private val repository: SelectClassRepository by lazy {
        Log.d("TAG", ":2")
        SelectClassRepository()
    }
    val selectClassLiveData: ResLiveData<ArrayList<SelectClass>> by lazy {
        ResLiveData()
    }

    fun getSelectClass() {
        request(
            selectClassLiveData,
            object : LiveDataCallback<ArrayList<SelectClass>, ArrayList<SelectClass>> {
                override fun success(
                    emit: ResLiveData<ArrayList<SelectClass>>,
                    msg: String?,
                    data: ArrayList<SelectClass>?
                ) {
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<ArrayList<SelectClass>>,
                    code: Int?,
                    msg: String?,
                    data: ArrayList<SelectClass>?
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(emit: ResLiveData<ArrayList<SelectClass>>, e: ErrorResponse) {
                    emit.error(e, null)
                }
            }
        ) {
            repository.getSelectClass()
        }
    }
}



