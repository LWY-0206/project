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
import com.jxdx.mine.UserInfo
import com.jxdx.mine.Course
import com.jxdx.mine.adapter.ImageAdapter
import com.jxdx.mine.databinding.ActivityHomeworkDetailBinding
import com.jxdx.mine.databinding.ActivityHomeworkDetailStudentBinding
import com.jxdx.mine.http.ApiService
import com.jxdx.mine.http.RetrofitClient
import com.jxdx.mine.http.request.SubmitHomeworkRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//作业详情页
class HomeworkDetailActivity : BaseActivity<ActivityHomeworkDetailStudentBinding>() {
    private var homeworkId: Int = -1
    private lateinit var imageAdapter: ImageAdapter
    private var currentHomeworkDetail: StuHomeWorkDetailVO? = null
    private val studentContentList = mutableListOf<String>()
    private val selectedImages = mutableListOf<String>()
    private val REQUEST_CODE_PHOTO = 1001

    override fun bindLayout(): ActivityHomeworkDetailStudentBinding {
        Log.d("HomeworkDetailActivity", "bindLayout 开始")
        try {
            val binding = ActivityHomeworkDetailStudentBinding.inflate(layoutInflater)
            Log.d("HomeworkDetailActivity", "bindLayout 成功")
            return binding
        } catch (e: Exception) {
            Log.e("HomeworkDetailActivity", "bindLayout 失败", e)
            throw e
        }
    }

    override fun initView() {
        Log.d("HomeworkDetailActivity", "========== initView 开始 ==========")
        try {
            // 获取从上一个页面传递过来的作业ID
            homeworkId = intent.getIntExtra("homeworkId", -1)
            Log.d("HomeworkDetailActivity", "获取到的作业ID: $homeworkId")
            if (homeworkId == -1) {
                Log.e("HomeworkDetailActivity", "未获取到作业ID，即将finish()")
                finish()
                return
            }
            
            Log.d("HomeworkDetailActivity", "开始初始化RecyclerView")
            // 初始化图片适配器
            imageAdapter = ImageAdapter(this)
            view.recyclerViewImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            view.recyclerViewImages.adapter = imageAdapter
            Log.d("HomeworkDetailActivity", "recyclerViewImages 初始化完成")
            
            // 初始化已选图片适配器
            view.recyclerViewSelectedImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            view.recyclerViewSelectedImages.adapter = imageAdapter
            Log.d("HomeworkDetailActivity", "recyclerViewSelectedImages 初始化完成")
            
            // 初始化已提交图片适配器
            view.recyclerViewSubmittedImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            view.recyclerViewSubmittedImages.adapter = imageAdapter
            Log.d("HomeworkDetailActivity", "recyclerViewSubmittedImages 初始化完成")
            
            // 设置按钮点击事件
            Log.d("HomeworkDetailActivity", "开始设置按钮点击事件")
            view.btnAddImage.setOnClickListener {
                Log.d("HomeworkDetailActivity", "btnAddImage 点击")
                pickImageFromGallery()
            }
            
            view.btnSubmitHomework.setOnClickListener {
                Log.d("HomeworkDetailActivity", "btnSubmitHomework 点击")
                submitHomework()
            }
            Log.d("HomeworkDetailActivity", "========== initView 完成 ==========")
        } catch (e: Exception) {
            Log.e("HomeworkDetailActivity", "初始化视图时发生错误", e)
            e.printStackTrace()
            finish()
            return
        }
    }

    override fun subscribeUi() {
        // 加载作业详情
        loadHomeworkDetail()
    }

    // 每次回到此页面时重新加载作业详情，确保状态正确显示
    override fun onResume() {
        super.onResume()
        // 只在有数据时才重新加载，避免重复调用
        if (currentHomeworkDetail == null) {
            loadHomeworkDetail()
        }
    }

