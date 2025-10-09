package Schedule.FirstPage

import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ScheduleApi {
    @GET("/api/student/courses/combined")
    suspend fun getSchedule(
        @Query("week") week: String,
        @Query("weekday") weekday: String,
        @Header("satoken") satoken: String = TokenManager.getToken().toString()
    ): BaseResp<List<ScheduleItem>>
}
