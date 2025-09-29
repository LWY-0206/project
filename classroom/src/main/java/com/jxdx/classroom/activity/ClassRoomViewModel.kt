package com.jxdx.classroom.activity

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.classroom.entity.ClassLive
import com.jxdx.classroom.response.ClassEnterRepository

class ClassRoomViewModel(application: Application): BaseViewModel(application) {
    private val repository: ClassEnterRepository by lazy {
        Log.d("TAG",":2")
        ClassEnterRepository()
    }
    val classLiveData:ResLiveData<ArrayList<ClassLive>> by lazy{
        ResLiveData() }
    fun getClassRoom(){
        request(
            classLiveData,
            object : LiveDataCallback<ArrayList<ClassLive>,ArrayList<ClassLive>> {
                override fun success(
                    emit: ResLiveData<ArrayList<ClassLive>>,
                    msg: String?,
                    data: ArrayList<ClassLive>?
                ) {
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<ArrayList<ClassLive>>,
                    code: Int?,
                    msg: String?,
                    data: ArrayList<ClassLive>?
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(emit: ResLiveData<ArrayList<ClassLive>>, e: ErrorResponse) {
                    emit.error(e, null)
                }
            }
        ) {
            repository.getClassRoom()
        }
    }
}