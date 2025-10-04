// ErrorQuizRepository.kt
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
        val base = service.getErrorQuestions(subjectId, page, size)
        Log.d("获取错题", "${base}")
        return base
    }

    // 恢复或开始练习
    suspend fun resumePractice(
        subjectId: Int,
        questionCount: Int
    ): BaseResp<PracticeSession> {
        val base = service.resumePractice(subjectId, questionCount)
        Log.d("恢复练习", "subjectId: $subjectId, questionCount: $questionCount, response: $base")
        return base
    }

    // 提交答案
    suspend fun submitAnswer(
        sessionId: Int,
        questionId: Int,
        isCorrect: Boolean
    ): BaseResp<SubmitResponse> {
        val base = service.submitAnswer(sessionId, questionId, isCorrect)
        Log.d("提交答案", "sessionId: $sessionId, questionId: $questionId, isCorrect: $isCorrect, response: $base")
        return base
    }
}