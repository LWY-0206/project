package com.jxdx.mine.adapter

import com.jxdx.mine.homework.Homework
import com.jxdx.mine.homework.SubjectGroup

class HomeworkRepository {

    /**
     * 根据状态获取作业列表
     */
    suspend fun getHomeworkByStatus(status: Int): List<SubjectGroup> {
        // 模拟接口数据 - 创建原始数据
        val allGroups = listOf(
            SubjectGroup(
                subjectName = "数学",
                isExpanded = true,
                homeworkList = mutableListOf(
                    Homework("1", "第5章函数作业", "2025-09-20", "数学", 0),
                    Homework("2", "几何练习题", "2025-09-22", "数学", 1),
                    Homework("3", "代数综合题", "2025-09-28", "数学", 2),
                    Homework("4", "综合题", "2025-09-28", "数学", 2),
                    Homework("5", "选择综合题", "2025-09-28", "数学", 2)
                )
            ),
            SubjectGroup(
                subjectName = "语文",
                isExpanded = false,
                homeworkList = mutableListOf(
                    Homework("1", "古诗文背诵", "2025-09-25", "语文", 0),
                    Homework("2", "作文练习", "2025-09-27", "语文", 1),
                    Homework("3", "阅读理解", "2025-09-23", "语文", 2)
                )
            ),
            SubjectGroup(
                subjectName = "英语",
                isExpanded = true,
                homeworkList = mutableListOf(
                    Homework("1", "单词拼写", "2025-09-26", "英语", 0),
                    Homework("2", "语法练习", "2025-09-29", "英语", 1),
                    Homework("3", "阅读理解", "2025-09-24", "英语", 2)
                )
            )
        )

        // 根据传入的status参数过滤作业
        return allGroups.map { group ->
            // 过滤每个科目组中状态匹配的作业
            val filteredHomeworkList = group.homeworkList.filter { it.status == status }.toMutableList()
            // 只返回包含匹配作业的科目组
            if (filteredHomeworkList.isNotEmpty()) {
                SubjectGroup(
                    subjectName = group.subjectName,
                    isExpanded = true, // 确保过滤后的分组是展开状态
                    homeworkList = filteredHomeworkList
                )
            } else {
                null
            }
        }.filterNotNull() // 移除没有匹配作业的科目组
    }
}
