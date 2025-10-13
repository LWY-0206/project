package org.jxxy.debug.h5.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.ValueCallback
import android.widget.MediaController
import android.widget.VideoView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private var isVideoFullscreen = false
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
        initX5WebView()

        // 获取传递的参数
        intent.extras?.let {
            currentUrl = intent.getStringExtra(EXTRA_URL) ?: ""
            title = intent.getStringExtra(EXTRA_TITLE) ?: "文件查看器"
            fileType = FileType.values()[intent.getIntExtra(EXTRA_FILE_TYPE, FileType.AUTO.ordinal)]
        }

        // 设置标题
        view.toolbar.title = title

        // 根据文件类型决定显示方式
        when (detectFileType()) {
            FileType.IMAGE -> showImage()
            FileType.VIDEO -> showVideo()
            FileType.PDF -> showPdfWithOfficeOnline()
            else -> showWebView()
        }

        setupCommonControls()
    }

    private fun detectFileType(): FileType {
        // 如果明确指定了类型，使用指定类型
        if (fileType != FileType.AUTO) {
            return fileType
        }

        // 根据URL后缀自动检测文件类型
        return when {
            currentUrl.contains(".mp4") || currentUrl.contains(".avi") ||
                    currentUrl.contains(".mov") || currentUrl.contains(".mkv") -> FileType.VIDEO
            currentUrl.contains(".pdf") -> FileType.PDF
            currentUrl.contains(".png") || currentUrl.contains(".jpg") ||
                    currentUrl.contains(".jpeg") || currentUrl.contains(".gif") ||
                    currentUrl.contains(".webp") -> FileType.IMAGE
            else -> FileType.WEB
        }
    }

    private fun showImage() {
        // 隐藏WebView，显示图片视图
        view.x5WebView.visibility = View.GONE
        view.videoContainer.visibility = View.GONE
        view.imageView.visibility = View.VISIBLE

        // 使用Glide加载图片
        Glide.with(this)
            .load(currentUrl)
            .into(view.imageView)

        // 添加图片点击缩放功能
        view.imageView.setOnClickListener {
            // 这里可以添加图片全屏查看功能
            "点击图片可缩放".toast(false)
        }
    }

    private fun showVideo() {
        // 隐藏其他视图，显示视频容器
        view.x5WebView.visibility = View.GONE
        view.imageView.visibility = View.GONE
        view.videoContainer.visibility = View.VISIBLE

        // 设置视频控制器
        val mediaController = MediaController(this)
        mediaController.setAnchorView(view.videoView)
        view.videoView.setMediaController(mediaController)

        // 设置视频路径并开始准备
        view.videoView.setVideoURI(Uri.parse(currentUrl))
        view.videoView.requestFocus()

        // 设置视频准备监听器，获取视频尺寸
        view.videoView.setOnPreparedListener { mp ->
            videoWidth = mp.videoWidth
            videoHeight = mp.videoHeight
            adjustVideoSize()
        }

        // 开始播放视频
        view.videoView.start()

        // 视频错误处理
        view.videoView.setOnErrorListener { mp, what, extra ->
            "视频播放错误，尝试使用WebView播放".toast(false)
            // 如果视频播放失败，回退到WebView
            view.videoContainer.visibility = View.GONE
            setupWebViewForVideo()
            true
        }

        // 视频完成回调
        view.videoView.setOnCompletionListener {
            // 视频播放完成后可以自动重播或显示重播按钮
            "视频播放完成".toast(false)
        }

        // 添加视频点击事件 - 切换控制栏显示
        view.videoView.setOnClickListener {
            if (mediaController.isShowing) {
                mediaController.hide()
            } else {
                mediaController.show(0) // 显示控制栏并设置超时隐藏
            }
        }
    }

    /**
     * 使用Microsoft Office Online Viewer显示PDF
     */
    private fun showPdfWithOfficeOnline() {
       val intent = Intent(this, PreviewActivity::class.java)
           .putExtra("url", currentUrl)
           .putExtra("title", title)
        Log.d("Intent", "url=${currentUrl}, title=${title}")
         startActivity(intent)
    }

    /**
     * 为PDF显示配置WebView
     */
    private fun setupWebViewForPdf() {
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
                    this@WebViewActivity.view.progressBar.visibility = View.GONE

                    // 检查是否成功加载PDF
                    if (url?.contains("view.officeapps.live.com") == true) {
                        Log.d("PDF_VIEWER", "Office Online Viewer loaded successfully")
                    }
                }

                override fun onPageStarted(view: com.tencent.smtt.sdk.WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    this@WebViewActivity.view.progressBar.visibility = View.VISIBLE
                    currentUrl = url ?: ""
                }

                override fun shouldOverrideUrlLoading(view: com.tencent.smtt.sdk.WebView?, url: String?): Boolean {
                    url?.let {
                        if (it.startsWith("http://") || it.startsWith("https://") || it.startsWith("file://")) {
                            view?.loadUrl(it)
                        } else {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                                startActivity(intent)
                            } catch (e: Exception) {
                                "无法打开链接: $it".toast(false)
                            }
                        }
                    }
                    return true
                }

                override fun onReceivedError(
                    view: com.tencent.smtt.sdk.WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    Log.e("PDF_VIEWER", "WebView error: $errorCode, $description, $failingUrl")

                    if (failingUrl?.contains("view.officeapps.live.com") == true) {
                        "Office Online Viewer加载失败，尝试直接加载PDF".toast(false)
                        // 备选方案：直接加载PDF
                        try {
                            view?.loadUrl(currentUrl)
                        } catch (e: Exception) {
                            Log.e("PDF_VIEWER", "Error loading PDF directly as fallback", e)
                        }
                    }
                }
            }

            // 设置Chrome客户端
            webChromeClient = object : com.tencent.smtt.sdk.WebChromeClient() {
                override fun onProgressChanged(view: com.tencent.smtt.sdk.WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    this@WebViewActivity.view.progressBar.progress = newProgress
                    if (newProgress == 100) {
                        this@WebViewActivity.view.progressBar.visibility = View.GONE
                    }
                }

                override fun onReceivedTitle(view: com.tencent.smtt.sdk.WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    // 保持原始标题，不覆盖
                    if (title?.contains("Office") != true) {
                        this@WebViewActivity.view.toolbar.title = title ?: this@WebViewActivity.title
                    }
                }
            }

            // Chrome客户端扩展
            webChromeClientExtension = createWebChromeClientExtension()
        }
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
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            size
        }

        val screenWidth = display.x
        val screenHeight = display.y - getToolbarHeight()

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

    private fun getToolbarHeight(): Int {
        return view.toolbar.height
    }

    private fun enterFullscreenMode() {
        isVideoFullscreen = true

        // 隐藏状态栏和导航栏
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )

        // 设置全屏布局
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // 隐藏工具栏和进度条
        view.toolbar.visibility = View.GONE
        view.progressBar.visibility = View.GONE
        view.fullscreenExitButton.visibility = View.VISIBLE

        // 全屏时调整视频尺寸
        adjustVideoSizeForFullscreen()
    }

    private fun adjustVideoSizeForFullscreen() {
        if (videoWidth == 0 || videoHeight == 0) return

        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            Point(metrics.bounds.width(), metrics.bounds.height())
        } else {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            size
        }

        val screenWidth = display.x
        val screenHeight = display.y

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

    private fun exitFullscreenMode() {
        isVideoFullscreen = false

        // 显示状态栏和导航栏
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        // 清除全屏标志
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // 显示工具栏
        view.toolbar.visibility = View.VISIBLE
        view.fullscreenExitButton.visibility = View.GONE
    }

    private fun setupWebViewForVideo() {
        view.x5WebView.visibility = View.VISIBLE
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

    private fun showWebView() {
        // 显示WebView
        view.x5WebView.visibility = View.VISIBLE
        view.videoContainer.visibility = View.GONE
        view.imageView.visibility = View.GONE

        // 初始化WebView设置
        setupWebView()

        // 加载URL
        if (currentUrl.isNotEmpty()) {
            view.x5WebView.loadUrl(currentUrl)
        } else {
            "URL为空".toast(false)
        }
    }

    private fun setupWebView() {
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

                // 针对微信公众号的特殊设置
                cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                mediaPlaybackRequiresUserGesture = false

                // 设置User-Agent，确保微信公众号内容正常显示
                val originalUserAgent = getUserAgentString()
                val wechatUserAgent = "$originalUserAgent WeChat"
                setUserAgentString(wechatUserAgent)
            }

            // 设置WebView客户端
            webViewClient = object : com.tencent.smtt.sdk.WebViewClient() {
                override fun onPageFinished(view: com.tencent.smtt.sdk.WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    this@WebViewActivity.view.progressBar.visibility = View.GONE

                    // 针对微信公众号文章的特殊处理
                    if (url?.contains("mp.weixin.qq.com") == true) {
                        // 注入CSS优化微信公众号文章显示
                        injectWeChatCSS()
                    }
                }

                override fun onPageStarted(view: com.tencent.smtt.sdk.WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    this@WebViewActivity.view.progressBar.visibility = View.VISIBLE
                    currentUrl = url ?: ""
                }

                override fun shouldOverrideUrlLoading(view: com.tencent.smtt.sdk.WebView?, url: String?): Boolean {
                    url?.let {
                        // 处理微信公众号文章内的链接
                        if (it.startsWith("http://") || it.startsWith("https://") || it.startsWith("file://")) {
                            // 如果是微信公众号域名内的链接，在当前WebView打开
                            if (it.contains("mp.weixin.qq.com")) {
                                view?.loadUrl(it)
                            } else {
                                // 其他外部链接，使用系统浏览器打开
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    "无法打开链接: $it".toast(false)
                                }
                            }
                        } else {
                            // 处理其他协议
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                                startActivity(intent)
                            } catch (e: Exception) {
                                "无法打开链接: $it".toast(false)
                            }
                        }
                    }
                    return true
                }

                override fun onReceivedError(
                    view: com.tencent.smtt.sdk.WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    if (failingUrl?.contains("mp.weixin.qq.com") == true) {
                        "微信公众号文章加载失败".toast(false)
                    }
                }
            }

            // 设置Chrome客户端
            webChromeClient = object : com.tencent.smtt.sdk.WebChromeClient() {
                override fun onProgressChanged(view: com.tencent.smtt.sdk.WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    this@WebViewActivity.view.progressBar.progress = newProgress
                    if (newProgress == 100) {
                        this@WebViewActivity.view.progressBar.visibility = View.GONE
                    }
                }

                override fun onReceivedTitle(view: com.tencent.smtt.sdk.WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    this@WebViewActivity.view.toolbar.title = title ?: this@WebViewActivity.title
                }
            }

            webChromeClientExtension = createWebChromeClientExtension()
        }
    }

    /**
     * 注入CSS优化微信公众号文章显示
     */
    private fun injectWeChatCSS() {
        val css = """
            javascript:(function() {
                var style = document.createElement('style');
                style.type = 'text/css';
                style.innerHTML = `
                    /* 优化微信公众号文章显示 */
                    .rich_media_content {
                        max-width: 100% !important;
                    }
                    img {
                        max-width: 100% !important;
                        height: auto !important;
                    }
                    /* 隐藏一些不必要的元素 */
                    .qr_code_pc, .reward_area, .article_bottom_ad {
                        display: none !important;
                    }
                    /* 优化文字显示 */
                    body {
                        font-size: 16px !important;
                        line-height: 1.6 !important;
                    }
                `;
                document.head.appendChild(style);
            })()
        """.trimIndent()

        view.x5WebView.loadUrl(css)
    }

    private fun createWebChromeClientExtension(): IX5WebChromeClientExtension {
        return object : IX5WebChromeClientExtension {
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

    private fun setupCommonControls() {
        view.apply {
            // 设置工具栏返回按钮
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }

            // 设置全屏退出按钮（仅在视频全屏时显示）
            fullscreenExitButton.setOnClickListener {
                exitFullscreenMode()
                finish()
            }
        }
    }

    override fun subscribeUi() {
        // 可以添加其他UI订阅逻辑
    }

    private fun initX5WebView() {
        // 使用腾讯x5 WebView，解决安卓原生WebView不适配不同机型问题
        QbSdk.initX5Environment(applicationContext, object : PreInitCallback {
            override fun onViewInitFinished(arg0: Boolean) {
                if (arg0) {
                    Log.e("腾讯X5", " onViewInitFinished 加载 成功 $arg0")
                } else {
                    Log.e("腾讯X5", " onViewInitFinished 加载 失败！！！使用原生安卓webview $arg0")
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

    override fun onBackPressed() {
        if (isVideoFullscreen) {
            exitFullscreenMode()
            finish()
        } else if (view.x5WebView.visibility == View.VISIBLE && view.x5WebView.canGoBack()) {
            view.x5WebView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 处理屏幕旋转
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && isVideoFullscreen) {
            // 横屏时保持全屏
            enterFullscreenMode()
        }
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