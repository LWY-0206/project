package com.jxdx.resource.FirstPage

import androidx.lifecycle.LiveData
import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp
import com.example.corekit.http.bean.Resource

class ScheduleRepository {
    private val service: ScheduleApi by lazy {
        HttpManager.instance.service(ScheduleApi::class.java)
    }

    suspend fun getScheduleList(week: String,weekday: String): BaseResp<List<ScheduleItem>>? {
        return service.getSchedule(week,weekday)
    }
}
