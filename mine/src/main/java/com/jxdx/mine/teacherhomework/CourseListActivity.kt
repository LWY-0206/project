package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.SubjectsVO
import com.jxdx.classroom.http.RetrofitClient
import com.jxdx.mine.CourseDetail
import com.jxdx.mine.databinding.ActivityCourseListBinding
import com.jxdx.mine.teacherhomework.adapter.CourseAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        // 显示加载状态
        binding.loadingLayout.visibility = android.view.View.VISIBLE
        binding.recyclerViewCourses.visibility = android.view.View.GONE
        binding.errorLayout.visibility = android.view.View.GONE
        
        // 调用老师学科列表接口
        RetrofitClient.apiService.getTeacherSubject()
            .enqueue(object : Callback<BaseResp<List<SubjectsVO>>> {
                override fun onResponse(
                    call: Call<BaseResp<List<SubjectsVO>>>,
                    response: Response<BaseResp<List<SubjectsVO>>>
                ) {
                    binding.loadingLayout.visibility = android.view.View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        val baseResp = response.body()
                        if (baseResp?.code == 0) {
                            val subjects = baseResp.data
                            if (subjects != null && subjects.isNotEmpty()) {
                                // 成功获取数据
                                courses.clear()
                                
                                // 将SubjectsVO转换为CourseDetail
                                subjects.forEachIndexed { index, subject ->
                                    courses.add(CourseDetail(
                                        subjectName = subject.subjectName ?: "未知科目",
                                        subjectId = subject.subjectId ?: index + 1,
                                        teacherName = "", // 接口未提供教师姓名
                                        teacherId = 0, // 接口未提供教师ID
                                        uploadTime = "", // 接口未提供上传时间
                                        file = emptyMap() // 接口未提供文件列表
                                    ))
                                }
                            }
                            
                            // 通知适配器数据已更改
                            courseAdapter.notifyDataSetChanged()
                            
                            // 显示RecyclerView
                            binding.recyclerViewCourses.visibility = android.view.View.VISIBLE
                        } else {
                            // 服务器返回错误或数据为空
                            showError("获取学科列表失败：${baseResp?.message ?: "未知错误"}")
                        }
                    } else {
                        // 请求失败
                        showError("网络请求失败，请检查网络连接")
                    }
                }
                
                override fun onFailure(call: Call<BaseResp<List<SubjectsVO>>>?, t: Throwable?) {
                    binding.loadingLayout.visibility = android.view.View.GONE
                    showError("网络请求失败：${t?.message ?: "未知错误"}")
                    Log.e("CourseListActivity", "Load courses failed", t)
                    
                    // 显示模拟数据作为备选
                    showMockData()
                }
            })
    }
    
    private fun showError(message: String) {
        binding.errorMessage.text = message
        binding.errorLayout.visibility = android.view.View.VISIBLE
        binding.retryButton.setOnClickListener {
            loadCourses() // 重试加载
        }
    }
    
    private fun showMockData() {
        // 模拟数据，仅在API调用失败时使用
        courses.clear()
        courses.add(CourseDetail(
            subjectName = "高等数学（微积分）",
            subjectId = 1,
            teacherName = "张明教授",
            teacherId = 2,
            uploadTime = "2025-10-10",
            file = mapOf(
                "第一章 函数、极限与连续课件" to "https://example.com/calculus1.pdf",
                "第二章 导数与微分课件" to "https://example.com/calculus2.pdf"
            )
        ))
        courses.add(CourseDetail(
            subjectName = "线性代数",
            subjectId = 2,
            teacherName = "李华教授",
            teacherId = 3,
            uploadTime = "2025-10-08",
            file = mapOf(
                "矩阵与行列式基础" to "https://example.com/linear1.pdf"
            )
        ))
        courses.add(CourseDetail(
            subjectName = "大学物理（力学）",
            subjectId = 3,
            teacherName = "王强教授",
            teacherId = 4,
            uploadTime = "2025-10-05",
            file = mapOf(
                "牛顿力学基础" to "https://example.com/physics1.pdf"
            )
        ))
        courseAdapter.notifyDataSetChanged()
        binding.recyclerViewCourses.visibility = android.view.View.VISIBLE
    }
}