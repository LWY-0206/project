// ScheduleViewModel.kt
package com.jxdx.resource.Schedule

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request

class ScheduleViewModel(application: Application): BaseViewModel(application) {
    private val repository: ScheduleRepository by lazy{
        ScheduleRepository()
    }

    val scheduleLiveData: ResLiveData<List<ScheduleItem>> by lazy {
        ResLiveData()
    }
    fun getScheduleList(week: String, weekday: String = "") {
        Log.d("ScheduleViewModel", "getScheduleList 获取中... week=$week, weekday=$weekday")
        request(scheduleLiveData, object : LiveDataCallback<List<ScheduleItem>, List<ScheduleItem>> {
            override fun success(
                emit: ResLiveData<List<ScheduleItem>>,
                msg: String?,
                data: List<ScheduleItem>?
            ) {
                Log.d("ScheduleViewModel", "getScheduleList 获取成功 with data: ${data?.size} 条记录")
                data?.let {
                    emit.success(it)
                    // 同时处理并发送按天分组的数据
                }
            }

            override fun otherCode(
                emit: ResLiveData<List<ScheduleItem>>,
                code: Int?,
                msg: String?,
                data: List<ScheduleItem>?
            ) {
                Log.e("ScheduleViewModel", "其他错误 code: $code, msg: $msg")
            }

            override fun error(
                emit: ResLiveData<List<ScheduleItem>>,
                e: ErrorResponse
            ) {
                Log.e("ScheduleViewModel", "请求错误: ${e.message}")
                emit.error(e)
            }
        }){
            repository.getScheduleList(week, weekday)
        }
    }

    // 获取当前周的课表
}