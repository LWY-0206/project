package com.jxdx.classroom.response

import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.entity.SelectClass
import com.jxdx.classroom.http.ApiService

class SelectClassRepository {
    private val service: ApiService by lazy {
        HttpManager.instance.service(ApiService::class.java)
    }

    suspend fun getSelectClass(): BaseResp<ArrayList<SelectClass>> {
        return service.getSubject(TokenManager.getToken() ?: "")
    }
}
