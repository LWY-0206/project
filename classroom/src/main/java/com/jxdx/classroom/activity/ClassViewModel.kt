package com.jxdx.classroom.activity

import android.app.Application
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.classroom.entity.Classroom
import com.jxdx.classroom.response.ClassRoomRepository

class ClassViewModel(application: Application): BaseViewModel(application) {
    private val repository: ClassRoomRepository by lazy {
        ClassRoomRepository()
    }
    val classroomData: ResLiveData<ArrayList<Classroom>> by lazy{
        ResLiveData()
    }
    fun getClassRoom(subjectId: Int) {
        request(
            classroomData,
            object : LiveDataCallback<ArrayList<Classroom>, ArrayList<Classroom>> {
                override fun success(
                    emit: ResLiveData<ArrayList<Classroom>>,
                    msg: String?,
                    data: ArrayList<Classroom>?
                ) {
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<ArrayList<Classroom>>,
                    code: Int?,
                    msg: String?,
                    data: ArrayList<Classroom>?
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(emit: ResLiveData<ArrayList<Classroom>>, e: ErrorResponse) {
                    emit.error(e, null)
                }
            }
        ){
            repository.getClassRoom(subjectId)
        }
    }
}