package com.jxdx.classroom.response

import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.com.jxdx.classroom.entity.ClassLive
import com.jxdx.classroom.http.ApiService

class ClassEnterRepository {
    private val service: ApiService by lazy {
        HttpManager.instance.service(ApiService::class.java)
    }

    suspend fun getClassRoom(): BaseResp<ArrayList<ClassLive>> {
        return service.getClassRoom(TokenManager.getToken() ?: "")
    }
}