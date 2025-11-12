package com.jxdx.mine.teacherhomework

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.jxdx.mine.databinding.ActivityCreateHomeworkBinding
import com.jxdx.mine.http.ApiService
import com.jxdx.mine.http.RetrofitClient
import com.jxdx.mine.http.request.CreateHomeworkRequest
import com.jxdx.mine.teacherhomework.adapter.ImageSelectionAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CreateHomeworkActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateHomeworkBinding
    private lateinit var imageAdapter: ImageSelectionAdapter
    private val selectedImages = mutableListOf<Uri>()
    private var courseId: String? = null
    private var courseName: String? = null
    
    // 日期选择器
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var selectedDate: Date? = null

    // 图片选择器
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages.addAll(uris)
            imageAdapter.notifyDataSetChanged()
            updateImageCount()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateHomeworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取课程信息
        courseId = intent.getStringExtra("courseId")
        courseName = intent.getStringExtra("courseName")
        
        supportActionBar?.title = "创建作业 - $courseName"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        // 初始化图片选择RecyclerView
        imageAdapter = ImageSelectionAdapter(selectedImages) { uri ->
            // 删除图片
            selectedImages.remove(uri)
            imageAdapter.notifyDataSetChanged()
            updateImageCount()
        }
        
        binding.recyclerViewImages.apply {
            layoutManager = GridLayoutManager(this@CreateHomeworkActivity, 3)
            adapter = imageAdapter
        }
        
        updateImageCount()
    }

    private fun setupListeners() {
        // 返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 选择截止日期
        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        // 添加图片
        binding.btnAddImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // 发布作业
        binding.btnPublish.setOnClickListener {
            publishHomework()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }.time
                binding.btnSelectDate.text = dateFormat.format(selectedDate!!)
            },
            year, month, day
        ).show()
    }

    private fun updateImageCount() {
        binding.tvImageCount.text = "已选择 ${selectedImages.size} 张图片"
    }

    private fun publishHomework() {
        // 验证输入
        val title = binding.etHomeworkTitle.text.toString().trim()
        val content = binding.etHomeworkContent.text.toString().trim()
        
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入作业标题", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入作业内容", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedDate == null) {
            Toast.makeText(this, "请选择截止日期", Toast.LENGTH_SHORT).show()
            return
        }

        // 显示加载状态
        binding.btnPublish.isEnabled = false
        binding.btnPublish.text = "发布中..."

        // 上传图片并获取真实URL
        uploadImagesAndCreateHomework(title, content)
    }
    
    private fun uploadImagesAndCreateHomework(title: String, content: String) {
        if (selectedImages.isEmpty()) {
            // 没有图片，直接创建作业
            createHomeworkWithUrls(title, content, emptyList())
            return
        }
        
        // 显示上传进度
        binding.btnPublish.text = "上传图片中..."
        
        uploadImagesToServer(selectedImages) { uploadedUrls ->
            createHomeworkWithUrls(title, content, uploadedUrls)
        }
    }
    
    private fun uploadImagesToServer(images: List<Uri>, callback: (List<String>) -> Unit) {
        val uploadedUrls = mutableListOf<String>()
        var completedCount = 0
        
        if (images.isEmpty()) {
            callback(emptyList())
            return
        }
        
        // 逐个上传图片
        images.forEachIndexed { index, imageUri ->
            uploadSingleImage(imageUri) { success, url ->
                if (success && url != null) {
                    uploadedUrls.add(url)
                }
                
                completedCount++
                if (completedCount == images.size) {
                    // 所有图片上传完成
                    callback(uploadedUrls)
                }
            }
        }
    }
    
    private fun uploadSingleImage(imageUri: Uri, callback: (Boolean, String?) -> Unit) {
        try {
            // 使用正确的方式处理Uri
            val inputStream = contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                callback(false, null)
                return
            }
            
            // 读取图片数据
            val imageBytes = inputStream.readBytes()
            inputStream.close()
            
            // 创建文件名
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            
            // 创建RequestBody
            val requestBody = imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("fileList", fileName, requestBody)
            
            // 调用上传API
            RetrofitClient.apiService.uploadImage(multipartBody).enqueue(object : Callback<com.example.corekit.http.bean.BaseResp<List<String>>> {
                override fun onResponse(
                    call: Call<com.example.corekit.http.bean.BaseResp<List<String>>>,
                    response: Response<com.example.corekit.http.bean.BaseResp<List<String>>>
                ) {
                    if (response.isSuccessful && response.body()?.code == 0) {
                        val imageUrls = response.body()?.data
                        val imageUrl = imageUrls?.firstOrNull()
                        callback(true, imageUrl)
                    } else {
                        callback(false, null)
                    }
                }
                
                override fun onFailure(call: Call<com.example.corekit.http.bean.BaseResp<List<String>>>, t: Throwable) {
                    callback(false, null)
                }
            })
        } catch (e: Exception) {
            callback(false, null)
        }
    }
    
    private fun createHomeworkWithUrls(title: String, content: String, imageUrls: List<String>) {
        // 更新按钮状态
        binding.btnPublish.text = "发布作业中..."

        // 检查courseId是否有效
        if (courseId.isNullOrEmpty()) {
            Toast.makeText(this, "课程信息缺失，无法创建作业", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 创建作业请求 - 按照新的API格式
        val request = CreateHomeworkRequest(
            subjectId = courseId?.toInt(), // 使用课程ID
            homeworkName = title,
            homeworkContent = content,
            deadTime = dateFormat.format(selectedDate!!),
            imageUrls = if (imageUrls.isEmpty()) null else imageUrls
        )

        // 添加详细的调试日志
        Log.d("CreateHomework", "=== 创建作业请求数据 ===")
        Log.d("CreateHomework", "courseId: $courseId")
        Log.d("CreateHomework", "courseName: $courseName")
        Log.d("CreateHomework", "subjectId: ${request.subjectId}")
        Log.d("CreateHomework", "title: $title")
        Log.d("CreateHomework", "content: $content")
        Log.d("CreateHomework", "deadTime: ${dateFormat.format(selectedDate!!)}")
        Log.d("CreateHomework", "imageUrls: $imageUrls")
        Log.d("CreateHomework", "Request: $request")
        
        // 调用API
        RetrofitClient.apiService.createHomework(request).enqueue(object : Callback<com.example.corekit.http.bean.BaseResp<String>> {
            override fun onResponse(
                call: Call<com.example.corekit.http.bean.BaseResp<String>>,
                response: Response<com.example.corekit.http.bean.BaseResp<String>>
            ) {
                binding.btnPublish.isEnabled = true
                binding.btnPublish.text = "发布作业"
                
                if (response.isSuccessful && response.body()?.code == 0) {
                    Toast.makeText(this@CreateHomeworkActivity, "作业创建成功", Toast.LENGTH_SHORT).show()
                    // 返回作业列表页面
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorMsg = response.body()?.message ?: "HTTP ${response.code()}: ${response.message()}"
                    Log.e("CreateHomework", "=== API 错误信息 ===")
                    Log.e("CreateHomework", "Response code: ${response.code()}")
                    Log.e("CreateHomework", "Response message: ${response.message()}")
                    Log.e("CreateHomework", "Response body: ${response.body()}")
                    Log.e("CreateHomework", "Error message: $errorMsg")
                    Toast.makeText(this@CreateHomeworkActivity, "作业创建失败: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<com.example.corekit.http.bean.BaseResp<String>>, t: Throwable) {
                binding.btnPublish.isEnabled = true
                binding.btnPublish.text = "发布作业"
                Toast.makeText(this@CreateHomeworkActivity, "网络异常，作业创建失败", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
