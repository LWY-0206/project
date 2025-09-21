package com.jxdx.resource.QuizSuggestion

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.resource.Questions.ErrorQuizData

class SuggestionViewModel(application: Application): BaseViewModel(application){
    private val repository: SuggestionRepository by lazy{
        Log.d("SuggestionViewModel","Initializing SuggestionRepository")
        SuggestionRepository()
    }
    val SuggesstionLiveData: ResLiveData<SuggestionData> by lazy{
        Log.d("SuggestionViewModel","Initializing SuggestionLiveData")
        ResLiveData()
    }
    fun getQuizSuggestion(subjectId: Int, questionIds: List<Int>){
        Log.d("ErrorQuizViewModel", "getSuggestion called with subjectId=$subjectId,questionids=$questionIds")

        request(
            SuggesstionLiveData,
            object : LiveDataCallback<SuggestionData, SuggestionData> {
                override fun success(
                    emit: ResLiveData<SuggestionData>,
                    msg: String?,
                    data: SuggestionData?
                ) {
                    Log.d("ErrorQuizViewModel", "Request success: $msg, data: $data")
                    data?.let {
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<SuggestionData>,
                    code: Int?,
                    msg: String?,
                    data: SuggestionData?
                ) {
                    Log.w("ErrorQuizViewModel", "Request otherCode: code=$code, msg=$msg, data=$data")
                    emit.error(ErrorResponse.otherCode(code, msg), data)
                }

                override fun error(
                    emit: ResLiveData<SuggestionData>,
                    e: ErrorResponse
                ) {
                    Log.e("ErrorQuizViewModel", "Request error: ${e.message},${emit.data}", e.cause)
                    emit.error(e, null)
                }
            }
        ) {
            Log.d("ErrorQuizViewModel", "Executing repository call")
            repository.getQuizSuggestion(subjectId, questionIds)
        }
    }
}