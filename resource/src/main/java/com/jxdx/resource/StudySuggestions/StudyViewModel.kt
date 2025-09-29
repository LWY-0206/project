package com.jxdx.resource.StudySuggestions

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.corekit.common.BaseActivity
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class StudyViewModel(application: Application): BaseViewModel(application) {
    private val repository: StudyRepository by lazy {
        StudyRepository()
    }
   val studyLiveData : ResLiveData<StudyData> by lazy{
       ResLiveData()
   }
    fun getStudyPlan(){
        request(
            studyLiveData,
            object : LiveDataCallback<StudyData, StudyData> {
                override fun success(
                    emit: ResLiveData<StudyData>,
                    msg: String?,
                    data: StudyData?
                ) {
                    data?.let{
                        emit.success(it)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<StudyData>,
                    code: Int?,
                    msg: String?,
                    data: StudyData?
                ){
                    emit.error(ErrorResponse.otherCode(code, msg), data)
                }

                override fun error(
                    emit: ResLiveData<StudyData>,
                    e: ErrorResponse
                ) {
                    emit.error(e, null)
                }
            }
        ){
            repository.generateStudyPlan()
        }
    }
}