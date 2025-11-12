package com.jxdx.classroom.activity

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.jxdx.classroom.R
import com.jxdx.classroom.databinding.ActivityPdfPreviewBinding
import com.shockwave.pdfium.PdfDocument

class PdfPreviewActivity : AppCompatActivity(), OnPageChangeListener, OnLoadCompleteListener,
    OnPageErrorListener {

    private lateinit var binding: ActivityPdfPreviewBinding
    
    private var currentPage = 0
    private var totalPages = 0
    private var pdfFileName = "未选择文件"
    private var pdfUri: Uri? = null

    companion object {
        private const val TAG = "PdfPreviewActivity"
        private const val PERMISSION_CODE = 42042
    }

    // 文件选择结果处理
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                pdfUri = uri
                displayPdfFromUri(uri)
            }
        }
    }

    // 权限请求处理
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchFilePicker()
        } else {
            Toast.makeText(this, "需要存储权限来选择PDF文件", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupClickListeners()
        
        // 如果有传递的PDF文件URI，直接显示
        intent?.data?.let { uri ->
            pdfUri = uri
            displayPdfFromUri(uri)
        }
    }

    private fun initViews() {
        // 设置PDFView背景色
        binding.pdfView.setBackgroundColor(Color.LTGRAY)
        
        // 初始化页面信息
        updatePageInfo()
        updateFileName()
        
        // 显示选择文件提示
        showSelectFileHint()
    }

    private fun setupClickListeners() {
        // 返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 选择文件按钮
        binding.btnSelectFile.setOnClickListener {
            checkPermissionAndPickFile()
        }

        // 重试按钮
        binding.btnRetry.setOnClickListener {
            pdfUri?.let { uri ->
                displayPdfFromUri(uri)
            } ?: run {
                checkPermissionAndPickFile()
            }
        }
    }

    private fun checkPermissionAndPickFile() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchFilePicker()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun launchFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        try {
            filePickerLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "未找到文件管理器", Toast.LENGTH_SHORT).show()
        }
    }


    private fun displayPdfFromUri(uri: Uri) {
        showLoading(true)
        hideError()
        
        pdfFileName = getFileName(uri)
        updateFileName()
        
        Log.d(TAG, "开始加载PDF文件: $pdfFileName, URI: $uri")

        try {
            binding.pdfView.fromUri(uri)
                .defaultPage(currentPage)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(DefaultScrollHandle(this))
                .spacing(10) // 页面间距
                .onPageError(this)
                .pageFitPolicy(FitPolicy.BOTH)
                .load()
            Log.d(TAG, "PDF加载请求已发送")
        } catch (e: Exception) {
            Log.e(TAG, "PDF加载失败", e)
            showError("PDF加载失败: ${e.message}")
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.lastPathSegment
        }
        return result ?: "未知文件"
    }

    private fun showLoading(show: Boolean) {
        binding.loadingLayout.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showError(message: String) {
        binding.errorLayout.visibility = android.view.View.VISIBLE
        binding.tvErrorMessage.text = message
        showLoading(false)
    }

    private fun hideError() {
        binding.errorLayout.visibility = android.view.View.GONE
    }

    private fun showSelectFileHint() {
        binding.loadingLayout.visibility = android.view.View.VISIBLE
        binding.tvLoadingText.text = "请点击右上角文件夹图标选择PDF文件"
    }

    private fun updatePageInfo() {
        binding.tvPageInfo.text = "第 ${currentPage + 1} 页，共 $totalPages 页"
    }

    private fun updateFileName() {
        binding.tvFileName.text = pdfFileName
    }

    // OnPageChangeListener
    override fun onPageChanged(page: Int, pageCount: Int) {
        currentPage = page
        totalPages = pageCount
        updatePageInfo()
        Log.d(TAG, "页面改变: $page / $pageCount")
    }

    // OnLoadCompleteListener
    override fun loadComplete(nbPages: Int) {
        totalPages = nbPages
        currentPage = 0
        updatePageInfo()
        showLoading(false)
        hideError()
        
        Log.d(TAG, "PDF加载完成，总页数: $nbPages")
        
        // 打印PDF元数据
        val meta = binding.pdfView.documentMeta
        meta?.let {
            Log.d(TAG, "PDF标题: ${it.title}")
            Log.d(TAG, "PDF作者: ${it.author}")
            Log.d(TAG, "PDF主题: ${it.subject}")
            Log.d(TAG, "PDF关键词: ${it.keywords}")
            Log.d(TAG, "PDF创建者: ${it.creator}")
            Log.d(TAG, "PDF生产者: ${it.producer}")
        }
        
        // 打印书签
        printBookmarksTree(binding.pdfView.tableOfContents, "-")
    }

    private fun printBookmarksTree(bookmarks: List<PdfDocument.Bookmark>?, sep: String) {
        bookmarks?.forEach { bookmark ->
            Log.d(TAG, "$sep ${bookmark.title}, 页码: ${bookmark.pageIdx}")
            if (bookmark.hasChildren()) {
                printBookmarksTree(bookmark.children, "$sep-")
            }
        }
    }

    // OnPageErrorListener
    override fun onPageError(page: Int, t: Throwable) {
        Log.e(TAG, "无法加载第 $page 页", t)
        showError("无法加载第 ${page + 1} 页")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
