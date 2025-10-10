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
        Log.d("CourseListViewModel", "开始加载课程详情，subjectId: $subjectId")
        //获取本学科课件
        RetrofitClient.apiService.getCourseDetail(subjectId)
            .enqueue(object : Callback<BaseResp<List<CourseDetail>>> {

                override fun onResponse(
                    call: Call<BaseResp<List<CourseDetail>>?>,
                    response: Response<BaseResp<List<CourseDetail>>?>?
                ) {
                    Log.d("CourseListViewModel", "响应状态: ${response?.code()}, 是否成功: ${response?.isSuccessful}")
                    
                    if (response != null) {
                        val baseResp = response.body()
                        Log.d("CourseListViewModel", "响应体: $baseResp")

                        if (response.isSuccessful && baseResp != null) {
                            Log.d("CourseListViewModel", "响应码: ${baseResp.code}, 响应消息: ${baseResp.message}")
                            
                            if (baseResp.code == 0) {
                                val detailList = baseResp.data
                                Log.d("CourseListViewModel", "课程详情列表数据: $detailList")
                                Log.d("CourseListViewModel", "课程详情列表长度: ${detailList?.size}")
                                
                                if (detailList != null) {
                                    //更新课程详情列表
                                    courseDetailList.value = detailList
                                    Log.d("CourseListViewModel", "更新后的courseDetailList: ${courseDetailList.value}")
                                
                                    //处理课件数据
                                    processCoursewareData(detailList)
                                }
                                    else {
                                        Log.w("CourseListViewModel", "课程详情列表数据为空")
                                        // 设置空列表以触发Observer
                                        courseWareList.value = emptyList()
                                        courseDetailList.value = emptyList()
                                    }
                            } else {
                                Log.w("CourseListViewModel", "API返回错误码: ${baseResp.code}, 错误消息: ${baseResp.message}")
                                // API返回错误码，设置空列表
                                courseWareList.value = emptyList()
                                courseDetailList.value = emptyList()
                            }
                        } else {
                            Log.e("CourseListViewModel", "响应不成功或响应体为空")
                            // 设置空列表以触发Observer
                            courseWareList.value = emptyList()
                            courseDetailList.value = emptyList()
                        }
                    } else {
                        Log.e("CourseListViewModel", "Response对象为空")
                        // Response为空，设置空列表以触发Observer
                        courseWareList.value = emptyList()
                        courseDetailList.value = emptyList()
                    }
                }

                override fun onFailure(
                    call: Call<BaseResp<List<CourseDetail>>?>,
                    t: Throwable
                ) {
                    Log.e("CourseListViewModel", "请求失败: ${t.message}", t)
                    // API调用失败，设置空列表以触发Observer
                    courseWareList.value = emptyList()
                    courseDetailList.value = emptyList()
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
    
    /**
     * 处理课件数据
     */
    private fun processCoursewareData(detailList: List<CourseDetail>?) {
        val allCoursewares = mutableListOf<Courseware>()
        
        if (detailList != null && detailList.isNotEmpty()) {
            for (courseDetail in detailList) {
                val fileMap = courseDetail.file
                Log.d("CourseListViewModel", "课程${courseDetail.subjectName}的文件map: $fileMap")
                
                if (fileMap != null && fileMap.isNotEmpty()) {
                    val coursewares = fileMap.map {
                        Courseware(
                            CoursewareName = it.key,
                            url = it.value
                        )
                    }
                    allCoursewares.addAll(coursewares)
                }
            }
        }
        
        Log.d("CourseListViewModel", "处理后的课件总数: ${allCoursewares.size}")
        courseWareList.value = if (allCoursewares.isNotEmpty()) allCoursewares else emptyList()
    }
}