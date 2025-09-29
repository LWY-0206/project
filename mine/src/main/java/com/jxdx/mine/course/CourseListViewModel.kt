package com.jxdx.mine.course

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.CourseDetail
import com.jxdx.mine.Courseware
import com.jxdx.mine.StudyReport
import com.jxdx.mine.http.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CourseListViewModel : ViewModel() {
    val courseWareList = MutableLiveData<List<Courseware>?>()
    val courseDetailList = MutableLiveData<List<CourseDetail>?>()
    val studyReport = MutableLiveData<StudyReport>()

    fun loadCourseDetail(subjectId: Int) {
        //获取本学科课件
        RetrofitClient.apiService.getCourseDetail(subjectId)
            .enqueue(object : Callback<BaseResp<CourseDetail>> {

                override fun onResponse(
                    call: Call<BaseResp<CourseDetail>?>,
                    response: Response<BaseResp<CourseDetail>?>?
                ) {
                    
                    if (response != null) {
                        val baseResp = response.body()

                        if (response.isSuccessful && baseResp != null && baseResp.code == 0) {
                            val coursedDetail = baseResp.data
                            if (coursedDetail != null) {
                                Log.d("CourseListViewModel", "Response data: $coursedDetail")//如果有返回数据，则更新MutableLiveData
                                courseDetailList.value = listOf(coursedDetail)                          //保存一个CourseDetail类型的
                                Log.d("CourseListViewModel", "courseDetailList: ${courseDetailList.value}")
                                val fileMap = baseResp.data?.file                                       //CourseDetail中的file为Map提出来单独保存
                                Log.d("CourseListViewModel", "File map: $fileMap")
                                if (fileMap != null && fileMap.isNotEmpty()) {
                                    val coursewares = fileMap.map {
                                        Courseware(
                                            CoursewareName = it.key,
                                            url = it.value
                                        )
                                    }
                                    courseWareList.value = coursewares                                  //保存一个Courseware类型的
                                }
                            }
                        } else {
                            Log.d("CourseListViewModel", "Response not successful or code != 0")
                        }
                    } else {
                        Log.d("CourseListViewModel", "Response is null")
                    }
                }

                override fun onFailure(
                    call: Call<BaseResp<CourseDetail>?>,
                    t: Throwable
                ) {
                    Log.d("CourseListViewModel", "onFailure: ${t.message}")
                    t.printStackTrace()
                }
            })


        // 模拟学习报告
        studyReport.value = StudyReport(
            totalStudyTime = "12小时",
            completedHomework = 6,
            totalHomework = 8,
            averageScore = 85
        )
    }
}