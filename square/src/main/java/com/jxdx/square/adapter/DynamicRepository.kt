package com.jxdx.square.adapter

import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.square.entity.Dynamic

class DynamicRepository {
    private val service: DynamicApi by lazy {
        HttpManager.instance.service(DynamicApi::class.java)
    }

    suspend fun getDynamics(
        page: Int? = null,
        size: Int,
    ): BaseResp<List<Dynamic>> = service.getDynamics(TokenManager.getToken().toString(), page, size)

    suspend fun postDynamic(dynamicBody: DynamicBody): BaseResp<Unit> =
        service.postDynamic(TokenManager.getToken().toString(), dynamicBody)

    // 修复参数名：从commentId改为postId，与API接口定义一致
    suspend fun likeDynamic(postId: Int): BaseResp<Unit> = service.likeDynamic(TokenManager.getToken().toString(), postId)
}
