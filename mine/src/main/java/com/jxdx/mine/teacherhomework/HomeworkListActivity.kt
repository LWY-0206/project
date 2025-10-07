package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.HomeworkDetail
import com.jxdx.mine.PageData
import com.jxdx.mine.StudentSubmission
import com.jxdx.mine.databinding.ActivityHomeworkListBinding
import com.jxdx.mine.http.ApiService
import com.jxdx.mine.http.CreateHomeworkRequest
import com.jxdx.mine.http.RetrofitClient
import com.jxdx.mine.http.TeachCreateHWSimpleVO
import com.jxdx.mine.teacherhomework.adapter.HomeworkAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeworkListActivity: AppCompatActivity() {
    private lateinit var binding: ActivityHomeworkListBinding
    private lateinit var homeworkAdapter: HomeworkAdapter
    private val homeworkList = mutableListOf<HomeworkDetail>()
    private var courseId: String? = null
    private var courseName: String? = null
    private var currentPage = 1
    private val pageSize = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeworkListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取从CourseListActivity传递过来的课程信息
        courseId = intent.getStringExtra("courseId")
        courseName = intent.getStringExtra("courseName")
        
        supportActionBar?.title = "$courseName - 作业列表"

        initRecyclerView()
        initCreateHomeworkButton()
        loadHomework()
    }
    
    private fun initCreateHomeworkButton() {
        binding.fabCreateHomework.setOnClickListener {
            // 调用创建作业API
            createHomework()
        }
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
        // 显示加载状态
        // 调用API获取作业列表 (@GET /api/teach/homework/create/list)
        RetrofitClient.apiService.getTeacherHomeworkList(
            page = currentPage,
            size = pageSize
        ).enqueue(object : Callback<BaseResp<PageData<TeachCreateHWSimpleVO>>> {
            override fun onResponse(call: Call<BaseResp<PageData<TeachCreateHWSimpleVO>>>, response: Response<BaseResp<PageData<TeachCreateHWSimpleVO>>>) {
                
                if (response.isSuccessful && response.body()?.code == 200) {
                    val data = response.body()?.data
                    data?.records?.let { records ->
                        homeworkList.clear()
                        // 将API返回的数据转换为HomeworkDetail对象
                        records.forEach { hw ->
                            homeworkList.add(
                                HomeworkDetail(
                                    id = hw.homeworkId?.toString() ?: "",
                                    title = hw.homeworkName ?: "未命名作业",
                                    description = "", // 详情API获取
                                    dueDate = hw.deadTime ?: "",
                                    submissions = mutableListOf()
                                )
                            )
                        }
                        homeworkAdapter.notifyDataSetChanged()
                    }
                } else {
                    // API调用失败，显示模拟数据
                    showMockData()
                    // 显示错误信息
                    Toast.makeText(this@HomeworkListActivity, "获取作业列表失败: ${response.body()?.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResp<PageData<TeachCreateHWSimpleVO>>>, t: Throwable) {
                Log.e("HomeworkList", "Failed to load homework: ${t.message}")
                // 网络失败，显示模拟数据
                showMockData()
                // 显示错误信息
                Toast.makeText(this@HomeworkListActivity, "网络异常，获取作业列表失败", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun showMockData() {
        // 模拟数据 - 根据课程ID显示不同的大学作业
        homeworkList.clear()
        
        when (courseId) {
            "1" -> {
                // 高等数学（微积分）的作业
                homeworkList.add(
                    HomeworkDetail(
                        id = "h1",
                        title = "函数极限与连续性作业",
                        description = "完成教材第25页练习题1-10题，要求写出详细的解题步骤和计算过程，重点关注极限的ε-δ定义证明。",
                        dueDate = "2025-10-18",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "答案内容：根据函数极限的定义，对于任意ε>0，存在δ=ε/2，当0<|x-2|<δ时...", null, null, false),
                            StudentSubmission("s2", "张伟", "https://example.com/calculus_answer1.jpg", 92, "解题步骤清晰，极限证明方法正确，计算准确", true),
                            StudentSubmission("s3", "李娜", "答案内容：我采用了夹逼定理来求解第5题的极限，过程如下...", null, null, false),
                            StudentSubmission("s4", "王芳", "https://example.com/calculus_answer2.jpg", 85, "整体表现良好，但第7题的连续性证明可以更严谨", true)
                        )
                    )
                )
                homeworkList.add(
                    HomeworkDetail(
                        id = "h2",
                        title = "导数与微分作业",
                        description = "完成教材第48页练习题1-12题，包括导数的定义计算、基本初等函数求导法则、隐函数求导和高阶导数计算。",
                        dueDate = "2025-10-25",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "https://example.com/derivative_answer1.jpg", null, null, false),
                            StudentSubmission("s2", "张伟", "", null, null, false),
                            StudentSubmission("s3", "李娜", "https://example.com/derivative_answer2.jpg", 88, "隐函数求导掌握较好，但高阶导数计算有小错误", true)
                        )
                    )
                )
            }
            "2" -> {
                // 线性代数的作业
                homeworkList.add(
                    HomeworkDetail(
                        id = "h3",
                        title = "矩阵运算与行列式计算作业",
                        description = "完成教材第36页练习题1-8题，包括矩阵的加减乘运算、转置、行列式计算和伴随矩阵求解。",
                        dueDate = "2025-10-20",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "答案内容：矩阵乘法需要注意行列对应，第1题的计算结果为...", null, null, false),
                            StudentSubmission("s2", "张伟", "https://example.com/matrix_answer1.jpg", 90, "行列式计算正确，矩阵运算熟练", true)
                        )
                    )
                )
            }
            "3" -> {
                // 大学物理（力学）的作业
                homeworkList.add(
                    HomeworkDetail(
                        id = "h4",
                        title = "牛顿运动定律应用作业",
                        description = "完成教材第42页练习题1-6题，分析物体受力情况，应用牛顿三大定律解决力学问题，要求画出受力分析图。",
                        dueDate = "2025-10-22",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "https://example.com/physics_answer1.jpg", null, null, false),
                            StudentSubmission("s2", "张伟", "答案内容：根据牛顿第二定律F=ma，对于斜面问题，物体受到重力、支持力和摩擦力...", 95, "受力分析图清晰，解题过程完整正确", true)
                        )
                    )
                )
            }
            "4" -> {
                // 程序设计基础（Java）的作业
                homeworkList.add(
                    HomeworkDetail(
                        id = "h5",
                        title = "Java类与对象编程作业",
                        description = "设计一个学生类(Student)，包含姓名、学号、成绩等属性，以及构造方法、getter/setter方法和显示信息的方法。编写主程序测试该类的功能。",
                        dueDate = "2025-10-28",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "https://example.com/java_code1.zip", null, null, false),
                            StudentSubmission("s2", "张伟", "https://example.com/java_code2.zip", 94, "类设计合理，代码规范，功能完整", true),
                            StudentSubmission("s3", "李娜", "https://example.com/java_code3.zip", 87, "实现了基本功能，但可以增加异常处理机制", true)
                        )
                    )
                )
            }
            "5" -> {
                // 宏观经济学的作业
                homeworkList.add(
                    HomeworkDetail(
                        id = "h6",
                        title = "国民收入决定模型作业",
                        description = "结合IS-LM模型分析财政政策和货币政策对国民收入的影响，并探讨当前经济形势下的政策选择。",
                        dueDate = "2025-10-30",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "答案内容：IS曲线表示产品市场均衡，LM曲线表示货币市场均衡，两者交点决定均衡国民收入...", null, null, false),
                            StudentSubmission("s2", "张伟", "https://example.com/econ_answer1.pdf", 91, "分析深入，理论结合实际，论证充分", true)
                        )
                    )
                )
            }
            else -> {
                // 默认作业（适用于未指定课程）
                homeworkList.add(
                    HomeworkDetail(
                        id = "default1",
                        title = "课程作业",
                        description = "完成相关习题，掌握本章节的核心知识点",
                        dueDate = "2025-10-20",
                        submissions = mutableListOf()
                    )
                )
            }
        }
        homeworkAdapter.notifyDataSetChanged()
    }
    
    private fun createHomework() {
        // 创建一个示例作业请求
        val request = CreateHomeworkRequest(
            subjectId = courseId?.toInt(),
            homeworkName = "新创建的作业",
            homeworkContent = "这是作业内容",
            deadTime = "2025-12-31",
            imageUrls = listOf()
        )
        
        // 调用API创建作业 (@POST /api/teach/homework/create)
        RetrofitClient.apiService.createHomework(request).enqueue(object : Callback<BaseResp<String>> {
            override fun onResponse(call: Call<BaseResp<String>>, response: Response<BaseResp<String>>) {
                if (response.isSuccessful && response.body()?.code == 200) {
                    Toast.makeText(this@HomeworkListActivity, "作业创建成功", Toast.LENGTH_SHORT).show()
                    // 重新加载作业列表
                    loadHomework()
                } else {
                    Toast.makeText(this@HomeworkListActivity, "作业创建失败: ${response.body()?.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResp<String>>, t: Throwable) {
                Log.e("HomeworkList", "Failed to create homework: ${t.message}")
                Toast.makeText(this@HomeworkListActivity, "网络异常，作业创建失败", Toast.LENGTH_SHORT).show()
            }
        })
    }
}