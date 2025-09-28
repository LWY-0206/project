package com.jxdx.classroom.activity

import android.app.Application
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.classroom.entity.CreateLiveRoomRequest
import com.jxdx.classroom.entity.CreateLiveRoomResponse
import com.jxdx.classroom.response.CreateLiveRoomRepository

class CreateLiveRoomViewModel(application: Application): BaseViewModel(application) {
    private val repository: CreateLiveRoomRepository by lazy {
        CreateLiveRoomRepository()
    }
    
    val createLiveRoomData: ResLiveData<Int> by lazy {
        ResLiveData()
    }
    
    fun createLiveRoom(
        subjectId: Int,
        roomName: String,
        classIds: List<Int>
    ) {
        val request = CreateLiveRoomRequest(
            subjectId = subjectId,
            roomName = roomName,
            description = "",
            startTime = "",
            classIds = classIds
        )
        
        // 添加请求参数日志
        android.util.Log.d("CreateLiveRoomViewModel", "创建直播房间请求参数：")
        android.util.Log.d("CreateLiveRoomViewModel", "subjectId: $subjectId")
        android.util.Log.d("CreateLiveRoomViewModel", "roomName: $roomName")
        android.util.Log.d("CreateLiveRoomViewModel", "classIds: $classIds")
        
        request(
            createLiveRoomData,
            object : LiveDataCallback<Int, Int> {
                override fun success(
                    emit: ResLiveData<Int>,
                    msg: String?,
                    data: Int?
                ) {
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<Int>,
                    code: Int?,
                    msg: String?,
                    data: Int?
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(emit: ResLiveData<Int>, e: ErrorResponse) {
                    emit.error(e, null)
                }
            }
        ) {
            repository.createLiveRoom(request)
        }
    }
}
