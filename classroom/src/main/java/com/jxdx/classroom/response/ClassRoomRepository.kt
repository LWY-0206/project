package com.jxdx.classroom.response

import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.entity.ClassLive
import com.jxdx.classroom.entity.Classroom
import com.jxdx.classroom.http.ApiService

class ClassRoomRepository {
    private val service: ApiService by lazy {
        HttpManager.instance.service(ApiService::class.java)
    }
    suspend fun getClassRoom(
        subjectId: Int
    ): BaseResp<ArrayList<Classroom>> {
        return service.getClassRoom(TokenManager.getToken() ?: "", subjectId)
    }
}