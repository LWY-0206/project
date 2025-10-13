package com.jxdx.resource.Schedule
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ScheduleApi {
    @GET("/api/student/courses/combined")
    suspend fun getSchedule(
        @Query("week") week: String,
        @Query("weekday") weekday: String = "", // 设置默认值为空字符串
        @Header("satoken") satoken: String = TokenManager.getToken().toString()
    ): BaseResp<List<ScheduleItem>>
}