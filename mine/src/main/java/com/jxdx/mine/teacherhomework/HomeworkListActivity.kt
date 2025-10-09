package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import kotlin.math.min
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
    private val pageSize = 20 // 增加每次加载的作业数量
    private var hasMoreData = true // 标记是否还有更多数据
    private var isLoading = false // 标记是否正在加载数据

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
        
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerViewHomework.apply {
            this.layoutManager = layoutManager
            adapter = homeworkAdapter
        }
        
        // 添加滑动监听，实现上拉加载更多
        binding.recyclerViewHomework.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                // 只有向下滑动且没有在加载中且有更多数据时才处理
                if (dy > 0 && !isLoading && hasMoreData) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
                    
                    // 当滑动到最后几个item时，加载更多
                    if (visibleItemCount + pastVisibleItems >= totalItemCount - 3) {
                        loadMoreData()
                    }
                }
            }
        })
    }
    
    // 加载更多数据
    private fun loadMoreData() {
        currentPage++
        loadHomework()
    }

    private fun loadHomework() {
        // 如果正在加载或没有更多数据，则不执行加载
        if (isLoading || !hasMoreData) return
        
        isLoading = true
        
        // 显示加载状态（首次加载时）
        if (currentPage == 1) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBarBottom.visibility = View.VISIBLE
        }
        
        // 调用API获取作业列表 (@GET /api/teach/homework/create/list)
        RetrofitClient.apiService.getTeacherHomeworkList(
            page = currentPage,
            size = pageSize
        ).enqueue(object : Callback<BaseResp<PageData<TeachCreateHWSimpleVO>>> {
            override fun onResponse(call: Call<BaseResp<PageData<TeachCreateHWSimpleVO>>>, response: Response<BaseResp<PageData<TeachCreateHWSimpleVO>>>) {
                isLoading = false
                binding.progressBar.visibility = View.GONE
                binding.progressBarBottom.visibility = View.GONE
                
                if (response.isSuccessful && response.body()?.code == 200) {
                    val data = response.body()?.data
                    data?.records?.let { records ->
                        // 首次加载时清空列表，加载更多时追加
                        if (currentPage == 1) {
                            homeworkList.clear()
                        }
                        
                        // 将API返回的数据转换为HomeworkDetail对象
                        val newHomeworks = mutableListOf<HomeworkDetail>()
                        records.forEach { hw ->
                            newHomeworks.add(
                                HomeworkDetail(
                                    id = hw.homeworkId?.toString() ?: "",
                                    title = hw.homeworkName ?: "未命名作业",
                                    description = "", // 详情API获取
                                    dueDate = hw.deadTime ?: "",
                                    submissions = mutableListOf()
                                )
                            )
                        }
                        
                        // 判断是否还有更多数据
                        hasMoreData = records.size == pageSize
                        
                        // 添加新数据并通知适配器
                        if (newHomeworks.isNotEmpty()) {
                            val startPosition = homeworkList.size
                            homeworkList.addAll(newHomeworks)
                            
                            if (currentPage == 1) {
                                homeworkAdapter.notifyDataSetChanged()
                            } else {
                                homeworkAdapter.notifyItemRangeInserted(startPosition, newHomeworks.size)
                            }
                        }
                        
                        // 如果是首次加载但没有数据，显示空状态
                        if (currentPage == 1 && homeworkList.isEmpty()) {
                            binding.emptyState.visibility = View.VISIBLE
                        } else {
                            binding.emptyState.visibility = View.GONE
                        }
                    }
                } else {
                    // API调用失败，显示模拟数据
                    showMockData()
                    // 显示错误信息
                    Toast.makeText(this@HomeworkListActivity, "获取作业列表失败: ${response.body()?.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResp<PageData<TeachCreateHWSimpleVO>>>, t: Throwable) {
                isLoading = false
                binding.progressBar.visibility = View.GONE
                binding.progressBarBottom.visibility = View.GONE
                
                Log.e("HomeworkList", "Failed to load homework: ${t.message}")
                // 网络失败，显示模拟数据
                showMockData()
                // 显示错误信息
                Toast.makeText(this@HomeworkListActivity, "网络异常，获取作业列表失败", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun showMockData() {
        // 模拟数据 - 根据课程ID和页码显示不同的大学作业
        // 首次加载时清空列表，加载更多时追加
        if (currentPage == 1) {
            homeworkList.clear()
        }
        
        // 为每个课程生成更多的模拟作业数据
        val mockHomeworks = mutableListOf<HomeworkDetail>()
        
        when (courseId) {
            "1" -> {
                // 高等数学（微积分）的作业
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h1_${currentPage}",
                        title = "函数极限与连续性作业",
                        description = "完成教材第25页练习题1-10题，要求写出详细的解题步骤和计算过程，重点关注极限的ε-δ定义证明。",
                        dueDate = "2025-10-18",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "答案内容：根据函数极限的定义，对于任意ε>0，存在δ=ε/2，当0<|x-2|<δ时...", null, null, false),
                            StudentSubmission("s2", "张伟", "https://example.com/calculus_answer1.jpg", 92, "解题步骤清晰，极限证明方法正确，计算准确", true)
                        )
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h2_${currentPage}",
                        title = "导数与微分作业",
                        description = "完成教材第48页练习题1-12题，包括导数的定义计算、基本初等函数求导法则、隐函数求导和高阶导数计算。",
                        dueDate = "2025-10-25",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "https://example.com/derivative_answer1.jpg", null, null, false),
                            StudentSubmission("s2", "张伟", "", null, null, false)
                        )
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h3_${currentPage}",
                        title = "微分中值定理与导数的应用作业",
                        description = "完成教材第65页练习题1-15题，包括罗尔定理、拉格朗日中值定理、柯西中值定理的应用以及洛必达法则的使用。",
                        dueDate = "2025-11-05",
                        submissions = mutableListOf()
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h4_${currentPage}",
                        title = "不定积分作业",
                        description = "完成教材第82页练习题1-20题，熟练掌握基本积分公式、换元积分法和分部积分法。",
                        dueDate = "2025-11-12",
                        submissions = mutableListOf()
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h5_${currentPage}",
                        title = "定积分及其应用作业",
                        description = "完成教材第105页练习题1-18题，理解定积分的概念和性质，掌握牛顿-莱布尼茨公式和定积分的计算。",
                        dueDate = "2025-11-19",
                        submissions = mutableListOf()
                    )
                )
            }
            "2" -> {
                // 线性代数的作业
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h1_${currentPage}",
                        title = "矩阵运算与行列式计算作业",
                        description = "完成教材第36页练习题1-8题，包括矩阵的加减乘运算、转置、行列式计算和伴随矩阵求解。",
                        dueDate = "2025-10-20",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "答案内容：矩阵乘法需要注意行列对应，第1题的计算结果为...", null, null, false),
                            StudentSubmission("s2", "张伟", "https://example.com/matrix_answer1.jpg", 90, "行列式计算正确，矩阵运算熟练", true)
                        )
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h2_${currentPage}",
                        title = "矩阵的秩与线性方程组作业",
                        description = "完成教材第58页练习题1-10题，掌握矩阵的秩的求法，以及利用矩阵的秩判断线性方程组的解的存在性和唯一性。",
                        dueDate = "2025-10-27",
                        submissions = mutableListOf()
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h3_${currentPage}",
                        title = "向量组的线性相关性作业",
                        description = "完成教材第75页练习题1-12题，理解向量组的线性相关性概念，掌握向量组的秩和极大线性无关组的求法。",
                        dueDate = "2025-11-03",
                        submissions = mutableListOf()
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h4_${currentPage}",
                        title = "相似矩阵与二次型作业",
                        description = "完成教材第96页练习题1-9题，掌握矩阵的特征值和特征向量的求法，理解相似矩阵的概念和性质。",
                        dueDate = "2025-11-10",
                        submissions = mutableListOf()
                    )
                )
            }
            "3" -> {
                // 大学物理（力学）的作业
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h1_${currentPage}",
                        title = "牛顿运动定律应用作业",
                        description = "完成教材第42页练习题1-6题，分析物体受力情况，应用牛顿三大定律解决力学问题，要求画出受力分析图。",
                        dueDate = "2025-10-22",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "https://example.com/physics_answer1.jpg", null, null, false),
                            StudentSubmission("s2", "张伟", "答案内容：根据牛顿第二定律F=ma，对于斜面问题，物体受到重力、支持力和摩擦力...", 95, "受力分析图清晰，解题过程完整正确", true)
                        )
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h2_${currentPage}",
                        title = "动量守恒定律作业",
                        description = "完成教材第60页练习题1-8题，理解动量、冲量的概念，掌握动量定理和动量守恒定律及其应用。",
                        dueDate = "2025-10-29",
                        submissions = mutableListOf()
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h3_${currentPage}",
                        title = "能量守恒定律作业",
                        description = "完成教材第78页练习题1-10题，掌握功、动能、势能的概念，理解动能定理和机械能守恒定律。",
                        dueDate = "2025-11-05",
                        submissions = mutableListOf()
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h4_${currentPage}",
                        title = "刚体力学基础作业",
                        description = "完成教材第95页练习题1-7题，理解刚体的平动和转动，掌握转动定律和角动量守恒定律。",
                        dueDate = "2025-11-12",
                        submissions = mutableListOf()
                    )
                )
            }
            "4" -> {
                // 程序设计基础（Java）的作业
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h1_${currentPage}",
                        title = "Java类与对象编程作业",
                        description = "设计一个学生类(Student)，包含姓名、学号、成绩等属性，以及构造方法、getter/setter方法和显示信息的方法。编写主程序测试该类的功能。",
                        dueDate = "2025-10-28",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "https://example.com/java_code1.zip", null, null, false),
                            StudentSubmission("s2", "张伟", "https://example.com/java_code2.zip", 94, "类设计合理，代码规范，功能完整", true)
                        )
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h2_${currentPage}",
                        title = "Java继承与多态作业",
                        description = "设计一个图形类的继承体系，包括抽象基类Shape和具体子类Circle、Rectangle、Triangle等，实现多态。",
                        dueDate = "2025-11-04",
                        submissions = mutableListOf()
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h3_${currentPage}",
                        title = "Java异常处理作业",
                        description = "编写一个文件读写程序，使用try-catch-finally语句处理各种可能的异常情况，确保程序的健壮性。",
                        dueDate = "2025-11-11",
                        submissions = mutableListOf()
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h4_${currentPage}",
                        title = "Java集合框架作业",
                        description = "使用ArrayList、HashMap等集合类实现一个简单的学生管理系统，包括添加、查询、修改和删除学生信息的功能。",
                        dueDate = "2025-11-18",
                        submissions = mutableListOf()
                    )
                )
            }
            "5" -> {
                // 宏观经济学的作业
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h1_${currentPage}",
                        title = "国民收入决定模型作业",
                        description = "结合IS-LM模型分析财政政策和货币政策对国民收入的影响，并探讨当前经济形势下的政策选择。",
                        dueDate = "2025-10-30",
                        submissions = mutableListOf(
                            StudentSubmission("s1", "刘小明", "答案内容：IS曲线表示产品市场均衡，LM曲线表示货币市场均衡，两者交点决定均衡国民收入...", null, null, false),
                            StudentSubmission("s2", "张伟", "https://example.com/econ_answer1.pdf", 91, "分析深入，理论结合实际，论证充分", true)
                        )
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h2_${currentPage}",
                        title = "总需求与总供给模型作业",
                        description = "分析总需求曲线和总供给曲线的形状和移动因素，探讨不同类型的宏观经济政策对经济均衡的影响。",
                        dueDate = "2025-11-06",
                        submissions = mutableListOf()
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h3_${currentPage}",
                        title = "失业与通货膨胀作业",
                        description = "理解失业的类型和原因，分析通货膨胀的形成机制，探讨菲利普斯曲线的含义和政策启示。",
                        dueDate = "2025-11-13",
                        submissions = mutableListOf()
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "h4_${currentPage}",
                        title = "经济增长与经济周期作业",
                        description = "解释经济增长的源泉，分析索洛增长模型的基本内容，探讨经济周期的特征和形成原因。",
                        dueDate = "2025-11-20",
                        submissions = mutableListOf()
                    )
                )
            }
            else -> {
                // 默认作业（适用于未指定课程）
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "default1_${currentPage}",
                        title = "课程作业 ${currentPage}-1",
                        description = "完成相关习题，掌握本章节的核心知识点",
                        dueDate = "2025-10-20",
                        submissions = mutableListOf()
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "default2_${currentPage}",
                        title = "课程作业 ${currentPage}-2",
                        description = "完成相关习题，掌握本章节的核心知识点",
                        dueDate = "2025-10-27",
                        submissions = mutableListOf()
                    )
                )
                mockHomeworks.add(
                    HomeworkDetail(
                        id = "default3_${currentPage}",
                        title = "课程作业 ${currentPage}-3",
                        description = "完成相关习题，掌握本章节的核心知识点",
                        dueDate = "2025-11-03",
                        submissions = mutableListOf()
                    )
                )
            }
        }
        
        // 模拟分页：根据当前页码选择显示的数据
        val startIndex = (currentPage - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, mockHomeworks.size)
        val pageData = mockHomeworks.subList(startIndex, endIndex)
        
        // 判断是否还有更多数据
        hasMoreData = endIndex < mockHomeworks.size
        
        // 添加新数据并通知适配器
        if (pageData.isNotEmpty()) {
            val startPosition = homeworkList.size
            homeworkList.addAll(pageData)
            
            if (currentPage == 1) {
                homeworkAdapter.notifyDataSetChanged()
            } else {
                homeworkAdapter.notifyItemRangeInserted(startPosition, pageData.size)
            }
        }
        
        // 如果是首次加载但没有数据，显示空状态
        if (currentPage == 1 && homeworkList.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.emptyState.visibility = View.GONE
        }
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