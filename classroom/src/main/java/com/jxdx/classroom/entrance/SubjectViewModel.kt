package com.jxdx.classroom.entrance

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.AllCourse
import com.jxdx.classroom.SubjectsVO
import com.jxdx.classroom.http.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SubjectViewModel: ViewModel() {
    // 修改为更通用的LiveData类型，使其能接收不同接口返回的数据
    val subjectsList= MutableLiveData<List<Any>?>()

    fun getSubject(identity: Int) {
        if (identity == 1) {
            // 老师身份，调用getTeacherSubject接口
            RetrofitClient.apiService.getTeacherSubject()
                .enqueue(object: Callback<BaseResp<List<SubjectsVO>>>{ 
                    override fun onResponse(
                        call: Call<BaseResp<List<SubjectsVO>>?>,
                        response: Response<BaseResp<List<SubjectsVO>>?>?
                    ) {
                        if(response != null && response.isSuccessful){
                            val baseResp = response.body()
                            if (baseResp != null && baseResp.code == 0) {
                                val data = baseResp.data
                                Log.d("SubjectViewModel", "onResponse Teacher Subject:$data")
                                subjectsList.value = data
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<BaseResp<List<SubjectsVO>>?>,
                        t: Throwable
                    ) {
                        Log.d("SubjectViewModel","onFailure Teacher Subject:"+t.message)
                        t.printStackTrace()
                    }
                })
        } else {
            // 学生身份，调用getAllCourse接口
            RetrofitClient.apiService.getAllCourse()
                .enqueue(object: Callback<BaseResp<List<AllCourse>>>{ 
                    override fun onResponse(
                        call: Call<BaseResp<List<AllCourse>>?>,
                        response: Response<BaseResp<List<AllCourse>>?>?
                    ) {
                        if(response != null && response.isSuccessful){
                            val baseResp = response.body()
                            if (baseResp != null && baseResp.code == 0) {
                                val data = baseResp.data
                                Log.d("SubjectViewModel", "onResponse Student Course:$data")
                                subjectsList.value = data
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<BaseResp<List<AllCourse>>?>,
                        t: Throwable
                    ) {
                        Log.d("SubjectViewModel","onFailure Student Course:"+t.message)
                        t.printStackTrace()
                    }
                })
        }
    }
}