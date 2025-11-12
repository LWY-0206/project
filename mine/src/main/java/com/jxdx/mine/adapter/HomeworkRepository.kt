package com.jxdx.mine.adapter
import android.util.Log
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.Homework
import com.jxdx.mine.PageData
import com.jxdx.mine.SubjectGroup
import com.jxdx.mine.Course
import com.jxdx.mine.http.RetrofitClient
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HomeworkRepository {

    /**
     * 将 Retrofit 的 Call<T> 转成协程 suspend
     */
    private suspend fun <T> Call<T>.await(): T = suspendCancellableCoroutine { cont ->
        enqueue(object : retrofit2.Callback<T> {
            override fun onResponse(call: Call<T>, response: retrofit2.Response<T>) {
                if (response.isSuccessful && response.body() != null) {
                    cont.resume(response.body()!!)
                } else {
                    cont.resumeWithException(Throwable("HTTP error ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                cont.resumeWithException(t)
            }
        })
    }

    /**
     * 获取作业数据并按 subject 分组
     * @param status 作业状态：0 未提交，1 待批改，2 已完成
     */
    suspend fun getHomeworkByStatus(status: Int, page: Int = 1, size: Int = 5): List<SubjectGroup>? {
        // 输出请求参数日志
        val statusText = when (status) {
            0 -> "未提交"
            1 -> "待批改"
            2 -> "已完成"
            else -> "未知状态"
        }
        Log.d("HomeworkRepository", "请求作业列表 - 状态: $status($statusText), 页码: $page, 每页大小: $size")
        
        // 获取所有课程信息，以获取subjectId
        var subjectId: Int? = null
        try {
            Log.d("HomeworkRepository", "尝试获取所有课程信息以获取subjectId")
            val courseResponse = RetrofitClient.apiService.getAllCourse(TokenManager.getToken() ?: "").await()
            val courseList = courseResponse.data
            if (courseResponse.code == 0 && courseList != null && courseList.isNotEmpty()) {
                subjectId = courseList[0].subjectId
                Log.d("HomeworkRepository", "获取到subjectId: $subjectId, 课程名称: ${courseList[0].subjectName}")
            } else {
                Log.e("HomeworkRepository", "获取课程列表失败或课程列表为空")
            }
        } catch (e: Exception) {
            Log.e("HomeworkRepository", "获取课程列表异常: ${e.message}", e)
        }
        
        // 根据状态调用不同的接口
        val resp: BaseResp<PageData<Homework>> = try {
            when (status) {
                0 -> {
                    Log.d("HomeworkRepository", "调用未提交作业接口: getHomework, subjectId: $subjectId")
                    RetrofitClient.apiService.getHomework(subjectId, page, size).await()
                }
                1 -> {
                    Log.d("HomeworkRepository", "调用待批改作业接口: getHomeworkCmpl, subjectId: $subjectId")
                    RetrofitClient.apiService.getHomeworkCmpl(subjectId, page, size).await()
                }
                2 -> {
                    Log.d("HomeworkRepository", "调用已完成作业接口: getHomeworkCmplcor, subjectId: $subjectId")
                    RetrofitClient.apiService.getHomeworkCmplcor(subjectId, page, size).await()
                }
                else -> throw IllegalArgumentException("不支持的作业状态: $status")
            }
        } catch (e: Exception) {
            Log.e("HomeworkRepository", "作业接口调用失败: ${e.message}", e)
            throw e
        }

        // 输出接口返回日志
        Log.d("HomeworkRepository", "作业接口返回结果 - code: ${resp.code}, message: ${resp.message}")
        
        if (resp.code != 0) {
            Log.e("HomeworkRepository", "接口请求失败: code=${resp.code}, message=${resp.message}")
            throw RuntimeException("接口请求失败: ${resp.message}")
        }

        // 输出返回数据详情
        val totalRecords = resp.data?.total ?: 0
        val currentRecords = resp.data?.records?.size ?: 0
        Log.d("HomeworkRepository", "接口请求成功 - 总条数: $totalRecords, 当前页条数: $currentRecords")
        Log.d("HomeworkRepository", "接口返回数据: ${resp.data?.records}")
        // 按 subject 分组
        val groupedMap = resp.data?.records?.groupBy { it.subject }

        return groupedMap?.map { (subject, list) ->
            SubjectGroup(
                subjectName = subject,
                isExpanded = true,
                homeworkList = list.toMutableList()
            )
        }
    }
}
