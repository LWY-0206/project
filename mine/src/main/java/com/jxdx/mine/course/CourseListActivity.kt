package com.jxdx.mine.course

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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

                // 创建文件输出流（兼容Android 10及以上版本）
                val outputStream = contentResolver.openOutputStream(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    "w"
                )

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
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    Log.d("CourseListActivity", "已下载: $totalBytesRead 字节")
                }

                outputStream.close()
                inputStream.close()

                launch(Dispatchers.Main) {
                    Toast.makeText(this@CourseListActivity, "下载完成: $fileName", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("CourseListActivity", "下载异常", e)
                launch(Dispatchers.Main) {
                    Toast.makeText(this@CourseListActivity, "下载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
