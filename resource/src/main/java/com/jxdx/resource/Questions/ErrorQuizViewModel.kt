// ErrorQuizViewModel.kt
package com.jxdx.resource.Questions
import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class ErrorQuizViewModel(application: Application) : BaseViewModel(application) {
    private val repository: ErrorQuizRepository by lazy {
        Log.d("ErrorQuizViewModel", "Initializing ErrorQuizRepository")
        ErrorQuizRepository()
    }

    val errorQuizLiveData: ResLiveData<ErrorQuizData> by lazy {
        Log.d("ErrorQuizViewModel", "Initializing errorQuizLiveData")
        ResLiveData()
    }

    // 修改为 PracticeSession 类型
    val practiceSessionLiveData: ResLiveData<PracticeSession> by lazy {
        Log.d("ErrorQuizViewModel", "Initializing practiceSessionLiveData")
        ResLiveData()
    }

    val submitQuestionLiveData: ResLiveData<SubmitResponse> by lazy {
        Log.d("ErrorQuizViewModel", "Initializing submitQuestionLiveData")
        ResLiveData()
    }

    fun getErrorQuestions(subjectId: Int, page: Int, size: Int) {
        Log.d("ErrorQuizViewModel", "getErrorQuestions called with subjectId=$subjectId, page=$page, size=$size")

        request(
            errorQuizLiveData,
            object : LiveDataCallback<ErrorQuizData, ErrorQuizData> {
                override fun success(
                    emit: ResLiveData<ErrorQuizData>,
                    msg: String?,
                    data: ErrorQuizData?
                ) {
                    Log.d("ErrorQuizViewModel", "Request success: $msg, data: $data")
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<ErrorQuizData>,
                    code: Int?,
                    msg: String?,
                    data: ErrorQuizData?
                ) {
                    Log.w("ErrorQuizViewModel", "Request otherCode: code=$code, msg=$msg, data=$data")
                    emit.error(ErrorResponse.otherCode(code, msg), data)
                }

                override fun error(
                    emit: ResLiveData<ErrorQuizData>,
                    e: ErrorResponse
                ) {
                    Log.e("ErrorQuizViewModel", "Request error: ${e.message},${emit.data}", e.cause)
                    emit.error(e, null)
                }
            }
        ) {
            Log.d("ErrorQuizViewModel", "Executing repository call")
            repository.getErrorQuestions(subjectId, page, size)
        }
    }

    // 恢复或开始练习
    fun resumePractice(subjectId: Int, questionCount: Int) {
        Log.d("ErrorQuizViewModel", "resumePractice called with subjectId=$subjectId, questionCount=$questionCount")

        request(
            practiceSessionLiveData,
            object : LiveDataCallback<PracticeSession, PracticeSession> {
                override fun success(
                    emit: ResLiveData<PracticeSession>,
                    msg: String?,
                    data: PracticeSession?
                ) {
                    Log.d("ErrorQuizViewModel1", "Practice session request success: $msg, data: $data")
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<PracticeSession>,
                    code: Int?,
                    msg: String?,
                    data: PracticeSession?
                ) {
                    Log.w("ErrorQuizViewModel1", "Practice session request otherCode: code=$code, msg=$msg, data=$data")
                    emit.error(ErrorResponse.otherCode(code, msg), data)
                }

                override fun error(
                    emit: ResLiveData<PracticeSession>,
                    e: ErrorResponse
                ) {
                    Log.e("ErrorQuizViewModel1", "Practice session request error: ${e.message}", e.cause)
                    emit.error(e, null)
                }
            }
        ) {
            Log.d("ErrorQuizViewModel", "Executing resume practice repository call")
            repository.resumePractice(subjectId, questionCount)
        }
    }

    // 提交答案
    fun submitAnswer(sessionId: Int, questionId: Int, isCorrect: Boolean) {
        Log.d("ErrorQuizViewModel2", "submitAnswer called with sessionId=$sessionId, questionId=$questionId, isCorrect=$isCorrect")

        request(
            submitQuestionLiveData,
            object : LiveDataCallback<SubmitResponse, SubmitResponse> {
                override fun success(
                    emit: ResLiveData<SubmitResponse>,
                    msg: String?,
                    data: SubmitResponse?
                ) {
                    Log.d("ErrorQuizViewModel2", "Submit answer success: $msg, data: $data")
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<SubmitResponse>,
                    code: Int?,
                    msg: String?,
                    data: SubmitResponse?
                ) {
                    Log.w("ErrorQuizViewModel2", "Submit answer otherCode: code=$code, msg=$msg, data=$data")
                    emit.error(ErrorResponse.otherCode(code, msg), data)
                }

                override fun error(
                    emit: ResLiveData<SubmitResponse>,
                    e: ErrorResponse
                ) {
                    Log.e("ErrorQuizViewModel2", "Submit answer error: ${e.message}", e.cause)
                    emit.error(e, null)
                }
            }
        ) {
            Log.d("ErrorQuizViewModel2", "Executing submit answer repository call")
            repository.submitAnswer(sessionId, questionId, isCorrect)
        }
    }
}