package org.jxxy.debug.h5.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.corekit.common.BaseActivity
import com.example.corekit.util.gone
import com.example.corekit.util.toast
import com.jxdx.resource.databinding.ActivityToolBinding
import com.lxj.xpopup.XPopup
import com.tencent.smtt.export.external.extension.interfaces.IX5WebChromeClientExtension
import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewExtension
import com.tencent.smtt.export.external.interfaces.IX5WebViewBase
import com.tencent.smtt.export.external.interfaces.JsResult
import com.tencent.smtt.export.external.interfaces.MediaAccessPermissionsCallback
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.QbSdk.PreInitCallback


open class ToolActivity : BaseActivity<ActivityToolBinding>() {

    companion object {
        fun actionStart(
            context: Context,
            files: String
        ) {
            context.startActivity(Intent(context, ToolActivity::class.java).apply {
                putExtra("files", files)
            })
        }
    }
    override fun bindLayout(): ActivityToolBinding {
        return ActivityToolBinding.inflate(layoutInflater)
    }

    override fun initView() {
        initX5WebView()

        view.apply {
            x5WebView.apply {
                settings.javaScriptEnabled = true
                webChromeClientExtension = object : IX5WebChromeClientExtension {
                    override fun getX5WebChromeClientInstance(): Any? {
                        return null
                    }

                    override fun getVideoLoadingProgressView(): View? {
                        return null
                    }

                    override fun onAllMetaDataFinished(
                        p0: IX5WebViewExtension?,
                        p1: HashMap<String, String>?
                    ) {
                        
                    }

                    override fun onBackforwardFinished(p0: Int) {
                        
                    }

                    override fun onHitTestResultForPluginFinished(
                        p0: IX5WebViewExtension?,
                        p1: IX5WebViewBase.HitTestResult?,
                        p2: Bundle?
                    ) {
                        
                    }

                    override fun onHitTestResultFinished(
                        p0: IX5WebViewExtension?,
                        p1: IX5WebViewBase.HitTestResult?
                    ) {
                        
                    }

                    override fun onPromptScaleSaved(p0: IX5WebViewExtension?) {
                        
                    }

                    override fun onPromptNotScalable(p0: IX5WebViewExtension?) {
                        
                    }

                    override fun onAddFavorite(
                        p0: IX5WebViewExtension?,
                        p1: String?,
                        p2: String?,
                        p3: JsResult?
                    ): Boolean {
                        return false
                    }

                    override fun onPrepareX5ReadPageDataFinished(
                        p0: IX5WebViewExtension?,
                        p1: HashMap<String, String>?
                    ) {
                       
                    }

                    override fun onSavePassword(
                        p0: String?,
                        p1: String?,
                        p2: String?,
                        p3: Boolean,
                        p4: Message?
                    ): Boolean {
                        return false
                    }

                    override fun onSavePassword(
                        p0: ValueCallback<String>?,
                        p1: String?,
                        p2: String?,
                        p3: String?,
                        p4: String?,
                        p5: String?,
                        p6: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onX5ReadModeAvailableChecked(p0: HashMap<String, String>?) {
                       
                    }

                    override fun addFlashView(p0: View?, p1: ViewGroup.LayoutParams?) {
                        
                    }

                    override fun h5videoRequestFullScreen(p0: String?) {
                        
                    }

                    override fun h5videoExitFullScreen(p0: String?) {
                        
                    }

                    override fun requestFullScreenFlash() {
                        
                    }

                    override fun exitFullScreenFlash() {
                        
                    }

                    override fun jsRequestFullScreen() {
                        
                    }

                    override fun jsExitFullScreen() {
                        
                    }

                    override fun acquireWakeLock() {
                        
                    }

                    override fun releaseWakeLock() {
                        
                    }

                    override fun getApplicationContex(): Context? {
                        return null
                    }

                    override fun onPageNotResponding(p0: Runnable?): Boolean {
                        return false
                    }

                    override fun onMiscCallBack(p0: String?, p1: Bundle?): Any? {
                        return null
                    }

                    override fun openFileChooser(
                        p0: ValueCallback<Array<Uri>>?,
                        p1: String?,
                        p2: String?
                    ) {
                        
                    }

                    override fun onPrintPage() {
                        
                    }

                    override fun onColorModeChanged(p0: Long) {
                        
                    }

                    override fun onPermissionRequest(
                        p0: String?,
                        p1: Long,
                        p2: MediaAccessPermissionsCallback?
                    ): Boolean {
                        p2?.invoke(p0, p1, true)
                        return true
                    }
                }
                loadUrl("file:///android_asset/webpage/tool/balancing_chemical_equations.html") // 默认绘图计算器
                intent.extras?.let {
                    loadUrl(it.getString("files"))
                }
                geogebraMenuButton.apply {
                    var isDrag = false
                    var dX = 0f
                    var dY = 0f
                    setOnTouchListener { v, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                isDrag = false
                                dX = v.x - event.rawX
                                dY = v.y - event.rawY
                            }

                            MotionEvent.ACTION_MOVE -> {
                                isDrag = true
                                v.animate()
                                    .x(event.rawX + dX)
                                    .y(event.rawY + dY)
                                    .setDuration(0)
                                    .start()
                            }

                            MotionEvent.ACTION_UP -> {
                                if (!isDrag) {
                                    Log.d("ToolActivity", "Confirm clicked")
                                    v.performClick()
                                }
                            }

                            else -> return@setOnTouchListener false
                        }
                        true
                    }
                    setOnClickListener {
                        XPopup.Builder(this@ToolActivity)
                            .asCenterList(
                                "模式选择",
                                arrayOf(
                                    "原子相互作用",
                                    "酸碱溶液",
                                    "ph值",
                                    "气球和静电",
                                    "比尔定律",
                                    "创造一个分子",
                                    "原子模型",
                                    "经典"
                                )

                            ) { position, text ->
                                text.toast(false)
                                when (position) {
                                    0 -> {
                                        x5WebView.loadUrl("file:///android_asset/webpage/tool/atomic_interactions.html")
                                    }

                                    1 -> {

                                        x5WebView.loadUrl("file:///android_asset/webpage/tool/acid_base_solutions.html")
                                    }

                                    2 -> {
                                        x5WebView.loadUrl("file:///android_asset/webpage/tool/ph_scale_basics.html")
                                    }

                                    3 -> {
                                        x5WebView.loadUrl("file:///android_asset/webpage/tool/balloons_and_static_electricity.html")
                                    }

                                    4 -> {
                                        x5WebView.loadUrl("file:///android_asset/webpage/tool/beers_law_lab.html")
                                    }

                                    5 -> {
                                        x5WebView.loadUrl("file:///android_asset/webpage/tool/build_a_molecule.html")
                                    }

                                    6 -> {
                                        x5WebView.loadUrl("file:///android_asset/webpage/tool/build_an_atom.html")
                                    }
                                    7 -> {
                                        x5WebView.loadUrl("file:///android_asset/webpage/tool/balancing_chemical_equations.html")
                                    }
                                }
                            }
                            .show()
                    }
                }
            }
        }
    }

    override fun subscribeUi() {
    }

    private fun initX5WebView() {
        //使用腾讯x5 WebView，解决安卓原生WebView不适配不同机型问题
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        //x5内核初始化接口
        QbSdk.initX5Environment(applicationContext, object : PreInitCallback {
            override fun onViewInitFinished(arg0: Boolean) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。

                if (arg0) { //true
                    Log.e("腾讯X5", " onViewInitFinished 加载 成功 $arg0")
                } else {
                    Log.e(
                        "腾讯X5",
                        " onViewInitFinished 加载 失败！！！使用原生安卓webview $arg0"
                    )
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
}