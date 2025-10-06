package com.jxdx.mine.course

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.Course
import com.jxdx.mine.http.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CourseViewModel : ViewModel() {
    var courseList = MutableLiveData<List<Course>?>()

    fun loadCourses() {
        RetrofitClient.apiService.getAllCourse()
            .enqueue(object : Callback<BaseResp<List<Course>>> {
                override fun onResponse(
                    call: Call<BaseResp<List<Course>>?>,
                    response: Response<BaseResp<List<Course>>?>?
                ) {
                    if (response != null && response.isSuccessful) {
                        val baseResp = response.body()
                        Log.d("CourseViewModel", "onResponse: " + baseResp?.data)
                        if (baseResp?.code == 0) {
                            courseList.value = baseResp.data
                        }
                    }
                }

                override fun onFailure(
                    call: Call<BaseResp<List<Course>>?>,
                    t: Throwable
                ) {
                    Log.d("CourseViewModel", "onFailure: " + t.message)
                }
            })
    }
}