    private fun loadHomeworkDetail() {
        Log.d("HomeworkDetailActivity", "开始加载作业详情，作业ID: $homeworkId")
        // 显示加载状态
        showLoading()

        RetrofitClient.apiService.getHomeworkDetail(homeworkId).enqueue(object : Callback<BaseResp<StuHomeWorkDetailVO>> {
            override fun onResponse(call: Call<BaseResp<StuHomeWorkDetailVO>>, response: Response<BaseResp<StuHomeWorkDetailVO>>) {
                Log.d("HomeworkDetailActivity", "网络请求响应: ${response.code()}")
                // 隐藏加载状态
                hideLoading()

                if (response.isSuccessful && response.body() != null) {
                    val resp = response.body()
                    Log.d("HomeworkDetailActivity", "响应数据: code=${resp?.code}, data=${resp?.data}")
                    if (resp?.code == 0 && resp.data != null) {
                        // 成功获取作业详情，使用非空断言操作符
                        Log.d("HomeworkDetailActivity", "开始显示作业详情")
                        showHomeworkDetail(resp.data!!)
                    } else {
                        // 显示友好的错误信息，区分作业不存在和其他错误
                        val errorMsg = if (response.code() == 404) "作业不存在或已被删除" else "获取作业详情失败：${resp?.message}"
                        Log.e("HomeworkDetailActivity", "API返回错误: $errorMsg")
                        showError(errorMsg)
                    }
                } else {
                    // 网络请求失败时显示具体错误
                    val errorMsg = if (response.code() == 404) "作业不存在或已被删除" else "网络请求失败（${response.code()}）"
                    Log.e("HomeworkDetailActivity", "网络请求失败: $errorMsg")
                    showError(errorMsg)
                }
            }

            override fun onFailure(call: Call<BaseResp<StuHomeWorkDetailVO>>, t: Throwable) {
                hideLoading()
                showError("网络请求失败：${t.message}")
            }
        })
    }

    private fun showHomeworkDetail(homeworkDetail: StuHomeWorkDetailVO) {
        Log.d("HomeworkDetailActivity", "========== showHomeworkDetail 开始 ==========")
        currentHomeworkDetail = homeworkDetail
        
        try {
            // 设置作业ID
            Log.d("HomeworkDetailActivity", "设置作业ID: ${homeworkDetail.homeworkId}")
            view.tvHomeworkId.text = homeworkDetail.homeworkId.toString()
            
            // 设置科目
            Log.d("HomeworkDetailActivity", "设置科目: ${homeworkDetail.subject}")
            view.tvSubject.text = homeworkDetail.subject
            
            // 设置作业标题
            Log.d("HomeworkDetailActivity", "设置作业标题: ${homeworkDetail.homeworkName}")
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
        
        // 显示内容布局
        Log.d("HomeworkDetailActivity", "显示内容布局")
        view.contentLayout.visibility = View.VISIBLE
        view.loadingLayout.visibility = View.GONE
        view.errorLayout.visibility = View.GONE
        Log.d("HomeworkDetailActivity", "========== showHomeworkDetail 完成 ==========")
        } catch (e: Exception) {
            Log.e("HomeworkDetailActivity", "显示作业详情时发生错误", e)
            e.printStackTrace()
            showError("显示作业详情失败：${e.message}")
        }
    }

    private fun showLoading() {
        Log.d("HomeworkDetailActivity", "显示加载状态")
        // 显示加载状态
        view.loadingLayout.visibility = View.VISIBLE
        view.contentLayout.visibility = View.GONE
        view.errorLayout.visibility = View.GONE
        Log.d("HomeworkDetailActivity", "加载状态设置完成")
    }

    private fun hideLoading() {
        // 隐藏加载状态
        view.loadingLayout.visibility = View.GONE
    }

