// ScheduleRepository.kt
package com.jxdx.resource.Schedule

import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp

class ScheduleRepository {
    private val service: ScheduleApi by lazy {
        HttpManager.instance.service(ScheduleApi::class.java)
    }
    suspend fun getScheduleList(week: String, weekday: String = ""): BaseResp<List<ScheduleItem>> {
        return service.getSchedule(week, weekday)
    }
}