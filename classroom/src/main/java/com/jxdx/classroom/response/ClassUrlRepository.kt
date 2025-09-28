package com.jxdx.classroom.response

import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.entity.RtmpUrl

import com.jxdx.classroom.http.ApiService

class ClassUrlRepository {
    private val service: ApiService by lazy {
        HttpManager.instance.service(ApiService::class.java)
    }

    suspend fun getRtmpUrl(liveId: Int): BaseResp<RtmpUrl> {
        return service.getRtmpUrl(TokenManager.getToken() ?: "",liveId)
    }
}