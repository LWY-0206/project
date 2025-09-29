package com.jxdx.resource.StudySuggestions

import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Query

interface StudyApi {
    @GET("/api/learning-plan/generate")
    suspend fun generateStudyPlan(
    ): BaseResp<StudyData>
}