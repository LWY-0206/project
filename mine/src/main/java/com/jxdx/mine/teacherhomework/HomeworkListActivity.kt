package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jxdx.mine.HomeworkDetail
import com.jxdx.mine.StudentSubmission
import com.jxdx.mine.databinding.ActivityHomeworkListBinding
import com.jxdx.mine.teacherhomework.adapter.HomeworkAdapter

class HomeworkListActivity: AppCompatActivity() {
    private lateinit var binding: ActivityHomeworkListBinding
    private lateinit var homeworkAdapter: HomeworkAdapter
    private val homeworkList = mutableListOf<HomeworkDetail>()
    private var courseId: String? = null
    private var courseName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeworkListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取从CourseListActivity传递过来的课程信息
        courseId = intent.getStringExtra("courseId")
        courseName = intent.getStringExtra("courseName")
        
        supportActionBar?.title = "$courseName - 作业列表"

        initRecyclerView()
        loadHomework()
    }

    private fun initRecyclerView() {
        homeworkAdapter = HomeworkAdapter(homeworkList) { homework ->
            val intent = Intent(this, HomeworkDetailActivity::class.java)
            intent.putExtra("homeworkId", homework.id)
            intent.putExtra("homeworkTitle", homework.title)
            startActivity(intent)
        }
        binding.recyclerViewHomework.apply {
            layoutManager = LinearLayoutManager(this@HomeworkListActivity)
            adapter = homeworkAdapter
        }
    }

    private fun loadHomework() {
        // 模拟数据
        homeworkList.clear()
        homeworkList.add(
            HomeworkDetail(
                id = "h1",
                title = "数学第一章函数作业",
                description = "完成教材第15页练习题1-10题，要求写出详细解题步骤",
                dueDate = "2025-10-10",
                submissions = mutableListOf(
                    StudentSubmission("s1", "张三", "答案内容：根据函数定义，f(x) = 2x + 1...", null, null, false),
                    StudentSubmission("s2", "李四", "https://example.com/answer1.jpg", 90, "解题步骤清晰，答案正确", true),
                    StudentSubmission("s3", "王五", "答案内容：我是这样理解这道题的...", null, null, false)
                )
            )
        )
        homeworkList.add(
            HomeworkDetail(
                id = "h2",
                title = "数学第二章极限作业",
                description = "完成教材第32页练习题1-8题，重点掌握极限的计算方法",
                dueDate = "2025-10-15",
                submissions = mutableListOf(
                    StudentSubmission("s1", "张三", "https://example.com/limit_answer1.jpg", null, null, false),
                    StudentSubmission("s2", "李四", "", null, null, false),
                    StudentSubmission("s3", "王五", "https://example.com/limit_answer2.jpg", 85, "计算准确，但可以尝试用更简洁的方法", true)
                )
            )
        )
        homeworkList.add(
            HomeworkDetail(
                id = "h3",
                title = "数学第三章导数作业",
                description = "完成教材第50页练习题1-12题，包括导数的定义和基本求导法则",
                dueDate = "2025-10-20",
                submissions = mutableListOf()
            )
        )
        homeworkAdapter.notifyDataSetChanged()
    }
}