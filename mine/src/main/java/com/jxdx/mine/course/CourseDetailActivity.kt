package com.jxdx.mine.course

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.viewModels
import com.jxdx.mine.databinding.ActivityCourseDetailBinding

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseDetailBinding
    private lateinit var adapter: CoursewareAdapter
    private val viewModel: CourseDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("courseId") ?: return

        adapter = CoursewareAdapter(
            onPreviewClick = { courseware ->
                Toast.makeText(this, "预览: ${courseware.title}", Toast.LENGTH_SHORT).show()
            },
            onDownloadClick = { courseware ->
                Toast.makeText(this, "下载: ${courseware.title}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerViewCourseware.adapter = adapter
        binding.recyclerViewCourseware.layoutManager = LinearLayoutManager(this)

        // 观察课件列表
        viewModel.coursewareList.observe(this) {
            adapter.submitList(it)
        }

        // 观察学习报告
        viewModel.studyReport.observe(this) {
            binding.tvTotalStudyTime.text = "总学习时长：${it.totalStudyTime}"
            binding.tvHomeworkProgress.text = "已完成作业：${it.completedHomework}/${it.totalHomework}"
            binding.tvAverageScore.text = "平均得分：${it.averageScore}"
        }

        viewModel.loadCourseDetail(courseId)
    }
}
