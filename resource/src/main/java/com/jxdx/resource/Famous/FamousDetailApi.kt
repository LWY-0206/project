package com.jxdx.resource.Famous

import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FamousDetailApi {
    @GET("/api/celebrity/detail")
    suspend fun getFamousDetail(
        @Query("celebrityId") celebrityId: Int,
        @Header("satoken") satoken: String = TokenManager.getToken().toString()
    ): BaseResp<FamousDetail>


}