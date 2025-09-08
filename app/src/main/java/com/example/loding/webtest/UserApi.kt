package com.example.loding.webtest

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {
    @GET("test/compute")
    suspend fun getUsers(
        @Query("type") type: Int,
        @Query("a") a: Int,
        @Query("b") b: Int,
    ): BaseResp<UserData>
}
