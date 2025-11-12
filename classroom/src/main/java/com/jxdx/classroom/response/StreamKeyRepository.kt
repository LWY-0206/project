package com.jxdx.classroom.response

import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.http.ApiService

class StreamKeyRepository {
    private val service: ApiService by lazy {
        HttpManager.instance.service(ApiService::class.java)
    }
    
    suspend fun getStreamKey(liveId: Int): BaseResp<String> {
        return service.getStreamKey(TokenManager.getToken() ?: "", liveId)
    }
}
