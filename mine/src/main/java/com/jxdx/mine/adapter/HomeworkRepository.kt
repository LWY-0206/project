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
        // 调用接口
        val resp: BaseResp<PageData<Homework>> =
            RetrofitClient.apiService.getHomework(page, size).await()

        if (resp.code != 0) {
            throw RuntimeException("接口请求失败: ${resp.message}")
        }

        Log.d("HomeworkRepository", "接口请求成功: ${resp.data?.records}")
        // 只保留该状态的数据
        val filteredList = resp.data?.records?.filter { it.status == status }

        // 按 subject 分组
        val groupedMap = filteredList?.groupBy { it.subject }

        return groupedMap?.map { (subject, list) ->
            SubjectGroup(
                subjectName = subject,
                isExpanded = true,
                homeworkList = list.toMutableList()
            )
        }
    }
}
//class HomeworkRepository {
//        // 模拟接口数据 - 创建原始数据
//        val allGroups = listOf(
//            SubjectGroup(
//                subjectName = "数学",
//                isExpanded = true,
//                homeworkList = mutableListOf(
//                    Homework("1", "第5章函数作业", "2025-09-20", "数学", 0),
//                    Homework("2", "几何练习题", "2025-09-22", "数学", 1),
//                    Homework("3", "代数综合题", "2025-09-28", "数学", 2),
//                    Homework("4", "综合题", "2025-09-28", "数学", 2),
//                    Homework("5", "选择综合题", "2025-09-28", "数学", 2)
//                )
//            ),
//            SubjectGroup(
//                subjectName = "语文",
//                isExpanded = false,
//                homeworkList = mutableListOf(
//                    Homework("1", "古诗文背诵", "2025-09-25", "语文", 0),
//                    Homework("2", "作文练习", "2025-09-27", "语文", 1),
//                    Homework("3", "阅读理解", "2025-09-23", "语文", 2)
//                )
//            ),
//            SubjectGroup(
//                subjectName = "英语",
//                isExpanded = true,
//                homeworkList = mutableListOf(
//                    Homework("1", "单词拼写", "2025-09-26", "英语", 0),
//                    Homework("2", "语法练习", "2025-09-29", "英语", 1),
//                    Homework("3", "阅读理解", "2025-09-24", "英语", 2)
//                )
//            )
//        )
//
//        // 根据传入的status参数过滤作业
//        return allGroups.map { group ->
//            // 过滤每个科目组中状态匹配的作业
//            val filteredHomeworkList = group.homeworkList.filter { it.status == status }.toMutableList()
//            // 只返回包含匹配作业的科目组
//            if (filteredHomeworkList.isNotEmpty()) {
//                SubjectGroup(
//                    subjectName = group.subjectName,
//                    isExpanded = true, // 确保过滤后的分组是展开状态
//                    homeworkList = filteredHomeworkList
//                )
//            } else {
//                null
//            }
//        }.filterNotNull() // 移除没有匹配作业的科目组
//    }
//}
