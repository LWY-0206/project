package com.jxdx.resource.Famous

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FamousDetailApi {
    @GET("/api/celebrity/detail")
    suspend fun getFamousDetail(
        @Query("celebrityId") celebrityId: Int,
        @Header("satoken") satoken: String = "e4bf8921-b19b-4933-a386-1dc82a5ea501"
    ): BaseResp<FamousDetail>


}