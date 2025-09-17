package com.jxdx.resource.Questions
import android.util.Log
import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp

class ErrorQuizRepository {
    private val service: ErrorQuizApi by lazy {
        HttpManager.instance.service(ErrorQuizApi::class.java)
    }

    suspend fun getErrorQuestions(
        subjectId: Int,
        page: Int,
        size: Int
    ): BaseResp<ErrorQuizData> {
        val base=service.getErrorQuestions(subjectId, page, size)
        Log.d("获取","${base}")
        return base
    }
}