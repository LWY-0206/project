package com.jxdx.mine.course

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.AllCourse
import com.jxdx.mine.http.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CourseViewModel : ViewModel() {
    var courseList = MutableLiveData<List<AllCourse>?>()

    fun loadCourses() {
        RetrofitClient.apiService.getAllCourse()
            .enqueue(object : Callback<BaseResp<List<AllCourse>>> {
                override fun onResponse(
                    call: Call<BaseResp<List<AllCourse>>?>,
                    response: Response<BaseResp<List<AllCourse>>?>?
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
                    call: Call<BaseResp<List<AllCourse>>?>,
                    t: Throwable
                ) {
                    Log.d("CourseViewModel", "onFailure: " + t.message)
                }
            })
    }
}