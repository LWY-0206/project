package com.jxdx.classroom.entrance

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.AllCourse
import com.jxdx.classroom.http.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SubjectViewModel: ViewModel() {
    val subjectsList= MutableLiveData<List<AllCourse>?>()

    fun getSubject(){
        RetrofitClient.apiService.getAllCourse()
            .enqueue(object: Callback<BaseResp<List<AllCourse>>>{ // 修改泛型类型为List<AllCourse>
                override fun onResponse(
                    call: Call<BaseResp<List<AllCourse>>?>,
                    response: Response<BaseResp<List<AllCourse>>?>?
                ) {
                    if(response != null && response.isSuccessful){
                        val baseResp = response.body()
                        if (baseResp != null && baseResp.code == 0) {
                            val data = baseResp.data
                            Log.d("SubjectViewModel", "onResponse:$data")
                            subjectsList.value = data // 直接赋值，因为data已经是List<AllCourse>类型
                        }
                    }
                }

                override fun onFailure(
                    call: Call<BaseResp<List<AllCourse>>?>,
                    t: Throwable
                ) {
                    Log.d("SubjectViewModel","onFailure:"+t.message)
                    t.printStackTrace()
                }

            })
    }
}