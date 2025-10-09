package com.jxdx.mine.adapter
import android.util.Log
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.Homework
import com.jxdx.mine.PageData
import com.jxdx.mine.SubjectGroup
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
        // 根据状态调用不同的接口
        val resp: BaseResp<PageData<Homework>> = when (status) {
            0 -> RetrofitClient.apiService.getHomework(null, page, size).await()
            1 -> RetrofitClient.apiService.getHomeworkCmpl(null, page, size).await()
            2 -> RetrofitClient.apiService.getHomeworkCmplcor(null, page, size).await()
            else -> throw IllegalArgumentException("不支持的作业状态: $status")
        }

        if (resp.code != 0) {
            throw RuntimeException("接口请求失败: ${resp.message}")
        }

        Log.d("HomeworkRepository", "接口请求成功: ${resp.data?.records}")
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
