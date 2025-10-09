package com.jxdx.mine.course


import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jxdx.mine.databinding.ActivityPreviewBinding
import com.jxdx.mine.R

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding
    private lateinit var webView: WebView
    private val TAG = "PreviewActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = binding.webView

        // 获取传递过来的URL和标题
        val url = intent.getStringExtra("url")
        val title = intent.getStringExtra("title")

        // 设置标题
        supportActionBar?.title = title

        if (url.isNullOrEmpty()) {
            Toast.makeText(this, "预览链接为空", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(TAG, "要加载的URL: $url")

        // 设置WebView的属性
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        // 添加额外的WebView设置以支持更多文件类型
        webSettings.allowContentAccess = true
        webSettings.allowFileAccess = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // 设置WebViewClient以在WebView内打开链接并处理错误
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "页面加载完成: $url")
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                Log.e(TAG, "WebView错误: ${error?.description}")
                Toast.makeText(this@PreviewActivity, "加载错误: ${error?.description}", Toast.LENGTH_SHORT).show()
            }

            // 为旧版Android添加兼容的错误处理方法
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e(TAG, "WebView错误(旧版): $errorCode, $description")
                Toast.makeText(this@PreviewActivity, "加载错误: $description", Toast.LENGTH_SHORT).show()
            }
        }

        // 添加WebChromeClient来处理JavaScript对话框和其他Chrome功能
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                Log.d(TAG, "加载进度: $newProgress%")
                
                // 如果加载时间过长但进度没有变化，尝试切换到备用查看器
                if (newProgress > 0 && newProgress < 100) {
                    // 这里可以添加一个计时器来检测加载是否卡住
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                Log.d(TAG, "页面标题: $title")
            }
        }

        // 处理不同类型的文件
        if (url.endsWith(".pptx", true) || url.endsWith(".ppt", true) || 
            url.endsWith(".docx", true) || url.endsWith(".doc", true) ||
            url.endsWith(".xlsx", true) || url.endsWith(".xls", true)) {
            // 尝试使用Microsoft Office Online Viewer预览Office文档（作为主要选项）
            val officeOnlineUrl = "https://view.officeapps.live.com/op/embed.aspx?src=" + url
            Log.d(TAG, "使用Office Online Viewer加载: $officeOnlineUrl")
            webView.loadUrl(officeOnlineUrl)
        } else {
            // 直接加载其他类型的文件
            webView.loadUrl(url)
        }
    }

    // 处理返回键，让WebView可以返回上一页
    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}