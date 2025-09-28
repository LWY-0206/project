package com.jxdx.classroom.response

import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.entity.CreateLiveRoomRequest
import com.jxdx.classroom.entity.CreateLiveRoomResponse
import com.jxdx.classroom.http.ApiService

class CreateLiveRoomRepository {
    private val service: ApiService by lazy {
        HttpManager.instance.service(ApiService::class.java)
    }
    
    suspend fun createLiveRoom(request: CreateLiveRoomRequest): BaseResp<Int> {
        // 添加详细的请求日志
        android.util.Log.d("CreateLiveRoomRepository", "开始创建直播房间")
        android.util.Log.d("CreateLiveRoomRepository", "请求参数: $request")
        android.util.Log.d("CreateLiveRoomRepository", "Token: ${TokenManager.getToken()}")
        
        return try {
            val result = service.createLiveRoom(TokenManager.getToken() ?: "", request)
            android.util.Log.d("CreateLiveRoomRepository", "请求成功: $result")
            result
        } catch (e: Exception) {
            android.util.Log.e("CreateLiveRoomRepository", "请求异常: ${e.message}", e)
            throw e
        }
    }
}
