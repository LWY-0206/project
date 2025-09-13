
package com.example.loding.Schools

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

    interface SchoolDetailApi {
        @GET("/api/school/detail")
        suspend fun getSchoolDetail(
            @Query("schoolId") schoolId: Int,
            @Header("satoken") satoken: String = "bc562136-4e77-40b1-a644-50c999ff0b19"
        ): BaseResp<SchoolDetail>
    }