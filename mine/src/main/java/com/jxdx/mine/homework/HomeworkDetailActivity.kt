package com.jxdx.mine.homework

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseActivity
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.R
import com.jxdx.mine.StuHomeWorkDetailVO
import com.jxdx.mine.adapter.ImageAdapter
import com.jxdx.mine.databinding.ActivityHomeworkDetailBinding
import com.jxdx.mine.http.ApiService
import com.jxdx.mine.http.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//作业详情页
class HomeworkDetailActivity : BaseActivity<ActivityHomeworkDetailBinding>() {
    private var homeworkId: Int = -1
    private lateinit var imageAdapter: ImageAdapter
    private var currentHomeworkDetail: StuHomeWorkDetailVO? = null
    private val studentContentList = mutableListOf<String>()
    private val selectedImages = mutableListOf<String>()
    private val REQUEST_CODE_PHOTO = 1001

    override fun bindLayout(): ActivityHomeworkDetailBinding {
        return ActivityHomeworkDetailBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 获取从上一个页面传递过来的作业ID
        homeworkId = intent.getIntExtra("homeworkId", -1)
        if (homeworkId == -1) {
            Log.e("HomeworkDetailActivity", "未获取到作业ID")
            finish()
            return
        }
        
        // 初始化图片适配器
        imageAdapter = ImageAdapter(this)
        view.recyclerViewImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        view.recyclerViewImages.adapter = imageAdapter
        
        // 初始化已选图片适配器
        view.recyclerViewSelectedImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        view.recyclerViewSelectedImages.adapter = imageAdapter
        
        // 初始化已提交图片适配器
        view.recyclerViewSubmittedImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        view.recyclerViewSubmittedImages.adapter = imageAdapter
        
        // 设置按钮点击事件
        view.btnAddImage.setOnClickListener {
            pickImageFromGallery()
        }
        
        view.btnSubmitHomework.setOnClickListener {
            submitHomework()
        }
    }

    override fun subscribeUi() {
        // 加载作业详情
        loadHomeworkDetail()
    }

    private fun loadHomeworkDetail() {
        // 显示加载状态
        showLoading()

        RetrofitClient.apiService.getHomeworkDetail(homeworkId).enqueue(object : Callback<BaseResp<StuHomeWorkDetailVO>> {
            override fun onResponse(
                call: Call<BaseResp<StuHomeWorkDetailVO>>,
                response: Response<BaseResp<StuHomeWorkDetailVO>>
            ) {
                // 隐藏加载状态
                hideLoading()

                if (response.isSuccessful && response.body() != null) {
                    val resp = response.body()
                    if (resp?.code == 0 && resp.data != null) {
                        // 成功获取作业详情，使用非空断言操作符
                        showHomeworkDetail(resp.data!!)
                    } else {
                        showError("获取作业详情失败：${resp?.message}")
                    }
                } else {
                    showError("网络请求失败")
                }
            }

            override fun onFailure(call: Call<BaseResp<StuHomeWorkDetailVO>>, t: Throwable) {
                hideLoading()
                showError("网络请求失败：${t.message}")
            }
        })
    }

