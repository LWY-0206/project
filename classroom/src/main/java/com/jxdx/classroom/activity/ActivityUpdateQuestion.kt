package com.jxdx.classroom.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.corekit.common.BaseActivity
import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.jxdx.classroom.databinding.ExerciseUploadActivityBinding
import com.jxdx.classroom.http.WhiteboardApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class ActivityUpdateQuestion: BaseActivity<ExerciseUploadActivityBinding>() {
    private val TAG = "ActivityUpdateQuestion"
    
    // 获取binding对象，用于访问布局中的控件
    private lateinit var binding: ExerciseUploadActivityBinding
    
    // 图片上传API
    private val whiteboardApi: WhiteboardApi by lazy {
        HttpManager.instance.service(WhiteboardApi::class.java)
    }
    
    // 图片列表，用于存储上传成功的图片URL
    private val imageUrls = mutableListOf<String>()
    
    // 图片URI列表，用于本地预览
    private val imageUris = mutableListOf<Uri>()
    
    // 图片容器列表
    private lateinit var imageContainers: List<View>
    
    // SharedPreferences用于持久化存储图片URL
    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "exercise_images"
    private val KEY_IMAGE_URLS = "image_urls"

    override fun bindLayout(): ExerciseUploadActivityBinding {
        binding = ExerciseUploadActivityBinding.inflate(layoutInflater)
        return binding
    }

    override fun initView() {
        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // 为返回按钮设置点击事件
        binding.btnBack.setOnClickListener {
            Log.d(TAG, "返回按钮被点击，执行返回操作")
            finish()
        }
        
        // 为添加按钮设置点击事件，用于选择图片
        binding.btnAdd.setOnClickListener {
            Log.d(TAG, "添加按钮被点击，选择图片")
            openGallery()
        }
        
        // 初始化图片容器列表
        imageContainers = listOf(
            binding.exercise1, binding.exercise2, binding.exercise3, binding.exercise4,
            binding.exercise5, binding.exercise6, binding.exercise7, binding.exercise8
        )
    }

    override fun subscribeUi() {
        // 加载保存的图片URL
        loadSavedImageUrls()
    }
    
    // 从SharedPreferences加载保存的图片URL
    private fun loadSavedImageUrls() {
        val savedUrlsJson = sharedPreferences.getString(KEY_IMAGE_URLS, "[]")
        try {
            val jsonArray = JSONArray(savedUrlsJson)
            for (i in 0 until jsonArray.length()) {
                val imageUrl = jsonArray.getString(i)
                imageUrls.add(imageUrl)
                // 将加载的图片显示在网格中
                loadSavedImageToGrid(imageUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载保存的图片URL失败: ${e.message}")
        }
    }
    
    // 将保存的图片加载到网格中
    private fun loadSavedImageToGrid(imageUrl: String) {
        // 找到第一个未使用的格子
        for (i in imageContainers.indices) {
            val container = imageContainers[i]
            if (container !is ImageView) {
                // 将View替换为ImageView
                val parent = container.parent as View
                val layoutParams = container.layoutParams
                
                // 移除原View
                val linearLayout = parent as android.widget.LinearLayout
                linearLayout.removeView(container)
                
                // 创建并添加ImageView
                val imageView = ImageView(this)
                imageView.layoutParams = layoutParams
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                
                // 使用Glide加载网络图片
                Glide.with(this).load(imageUrl).into(imageView)
                
                // 添加长按删除功能
                imageView.setOnLongClickListener {
                    // 显示确认删除对话框
                    showDeleteConfirmDialog(i, imageUrl)
                    true
                }
                
                linearLayout.addView(imageView, i)
                
                // 更新图片容器列表
                imageContainers = imageContainers.toMutableList().also {
                    it[i] = imageView
                }
                
                // 打印日志，保存的图片已加载到格子中
                Log.d(TAG, "保存的图片已加载到格子中: $i, URL: $imageUrl")
                break
            }
        }
    }
    
    // 保存图片URL列表到SharedPreferences
    private fun saveImageUrls() {
        val jsonArray = JSONArray()
        for (url in imageUrls) {
            jsonArray.put(url)
        }
        sharedPreferences.edit()
            .putString(KEY_IMAGE_URLS, jsonArray.toString())
            .apply()
        Log.d(TAG, "图片URL列表已保存")
    }

    // 重写系统返回键方法
    override fun onBackPressed() {
        // 保存图片URL列表
        saveImageUrls()
        super.onBackPressed()
    }
    
    // 重写onDestroy方法，确保退出时保存数据
    override fun onDestroy() {
        // 保存图片URL列表
        saveImageUrls()
        super.onDestroy()
    }
    
    // 打开系统相册选择图片
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }
    
    // 处理图片选择结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                // 保存URI用于本地预览
                imageUris.add(imageUri)
                
                // 将URI转换为文件路径
                val filePath = uriToFilePath(imageUri)
                if (filePath != null) {
                    // 上传文件
                    uploadImage(filePath)
                } else {
                    Log.e(TAG, "无法获取图片文件路径")
                }
            }
        }
    }
    
    // 将URI转换为文件路径
    private fun uriToFilePath(uri: Uri): String? = try {
        val inputStream = contentResolver.openInputStream(uri)
        val cacheDir = getExternalFilesDir("images")
        if (inputStream != null && cacheDir != null) {
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val fileName = "${UUID.randomUUID()}.jpg"
            val file = File(cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file.absolutePath
        } else {
            null
        }
    } catch (e: IOException) {
        Log.e(TAG, "URI转文件失败: ${e.message}")
        null
    }
    
    // 上传图片
    private fun uploadImage(file: String) {
        lifecycleScope.launchWhenCreated {
            try {
                // 创建File对象
                val fileObj = File(file)

                // 创建RequestBody
                val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), fileObj)

                // 创建MultipartBody.Part
                val filePart = MultipartBody.Part.createFormData("fileList", fileObj.name, requestBody)
                
                // 发送请求
                val result = whiteboardApi.uploadFile(
                    TokenManager.getToken().toString(),
                    filePart
                )
                
                // 处理上传结果
                val data = result.data
                if (data != null && data.isNotEmpty()) {
                    val imageUrl = data[0]
                    imageUrls.add(imageUrl)
                    Log.d(TAG, "图片上传成功，URL: $imageUrl")
                    // 将图片加载到格子中
                    loadImageToGrid(imageUrl)
                    // 保存图片URL列表
                    saveImageUrls()
                } else {
                    Log.e(TAG, "图片上传失败: ${result.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "图片上传异常: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    // 将图片加载到格子中
    private fun loadImageToGrid(imageUrl: String) {
        // 找到第一个未使用的格子
        for (i in imageContainers.indices) {
            val container = imageContainers[i]
            if (container !is ImageView) {
                // 将View替换为ImageView
                val parent = container.parent as View
                val layoutParams = container.layoutParams
                
                // 移除原View
                val linearLayout = parent as android.widget.LinearLayout
                linearLayout.removeView(container)
                
                // 创建并添加ImageView
                val imageView = ImageView(this)
                imageView.layoutParams = layoutParams
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                
                // 如果有对应的本地URI，先显示本地图片进行预览
                if (i < imageUris.size) {
                    imageView.setImageURI(imageUris[i])
                }
                // 同时异步加载网络图片，确保重新进入应用时能显示
                Glide.with(this).load(imageUrl).into(imageView)
                
                // 添加长按删除功能
                imageView.setOnLongClickListener {
                    // 显示确认删除对话框
                    showDeleteConfirmDialog(i, imageUrl)
                    true
                }
                
                linearLayout.addView(imageView, i)
                
                // 更新图片容器列表
                imageContainers = imageContainers.toMutableList().also {
                    it[i] = imageView
                }
                
                // 打印日志，图片已加载到格子中
                Log.d(TAG, "图片已加载到格子中: $i, URL: $imageUrl")
                break
            }
        }
    }
    
    // 显示删除确认对话框
    private fun showDeleteConfirmDialog(position: Int, imageUrl: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("删除图片")
        builder.setMessage("确定要删除这张图片吗？")
        builder.setPositiveButton("确定") { _, _ ->
            // 执行删除操作
            deleteImage(position, imageUrl)
        }
        builder.setNegativeButton("取消", null)
        builder.show()
    }
    
    // 删除图片的逻辑
    private fun deleteImage(position: Int, imageUrl: String) {
        try {
            // 找到对应的ImageView
            val imageView = imageContainers[position] as? ImageView
            if (imageView != null) {
                // 获取父容器
                val parent = imageView.parent as android.widget.LinearLayout
                val layoutParams = imageView.layoutParams
                
                // 移除ImageView
                parent.removeView(imageView)
                
                // 创建新的空白View并添加到原来的位置
                val emptyView = View(this)
                emptyView.layoutParams = layoutParams
                // 设置与XML布局中默认空白格子相同的背景色(#F0F0F0)
                emptyView.setBackgroundColor(0xFFF0F0F0.toInt())
                parent.addView(emptyView, position)
                
                // 更新图片容器列表
                imageContainers = imageContainers.toMutableList().also {
                    it[position] = emptyView
                }
                
                // 从数据列表中移除对应的元素
                if (imageUrls.contains(imageUrl)) {
                    imageUrls.remove(imageUrl)
                }
                
                // 如果imageUris的大小大于position，也移除对应的URI
                if (position < imageUris.size) {
                    imageUris.removeAt(position)
                }
                
                // 保存更新后的图片URL列表
                saveImageUrls()
                
                Log.d(TAG, "图片已删除: $position, URL: $imageUrl")
            }
        } catch (e: Exception) {
            Log.e(TAG, "删除图片失败: ${e.message}")
        }
    }
    
    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 1001
    }
}