
package com.jxdx.resource.Schools

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

    interface SchoolDetailApi {
        @GET("/api/school/detail")
        suspend fun getSchoolDetail(
            @Query("schoolId") schoolId: Int,
            @Header("satoken") satoken: String = "0f6b8b4c-6d39-47dc-8da8-0c6addd377d0"
        ): BaseResp<SchoolDetail>
    }