package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.StudentSubmission
import com.jxdx.mine.databinding.ActivityHomeworkDetailBinding
import com.jxdx.mine.http.ApiService
import com.jxdx.mine.http.RetrofitClient
import com.jxdx.mine.http.TeachCreateHWDetailVO
import com.jxdx.mine.adapter.ImageAdapter
import com.jxdx.mine.teacherhomework.adapter.StudentSubmissionAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeworkDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeworkDetailBinding
    private lateinit var adapter: StudentSubmissionAdapter
    private lateinit var imageAdapter: ImageAdapter
    private val submissions = mutableListOf<StudentSubmission>()
    private val imageUrls = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeworkDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val homeworkId = intent.getStringExtra("homeworkId")
        val homeworkTitle = intent.getStringExtra("homeworkTitle")
        
        supportActionBar?.title = "$homeworkTitle - 学生作业"
        
        // 初始化图片适配器
        imageAdapter = ImageAdapter(this)
        binding.recyclerViewImages.apply {
            layoutManager = GridLayoutManager(this@HomeworkDetailActivity, 3)
            adapter = imageAdapter
        }
          
        // 从API获取作业详情
        if (homeworkId != null) {
            getHomeworkDetail(homeworkId)
        }

        initRecyclerView()
        loadSubmissions(homeworkId)
    }

    private fun initRecyclerView() {
        adapter = StudentSubmissionAdapter(submissions) { submission ->
            val intent = Intent(this, ReviewHomeworkActivity::class.java)
            intent.putExtra("homeworkId", binding.tvHomeworkId.text.toString())
            intent.putExtra("studentId", submission.studentId)
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
    
    private fun getHomeworkDetail(homeworkId: String) {
        // 显示加载状态
        binding.loadingLayout.visibility = View.VISIBLE
        binding.contentLayout.visibility = View.GONE
        binding.errorLayout.visibility = View.GONE
        
        // 调用老师查看作业详情的接口 (@GET /api/teach/homework/create/find)
        RetrofitClient.apiService.getTeacherHomeworkDetail(homeworkId.toInt()).enqueue(object : Callback<BaseResp<TeachCreateHWDetailVO>> {
            override fun onResponse(
                call: Call<BaseResp<TeachCreateHWDetailVO>>,
                response: Response<BaseResp<TeachCreateHWDetailVO>>
            ) {
                // 隐藏加载状态
                binding.loadingLayout.visibility = View.GONE
                
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()
                    if (result?.code == 0 && result.data != null) {
                        val homeworkDetail = result.data
                        updateUIWithHomeworkDetail(homeworkDetail)
                        binding.contentLayout.visibility = View.VISIBLE
                    } else {
                        Log.e("HomeworkDetail", "Failed to get homework detail: ${result?.message ?: "Unknown error"}")
                        showError("获取作业详情失败: ${result?.message ?: "未知错误"}")
                        showMockData(homeworkId)
                    }
                } else {
                    Log.e("HomeworkDetail", "Response unsuccessful: ${response.code()}")
                    showError("网络请求失败，请检查网络连接")
                    showMockData(homeworkId)
                }
            }
            
            override fun onFailure(call: Call<BaseResp<TeachCreateHWDetailVO>>,
                                   t: Throwable) {
                // 隐藏加载状态
                binding.loadingLayout.visibility = View.GONE
                
                Log.e("HomeworkDetail", "Network failure: ${t.message}")
                showError("网络异常: ${t.message}")
                showMockData(homeworkId)
            }
        })
    }
    
    private fun updateUIWithHomeworkDetail(homeworkDetail: TeachCreateHWDetailVO?) {
        binding.tvHomeworkId.text = homeworkDetail?.homeworkId.toString()
        binding.tvHomeworkTitle.text = homeworkDetail?.homeworkName ?: "未命名作业"
        binding.tvSubject.text = homeworkDetail?.subject ?: ""
        binding.tvHomeworkDescription.text = homeworkDetail?.homeworkContent ?: ""
        binding.tvHomeworkDueDate.text = "截止日期：${homeworkDetail?.deadTime ?: "未设置"}"
        
        // 处理图片列表
        if (homeworkDetail?.imageUrls?.isNotEmpty() == true) {
            imageAdapter.setImageUrls(homeworkDetail.imageUrls)
            binding.tvImagesLabel.visibility = View.VISIBLE
            binding.recyclerViewImages.visibility = View.VISIBLE
        } else {
            binding.tvImagesLabel.visibility = View.GONE
            binding.recyclerViewImages.visibility = View.GONE
        }
        
        // 加载学生提交列表
        loadSubmissions(homeworkDetail?.homeworkId.toString())
    }
    
    private fun showError(message: String) {
        binding.errorMessage.text = message
        binding.errorLayout.visibility = View.VISIBLE
        binding.retryButton.setOnClickListener {
            val homeworkId = binding.tvHomeworkId.text.toString()
            if (homeworkId.isNotEmpty()) {
                getHomeworkDetail(homeworkId)
            } else {
                // 如果没有homeworkId，从Intent中重新获取
                val idFromIntent = intent.getStringExtra("homeworkId")
                if (idFromIntent != null) {
                    getHomeworkDetail(idFromIntent)
                }
            }
        }
    }
    
    private fun showMockData(homeworkId: String) {
        // 显示作业详情
        binding.tvHomeworkId.text = homeworkId
        
        // 根据作业ID设置不同的作业描述和截止日期
        when (homeworkId) {
            "h1" -> {
                binding.tvSubject.text = "高等数学"
                binding.tvHomeworkTitle.text = "函数极限与连续性作业"
                binding.tvHomeworkDescription.text = "完成教材第25页练习题1-10题，要求写出详细的解题步骤和计算过程，重点关注极限的ε-δ定义证明。作业需要手写完成后拍照上传或直接在系统中输入解答过程。"
                binding.tvHomeworkDueDate.text = "截止日期：2025-10-18 23:59"
            }
            "h2" -> {
                binding.tvSubject.text = "高等数学"
                binding.tvHomeworkTitle.text = "导数与微分作业"
                binding.tvHomeworkDescription.text = "完成教材第48页练习题1-12题，包括导数的定义计算、基本初等函数求导法则、隐函数求导和高阶导数计算。要求使用A4纸手写完成，字迹清晰。"
                binding.tvHomeworkDueDate.text = "截止日期：2025-10-25 23:59"
            }
            "h3" -> {
                binding.tvSubject.text = "线性代数"
                binding.tvHomeworkTitle.text = "矩阵运算与行列式计算作业"
                binding.tvHomeworkDescription.text = "完成教材第36页练习题1-8题，包括矩阵的加减乘运算、转置、行列式计算和伴随矩阵求解。注意矩阵运算的规则和行列式的展开方法。"
                binding.tvHomeworkDueDate.text = "截止日期：2025-10-20 23:59"
            }
            "h4" -> {
                binding.tvSubject.text = "大学物理"
                binding.tvHomeworkTitle.text = "牛顿运动定律应用作业"
                binding.tvHomeworkDescription.text = "完成教材第42页练习题1-6题，分析物体受力情况，应用牛顿三大定律解决力学问题，要求画出受力分析图。作业提交时需要包含详细的受力分析过程和计算步骤。"
                binding.tvHomeworkDueDate.text = "截止日期：2025-10-22 23:59"
            }
            "h5" -> {
                binding.tvSubject.text = "程序设计基础"
                binding.tvHomeworkTitle.text = "Java类与对象编程作业"
                binding.tvHomeworkDescription.text = "设计一个学生类(Student)，包含姓名、学号、成绩等属性，以及构造方法、getter/setter方法和显示信息的方法。编写主程序测试该类的功能。将源代码打包成zip文件上传。"
                binding.tvHomeworkDueDate.text = "截止日期：2025-10-28 23:59"
            }
            "h6" -> {
                binding.tvSubject.text = "宏观经济学"
                binding.tvHomeworkTitle.text = "国民收入决定模型作业"
                binding.tvHomeworkDescription.text = "结合IS-LM模型分析财政政策和货币政策对国民收入的影响，并探讨当前经济形势下的政策选择。论文要求3000字以上，格式规范，论点明确，论据充分。"
                binding.tvHomeworkDueDate.text = "截止日期：2025-10-30 23:59"
            }
            else -> {
                binding.tvSubject.text = "未设置科目"
                binding.tvHomeworkTitle.text = "未命名作业"
                binding.tvHomeworkDescription.text = "这是作业的详细描述内容，包含了作业要求和评分标准。请按照要求完成并按时提交。"
                binding.tvHomeworkDueDate.text = "截止日期：2025-10-20 23:59"
            }
        }
        
        // 隐藏图片相关UI，因为模拟数据中没有图片
        binding.tvImagesLabel.visibility = View.GONE
        binding.recyclerViewImages.visibility = View.GONE
        
        // 加载模拟的学生提交列表
        loadSubmissions(homeworkId)
    }

    private fun loadSubmissions(homeworkId: String?) {
        // 模拟数据 - 根据作业ID加载对应的学生提交
        submissions.clear()
        
        when (homeworkId) {
            "h1" -> {
                // 高等数学 - 函数极限与连续性作业的提交
                submissions.add(StudentSubmission("s1", "刘小明", "答案内容：根据函数极限的定义，对于任意ε>0，存在δ=ε/2，当0<|x-2|<δ时，|f(x)-4|=|2x-4|=2|x-2|<2δ=ε，因此lim(x→2)2x=4。\n\n第2题：利用夹逼定理，由于-|x|≤xsin(1/x)≤|x|，而lim(x→0)|x|=0，故lim(x→0)xsin(1/x)=0。", null, null, false))
                submissions.add(StudentSubmission("s2", "张伟", "https://example.com/calculus_answer1.jpg", 92, "解题步骤清晰，极限证明方法正确，计算准确，尤其是ε-δ定义的应用掌握很好。", true))
                submissions.add(StudentSubmission("s3", "李娜", "答案内容：我采用了夹逼定理来求解第5题的极限，过程如下...\n\n第7题：函数f(x)在x=0处连续，因为lim(x→0)f(x)=f(0)=0。", null, null, false))
                submissions.add(StudentSubmission("s4", "王芳", "https://example.com/calculus_answer2.jpg", 85, "整体表现良好，但第7题的连续性证明可以更严谨，建议使用ε-δ语言进行证明。", true))
                submissions.add(StudentSubmission("s5", "赵阳", "", null, null, false))
            }
            "h2" -> {
                // 高等数学 - 导数与微分作业的提交
                submissions.add(StudentSubmission("s1", "刘小明", "https://example.com/derivative_answer1.jpg", null, null, false))
                submissions.add(StudentSubmission("s2", "张伟", "", null, null, false))
                submissions.add(StudentSubmission("s3", "李娜", "https://example.com/derivative_answer2.jpg", 88, "隐函数求导掌握较好，但高阶导数计算有小错误，需要注意符号。", true))
            }
            "h3" -> {
                // 线性代数 - 矩阵运算与行列式计算作业的提交
                submissions.add(StudentSubmission("s1", "刘小明", "答案内容：矩阵乘法需要注意行列对应，第1题的计算结果为[[3, 8], [7, 18]]。\n\n第3题：行列式的计算可以使用展开定理，结果为-24。", null, null, false))
                submissions.add(StudentSubmission("s2", "张伟", "https://example.com/matrix_answer1.jpg", 90, "行列式计算正确，矩阵运算熟练，转置和伴随矩阵的求解方法掌握较好。", true))
            }
            "h4" -> {
                // 大学物理 - 牛顿运动定律应用作业的提交
                submissions.add(StudentSubmission("s1", "刘小明", "https://example.com/physics_answer1.jpg", null, null, false))
                submissions.add(StudentSubmission("s2", "张伟", "答案内容：根据牛顿第二定律F=ma，对于斜面问题，物体受到重力mg、支持力N和摩擦力f。沿斜面方向的合力为mgsinθ-f=ma，垂直斜面方向N=mgcosθ。当物体匀速下滑时，a=0，故f=mgsinθ。", 95, "受力分析图清晰，解题过程完整正确，对牛顿定律的应用掌握很好。", true))
                submissions.add(StudentSubmission("s3", "李娜", "https://example.com/physics_answer2.jpg", 89, "分析过程正确，但需要注意单位的统一和有效数字的保留。", true))
            }
            "h5" -> {
                // 程序设计基础 - Java类与对象编程作业的提交
                submissions.add(StudentSubmission("s1", "刘小明", "https://example.com/java_code1.zip", null, null, false))
                submissions.add(StudentSubmission("s2", "张伟", "https://example.com/java_code2.zip", 94, "类设计合理，代码规范，功能完整，包含了所有要求的方法，并进行了充分的测试。", true))
                submissions.add(StudentSubmission("s3", "李娜", "https://example.com/java_code3.zip", 87, "实现了基本功能，但可以增加异常处理机制和更完善的输入验证。", true))
            }
            "h6" -> {
                // 宏观经济学 - 国民收入决定模型作业的提交
                submissions.add(StudentSubmission("s1", "刘小明", "答案内容：IS曲线表示产品市场均衡，LM曲线表示货币市场均衡，两者交点决定均衡国民收入和利率。扩张性财政政策会使IS曲线右移，增加国民收入；扩张性货币政策会使LM曲线右移，降低利率并增加国民收入。在当前经济形势下，应采取积极的财政政策和稳健的货币政策相结合的方式...", null, null, false))
                submissions.add(StudentSubmission("s2", "张伟", "https://example.com/econ_answer1.pdf", 91, "分析深入，理论结合实际，论证充分，对IS-LM模型的理解和应用非常到位。", true))
            }
            else -> {
                // 默认提交数据
                submissions.add(StudentSubmission("s1", "刘小明", "答案内容：根据课程要求，我已完成相关习题...", null, null, false))
                submissions.add(StudentSubmission("s2", "张伟", "https://example.com/default_answer.jpg", 90, "完成质量良好，解题思路清晰", true))
                submissions.add(StudentSubmission("s3", "李娜", "答案内容：我对这部分内容的理解如下...", 85, "分析合理，但可以进一步完善", true))
                submissions.add(StudentSubmission("s4", "王芳", "", null, null, false))
            }
        }
        adapter.notifyDataSetChanged()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // 获取批改结果
            val score = data.getIntExtra("score", -1)
            val comment = data.getStringExtra("comment")
            val studentId = data.getStringExtra("studentId")
            
            // 更新对应的提交记录
            if (studentId != null) {
                val submission = submissions.find { it.studentId == studentId }
                if (submission != null) {
                    submission.score = score
                    submission.comment = comment
                    submission.isReviewed = true
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}