    private fun showHomeworkDetail(homeworkDetail: StuHomeWorkDetailVO) {
        currentHomeworkDetail = homeworkDetail
        
        // 设置作业ID
        view.tvHomeworkId.text = homeworkDetail.homeworkId.toString()
        
        // 设置科目
        view.tvSubject.text = homeworkDetail.subject
        
        // 设置作业标题
        view.tvHomeworkTitle.text = homeworkDetail.homeworkName
        
        // 设置作业内容，支持HTML格式
        view.tvHomeworkDescription.text = if (homeworkDetail.homeworkContent.isNullOrEmpty()) {
            "无内容描述"
        } else {
            HtmlCompat.fromHtml(
                homeworkDetail.homeworkContent, 
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
        
        // 设置截止日期
        view.tvHomeworkDueDate.text = "截止日期：${homeworkDetail.deadTime}"
        
        // 处理图片列表
        if (!homeworkDetail.imageUrls.isNullOrEmpty()) {
            // 过滤掉可能存在的空字符串或无效URL
            val validUrls = homeworkDetail.imageUrls.filter { !it.isNullOrEmpty() && it.trim().isNotBlank() }
            if (validUrls.isNotEmpty()) {
                // 显示图片标签和图片列表
                view.tvImagesLabel.visibility = View.VISIBLE
                view.recyclerViewImages.visibility = View.VISIBLE
                // 设置图片数据
                imageAdapter.setImageUrls(validUrls)
            } else {
                // 没有有效图片，隐藏相关视图
                view.tvImagesLabel.visibility = View.GONE
                view.recyclerViewImages.visibility = View.GONE
            }
        } else {
            // 没有图片数据，隐藏相关视图
            view.tvImagesLabel.visibility = View.GONE
            view.recyclerViewImages.visibility = View.GONE
        }
        
        // 根据作业状态显示不同的内容
        when (homeworkDetail.completeAndCorrect) {
            1 -> {
                // 未完成 - 显示提交作业区域
                view.layoutSubmitHomework.visibility = View.VISIBLE
                view.layoutSubmittedContent.visibility = View.GONE
                view.recyclerViewSubmissions.visibility = View.GONE
            }
            2 -> {
                // 已提交未批改 - 显示已提交内容
                view.layoutSubmitHomework.visibility = View.GONE
                view.layoutSubmittedContent.visibility = View.VISIBLE
                view.recyclerViewSubmissions.visibility = View.GONE
                
                // 显示提交的内容
                view.tvSubmittedContent.text = homeworkDetail.studentContent ?: "无提交内容"
                
                // 如果有提交的图片，显示图片
                if (!homeworkDetail.imageUrls.isNullOrEmpty()) {
                    val validUrls = homeworkDetail.imageUrls.filter { !it.isNullOrEmpty() && it.trim().isNotBlank() }
                    if (validUrls.isNotEmpty()) {
                        view.tvSubmittedImagesLabel.visibility = View.VISIBLE
                        view.recyclerViewSubmittedImages.visibility = View.VISIBLE
                        imageAdapter.setImageUrls(validUrls)
                    }
                }
            }
            3 -> {
                // 已批改 - 显示已提交内容和批改信息
                view.layoutSubmitHomework.visibility = View.GONE
                view.layoutSubmittedContent.visibility = View.VISIBLE
                view.recyclerViewSubmissions.visibility = View.GONE
                
                // 显示提交的内容
                view.tvSubmittedContent.text = homeworkDetail.studentContent ?: "无提交内容"
                
                // 如果有提交的图片，显示图片
                if (!homeworkDetail.imageUrls.isNullOrEmpty()) {
                    val validUrls = homeworkDetail.imageUrls.filter { !it.isNullOrEmpty() && it.trim().isNotBlank() }
                    if (validUrls.isNotEmpty()) {
                        view.tvSubmittedImagesLabel.visibility = View.VISIBLE
                        view.recyclerViewSubmittedImages.visibility = View.VISIBLE
                        imageAdapter.setImageUrls(validUrls)
                    }
                }
                
                // 显示批改信息
                view.layoutCorrectionInfo.visibility = View.VISIBLE
                view.tvScore.text = homeworkDetail.score?.toString() ?: "0"
                view.tvComment.text = homeworkDetail.teacherComment ?: "无评语"
            }
        }
    }

    private fun showLoading() {
        // 实现加载状态显示逻辑
        // 这里可以添加一个加载指示器
    }

    private fun hideLoading() {
        // 实现隐藏加载状态逻辑
    }

    private fun showError(message: String) {
        // 显示错误信息
        AlertDialog.Builder(this)
            .setTitle("错误")
            .setMessage(message)
            .setPositiveButton("确定") { _, _ -> }
            .show()
    }
    
    // 从相册选择图片
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PHOTO)
    }
    
    // 处理图片选择结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PHOTO && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                // 将URI转换为字符串并添加到已选图片列表
                val imagePath = imageUri.toString()
                selectedImages.add(imagePath)
                
                // 更新图片数量显示
                view.tvImageCount.text = "已选择${selectedImages.size}张图片"
                
                // 显示已选图片预览
                view.recyclerViewSelectedImages.visibility = View.VISIBLE
                imageAdapter.setImageUrls(selectedImages)
            }
        }
    }
    
    // 提交作业
    private fun submitHomework() {
        val content = view.etStudentContent.text.toString().trim()
        
        // 验证内容
        if (content.isEmpty() && selectedImages.isEmpty()) {
            showError("请至少输入作业内容或添加图片")
            return
        }
        
        // 准备提交数据
        val submitRequest = com.jxdx.mine.http.SubmitHomeworkRequest(
            homeworkId = homeworkId.toLong(),
            studentContent = if (content.isEmpty()) null else listOf(content)
            // 注意：这里简化了图片上传逻辑，实际项目中需要先上传图片获取URL
        )
        
        // 显示加载状态
        showLoading()
        
        // 发送提交请求
        RetrofitClient.apiService.submitHomework(submitRequest).enqueue(object : Callback<BaseResp<String>> {
            override fun onResponse(call: Call<BaseResp<String>>, response: Response<BaseResp<String>>) {
                hideLoading()
                if (response.isSuccessful && response.body() != null) {
                    val resp = response.body()
                    if (resp?.code == 0) {
                        // 提交成功，重新加载作业详情
                        showSuccess("作业提交成功")
                        loadHomeworkDetail()
                    } else {
                        showError("作业提交失败：${resp?.message}")
                    }
                } else {
                    showError("网络请求失败")
                }
            }
            
            override fun onFailure(call: Call<BaseResp<String>>, t: Throwable) {
                hideLoading()
                showError("网络请求失败：${t.message}")
            }
        })
    }
    
    private fun showSuccess(message: String) {
        AlertDialog.Builder(this)
            .setTitle("成功")
            .setMessage(message)
            .setPositiveButton("确定") { _, _ -> }
            .show()
    }
}