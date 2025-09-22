package com.jxdx.mine.course

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jxdx.mine.Course

class CourseViewModel : ViewModel() {
    val courseList = MutableLiveData<List<Course>>()

    fun loadCourses() {
        // 模拟数据
        courseList.value = listOf(
            Course("1", "数学", "李老师", "8/12"),
            Course("2", "英语", "王老师", "5/10"),
            Course("3", "物理", "张老师", "3/8")
        )
    }
}