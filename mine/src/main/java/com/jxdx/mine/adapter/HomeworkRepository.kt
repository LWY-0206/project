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
    suspend fun getHomeworkByStatus(completeAndCorrect: Int, page: Int = 1, size: Int = 5): List<SubjectGroup>? {
        // 对于待批改和已完成状态，返回模拟数据
        if (completeAndCorrect == 1 || completeAndCorrect == 2) {
            return getMockHomeworkData(completeAndCorrect)
        }
        
        // 只有未提交状态调用真实接口
        try {
            val resp: BaseResp<PageData<Homework>> = 
                RetrofitClient.apiService.getHomework(page, size).await()

            if (resp.code != 0) {
                throw RuntimeException("接口请求失败: ${resp.message}")
            }

            Log.d("HomeworkRepository", "接口请求成功: ${resp.data?.records}")
            // 只保留该状态的数据
            val filteredList = resp.data?.records?.filter { it.completeAndCorrect == completeAndCorrect }

            // 按 subject 分组
            val groupedMap = filteredList?.groupBy { it.subject }

            return groupedMap?.map { (subject, list) ->
                SubjectGroup(
                    subjectName = subject,
                    isExpanded = true,
                    homeworkList = list.toMutableList()
                )
            }
        } catch (e: Exception) {
            Log.e("HomeworkRepository", "接口调用失败，返回模拟数据", e)
            return getMockHomeworkData(completeAndCorrect)
        }
    }
    
    /**
     * 获取模拟作业数据
     */
    private fun getMockHomeworkData(completeAndCorrect: Int): List<SubjectGroup> {
        val mockHomeworkList = mutableListOf<Homework>()
        
        when (completeAndCorrect) {
            1 -> {
                // 待批改的作业
                mockHomeworkList.add(
                    Homework(
                        homeworkId = "hw1",
                        subject = "高等数学",
                        completeAndCorrect = 1,
                        homeworkName = "函数极限与连续性作业",
                        deadTime = "2025-10-18 23:59:59",
                        sendTime = "2025-10-15 09:00:00"
                    )
                )
                mockHomeworkList.add(
                    Homework(
                        homeworkId = "hw2", 
                        subject = "高等数学",
                        completeAndCorrect = 1,
                        homeworkName = "导数与微分作业",
                        deadTime = "2025-10-25 23:59:59",
                        sendTime = "2025-10-20 10:00:00"
                    )
                )
                mockHomeworkList.add(
                    Homework(
                        homeworkId = "hw3",
                        subject = "线性代数", 
                        completeAndCorrect = 1,
                        homeworkName = "矩阵运算与行列式计算作业",
                        deadTime = "2025-10-20 23:59:59",
                        sendTime = "2025-10-17 14:30:00"
                    )
                )
            }
            2 -> {
                // 已完成的作业
                mockHomeworkList.add(
                    Homework(
                        homeworkId = "hw4",
                        subject = "高等数学",
                        completeAndCorrect = 2,
                        homeworkName = "积分计算作业",
                        deadTime = "2025-10-10 23:59:59",
                        sendTime = "2025-10-05 08:00:00"
                    )
                )
                mockHomeworkList.add(
                    Homework(
                        homeworkId = "hw5",
                        subject = "大学物理",
                        completeAndCorrect = 2,
                        homeworkName = "牛顿运动定律应用作业",
                        deadTime = "2025-10-12 23:59:59",
                        sendTime = "2025-10-07 10:30:00"
                    )
                )
            }
        }
        
        // 按科目分组
        val groupedMap = mockHomeworkList.groupBy { it.subject }
        
        return groupedMap.map { (subject, list) ->
            SubjectGroup(
                subjectName = subject,
                isExpanded = true,
                homeworkList = list.toMutableList()
            )
        }
    }
}
