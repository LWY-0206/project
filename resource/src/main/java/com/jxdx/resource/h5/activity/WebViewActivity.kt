package com.jxdx.resource.h5.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.widget.MediaController
import android.widget.VideoView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.corekit.common.BaseActivity
import com.example.corekit.util.toast
import com.jxdx.resource.databinding.ActivityWebViewBinding
import com.tencent.smtt.export.external.extension.interfaces.IX5WebChromeClientExtension
import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewExtension
import com.tencent.smtt.export.external.interfaces.IX5WebViewBase
import com.tencent.smtt.export.external.interfaces.JsResult
import com.tencent.smtt.export.external.interfaces.MediaAccessPermissionsCallback
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.QbSdk.PreInitCallback
import java.util.HashMap

class WebViewActivity : BaseActivity<ActivityWebViewBinding>() {

    private var currentUrl: String = ""
    private var title: String = "文件查看器"
    private var fileType: FileType = FileType.WEB
    private var videoWidth = 0
    private var videoHeight = 0

    companion object {
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_FILE_TYPE = "extra_file_type"

        fun actionStart(context: Context, url: String, title: String = "文件查看器", fileType: FileType = FileType.AUTO) {
            context.startActivity(Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_FILE_TYPE, fileType.ordinal)
            })
        }
    }

    enum class FileType {
        AUTO, WEB, PDF, IMAGE, VIDEO
    }

    override fun bindLayout(): ActivityWebViewBinding {
        return ActivityWebViewBinding.inflate(layoutInflater)
    }

    override fun initView() {
        Log.d("WebViewActivity", "=== 初始化WebViewActivity ===")
        initX5WebView()

        // 获取传递的参数
        intent.extras?.let {
            currentUrl = intent.getStringExtra(EXTRA_URL) ?: ""
            title = intent.getStringExtra(EXTRA_TITLE) ?: "文件查看器"
            fileType = FileType.entries[intent.getIntExtra(EXTRA_FILE_TYPE, FileType.AUTO.ordinal)]
        }

        Log.d("WebViewActivity", "=== 接收参数 ===")
        Log.d("WebViewActivity", "URL: $currentUrl")
        Log.d("WebViewActivity", "Title: $title")
        Log.d("WebViewActivity", "FileType: $fileType")

        // 设置标题
        view.toolbar.title = title

        // 根据文件类型决定显示方式
        val detectedType = detectFileType()
        Log.d("WebViewActivity", "=== 文件类型检测结果 ===")
        Log.d("WebViewActivity", "检测到的类型: $detectedType")
        
        when (detectedType) {
            FileType.IMAGE -> {
                Log.d("WebViewActivity", "=== 开始显示图片 ===")
                showImage()
            }
            FileType.VIDEO -> {
                Log.d("WebViewActivity", "=== 开始显示视频 ===")
                showVideo()
            }
            FileType.PDF -> {
                Log.d("WebViewActivity", "=== 开始显示PDF ===")
                showPdf()
            }
            else -> {
                Log.d("WebViewActivity", "=== 开始显示网页 ===")
                showWebView()
            }
        }

        setupCommonControls()
        Log.d("WebViewActivity", "=== 初始化完成 ===")
    }

    private fun detectFileType(): FileType {
        // 如果明确指定了类型，使用指定类型
        if (fileType != FileType.AUTO) {
            Log.d("WebViewActivity", "使用指定的文件类型: $fileType")
            return fileType
        }

        // 根据URL后缀自动检测文件类型
        val detectedType = when {
            currentUrl.contains(".mp4") || currentUrl.contains(".avi") ||
                    currentUrl.contains(".mov") || currentUrl.contains(".mkv") -> FileType.VIDEO
            currentUrl.contains(".pdf") -> FileType.PDF
            currentUrl.contains(".png") || currentUrl.contains(".jpg") ||
                    currentUrl.contains(".jpeg") || currentUrl.contains(".gif") ||
                    currentUrl.contains(".webp") -> FileType.IMAGE
            else -> FileType.WEB
        }
        
        Log.d("WebViewActivity", "自动检测文件类型: $detectedType (URL: $currentUrl)")
        return detectedType
    }

    private fun showImage() {
        Log.d("WebViewActivity", "开始显示图片: $currentUrl")
        
        // 隐藏WebView，显示图片视图
        view.x5WebView.isVisible = false
        view.videoContainer.isVisible = false
        view.imageView.isVisible = true

        // 使用Glide加载图片
        Glide.with(this)
            .load(currentUrl)
            .into(view.imageView)

        Log.d("WebViewActivity", "图片加载完成")
    }

    private fun showVideo() {
        Log.d("WebViewActivity", "开始显示视频: $currentUrl")
        
        // 隐藏其他视图，显示视频容器
        view.x5WebView.isVisible = false
        view.imageView.isVisible = false
        view.videoContainer.isVisible = true

        // 设置视频控制器
        val mediaController = MediaController(this)
        mediaController.setAnchorView(view.videoView)
        view.videoView.setMediaController(mediaController)

        // 设置视频路径并开始准备
        view.videoView.setVideoURI(currentUrl.toUri())
        view.videoView.requestFocus()

        // 设置视频准备监听器
        view.videoView.setOnPreparedListener { mp ->
            Log.d("WebViewActivity", "视频准备完成")
            videoWidth = mp.videoWidth
            videoHeight = mp.videoHeight
            adjustVideoSize()
            view.videoView.start()
        }

        // 视频错误处理
        view.videoView.setOnErrorListener { _, what, extra ->
            Log.e("WebViewActivity", "视频播放错误: $what, $extra")
            "视频播放错误，尝试使用WebView播放".toast(false)
            // 如果视频播放失败，回退到WebView
            view.videoContainer.isVisible = false
            setupWebViewForVideo()
            true
        }

        Log.d("WebViewActivity", "视频设置完成")
    }

    private fun adjustVideoSize() {
        if (videoWidth == 0 || videoHeight == 0) return

        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(
                android.view.WindowInsets.Type.systemBars()
            )
            Point(metrics.bounds.width() - insets.left - insets.right,
                metrics.bounds.height() - insets.top - insets.bottom)
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            val size = Point()
            @Suppress("DEPRECATION")
            display.getSize(size)
            size
        }

        val screenWidth = display.x
        val screenHeight = display.y - view.toolbar.height - view.controlLayout.height

        val videoRatio = videoWidth.toFloat() / videoHeight.toFloat()
        val screenRatio = screenWidth.toFloat() / screenHeight.toFloat()

        val layoutParams = view.videoView.layoutParams as ViewGroup.MarginLayoutParams

        if (videoRatio > screenRatio) {
            // 视频比屏幕更宽，以宽度为准
            layoutParams.width = screenWidth
            layoutParams.height = (screenWidth / videoRatio).toInt()
        } else {
            // 视频比屏幕更高，以高度为准
            layoutParams.height = screenHeight
            layoutParams.width = (screenHeight * videoRatio).toInt()
        }

        // 设置居中显示
        layoutParams.setMargins(0, 0, 0, 0)
        view.videoView.layoutParams = layoutParams
    }


    private fun setupWebViewForVideo() {
        view.x5WebView.isVisible = true
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <style>
                    body { 
                        margin: 0; 
                        padding: 0; 
                        background: #000; 
                        display: flex; 
                        justify-content: center; 
                        align-items: center; 
                        height: 100vh; 
                        overflow: hidden;
                    }
                    video { 
                        max-width: 100%; 
                        max-height: 100%; 
                        object-fit: contain;
                    }
                </style>
            </head>
            <body>
                <video controls autoplay playsinline>
                    <source src="$currentUrl" type="video/mp4">
                    您的浏览器不支持视频播放
                </video>
            </body>
            </html>
        """.trimIndent()

        view.x5WebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    private fun showPdf() {
        Log.d("WebViewActivity", "=== PDF显示开始 ===")
        Log.d("WebViewActivity", "PDF URL: $currentUrl")
        
        // 显示WebView用于加载PDF
        view.x5WebView.isVisible = true
        view.videoContainer.isVisible = false
        view.imageView.isVisible = false
        Log.d("WebViewActivity", "WebView视图已显示")

        // 配置WebView以支持PDF显示
        setupWebView()
        Log.d("WebViewActivity", "WebView配置完成")

        // 显示静态PDF文章页面
        Log.d("WebViewActivity", "显示静态PDF文章页面")
        showStaticPdfArticle()
    }

    private fun showStaticPdfArticle() {
        Log.d("WebViewActivity", "=== 显示静态文章/试卷页面 ===")
        
        val html = """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>文章试卷阅读器</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
                        line-height: 1.6;
                        color: #333;
                        background: #f8f9fa;
                        padding: 10px;
                        margin: 0;
                        min-height: 100vh;
                    }
                    
                    .exam-container {
                        max-width: 100%;
                        width: 100%;
                        margin: 0 auto;
                        background: white;
                        border-radius: 10px;
                        box-shadow: 0 5px 15px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    
                    .exam-header {
                        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
                        color: white;
                        padding: 20px 15px;
                        text-align: center;
                        position: relative;
                    }
                    
                    .exam-title {
                        font-size: 20px;
                        font-weight: 700;
                        margin-bottom: 8px;
                    }
                    
                    .exam-subtitle {
                        font-size: 14px;
                        opacity: 0.9;
                    }
                    
                    .exam-info {
                        display: flex;
                        flex-direction: column;
                        gap: 8px;
                        padding: 15px;
                        background: #f8f9fa;
                        border-bottom: 1px solid #e9ecef;
                        font-size: 12px;
                        color: #666;
                    }
                    
                    .exam-content {
                        padding: 20px 15px;
                    }
                    
                    .article-section {
                        margin-bottom: 25px;
                    }
                    
                    .section-title {
                        font-size: 18px;
                        font-weight: 600;
                        color: #2c3e50;
                        margin-bottom: 15px;
                        padding-bottom: 8px;
                        border-bottom: 2px solid #4facfe;
                        position: relative;
                    }
                    
                    .section-title::before {
                        content: '';
                        position: absolute;
                        bottom: -2px;
                        left: 0;
                        width: 40px;
                        height: 2px;
                        background: #00f2fe;
                    }
                    
                    .article-text {
                        font-size: 15px;
                        line-height: 1.6;
                        margin-bottom: 15px;
                        text-align: justify;
                    }
                    
                    .article-text p {
                        margin-bottom: 15px;
                    }
                    
                    .question-item {
                        background: #fff;
                        border: 1px solid #e9ecef;
                        border-radius: 8px;
                        padding: 15px;
                        margin-bottom: 15px;
                        box-shadow: 0 1px 5px rgba(0,0,0,0.05);
                        transition: all 0.3s ease;
                    }
                    
                    .question-item:hover {
                        box-shadow: 0 3px 10px rgba(0,0,0,0.1);
                        transform: translateY(-1px);
                    }
                    
                    .question-number {
                        display: inline-block;
                        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
                        color: white;
                        width: 25px;
                        height: 25px;
                        border-radius: 50%;
                        text-align: center;
                        line-height: 25px;
                        font-weight: 600;
                        margin-right: 10px;
                        font-size: 12px;
                    }
                    
                    .question-title {
                        font-size: 16px;
                        font-weight: 600;
                        color: #2c3e50;
                        margin-bottom: 12px;
                        display: flex;
                        align-items: flex-start;
                    }
                    
                    .question-content {
                        font-size: 14px;
                        line-height: 1.5;
                        margin-bottom: 15px;
                        color: #555;
                    }
                    
                    .options {
                        margin-left: 35px;
                    }
                    
                    .option-item {
                        display: flex;
                        align-items: center;
                        margin-bottom: 8px;
                        padding: 8px;
                        border-radius: 6px;
                        transition: all 0.3s ease;
                        cursor: pointer;
                    }
                    
                    .option-item:hover {
                        background: #f8f9fa;
                    }
                    
                    .option-item.selected {
                        background: #e3f2fd;
                        border: 1px solid #4facfe;
                    }
                    
                    .option-label {
                        display: inline-block;
                        width: 20px;
                        height: 20px;
                        border: 2px solid #ddd;
                        border-radius: 50%;
                        text-align: center;
                        line-height: 16px;
                        margin-right: 10px;
                        font-weight: 600;
                        transition: all 0.3s ease;
                        font-size: 12px;
                    }
                    
                    .option-item.selected .option-label {
                        background: #4facfe;
                        border-color: #4facfe;
                        color: white;
                    }
                    
                    .option-text {
                        flex: 1;
                        font-size: 14px;
                    }
                    
                    .answer-input {
                        width: 100%;
                        padding: 10px 12px;
                        border: 1px solid #ddd;
                        border-radius: 6px;
                        font-size: 14px;
                        margin-top: 8px;
                        transition: border-color 0.3s ease;
                        resize: vertical;
                        min-height: 80px;
                    }
                    
                    .answer-input:focus {
                        outline: none;
                        border-color: #4facfe;
                        box-shadow: 0 0 0 2px rgba(79, 172, 254, 0.1);
                    }
                    
                    .action-buttons {
                        display: flex;
                        gap: 10px;
                        justify-content: center;
                        margin-top: 25px;
                        padding-top: 20px;
                        border-top: 1px solid #e9ecef;
                        flex-wrap: wrap;
                    }
                    
                    .btn {
                        padding: 10px 20px;
                        border: none;
                        border-radius: 20px;
                        font-size: 14px;
                        font-weight: 600;
                        cursor: pointer;
                        text-decoration: none;
                        display: inline-flex;
                        align-items: center;
                        gap: 6px;
                        transition: all 0.3s ease;
                        min-width: 100px;
                        justify-content: center;
                    }
                    
                    .btn-primary {
                        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
                        color: white;
                    }
                    
                    .btn-primary:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 10px 20px rgba(79, 172, 254, 0.3);
                    }
                    
                    .btn-secondary {
                        background: #28a745;
                        color: white;
                    }
                    
                    .btn-secondary:hover {
                        background: #218838;
                        transform: translateY(-2px);
                    }
                    
                    .btn-outline {
                        background: transparent;
                        color: #4facfe;
                        border: 2px solid #4facfe;
                    }
                    
                    .btn-outline:hover {
                        background: #4facfe;
                        color: white;
                        transform: translateY(-2px);
                    }
                    
                    .progress-bar {
                        width: 100%;
                        height: 6px;
                        background: #e9ecef;
                        border-radius: 3px;
                        margin: 20px 0;
                        overflow: hidden;
                    }
                    
                    .progress-fill {
                        height: 100%;
                        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
                        width: 0%;
                        transition: width 0.3s ease;
                    }
                    
                    .score-display {
                        text-align: center;
                        padding: 20px;
                        background: linear-gradient(135deg, #ffeaa7 0%, #fab1a0 100%);
                        border-radius: 10px;
                        margin-top: 20px;
                        display: none;
                    }
                    
                    .score-display.show {
                        display: block;
                        animation: slideDown 0.5s ease-out;
                    }
                    
                    @keyframes slideDown {
                        from {
                            opacity: 0;
                            transform: translateY(-20px);
                        }
                        to {
                            opacity: 1;
                            transform: translateY(0);
                        }
                    }
                    
                    .highlight {
                        background: linear-gradient(120deg, #a8edea 0%, #fed6e3 100%);
                        padding: 2px 4px;
                        border-radius: 3px;
                    }
                    
                    .important {
                        background: #fff3cd;
                        border: 1px solid #ffeaa7;
                        border-radius: 8px;
                        padding: 15px;
                        margin: 15px 0;
                    }
                    
                    .important::before {
                        content: '⚠️ ';
                        font-weight: bold;
                    }
                    
                    @media (max-width: 768px) {
                        body {
                            padding: 5px;
                        }
                        
                        .exam-container {
                            border-radius: 8px;
                        }
                        
                        .exam-header {
                            padding: 15px;
                        }
                        
                        .exam-title {
                            font-size: 18px;
                        }
                        
                        .exam-subtitle {
                            font-size: 13px;
                        }
                        
                        .exam-info {
                            flex-direction: column;
                            gap: 5px;
                            padding: 10px;
                            font-size: 11px;
                        }
                        
                        .exam-content {
                            padding: 15px;
                        }
                        
                        .section-title {
                            font-size: 16px;
                        }
                        
                        .article-text {
                            font-size: 14px;
                        }
                        
                        .question-item {
                            padding: 12px;
                        }
                        
                        .question-title {
                            font-size: 15px;
                        }
                        
                        .options {
                            margin-left: 30px;
                        }
                        
                        .option-item {
                            padding: 6px;
                        }
                        
                        .option-text {
                            font-size: 13px;
                        }
                        
                        .answer-input {
                            font-size: 14px;
                            padding: 8px 10px;
                        }
                        
                        .action-buttons {
                            flex-direction: column;
                            gap: 8px;
                        }
                        
                        .btn {
                            width: 100%;
                            padding: 12px 20px;
                            font-size: 14px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="exam-container">
                    <div class="exam-header">
                        <h1 class="exam-title">📚 文章试卷阅读器</h1>
                        <p class="exam-subtitle">智能文章内容展示与答题系统</p>
                    </div>
                    
                    <div class="exam-info">
                        <span>📄 文档来源: $currentUrl</span>
                        <span>⏰ 阅读时间: <span id="readingTime">0</span> 分钟</span>
                        <span>📊 进度: <span id="progress">0</span>%</span>
                    </div>
                    
                    <div class="progress-bar">
                        <div class="progress-fill" id="progressFill"></div>
                    </div>
                    
                    <div class="exam-content">
                        <!-- 文章内容部分 -->
                        <div class="article-section">
                            <h2 class="section-title">📖 文章内容</h2>
                            <div class="article-text">
                                <p>这是一篇关于<strong class="highlight">人工智能</strong>发展的重要文章。随着科技的不断进步，人工智能技术正在改变我们的生活方式和工作方式。</p>
                                
                                <p>在过去的几十年里，人工智能从概念走向现实，从实验室走向商业应用。今天，我们可以在智能手机、智能家居、自动驾驶汽车等各个领域看到AI的身影。</p>
                                
                                <div class="important">
                                    重要提示：人工智能的发展需要遵循伦理原则，确保技术的安全性和可控性。
                                </div>
                                
                                <p>未来，人工智能将继续发展，可能会在医疗、教育、环境保护等领域发挥更大的作用。我们需要做好准备，迎接这个充满机遇和挑战的时代。</p>
                            </div>
                        </div>
                        
                        <!-- 选择题部分 -->
                        <div class="article-section">
                            <h2 class="section-title">❓ 选择题</h2>
                            
                            <div class="question-item">
                                <div class="question-title">
                                    <span class="question-number">1</span>
                                    <span>人工智能的发展历程中，哪个阶段标志着AI从概念走向现实？</span>
                                </div>
                                <div class="options">
                                    <div class="option-item" onclick="selectOption(this, 'A')">
                                        <span class="option-label">A</span>
                                        <span class="option-text">1950年代的理论研究阶段</span>
                                    </div>
                                    <div class="option-item" onclick="selectOption(this, 'B')">
                                        <span class="option-label">B</span>
                                        <span class="option-text">1980年代的专家系统阶段</span>
                                    </div>
                                    <div class="option-item" onclick="selectOption(this, 'C')">
                                        <span class="option-label">C</span>
                                        <span class="option-text">2010年代的深度学习阶段</span>
                                    </div>
                                    <div class="option-item" onclick="selectOption(this, 'D')">
                                        <span class="option-label">D</span>
                                        <span class="option-text">2020年代的通用人工智能阶段</span>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="question-item">
                                <div class="question-title">
                                    <span class="question-number">2</span>
                                    <span>人工智能在哪些领域有重要应用？（多选）</span>
                                </div>
                                <div class="options">
                                    <div class="option-item" onclick="selectOption(this, 'A')">
                                        <span class="option-label">A</span>
                                        <span class="option-text">智能手机</span>
                                    </div>
                                    <div class="option-item" onclick="selectOption(this, 'B')">
                                        <span class="option-label">B</span>
                                        <span class="option-text">智能家居</span>
                                    </div>
                                    <div class="option-item" onclick="selectOption(this, 'C')">
                                        <span class="option-label">C</span>
                                        <span class="option-text">自动驾驶汽车</span>
                                    </div>
                                    <div class="option-item" onclick="selectOption(this, 'D')">
                                        <span class="option-label">D</span>
                                        <span class="option-text">传统制造业</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <!-- 简答题部分 -->
                        <div class="article-section">
                            <h2 class="section-title">✍️ 简答题</h2>
                            
                            <div class="question-item">
                                <div class="question-title">
                                    <span class="question-number">3</span>
                                    <span>请简述人工智能发展需要遵循的伦理原则有哪些？</span>
                                </div>
                                <textarea class="answer-input" placeholder="请在此输入您的答案..." rows="4"></textarea>
                            </div>
                            
                            <div class="question-item">
                                <div class="question-title">
                                    <span class="question-number">4</span>
                                    <span>您认为人工智能在未来可能会在哪些领域发挥更大作用？请说明理由。</span>
                                </div>
                                <textarea class="answer-input" placeholder="请在此输入您的答案..." rows="4"></textarea>
                            </div>
                        </div>
                        
                        <!-- 操作按钮 -->
                        <div class="action-buttons">
                            <button class="btn btn-primary" onclick="submitAnswers()">
                                📤 提交答案
                            </button>
                            <button class="btn btn-secondary" onclick="resetAnswers()">
                                🔄 重置答案
                            </button>
                            <button class="btn btn-outline" onclick="downloadAnswers()">
                                💾 保存答案
                            </button>
                        </div>
                        
                        <!-- 成绩显示 -->
                        <div class="score-display" id="scoreDisplay">
                            <h3>🎉 答题完成！</h3>
                            <p>您的得分：<span id="score">0</span>/100 分</p>
                            <p>用时：<span id="timeUsed">0</span> 分钟</p>
                        </div>
                    </div>
                </div>
                
                <script>
                    let startTime = Date.now();
                    let answers = {};
                    let correctAnswers = {
                        '1': 'C',
                        '2': ['A', 'B', 'C']
                    };
                    
                    // 更新阅读时间
                    function updateReadingTime() {
                        const elapsed = Math.floor((Date.now() - startTime) / 60000);
                        document.getElementById('readingTime').textContent = elapsed;
                    }
                    
                    // 更新进度
                    function updateProgress() {
                        const totalQuestions = 4;
                        const answeredQuestions = Object.keys(answers).length;
                        const progress = Math.floor((answeredQuestions / totalQuestions) * 100);
                        
                        document.getElementById('progress').textContent = progress;
                        document.getElementById('progressFill').style.width = progress + '%';
                    }
                    
                    // 选择选项
                    function selectOption(element, option) {
                        const questionNumber = element.closest('.question-item').querySelector('.question-number').textContent;
                        
                        // 清除同题目的其他选择
                        const questionItem = element.closest('.question-item');
                        questionItem.querySelectorAll('.option-item').forEach(item => {
                            item.classList.remove('selected');
                        });
                        
                        // 选中当前选项
                        element.classList.add('selected');
                        
                        // 保存答案
                        answers[questionNumber] = option;
                        updateProgress();
                    }
                    
                    // 提交答案
                    function submitAnswers() {
                        let score = 0;
                        let totalQuestions = 4;
                        
                        // 计算选择题得分
                        for (let q in correctAnswers) {
                            if (answers[q] === correctAnswers[q]) {
                                score += 25;
                            }
                        }
                        
                        // 简答题给基础分
                        if (answers['3'] && answers['3'].length > 10) {
                            score += 25;
                        }
                        if (answers['4'] && answers['4'].length > 10) {
                            score += 25;
                        }
                        
                        // 显示成绩
                        document.getElementById('score').textContent = score;
                        document.getElementById('timeUsed').textContent = Math.floor((Date.now() - startTime) / 60000);
                        document.getElementById('scoreDisplay').classList.add('show');
                        
                        // 滚动到成绩显示
                        document.getElementById('scoreDisplay').scrollIntoView({ behavior: 'smooth' });
                    }
                    
                    // 重置答案
                    function resetAnswers() {
                        answers = {};
                        document.querySelectorAll('.option-item').forEach(item => {
                            item.classList.remove('selected');
                        });
                        document.querySelectorAll('.answer-input').forEach(input => {
                            input.value = '';
                        });
                        document.getElementById('scoreDisplay').classList.remove('show');
                        updateProgress();
                    }
                    
                    // 保存答案
                    function downloadAnswers() {
                        const answerText = Object.keys(answers).map(q => 
                            '第' + q + '题: ' + (answers[q] || '未回答')
                        ).join('\\n');
                        
                        const blob = new Blob([answerText], { type: 'text/plain' });
                        const url = URL.createObjectURL(blob);
                        const a = document.createElement('a');
                        a.href = url;
                        a.download = '答题记录.txt';
                        a.click();
                        URL.revokeObjectURL(url);
                    }
                    
                    // 监听文本输入
                    document.querySelectorAll('.answer-input').forEach((input, index) => {
                        input.addEventListener('input', function() {
                            const questionNumber = (index + 3).toString();
                            answers[questionNumber] = this.value;
                            updateProgress();
                        });
                    });
                    
                    // 定时更新阅读时间
                    setInterval(updateReadingTime, 60000);
                    
                    // 页面加载完成
                    document.addEventListener('DOMContentLoaded', function() {
                        updateReadingTime();
                        updateProgress();
                    });
                </script>
            </body>
            </html>
        """.trimIndent()

        view.x5WebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        Log.d("WebViewActivity", "静态文章/试卷页面加载完成")
    }


    private fun showSimpleErrorPage(title: String, message: String) {
        Log.w("WebViewActivity", "显示简单错误页面: $title")
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        text-align: center; 
                        padding: 50px; 
                        background: #f5f5f5;
                    }
                    .error-container {
                        background: white;
                        padding: 30px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        max-width: 400px;
                        margin: 0 auto;
                    }
                    .error-icon {
                        font-size: 48px;
                        color: #ff6b6b;
                        margin-bottom: 20px;
                    }
                    .error-title {
                        font-size: 24px;
                        color: #333;
                        margin-bottom: 15px;
                    }
                    .error-message {
                        color: #666;
                        margin-bottom: 25px;
                        line-height: 1.5;
                    }
                    .download-btn {
                        background: #007bff;
                        color: white;
                        padding: 12px 24px;
                        border: none;
                        border-radius: 5px;
                        font-size: 16px;
                        cursor: pointer;
                        text-decoration: none;
                        display: inline-block;
                    }
                    .download-btn:hover {
                        background: #0056b3;
                    }
                </style>
            </head>
            <body>
                <div class="error-container">
                    <div class="error-icon">📄</div>
                    <div class="error-title">$title</div>
                    <div class="error-message">$message</div>
                    <a href="$currentUrl" class="download-btn" download>📥 下载文件</a>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        view.x5WebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    private fun showWebView() {
        // 显示WebView
        view.x5WebView.isVisible = true
        view.videoContainer.isVisible = false

        // 初始化WebView设置
        setupWebView()

        // 加载URL
        if (currentUrl.isNotEmpty()) {
            view.x5WebView.loadUrl(currentUrl)
        } else {
            "URL为空".toast(false)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        Log.d("WebViewActivity", "配置WebView设置")
        view.x5WebView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportMultipleWindows(false)
            }

            // 设置WebView客户端
            webViewClient = object : com.tencent.smtt.sdk.WebViewClient() {
                override fun onPageFinished(view: com.tencent.smtt.sdk.WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("WebViewActivity", "WebView页面加载完成: $url")
                    this@WebViewActivity.view.progressBar.isVisible = false
                    updateNavigationButtons()
                }

                override fun onPageStarted(view: com.tencent.smtt.sdk.WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    Log.d("WebViewActivity", "WebView页面开始加载: $url")
                    this@WebViewActivity.view.progressBar.isVisible = true
                    currentUrl = url ?: ""
                }

                override fun shouldOverrideUrlLoading(view: com.tencent.smtt.sdk.WebView?, url: String?): Boolean {
                    Log.d("WebViewActivity", "WebView URL跳转处理: $url")
                    url?.let {
                        if (it.startsWith("http://") || it.startsWith("https://") || it.startsWith("file://")) {
                            Log.d("WebViewActivity", "在WebView中加载URL: $it")
                            view?.loadUrl(it)
                        } else {
                            try {
                                Log.d("WebViewActivity", "使用外部应用打开URL: $it")
                                val intent = Intent(Intent.ACTION_VIEW, it.toUri())
                                startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("WebViewActivity", "无法打开链接: $it", e)
                                "无法打开链接: $it".toast(false)
                            }
                        }
                    }
                    return true
                }
            }

            // 设置Chrome客户端
            webChromeClient = object : com.tencent.smtt.sdk.WebChromeClient() {
                override fun onProgressChanged(view: com.tencent.smtt.sdk.WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    Log.d("WebViewActivity", "WebView加载进度: $newProgress%")
                    this@WebViewActivity.view.progressBar.progress = newProgress
                    if (newProgress == 100) {
                        this@WebViewActivity.view.progressBar.isVisible = false
                        Log.d("WebViewActivity", "WebView加载完成")
                    }
                }

                override fun onReceivedTitle(view: com.tencent.smtt.sdk.WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    Log.d("WebViewActivity", "WebView页面标题: $title")
                    this@WebViewActivity.view.toolbar.title = title ?: this@WebViewActivity.title
                }
            }

            // 设置Chrome客户端扩展
            webChromeClientExtension = object : IX5WebChromeClientExtension {
                override fun getX5WebChromeClientInstance(): Any? = null
                override fun getVideoLoadingProgressView(): View? = null
                override fun onAllMetaDataFinished(p0: IX5WebViewExtension?, p1: HashMap<String, String>?) {}
                override fun onBackforwardFinished(p0: Int) {}
                override fun onHitTestResultForPluginFinished(p0: IX5WebViewExtension?, p1: IX5WebViewBase.HitTestResult?, p2: Bundle?) {}
                override fun onHitTestResultFinished(p0: IX5WebViewExtension?, p1: IX5WebViewBase.HitTestResult?) {}
                override fun onPromptScaleSaved(p0: IX5WebViewExtension?) {}
                override fun onPromptNotScalable(p0: IX5WebViewExtension?) {}
                override fun onAddFavorite(p0: IX5WebViewExtension?, p1: String?, p2: String?, p3: JsResult?): Boolean = false
                override fun onPrepareX5ReadPageDataFinished(p0: IX5WebViewExtension?, p1: HashMap<String, String>?) {}
                override fun onSavePassword(p0: String?, p1: String?, p2: String?, p3: Boolean, p4: Message?): Boolean = false
                override fun onSavePassword(p0: ValueCallback<String>?, p1: String?, p2: String?, p3: String?, p4: String?, p5: String?, p6: Boolean): Boolean = false
                override fun onX5ReadModeAvailableChecked(p0: HashMap<String, String>?) {}
                override fun addFlashView(p0: View?, p1: ViewGroup.LayoutParams?) {}
                override fun h5videoRequestFullScreen(p0: String?) {}
                override fun h5videoExitFullScreen(p0: String?) {}
                override fun requestFullScreenFlash() {}
                override fun exitFullScreenFlash() {}
                override fun jsRequestFullScreen() {}
                override fun jsExitFullScreen() {}
                override fun acquireWakeLock() {}
                override fun releaseWakeLock() {}
                override fun getApplicationContex(): Context? = null
                override fun onPageNotResponding(p0: Runnable?): Boolean = false
                override fun onMiscCallBack(p0: String?, p1: Bundle?): Any? = null
                override fun openFileChooser(p0: ValueCallback<Array<Uri>>?, p1: String?, p2: String?) {}
                override fun onPrintPage() {}
                override fun onColorModeChanged(p0: Long) {}

                override fun onPermissionRequest(
                    p0: String?,
                    p1: Long,
                    p2: MediaAccessPermissionsCallback?
                ): Boolean {
                    p2?.invoke(p0, p1, true)
                    return true
                }
            }
        }
    }

    private fun setupCommonControls() {
        view.apply {
            // 设置工具栏返回按钮
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }

            // 设置刷新按钮
            refreshButton.setOnClickListener {
                when (detectFileType()) {
                    FileType.IMAGE -> showImage()
                    FileType.VIDEO -> showVideo()
                    FileType.PDF -> showPdf()
                    else -> {
                        if (x5WebView.visibility == View.VISIBLE) {
                            x5WebView.reload()
                        } else {
                            showWebView()
                        }
                    }
                }
                "刷新中...".toast(false)
            }

            // 设置前进后退按钮
            backButton.setOnClickListener {
                if (x5WebView.visibility == View.VISIBLE && x5WebView.canGoBack()) {
                    x5WebView.goBack()
                } else {
                    "无法后退".toast(false)
                }
            }

            forwardButton.setOnClickListener {
                if (x5WebView.visibility == View.VISIBLE && x5WebView.canGoForward()) {
                    x5WebView.goForward()
                } else {
                    "无法前进".toast(false)
                }
            }

            // 设置分享按钮
            shareButton.setOnClickListener {
                shareUrl(currentUrl)
            }
        }
    }
    override fun subscribeUi() {
        // 可以添加其他UI订阅逻辑
    }

    private fun initX5WebView() {
        // 使用腾讯x5 WebView，解决安卓原生WebView不适配不同机型问题
        QbSdk.initX5Environment(applicationContext, object : PreInitCallback {
            override fun onViewInitFinished(success: Boolean) {
                if (success) {
                    Log.e("腾讯X5", " onViewInitFinished 加载 成功 $success")
                } else {
                    Log.e("腾讯X5", " onViewInitFinished 加载 失败！！！使用原生安卓webview $success")
                }
            }

            override fun onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        })

        // 检查是否已经授予相机权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 如果没有权限，则请求权限
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1001)
        }
    }

    private fun updateNavigationButtons() {
        view.apply {
            backButton.isEnabled = x5WebView.canGoBack()
            forwardButton.isEnabled = x5WebView.canGoForward()
        }
    }

    private fun shareUrl(url: String) {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, url)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "分享链接"))
        } catch (e: Exception) {
            "分享失败".toast(false)
        }
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (view.x5WebView.isVisible && view.x5WebView.canGoBack()) {
            view.x5WebView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 处理屏幕旋转
    }

    override fun onPause() {
        super.onPause()
        // 暂停视频播放
        if (view.videoView.visibility == View.VISIBLE) {
            view.videoView.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        view.x5WebView.destroy()
        // 释放视频资源
        if (view.videoView.visibility == View.VISIBLE) {
            view.videoView.stopPlayback()
        }
    }
}