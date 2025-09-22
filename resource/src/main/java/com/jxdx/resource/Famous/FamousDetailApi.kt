package com.jxdx.resource.Famous

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FamousDetailApi {
    @GET("/api/celebrity/detail")
    suspend fun getFamousDetail(
        @Query("celebrityId") celebrityId: Int,
        @Header("satoken") satoken: String = "f646b77d-257e-4fd2-aaa7-e13263932d50"
    ): BaseResp<FamousDetail>


}