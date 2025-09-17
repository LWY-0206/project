
package com.jxdx.resource.Schools

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

    interface SchoolDetailApi {
        @GET("/api/school/detail")
        suspend fun getSchoolDetail(
            @Query("schoolId") schoolId: Int,
            @Header("satoken") satoken: String = "363a547e-4f76-4da9-9e56-cff752475e00"
        ): BaseResp<SchoolDetail>
    }