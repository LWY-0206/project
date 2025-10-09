
package com.jxdx.resource.RescourseDetail
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.jxdx.resource.R
import com.jxdx.resource.databinding.ActivityPdfViewerBinding
import java.io.File

class PdfViewerActivity : AppCompatActivity(), OnLoadCompleteListener, OnPageChangeListener, OnPageErrorListener {

    private lateinit var binding: ActivityPdfViewerBinding
    private lateinit var pdfView: PDFView
    private lateinit var progressBar: ProgressBar

    private var pdfFileName: String = ""
    private var pageNumber: Int = 0

    companion object {
        private const val TAG = "PdfViewerActivity"

        // Intent extras keys
        private const val EXTRA_PDF_PATH = "pdf_path"
        private const val EXTRA_PDF_TITLE = "pdf_title"
        private const val EXTRA_FROM_ASSETS = "from_assets"

        /**
         * 从文件路径启动PDF查看器
         * @param context 上下文
         * @param pdfPath PDF文件路径
         * @param title 显示标题
         */
        fun startFromFile(context: Context, pdfPath: String, title: String = "PDF文档") {
            context.startActivity(Intent(context, PdfViewerActivity::class.java).apply {
                putExtra(EXTRA_PDF_PATH, pdfPath)
                putExtra(EXTRA_PDF_TITLE, title)
                putExtra(EXTRA_FROM_ASSETS, false)
            })
        }

        /**
         * 从assets文件夹启动PDF查看器
         * @param context 上下文
         * @param assetPath assets中的PDF文件路径
         * @param title 显示标题
         */
        fun startFromAssets(context: Context, assetPath: String, title: String = "PDF文档") {
            context.startActivity(Intent(context, PdfViewerActivity::class.java).apply {
                putExtra(EXTRA_PDF_PATH, assetPath)
                putExtra(EXTRA_PDF_TITLE, title)
                putExtra(EXTRA_FROM_ASSETS, true)
            })
        }

        /**
         * 从Uri启动PDF查看器
         * @param context 上下文
         * @param uri PDF文件的Uri
         * @param title 显示标题
         */
        fun startFromUri(context: Context, uri: Uri, title: String = "PDF文档") {
            context.startActivity(Intent(context, PdfViewerActivity::class.java).apply {
                data = uri
                putExtra(EXTRA_PDF_TITLE, title)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        loadPdf()
    }

    private fun initView() {
        pdfView = binding.pdfView
        progressBar = binding.progressBar

        // 设置工具栏
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // 设置标题
        val title = intent.getStringExtra(EXTRA_PDF_TITLE) ?: "PDF文档"
        binding.tvTitle.text = title

        // 设置页面信息显示
        binding.tvPageInfo.text = "加载中..."

        // 设置翻页按钮
        binding.btnPrevious.setOnClickListener {
            if (pageNumber > 0) {
                pdfView.jumpTo(pageNumber - 1)
            }
        }

        binding.btnNext.setOnClickListener {
            pdfView.jumpTo(pageNumber + 1)
        }
    }

    private fun loadPdf() {
        val fromAssets = intent.getBooleanExtra(EXTRA_FROM_ASSETS, false)
        val pdfPath = intent.getStringExtra(EXTRA_PDF_PATH)

        try {
            if (fromAssets && pdfPath != null) {
                // 从assets加载
                loadFromAssets(pdfPath)
            } else if (pdfPath != null) {
                // 从文件路径加载
                loadFromFile(pdfPath)
            } else if (intent.data != null) {
                // 从Uri加载
                loadFromUri(intent.data!!)
            } else {
                throw IllegalArgumentException("没有提供PDF文件路径或Uri")
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载PDF失败", e)
            Toast.makeText(this, "加载PDF失败: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun loadFromAssets(assetPath: String) {
        pdfFileName = assetPath.substringAfterLast("/")

        pdfView.fromAsset(assetPath)
            .defaultPage(0)
            .onLoad(this)
            .onPageChange(this)
            .onPageError(this)
            .enableAnnotationRendering(true)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .spacing(10)
            .autoSpacing(false)
            .pageFitPolicy(FitPolicy.WIDTH)
            .pageSnap(true)
            .pageFling(true)
            .nightMode(false)
            .scrollHandle(DefaultScrollHandle(this))
            .load()
    }

    private fun loadFromFile(filePath: String) {
        val file = File(filePath)
        pdfFileName = file.name

        if (!file.exists()) {
            throw IllegalArgumentException("PDF文件不存在: $filePath")
        }

        pdfView.fromFile(file)
            .defaultPage(0)
            .onLoad(this)
            .onPageChange(this)
            .onPageError(this)
            .enableAnnotationRendering(true)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .spacing(10)
            .autoSpacing(false)
            .pageFitPolicy(FitPolicy.WIDTH)
            .pageSnap(true)
            .pageFling(true)
            .nightMode(false)
            .scrollHandle(DefaultScrollHandle(this))
            .load()
    }

    private fun loadFromUri(uri: Uri) {
        pdfFileName = uri.lastPathSegment ?: "document.pdf"

        pdfView.fromUri(uri)
            .defaultPage(0)
            .onLoad(this)
            .onPageChange(this)
            .onPageError(this)
            .enableAnnotationRendering(true)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .spacing(10)
            .autoSpacing(false)
            .pageFitPolicy(FitPolicy.WIDTH)
            .pageSnap(true)
            .pageFling(true)
            .nightMode(false)
            .scrollHandle(DefaultScrollHandle(this))
            .load()
    }

    // PDF加载完成回调
    override fun loadComplete(nbPages: Int) {
        progressBar.visibility = View.GONE
        binding.tvPageInfo.text = "第 ${pageNumber + 1} 页 / 共 $nbPages 页"
        updateButtonStates()

        Toast.makeText(this, "PDF加载完成", Toast.LENGTH_SHORT).show()
    }

    // 页面改变回调
    override fun onPageChanged(page: Int, pageCount: Int) {
        pageNumber = page
        binding.tvPageInfo.text = "第 ${page + 1} 页 / 共 $pageCount 页"
        updateButtonStates()
    }

    // 页面错误回调
    override fun onPageError(page: Int, t: Throwable?) {
        Log.e(TAG, "无法加载页面: $page", t)
        Toast.makeText(this, "页面 $page 加载错误", Toast.LENGTH_SHORT).show()
    }

    private fun updateButtonStates() {
        binding.btnPrevious.isEnabled = pageNumber > 0
        // 下一页按钮状态在页面改变时自动更新
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理资源
        pdfView.recycle()
    }
}