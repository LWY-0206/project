package com.jxdx.mine.course

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
 import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.viewModels
import com.jxdx.mine.databinding.ActivityCourseDetailBinding
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import android.os.Build
import com.jxdx.mine.R
import com.example.corekit.R as CoreR

class CourseListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseDetailBinding
    private lateinit var adapter: CourseListAdapter
    private val viewModel: CourseListViewModel by viewModels()

    private var lastDownloadUrl: String? = null
    private var lastDownloadFileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvCourseName.text=intent.getStringExtra("subjectName")
        binding.tvTeacherName.text=intent.getStringExtra("teacherName")
        
        // 为返回按钮添加点击事件
        binding.backButton.setOnClickListener{
            finish()
        }


        var courseId = intent.getIntExtra("courseId", -1)
        Log.d("CourseListActivity", "courseId: $courseId")

        adapter = CourseListAdapter(
            onPreviewClick = { courseware -> 
                // 使用WebView预览文件
                val intent = Intent(this, PreviewActivity::class.java)
                intent.putExtra("url", courseware.url)
                intent.putExtra("title", courseware.CoursewareName)
                startActivity(intent)
            },
            onDownloadClick = { courseware -> 
                // 实现文件下载功能
                downloadFile(courseware.url, courseware.CoursewareName)
            }
        )

        binding.recyclerViewCourseware.adapter = adapter
        binding.recyclerViewCourseware.layoutManager = LinearLayoutManager(this)


        // 观察课件列表
        viewModel.courseWareList.observe(this) {
            Log.d("CourseListActivity", "coursewareList: $it")
            adapter.submitList(it)
        }

        // 观察学习报告
        viewModel.studyReport.observe(this) {
            binding.tvTotalStudyTime.text = "总学习时长：${it.totalStudyTime}"
            binding.tvHomeworkProgress.text = "已完成作业：${it.completedHomework}/${it.totalHomework}"
            binding.tvAverageScore.text = "平均得分：${it.averageScore}"
        }
        
        // 设置学习报告点击事件，跳转到详细报告页面
        binding.tvTotalStudyTime.setOnClickListener {
            val courseName = intent.getStringExtra("subjectName") ?: "课程详情"
            val intent = Intent(this, StudyReportActivitySimple::class.java)
            intent.putExtra("courseName", courseName)
            startActivity(intent)
        }

        viewModel.loadCourseDetail(courseId)
        
        // 设置上传按钮点击事件
        binding.btnUploadCourseware.setOnClickListener {
            // 老师身份，直接跳转到上传信息页面
            val courseId = intent.getIntExtra("courseId", -1)
            val intent = Intent(this, UploadCoursewareInfoActivity::class.java)
            intent.putExtra("subjectId", courseId)
            startActivity(intent)
        }
        
        // 保存身份信息
        val identity = intent.getIntExtra("identity", 0)
        // 只有老师身份才显示上传按钮
        if (identity != 1) {
            binding.btnUploadCourseware.visibility = android.view.View.GONE
        }
    }
    
    // 加载菜单
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_course_list, menu)
        return true
    }
    
    // 处理菜单点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_upload -> {
                // 处理上传课件点击事件
                val identity = intent.getIntExtra("identity", 0)
                if (identity == 1) {
                    // 老师身份，直接跳转到上传信息页面
                    val courseId = intent.getIntExtra("courseId", -1)
                    val intent = Intent(this, UploadCoursewareInfoActivity::class.java)
                    intent.putExtra("subjectId", courseId)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "只有老师可以上传课件", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun downloadFile(url: String, fileName: String) {
        Log.d("CourseListActivity", "开始下载: $url, 文件名: $fileName")
        
        // 对于Android 10及以上版本，WRITE_EXTERNAL_STORAGE权限不是必需的
        // 但对于Android 6-9，我们仍然需要检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 保存最后一次尝试下载的文件信息
                lastDownloadUrl = url
                lastDownloadFileName = fileName
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                return
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val urlConnection = URL(url).openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection.connectTimeout = 30000 // 30秒超时
                urlConnection.readTimeout = 60000 // 60秒超时
                urlConnection.connect()

                val responseCode = urlConnection.responseCode
                Log.d("CourseListActivity", "下载响应码: $responseCode")
                
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@CourseListActivity, "下载失败: 服务器返回非200状态码", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // 获取文件大小
                val fileSize = urlConnection.contentLengthLong
                Log.d("CourseListActivity", "文件大小: $fileSize 字节")

                // 确保文件名有效
                val safeFileName = if (fileName.isNotEmpty()) fileName else "courseware_${System.currentTimeMillis()}.pdf"
                
                // 创建文件输出流（兼容Android 10及以上版本）
                val outputStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10及以上，使用MediaStore
                    val contentValues = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.Downloads.DISPLAY_NAME, safeFileName)
                        put(android.provider.MediaStore.Downloads.MIME_TYPE, getMimeType(safeFileName))
                        put(android.provider.MediaStore.Downloads.RELATIVE_PATH, "Download/")
                    }
                    
                    val uri = contentResolver.insert(
                        android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        contentValues
                    )
                    
                    if (uri != null) {
                        contentResolver.openOutputStream(uri)
                    } else {
                        null
                    }
                } else {
                    // Android 9及以下，使用传统文件存储
                    val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs()
                    }
                    val file = java.io.File(downloadDir, safeFileName)
                    java.io.FileOutputStream(file)
                }

                if (outputStream == null) {
                    Log.e("CourseListActivity", "无法创建输出流")
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@CourseListActivity, "下载失败: 无法创建文件", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val inputStream = urlConnection.inputStream

                val buffer = ByteArray(4096)
                var bytesRead: Int
                var totalBytesRead = 0
                
                // 在UI线程显示下载开始的提示
                launch(Dispatchers.Main) {
                    Toast.makeText(this@CourseListActivity, "开始下载: $safeFileName", Toast.LENGTH_SHORT).show()
                }
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    // 每下载100KB记录一次日志
                    if (totalBytesRead % 102400 == 0) {
                        Log.d("CourseListActivity", "已下载: $totalBytesRead 字节")
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                launch(Dispatchers.Main) {
                    Toast.makeText(this@CourseListActivity, "下载完成: $safeFileName\n已保存到Download文件夹", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("CourseListActivity", "下载异常", e)
                launch(Dispatchers.Main) {
                    Toast.makeText(this@CourseListActivity, "下载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    // 根据文件名获取MIME类型
    private fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast(".", "").lowercase()
        return when (extension) {
            "pdf" -> "application/pdf"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }
    
    // 处理权限请求结果
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，重新尝试下载
                if (lastDownloadUrl != null && lastDownloadFileName != null) {
                    downloadFile(lastDownloadUrl!!, lastDownloadFileName!!)
                }
            } else {
                // 权限被拒绝
                Toast.makeText(this, "需要存储权限才能下载文件", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
