package com.jxdx.resource.Schools

import android.util.Log
import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp

class SchoolDetailRepository {
    private val service: SchoolDetailApi by lazy {
        HttpManager.instance.service(SchoolDetailApi::class.java)
    }

    suspend fun getSchoolDetail(schoolId: Int): BaseResp<SchoolDetail> {
        Log.d("SchoolDetailRepository", "获取学校详情: schoolId=$schoolId")
        return service.getSchoolDetail(schoolId)
    }
}