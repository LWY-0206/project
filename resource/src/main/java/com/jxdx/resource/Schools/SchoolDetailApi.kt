
package com.jxdx.resource.Schools

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

    interface SchoolDetailApi {
        @GET("/api/school/detail")
        suspend fun getSchoolDetail(
            @Query("schoolId") schoolId: Int,
            @Header("satoken") satoken: String = "f646b77d-257e-4fd2-aaa7-e13263932d50"
        ): BaseResp<SchoolDetail>
    }