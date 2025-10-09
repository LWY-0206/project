package com.jxdx.mine.teacherhomework

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.jxdx.mine.databinding.ActivityEditHomeworkBinding
import com.jxdx.mine.http.ApiService
import com.jxdx.mine.http.RetrofitClient
import com.jxdx.mine.http.request.EditHomeworkRequest
import com.jxdx.mine.teacherhomework.adapter.MixedImageAdapter
import com.jxdx.mine.teacherhomework.adapter.ImageItem
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class EditHomeworkActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditHomeworkBinding
    private lateinit var imageAdapter: MixedImageAdapter
    private val selectedImages = mutableListOf<ImageItem>()
    private var homeworkId: String? = null
    private var subjectId: String? = null
    private var subjectName: String? = null
    
    // 日期选择器
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var selectedDate: Date? = null

    // 图片选择器
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                selectedImages.add(ImageItem(uri = uri, isExisting = false))
            }
            imageAdapter.notifyDataSetChanged()
            updateImageCount()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditHomeworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取作业信息
        homeworkId = intent.getStringExtra("homeworkId")
        val homeworkTitle = intent.getStringExtra("homeworkTitle")
        val homeworkContent = intent.getStringExtra("homeworkContent")
        val deadTime = intent.getStringExtra("deadTime")
        subjectId = intent.getStringExtra("subjectId")
        subjectName = intent.getStringExtra("subjectName")
        val imageUrls = intent.getStringArrayExtra("imageUrls")
        
        // 设置MaterialToolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "编辑作业"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // 设置返回按钮点击事件
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        initViews()
        setupListeners()
        loadHomeworkDetail(homeworkTitle, homeworkContent, deadTime, imageUrls)
    }

    private fun initViews() {
        // 初始化图片选择RecyclerView
        imageAdapter = MixedImageAdapter(selectedImages) { imageItem ->
            // 删除图片
            imageAdapter.removeImage(imageItem)
            updateImageCount()
        }
        
        binding.recyclerViewImages.apply {
            layoutManager = GridLayoutManager(this@EditHomeworkActivity, 3)
            adapter = imageAdapter
        }
        
        updateImageCount()
    }

    private fun setupListeners() {
        // 选择截止日期
        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        // 添加图片
        binding.btnAddImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // 保存作业
        binding.btnSave.setOnClickListener {
            saveHomework()
        }
    }

    private fun loadHomeworkDetail(title: String?, content: String?, deadTime: String?, imageUrls: Array<String>?) {
        // 填充作业标题
        binding.etHomeworkTitle.setText(title ?: "")
        
        // 填充作业内容
        binding.etHomeworkContent.setText(content ?: "")
        
        // 设置截止日期
        if (deadTime != null) {
            try {
                selectedDate = dateFormat.parse(deadTime)
                binding.btnSelectDate.text = deadTime
            } catch (e: Exception) {
                // 如果解析失败，使用当前时间
                selectedDate = Date()
                binding.btnSelectDate.text = dateFormat.format(selectedDate!!)
            }
        } else {
            selectedDate = Date()
            binding.btnSelectDate.text = dateFormat.format(selectedDate!!)
        }
        
        // 处理现有图片
        if (imageUrls != null && imageUrls.isNotEmpty()) {
            // 将现有图片URL添加到列表中
            imageUrls.forEach { url ->
                selectedImages.add(ImageItem(url = url, isExisting = true))
            }
            imageAdapter.notifyDataSetChanged()
            updateImageCount()
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

    private fun saveHomework() {
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
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "保存中..."

        // 上传图片并保存作业
        uploadImagesAndSaveHomework(title, content)
    }
    
    private fun uploadImagesAndSaveHomework(title: String, content: String) {
        // 分离新图片和现有图片
        val newImages = selectedImages.filter { !it.isExisting && it.uri != null }
        val existingUrls = selectedImages.filter { it.isExisting && it.url != null }.map { it.url!! }
        
        if (newImages.isEmpty()) {
            // 没有新图片，直接使用现有图片URL保存作业
            saveHomeworkWithUrls(title, content, existingUrls)
            return
        }
        
        // 显示上传进度
        binding.btnSave.text = "上传图片中..."
        
        // 只上传新图片
        uploadNewImagesToServer(newImages) { uploadedUrls ->
            // 合并现有URL和新上传的URL
            val allUrls = existingUrls + uploadedUrls
            saveHomeworkWithUrls(title, content, allUrls)
        }
    }
    
    private fun uploadNewImagesToServer(images: List<ImageItem>, callback: (List<String>) -> Unit) {
        val uploadedUrls = mutableListOf<String>()
        var completedCount = 0
        
        if (images.isEmpty()) {
            callback(emptyList())
            return
        }
        
        // 逐个上传新图片
        images.forEachIndexed { index, imageItem ->
            imageItem.uri?.let { uri ->
                uploadSingleImage(uri) { success, url ->
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
    
    private fun saveHomeworkWithUrls(title: String, content: String, imageUrls: List<String>) {
        // 更新按钮状态
        binding.btnSave.text = "保存作业中..."

        // 创建编辑作业请求 - 按照编辑作业API格式
        val request = EditHomeworkRequest(
            homeworkId = try {
                homeworkId?.toLongOrNull()
            } catch (e: NumberFormatException) {
                null
            },
            subjectId = try {
                subjectId?.toIntOrNull() ?: 1 // 如果转换失败，使用默认值1
            } catch (e: NumberFormatException) {
                1 // 使用默认值
            },
            homeworkName = title,
            homeworkContent = content,
            deadTime = dateFormat.format(selectedDate!!),
            imageUrls = if (imageUrls.isEmpty()) null else imageUrls
        )

        // 调用编辑作业API
        RetrofitClient.apiService.editHomework(request).enqueue(object : Callback<com.example.corekit.http.bean.BaseResp<String>> {
            override fun onResponse(
                call: Call<com.example.corekit.http.bean.BaseResp<String>>,
                response: Response<com.example.corekit.http.bean.BaseResp<String>>
            ) {
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "保存作业"
                
                if (response.isSuccessful && response.body()?.code == 0) {
                    Toast.makeText(this@EditHomeworkActivity, "作业保存成功", Toast.LENGTH_SHORT).show()
                    // 返回作业详情页面
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorMsg = response.body()?.message ?: "HTTP ${response.code()}: ${response.message()}"
                    Toast.makeText(this@EditHomeworkActivity, "作业保存失败: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<com.example.corekit.http.bean.BaseResp<String>>, t: Throwable) {
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "保存作业"
                Toast.makeText(this@EditHomeworkActivity, "网络异常，作业保存失败", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