    private fun showError(message: String) {
        // 显示错误信息
        view.errorMessage.text = message
        view.errorLayout.visibility = View.VISIBLE
        view.contentLayout.visibility = View.GONE
        view.loadingLayout.visibility = View.GONE
        
        // 设置重试按钮
        view.retryButton.setOnClickListener {
            loadHomeworkDetail()
        }
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

                // 创建一个新的适配器实例来显示已选图片，避免与作业详情图片混用
                val selectedImageAdapter = ImageAdapter(this)
                view.recyclerViewSelectedImages.adapter = selectedImageAdapter
                selectedImageAdapter.setImageUrls(selectedImages)
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
        
        // 获取当前用户信息以获取studentId
        showLoading()

        RetrofitClient.apiService.getUserInfo().enqueue(object : Callback<BaseResp<UserInfo>> {
            override fun onResponse(call: Call<BaseResp<UserInfo>>, userResponse: Response<BaseResp<UserInfo>>) {
                if (userResponse.isSuccessful && userResponse.body() != null) {
                    val userInfo = userResponse.body()
                    if (userInfo?.code == 0 && userInfo.data != null) {
                        // 获取到用户信息，准备提交作业数据
                        val studentId = userInfo.data!!.userId.toLong()
                        Log.d("HomeworkDetail", "用户ID: ${userInfo.data!!.userId}, 学生ID: $studentId")

                        // 获取所有课程信息，根据科目名称查找subjectId
                        RetrofitClient.apiService.getAllCourse().enqueue(object : Callback<BaseResp<List<Course>>> {
                            override fun onResponse(call: Call<BaseResp<List<Course>>>, courseResponse: Response<BaseResp<List<Course>>>) {
                                if (courseResponse.isSuccessful && courseResponse.body() != null) {
                                    val courseResp = courseResponse.body()
                                    if (courseResp?.code == 0 && courseResp.data != null) {
                                        // 查找匹配的subjectId
                                        val currentSubjectName = currentHomeworkDetail?.subject
                                        Log.d("HomeworkDetail", "当前作业科目: $currentSubjectName")

                                        var subjectId: Int? = null
                                        if (currentSubjectName != null) {
                                            // 遍历课程列表，查找匹配的科目
                                            for (course in courseResp.data!!) {
                                                Log.d("HomeworkDetail", "课程: ${course.subjectName}, ID: ${course.subjectId}")
                                                if (course.subjectName == currentSubjectName) {
                                                    subjectId = course.subjectId
                                                    break
                                                }
                                            }
                                        }

                                        // 准备提交数据
                                        val submitRequest = SubmitHomeworkRequest(
                                            homeworkId = homeworkId.toLong(),
                                            subjectId = subjectId,
                                            studentId = studentId,
                                            studentContent = if (content.isEmpty()) null else listOf(content)
                                            // 注意：这里简化了图片上传逻辑，实际项目中需要先上传图片获取URL
                                        )

                                        // 获取当前token
                                        val token = TokenManager.getToken() ?: ""
                                        Log.d("HomeworkDetail", "提交作业 - Token: $token")
                                        Log.d("HomeworkDetail", "提交作业 - Request: $submitRequest")

                                        // 发送提交请求，明确传递token
                                        RetrofitClient.apiService.submitHomework(submitRequest, token).enqueue(object : Callback<BaseResp<String>> {
                                            override fun onResponse(call: Call<BaseResp<String>>, response: Response<BaseResp<String>>) {
                                                hideLoading()
                                                Log.d("HomeworkDetail", "提交作业响应码: ${response.code()}")
                                                Log.d("HomeworkDetail", "提交作业响应体: ${response.body()}")
                                                Log.d("HomeworkDetail", "提交作业错误体: ${response.errorBody()?.string()}")

                                                if (response.isSuccessful && response.body() != null) {
                                                    val resp = response.body()
                                                    Log.d("HomeworkDetail", "提交作业响应数据: code=${resp?.code}, message=${resp?.message}")
                                                    if (resp?.code == 0) {
                                            // 提交成功，设置返回结果并重新加载作业详情
                                            showSuccess("作业提交成功")
                                            loadHomeworkDetail()
                                            // 设置返回结果，通知HomeworkListFragment刷新列表
                                            setResult(RESULT_OK)
                                        } else {
                                            showError("作业提交失败：${resp?.message ?: "未知错误"}")
                                        }
                                                } else {
                                                    showError("网络请求失败：响应码 ${response.code()}")
                                                }
                                            }

                                            override fun onFailure(call: Call<BaseResp<String>>, t: Throwable) {
                                                hideLoading()
                                                showError("网络请求失败：${t.message}")
                                            }
                                        })
                                    } else {
                                        hideLoading()
                                        showError("获取课程信息失败：${courseResp?.message}")
                                    }
                                } else {
                                    hideLoading()
                                    showError("获取课程信息网络请求失败")
                                }
                            }

                            override fun onFailure(call: Call<BaseResp<List<Course>>>, t: Throwable) {
                                hideLoading()
                                showError("获取课程信息网络请求失败：${t.message}")
                            }
                        })
                    } else {
                        hideLoading()
                        showError("获取用户信息失败：${userInfo?.message}")
                    }
                } else {
                    hideLoading()
                    showError("获取用户信息网络请求失败")
                }
            }

            override fun onFailure(call: Call<BaseResp<UserInfo>>, t: Throwable) {
                hideLoading()
                showError("获取用户信息网络请求失败：${t.message}")
            }
        })
        
        // 准备提交数据
        val submitRequest = SubmitHomeworkRequest(
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