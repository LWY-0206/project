package com.jxdx.mine.course

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.R
import com.jxdx.mine.databinding.ActivityUploadCoursewareInfoBinding
import com.jxdx.mine.http.ClassInfo
import com.jxdx.mine.http.RetrofitClient
import com.jxdx.mine.http.UploadCourseWareRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class UploadCoursewareInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadCoursewareInfoBinding
    private var subjectId: Int = -1
    private var fileName: String = ""
    private var selectedFileUri: Uri? = null
    private var fileUrl: String = ""
    private val FILE_PICK_REQUEST_CODE = 1003



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadCoursewareInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取从上一个页面传递过来的参数
        subjectId = intent.getIntExtra("subjectId", -1)
        Log.d("UploadCoursewareInfo", "接收到的subjectId: $subjectId")

        // 初始化文件名显示
        binding.tvFileName.text = "未选择文件"

        // 设置返回按钮点击事件
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 设置选择文件按钮点击事件
        binding.btnSelectFile.setOnClickListener {
            showFilePicker()
        }

        // 获取老师对应学科的上课班级
        if (subjectId != -1) {
            getTeacherSubjectClasses(subjectId)
        }

        // 设置提交按钮点击事件
        binding.btnSubmit.setOnClickListener {
            submitCourseware()
        }
    }

    // 获取老师对应学科的上课班级
    private fun getTeacherSubjectClasses(subjectId: Int) {
        RetrofitClient.apiService.getTeacherSubjectClass(subjectId).enqueue(object : Callback<BaseResp<List<ClassInfo>>> {
            override fun onResponse(call: Call<BaseResp<List<ClassInfo>>>, response: Response<BaseResp<List<ClassInfo>>>) {
                if (response.isSuccessful && response.body() != null) {
                    val resp = response.body()
                    if (resp?.code == 0) {
                        resp.data?.let { classList ->
                            // 格式化班级信息显示
                            val classInfoText = classList.joinToString(" ") { "(${it.classId}   ${it.className})" }
                            binding.tvClass.text = "当前老师对应学科的上课班级：$classInfoText"
                        }
                    } else {
                        binding.tvClass.text = "获取班级信息失败：${resp?.message}"
                    }
                } else {
                    binding.tvClass.text = "获取班级信息请求失败"
                }
            }

            override fun onFailure(call: Call<BaseResp<List<ClassInfo>>>, t: Throwable) {
                binding.tvClass.text = "获取班级信息网络异常：${t.message}"
            }
        })
    }

    // 显示文件选择器
    private fun showFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, FILE_PICK_REQUEST_CODE)
    }

    // 从URI获取文件名
    private fun getFileNameFromUri(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex("_display_name")
                if (displayNameIndex != -1) {
                    return it.getString(displayNameIndex)
                }
            }
        }
        return "unknown_file"
    }

    // 将URI转换为临时文件
    private fun createTempFileFromUri(uri: Uri): File {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File.createTempFile("courseware_${System.currentTimeMillis()}", ".tmp", cacheDir)
        
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    // 上传文件到服务器
    private fun uploadFile(uri: Uri, fileName: String) {
        // 显示加载提示
        Toast.makeText(this, "正在上传文件...", Toast.LENGTH_SHORT).show()
        
        try {
            // 将URI转换为临时文件
            val file = createTempFileFromUri(uri)
            
            // 创建RequestBody
            val requestFile = RequestBody.create("application/octet-stream".toMediaTypeOrNull(), file)
            
            // 创建MultipartBody.Part
            val filePart = MultipartBody.Part.createFormData("fileList", fileName, requestFile)
            
            Log.d("UploadCoursewareInfo", "开始上传文件: $fileName, 文件大小: ${file.length()/1024}KB")
            // 调用文件上传API
            RetrofitClient.apiService.uploadFile(filePart).enqueue(object : Callback<BaseResp<List<String>>> {
                override fun onResponse(
                    call: Call<BaseResp<List<String>>>,
                    response: Response<BaseResp<List<String>>> 
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val resp = response.body()
                        if (resp?.code == 0) {
                            resp.data?.let { urls ->
                                if (urls.isNotEmpty()) {
                                    // 文件上传成功，获取文件URL
                                    fileUrl = urls[0]
                                    Log.d("UploadCoursewareInfo", "文件上传成功，获取到的URL: $fileUrl")
                                    Toast.makeText(this@UploadCoursewareInfoActivity, "文件上传成功", Toast.LENGTH_SHORT).show()
                                } else {
                                    Log.d("UploadCoursewareInfo", "文件上传成功但未返回URL")
                                    Toast.makeText(this@UploadCoursewareInfoActivity, "文件上传成功但未返回URL", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Log.e("UploadCoursewareInfo", "文件上传失败：${resp?.code} - ${resp?.message}")
                            Toast.makeText(this@UploadCoursewareInfoActivity, "文件上传失败：${resp?.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                            Toast.makeText(this@UploadCoursewareInfoActivity, "文件上传请求失败", Toast.LENGTH_SHORT).show()
                        }
                }
                  
                override fun onFailure(
                    call: Call<BaseResp<List<String>>>,
                    t: Throwable
                ) {
                    Toast.makeText(this@UploadCoursewareInfoActivity, "文件上传失败：${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, "文件处理错误：${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 处理Activity返回结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICK_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                fileName = getFileNameFromUri(uri)
                binding.tvFileName.text = fileName
                Log.d("UploadCoursewareInfo", "选择的文件: $fileName, URI: $uri")
                // 上传文件
                uploadFile(uri, fileName)
            }
        }
    }



    /** 提交课件信息 */
    private fun submitCourseware() {
        // 获取用户输入的描述
        val description = binding.etDescription.text.toString().trim()
        
        // 获取用户输入的班级ID，使用空格分割并转换为整数列表
        val classIdsText = binding.etClassIds.text.toString().trim()
        val selectedClassIds = classIdsText.split(" ")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { 
                try {
                    it.toInt()
                } catch (e: NumberFormatException) {
                    null
                }
            }

        // 验证必填项
        if (selectedFileUri == null) {
            Toast.makeText(this, "请先选择并上传文件", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (fileUrl.isEmpty()) {
            Toast.makeText(this, "文件上传尚未完成，请等待", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "请输入课件描述", Toast.LENGTH_SHORT).show()
            return
        }

        if (classIdsText.isEmpty()) {
            Toast.makeText(this, "请输入班级ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedClassIds.isEmpty()) {
            Toast.makeText(this, "请输入有效的班级ID（数字）", Toast.LENGTH_SHORT).show()
            return
        }

        // 显示加载提示
        Toast.makeText(this, "正在提交课件信息...", Toast.LENGTH_SHORT).show()

        // 创建上传课件请求
            val courseWareRequest = UploadCourseWareRequest(
                subjectId = subjectId,
                fileUrl = fileUrl,
                fileDescription = description,
                classIds = selectedClassIds
            )
            
            Log.d("UploadCoursewareInfo", "提交课件信息：subjectId=$subjectId, fileUrl=$fileUrl, description=$description, classIds=$selectedClassIds")

        // 提交课件信息
        RetrofitClient.apiService.uploadCourseWare(courseWareRequest).enqueue(object : Callback<BaseResp<String>> {
            override fun onResponse(call: Call<BaseResp<String>>, response: Response<BaseResp<String>>) {
                if (response.isSuccessful && response.body() != null) {
                    val courseWareResp = response.body()
                    if (courseWareResp?.code == 0) {
                        Log.d("UploadCoursewareInfo", "课件信息提交成功")
                        Toast.makeText(this@UploadCoursewareInfoActivity, "上传成功", Toast.LENGTH_SHORT).show()
                        // 设置结果并返回上一个页面
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Log.e("UploadCoursewareInfo", "课件信息提交失败：${courseWareResp?.code} - ${courseWareResp?.message}")
                        Toast.makeText(this@UploadCoursewareInfoActivity, "课件信息提交失败：${courseWareResp?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@UploadCoursewareInfoActivity, "课件信息提交请求失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResp<String>>, t: Throwable) {
                Toast.makeText(this@UploadCoursewareInfoActivity, "课件信息提交失败：${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}