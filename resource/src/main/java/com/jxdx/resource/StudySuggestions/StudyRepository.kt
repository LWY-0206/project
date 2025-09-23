package com.jxdx.resource.StudySuggestions

import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp

class StudyRepository {
    private val service: StudyApi by lazy {
        HttpManager.instance.service(StudyApi::class.java)
    }
    suspend fun generateStudyPlan(): BaseResp<StudyData> {
        return service.generateStudyPlan()
    }
}