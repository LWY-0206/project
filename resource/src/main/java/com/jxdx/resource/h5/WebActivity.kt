package org.jxxy.debug.h5

import android.view.KeyEvent
import android.view.View
import com.example.corekit.common.BaseActivity
import com.example.corekit.util.hide
import com.jxdx.resource.databinding.ActivityWebBinding
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebViewClient

class WebActivity : BaseActivity<ActivityWebBinding>() {
    override fun bindLayout(): ActivityWebBinding {
        return ActivityWebBinding.inflate(layoutInflater)
    }

    override fun initView() {
        initWebViewSettings()
        val url = intent.getStringExtra("url")
        url?.let { startWebH5(it) }
    }

    override fun subscribeUi() {
    }

    // 按下返回键
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 是否触发按键为back键
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed()
            true
        } else { // 如果不是back键正常响应
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun startWebH5(url: String) {
        view.webView.clearFormData()
        view.webView.clearHistory()
        view.webView.clearCache(true)
        view.webView.loadUrl(url)
    }

    val mWebViewClient: WebViewClient = X5WebViewClient()
    var count = 0

    val mWebChromeClient = object : com.tencent.smtt.sdk.WebChromeClient() {
        override fun onProgressChanged(p0: com.tencent.smtt.sdk.WebView?, p1: Int) {
            this@WebActivity.view.loadingProgressBar.progress = p1
            if (p1 >= 80 || count == 1) {
                // 页面加载完成时隐藏 ProgressBar
                this@WebActivity.view.loadingProgressBar.visibility = View.INVISIBLE
                this@WebActivity.view.loadingTv.hide()
                count = 1
            } else {
                // 页面正在加载中时显示 ProgressBar
                this@WebActivity.view.loadingProgressBar.visibility = View.VISIBLE
            }
            super.onProgressChanged(p0, p1)
        }
    }

    private fun initWebViewSettings() {
        val webSetting = view.webView.settings
        webSetting.javaScriptEnabled = true
        webSetting.javaScriptCanOpenWindowsAutomatically = true
        webSetting.allowFileAccess = true
        webSetting.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        webSetting.setSupportZoom(true)
        webSetting.builtInZoomControls = true
        webSetting.useWideViewPort = true
        webSetting.setSupportMultipleWindows(true)
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true)
        webSetting.databaseEnabled = true
        webSetting.domStorageEnabled = true
        webSetting.setGeolocationEnabled(true)
        webSetting.displayZoomControls = false
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE)
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.pluginState = WebSettings.PluginState.ON_DEMAND
        // webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.cacheMode = WebSettings.LOAD_NO_CACHE
        webSetting.saveFormData = false
        view.webView.webViewClient = mWebViewClient
        view.webView.webChromeClient = mWebChromeClient
    }
}
