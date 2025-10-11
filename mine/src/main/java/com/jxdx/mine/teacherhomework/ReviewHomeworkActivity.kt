package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseActivity
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.R
import com.jxdx.mine.adapter.ImageAdapter
import com.jxdx.mine.databinding.ActivityReviewHomeworkBinding
import com.jxdx.mine.http.RetrofitClient
import com.jxdx.mine.http.request.ReviewHomeworkRequest
import com.jxdx.mine.http.request.AiReviewHomeworkRequest
import com.jxdx.mine.http.vo.UncorrectedHomeworkDetailVO
import com.jxdx.mine.http.vo.AiReviewResultVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewHomeworkActivity : BaseActivity<ActivityReviewHomeworkBinding>() {
    
    private var homeworkDetail: UncorrectedHomeworkDetailVO? = null
    private var homeworkId: Long = 0
    private var studentId: Long = 0
    private var subjectId: Int = 0
    private lateinit var imageAdapter: ImageAdapter
    
    override fun bindLayout(): ActivityReviewHomeworkBinding {
        Log.d("ReviewHomeworkActivity", "bindLayout 开始")
        try {
            val binding = ActivityReviewHomeworkBinding.inflate(layoutInflater)
            Log.d("ReviewHomeworkActivity", "bindLayout 成功")
            return binding
        } catch (e: Exception) {
            Log.e("ReviewHomeworkActivity", "bindLayout 失败", e)
            throw e
        }
    }
    
    override fun initView() {
        Log.d("ReviewHomeworkActivity", "========== initView 开始 ==========")
        try {
            // 获取传递的参数
            homeworkId = intent.getLongExtra("homeworkId", 0)
            studentId = intent.getLongExtra("studentId", 0)
            subjectId = intent.getIntExtra("subjectId", 0)
            homeworkDetail = intent.getSerializableExtra("homeworkDetail") as? UncorrectedHomeworkDetailVO
            
            Log.d("ReviewHomeworkActivity", "接收参数 - homeworkId: $homeworkId, studentId: $studentId, subjectId: $subjectId")
            
            // 设置标题
            supportActionBar?.title = "批改作业"
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            Log.d("ReviewHomeworkActivity", "ActionBar设置完成")
            
            // 初始化界面数据
            initData()
            
            Log.d("ReviewHomeworkActivity", "========== initView 完成 ==========")
        } catch (e: Exception) {
            Log.e("ReviewHomeworkActivity", "initView失败", e)
            e.printStackTrace()
            finish()
        }
    }
    
    override fun subscribeUi() {
        Log.d("ReviewHomeworkActivity", "========== subscribeUi 开始 ==========")
        try {
            // 设置返回按钮点击事件
            view.btnBack.setOnClickListener {
                Log.d("ReviewHomeworkActivity", "返回按钮点击")
                finish()
            }
            
            // 设置AI批改按钮点击事件
            view.btnAiReview.setOnClickListener {
                Log.d("ReviewHomeworkActivity", "AI批改按钮点击")
                performAiReview()
            }
            
            // 设置提交批改按钮点击事件
            view.btnSubmit.setOnClickListener {
                Log.d("ReviewHomeworkActivity", "提交批改按钮点击")
                submitReview()
            }
            
            // 设置分数输入监听
            view.etScore.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    validateScore()
                }
            }
            
            Log.d("ReviewHomeworkActivity", "========== subscribeUi 完成 ==========")
        } catch (e: Exception) {
            Log.e("ReviewHomeworkActivity", "subscribeUi失败", e)
            e.printStackTrace()
        }
    }
    
    private fun initData() {
        Log.d("ReviewHomeworkActivity", "========== initData 开始 ==========")
        try {
            // 初始化图片适配器
            imageAdapter = ImageAdapter(this)
            view.rvSubmitImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            view.rvSubmitImages.adapter = imageAdapter
            
            homeworkDetail?.let { detail ->
                // 设置学生信息
                view.tvStudentName.text = detail.studentName ?: "未知学生"
                view.tvSubmitTime.text = if (detail.submitTime != null) {
                    "提交时间: ${detail.submitTime}"
                } else {
                    "未提交"
                }
                
                // 处理提交内容，分离文本和图片URL
                val submitContent = detail.submitContent
                if (submitContent != null && submitContent.isNotEmpty()) {
                    val textContents = mutableListOf<String>()
                    val imageUrls = mutableListOf<String>()
                    
                    // 识别和分离文本内容与图片URL
                    submitContent.forEach { content ->
                        if (isImageUrl(content)) {
                            imageUrls.add(content)
                            Log.d("ReviewHomeworkActivity", "识别到图片URL: $content")
                        } else {
                            textContents.add(content)
                        }
                    }
                    
                    // 显示文本内容
                    if (textContents.isNotEmpty()) {
                        view.tvSubmitContent.text = textContents.joinToString("\n")
                    } else {
                        view.tvSubmitContent.text = "暂无文本内容"
                    }
                    
                    // 显示图片
                    if (imageUrls.isNotEmpty()) {
                        view.tvImagesLabel.visibility = android.view.View.VISIBLE
                        view.rvSubmitImages.visibility = android.view.View.VISIBLE
                        imageAdapter.setImageUrls(imageUrls)
                        Log.d("ReviewHomeworkActivity", "显示图片数量: ${imageUrls.size}")
                    } else {
                        view.tvImagesLabel.visibility = android.view.View.GONE
                        view.rvSubmitImages.visibility = android.view.View.GONE
                    }
                } else {
                    view.tvSubmitContent.text = "暂无提交内容"
                    view.tvImagesLabel.visibility = android.view.View.GONE
                    view.rvSubmitImages.visibility = android.view.View.GONE
                }
                
                // 设置当前分数和评语（如果有的话）
                detail.score?.let { score ->
                    view.etScore.setText(score.toString())
                }
                detail.comment?.let { comment ->
                    view.etComment.setText(comment)
                }
                
                Log.d("ReviewHomeworkActivity", "数据初始化完成")
            } ?: run {
                Log.e("ReviewHomeworkActivity", "作业详情数据为空")
                Toast.makeText(this, "作业数据异常", Toast.LENGTH_SHORT).show()
                finish()
            }
            
            Log.d("ReviewHomeworkActivity", "========== initData 完成 ==========")
        } catch (e: Exception) {
            Log.e("ReviewHomeworkActivity", "initData失败", e)
            e.printStackTrace()
        }
    }
    
    /**
     * 判断字符串是否为图片URL
     */
    private fun isImageUrl(content: String): Boolean {
        if (content.isBlank()) return false
        
        // 检查是否以http开头
        if (!content.startsWith("http://") && !content.startsWith("https://")) {
            return false
        }
        
        // 检查是否包含图片文件扩展名
        val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp")
        val lowerContent = content.lowercase()
        
        return imageExtensions.any { lowerContent.contains(it) }
    }
    
    private fun validateScore(): Boolean {
        Log.d("ReviewHomeworkActivity", "验证分数")
        try {
            val scoreText = view.etScore.text.toString().trim()
            if (scoreText.isEmpty()) {
                view.tilScore.error = "请输入分数"
                return false
            }
            
            val score = scoreText.toIntOrNull()
            if (score == null) {
                view.tilScore.error = "分数必须是数字"
                return false
            }
            
            if (score < 0 || score > 100) {
                view.tilScore.error = "分数必须在0-100之间"
                return false
            }
            
            view.tilScore.error = null
            return true
        } catch (e: Exception) {
            Log.e("ReviewHomeworkActivity", "验证分数失败", e)
            return false
        }
    }
    
    private fun performAiReview() {
        Log.d("ReviewHomeworkActivity", "========== performAiReview 开始 ==========")
        try {
            homeworkDetail?.let { detail ->
                val submitContent = detail.submitContent
                if (submitContent.isNullOrEmpty()) {
                    Toast.makeText(this, "学生没有提交内容，无法进行AI批改", Toast.LENGTH_SHORT).show()
                    return
                }
                
                // 显示加载状态
                view.btnAiReview.isEnabled = false
                view.btnAiReview.text = "AI批改中..."
                
                // 创建AI批改请求
                val request = AiReviewHomeworkRequest(
                    homeworkId = homeworkId,
                    subjectId = subjectId,
                    studentId = studentId,
                    studentContent = submitContent
                )
                
                Log.d("ReviewHomeworkActivity", "AI批改请求: $request")
                
                // 调用AI批改API
                RetrofitClient.apiService.aiReviewHomework(request).enqueue(object : Callback<BaseResp<AiReviewResultVO>> {
                    override fun onResponse(
                        call: Call<BaseResp<AiReviewResultVO>>,
                        response: Response<BaseResp<AiReviewResultVO>>
                    ) {
                        view.btnAiReview.isEnabled = true
                        view.btnAiReview.text = "AI批改"
                        
                        Log.d("ReviewHomeworkActivity", "AI批改API响应: ${response.code()}")
                        
                        if (response.isSuccessful && response.body()?.code == 0) {
                            val aiResult = response.body()?.data
                            if (aiResult != null) {
                                Log.d("ReviewHomeworkActivity", "AI批改成功: $aiResult")
                                
                                // 填充AI批改结果
                                aiResult.score?.let { score ->
                                    view.etScore.setText(score.toString())
                                }
                                aiResult.comment?.let { comment ->
                                    view.etComment.setText(comment)
                                }
                                
                                Toast.makeText(this@ReviewHomeworkActivity, "AI批改完成", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e("ReviewHomeworkActivity", "AI批改结果为空")
                                Toast.makeText(this@ReviewHomeworkActivity, "AI批改失败，请重试", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.e("ReviewHomeworkActivity", "AI批改API返回错误: ${response.body()?.message}")
                            Toast.makeText(this@ReviewHomeworkActivity, "AI批改失败: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    
                    override fun onFailure(
                        call: Call<BaseResp<AiReviewResultVO>>,
                        t: Throwable
                    ) {
                        view.btnAiReview.isEnabled = true
                        view.btnAiReview.text = "AI批改"
                        
                        Log.e("ReviewHomeworkActivity", "AI批改网络请求失败", t)
                        Toast.makeText(this@ReviewHomeworkActivity, "网络请求失败: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } ?: run {
                Log.e("ReviewHomeworkActivity", "作业详情数据为空")
                Toast.makeText(this, "作业数据异常", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: Exception) {
            view.btnAiReview.isEnabled = true
            view.btnAiReview.text = "AI批改"
            
            Log.e("ReviewHomeworkActivity", "performAiReview失败", e)
            e.printStackTrace()
            Toast.makeText(this, "AI批改失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun submitReview() {
        Log.d("ReviewHomeworkActivity", "========== submitReview 开始 ==========")
        try {
            // 验证输入
            if (!validateScore()) {
                return
            }
            
            val scoreText = view.etScore.text.toString().trim()
            val score = scoreText.toInt()
            val comment = view.etComment.text.toString().trim()
            
            if (comment.isEmpty()) {
                Toast.makeText(this, "请输入评语", Toast.LENGTH_SHORT).show()
                return
            }
            
            // 显示加载状态
            view.btnSubmit.isEnabled = false
            view.btnSubmit.text = "提交中..."
            
            // 创建请求对象
            val request = ReviewHomeworkRequest(
                homeworkId = homeworkId,
                studentId = studentId,
                teacherComment = comment,
                score = score
            )
            
            Log.d("ReviewHomeworkActivity", "提交批改请求: $request")
            
            // 调用API
            RetrofitClient.apiService.reviewHomework(request).enqueue(object : Callback<BaseResp<String>> {
                override fun onResponse(
                    call: Call<BaseResp<String>>,
                    response: Response<BaseResp<String>>
                ) {
                    view.btnSubmit.isEnabled = true
                    view.btnSubmit.text = "提交批改"
                    
                    Log.d("ReviewHomeworkActivity", "API响应: ${response.code()}")
                    
                    if (response.isSuccessful && response.body()?.code == 0) {
                        Log.d("ReviewHomeworkActivity", "批改成功")
                        Toast.makeText(this@ReviewHomeworkActivity, "批改成功", Toast.LENGTH_SHORT).show()
                        
                        // 返回结果
                        val resultIntent = Intent()
                        resultIntent.putExtra("reviewed", true)
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Log.e("ReviewHomeworkActivity", "API返回错误: ${response.body()?.message}")
                        Toast.makeText(this@ReviewHomeworkActivity, "批改失败: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                
                override fun onFailure(
                    call: Call<BaseResp<String>>,
                    t: Throwable
                ) {
                    view.btnSubmit.isEnabled = true
                    view.btnSubmit.text = "提交批改"
                    
                    Log.e("ReviewHomeworkActivity", "网络请求失败", t)
                    Toast.makeText(this@ReviewHomeworkActivity, "网络请求失败: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
            
        } catch (e: Exception) {
            view.btnSubmit.isEnabled = true
            view.btnSubmit.text = "提交批改"
            
            Log.e("ReviewHomeworkActivity", "submitReview失败", e)
            e.printStackTrace()
            Toast.makeText(this, "提交失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        Log.d("ReviewHomeworkActivity", "ActionBar返回按钮点击")
        finish()
        return true
    }
    
    companion object {
        fun start(context: android.content.Context, homeworkId: Long, studentId: Long, subjectId: Int, homeworkDetail: UncorrectedHomeworkDetailVO) {
            val intent = Intent(context, ReviewHomeworkActivity::class.java)
            intent.putExtra("homeworkId", homeworkId)
            intent.putExtra("studentId", studentId)
            intent.putExtra("subjectId", subjectId)
            intent.putExtra("homeworkDetail", homeworkDetail)
            context.startActivity(intent)
        }
    }
}