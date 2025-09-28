package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.StudentSubmission
import com.jxdx.mine.databinding.ActivityHomeworkDetailBinding
import com.jxdx.mine.teacherhomework.adapter.StudentSubmissionAdapter

class HomeworkDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeworkDetailBinding
    private lateinit var adapter: StudentSubmissionAdapter
    private val submissions = mutableListOf<StudentSubmission>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeworkDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val homeworkTitle = intent.getStringExtra("homeworkTitle")
        val homeworkId = intent.getStringExtra("homeworkId")
        
        supportActionBar?.title = "$homeworkTitle - 学生作业"
        
        // 显示作业详情
        binding.tvHomeworkTitle.text = homeworkTitle
        binding.tvHomeworkDescription.text = "这是作业的详细描述内容，包含了作业要求和评分标准。"
        binding.tvHomeworkDueDate.text = "截止日期：2025-10-10"

        initRecyclerView()
        loadSubmissions()
    }

    private fun initRecyclerView() {
        adapter = StudentSubmissionAdapter(submissions) { submission ->
            val intent = Intent(this, ReviewHomeworkActivity::class.java)
            intent.putExtra("studentName", submission.studentName)
            intent.putExtra("content", submission.content)
            intent.putExtra("score", submission.score ?: -1)
            intent.putExtra("comment", submission.comment ?: "")
            startActivityForResult(intent, 1001)
        }
        binding.recyclerViewSubmissions.apply {
            layoutManager = LinearLayoutManager(this@HomeworkDetailActivity)
            adapter = this@HomeworkDetailActivity.adapter
        }
    }

    private fun loadSubmissions() {
        // 模拟数据
        submissions.clear()
        submissions.add(StudentSubmission("s1", "张三", "答案内容：根据牛顿第二定律F=ma，当物体受到外力作用时...", null, null, false))
        submissions.add(StudentSubmission("s2", "李四", "https://example.com/answer2.jpg", 95, "解题思路清晰，计算准确", true))
        submissions.add(StudentSubmission("s3", "王五", "答案内容：我认为这个问题应该从能量守恒的角度来分析...", 88, "分析合理，但有小错误", true))
        submissions.add(StudentSubmission("s4", "赵六", "", null, null, false))
        adapter.notifyDataSetChanged()
    }
}