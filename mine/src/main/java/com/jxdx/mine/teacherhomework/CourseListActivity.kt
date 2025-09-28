package com.jxdx.mine.teacherhomework

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.widget.CommonToolbar
import com.jxdx.mine.CourseDetail
import com.jxdx.mine.databinding.ActivityCourseListBinding
import com.jxdx.mine.teacherhomework.adapter.CourseAdapter

class CourseListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCourseListBinding
    private lateinit var courseAdapter: CourseAdapter
    private val courses = mutableListOf<CourseDetail>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        loadCourses()
    }

    private fun initRecyclerView() {
        courseAdapter = CourseAdapter(courses) { course ->
            val intent = Intent(this, HomeworkListActivity::class.java)
            intent.putExtra("courseId", course.subjectId.toString())
            intent.putExtra("courseName", course.subjectName)
            startActivity(intent)
        }
        binding.recyclerViewCourses.apply {
            layoutManager = LinearLayoutManager(this@CourseListActivity)
            adapter = courseAdapter
        }
    }

    private fun loadCourses() {
        // 模拟数据 - 大学课程
        courses.clear()
        courses.add(CourseDetail(
            subjectName = "高等数学（微积分）",
            subjectId = 1,
            teacherName = "张明教授",
            teacherId = 2,
            uploadTime = "2025-10-10",
            file = mapOf(
                "第一章 函数、极限与连续课件" to "https://example.com/calculus1.pdf",
                "第二章 导数与微分课件" to "https://example.com/calculus2.pdf",
                "第一章习题解答" to "https://example.com/calculus_ex1.pdf"
            )
        ))
        courses.add(CourseDetail(
            subjectName = "线性代数",
            subjectId = 2,
            teacherName = "李华教授",
            teacherId = 3,
            uploadTime = "2025-10-08",
            file = mapOf(
                "矩阵与行列式基础" to "https://example.com/linear1.pdf",
                "线性方程组求解" to "https://example.com/linear2.pdf"
            )
        ))
        courses.add(CourseDetail(
            subjectName = "大学物理（力学）",
            subjectId = 3,
            teacherName = "王强教授",
            teacherId = 4,
            uploadTime = "2025-10-05",
            file = mapOf(
                "牛顿力学基础" to "https://example.com/physics1.pdf",
                "相对论基础" to "https://example.com/physics2.pdf"
            )
        ))
        courses.add(CourseDetail(
            subjectName = "程序设计基础（Java）",
            subjectId = 4,
            teacherName = "陈静副教授",
            teacherId = 5,
            uploadTime = "2025-10-03",
            file = mapOf(
                "Java语法基础" to "https://example.com/java1.pdf",
                "面向对象编程" to "https://example.com/java2.pdf",
                "实验指导书" to "https://example.com/java_lab.pdf"
            )
        ))
        courses.add(CourseDetail(
            subjectName = "宏观经济学",
            subjectId = 5,
            teacherName = "赵伟教授",
            teacherId = 6,
            uploadTime = "2025-10-01",
            file = mapOf(
                "国民收入决定理论" to "https://example.com/econ1.pdf",
                "宏观经济政策分析" to "https://example.com/econ2.pdf"
            )
        ))
        courseAdapter.notifyDataSetChanged()
    }
}