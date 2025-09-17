package com.jxdx.resource.Schools

import android.util.Log
import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp

class SchoolRepository {
    private val service: SchoolApi by lazy {
        HttpManager.instance.service(SchoolApi::class.java)
    }

    suspend fun getSchoolsByName(
        schoolName: String,
        page: Int,
        size: Int
    ): BaseResp<SchoolData> {
        Log.d("SchoolRepository", "按名称搜索: $schoolName, 页码: $page, 大小: $size")
        return service.getSchoolsByName(schoolName, page, size)
    }

    suspend fun getSchoolsByScore(
        score: Int,
        page: Int,
        size: Int
    ): BaseResp<SchoolData> {
        Log.d("SchoolRepository", "按成绩搜索: $score, 页码: $page, 大小: $size")
        return service.getSchoolsByScore(score, page, size)
    }
}