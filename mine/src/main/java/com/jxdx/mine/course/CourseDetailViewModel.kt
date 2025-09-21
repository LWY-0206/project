package com.jxdx.mine.course

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jxdx.mine.Courseware
import com.jxdx.mine.StudyReport

class CourseDetailViewModel : ViewModel() {
    val coursewareList = MutableLiveData<List<Courseware>>()
    val studyReport = MutableLiveData<StudyReport>()

    fun loadCourseDetail(courseId: String) {
        // 模拟课件数据
        coursewareList.value = listOf(
            Courseware("c1", courseId, "第八章 二次函数.pptx", "2025-09-10 12:30", "http://example.com/1.pptx"),
            Courseware("c2", courseId, "第七章 一次函数.pptx", "2025-09-05 11:20", "http://example.com/2.pptx")
        )

        // 模拟学习报告
        studyReport.value = StudyReport(
            totalStudyTime = "12小时",
            completedHomework = 6,
            totalHomework = 8,
            averageScore = 85
        )
    }
}