package com.example.loding.Questions
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
}