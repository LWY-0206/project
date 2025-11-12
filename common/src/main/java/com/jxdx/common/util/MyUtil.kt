package org.jxxy.debug.common.util

import android.animation.ObjectAnimator
import android.app.ActivityManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.corekit.common.BaseApplication
import com.example.corekit.util.gone
import com.example.corekit.util.load
import com.example.corekit.util.show
import com.example.corekit.util.singleClick
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.luck.picture.lib.PictureSelectorPreviewFragment
import com.luck.picture.lib.basic.IBridgeViewLifecycle
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.widget.MarqueeTextView
import com.luck.picture.lib.widget.PreviewTitleBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jxxy.debug.util.GlideEngine
import java.security.MessageDigest

fun getHeight(): Int = BaseApplication.context().resources.displayMetrics.heightPixels
fun getWidth(): Int = BaseApplication.context().resources.displayMetrics.widthPixels
fun getScreenWidth(): Int {
    return BaseApplication.context().resources.displayMetrics.heightPixels
}
fun dp2px(context: Context, dpValue: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}
fun getHysteresisOffset(textView: TextView, x: Int, y: Int, previousOffset: Int): Int {
    var previousOffset = previousOffset
    val layout = textView.layout ?: return -1
    var line = layout.getLineForVertical(y)

    // The "HACK BLOCK"S in this function is required because of how Android Layout for
    // TextView works - if 'offset' equals to the last character of a line, then
    //
    // * getLineForOffset(offset) will result the NEXT line
    // * getPrimaryHorizontal(offset) will return 0 because the next insertion point is on the next line
    // * getOffsetForHorizontal(line, x) will not return the last offset of a line no matter where x is
    // These are highly undesired and is worked around with the HACK BLOCK
    //
    // @see Moon+ Reader/Color Note - see how it can't select the last character of a line unless you move
    // the cursor to the beginning of the next line.
    //
    // //////////////////HACK BLOCK////////////////////////////////////////////////////
    if (isEndOfLineOffset(layout, previousOffset)) {
        // we have to minus one from the offset so that the code below to find
        // the previous line can work correctly.
        val left = layout.getPrimaryHorizontal(previousOffset - 1).toInt()
        val right = layout.getLineRight(line).toInt()
        val threshold = (right - left) / 2 // half the width of the last character
        if (x > right - threshold) {
            previousOffset -= 1
        }
    }
    // /////////////////////////////////////////////////////////////////////////////////
    val previousLine = layout.getLineForOffset(previousOffset)
    val previousLineTop = layout.getLineTop(previousLine)
    val previousLineBottom = layout.getLineBottom(previousLine)
    val hysteresisThreshold = (previousLineBottom - previousLineTop) / 2

    // If new line is just before or after previous line and y position is less than
    // hysteresisThreshold away from previous line, keep cursor on previous line.
    if (line == previousLine + 1 && y - previousLineBottom < hysteresisThreshold || line == previousLine - 1 && (
        previousLineTop -
            y
        ) < hysteresisThreshold
    ) {
        line = previousLine
    }
    var offset = layout.getOffsetForHorizontal(line, x.toFloat())

    // This allow the user to select the last character of a line without moving the
    // cursor to the next line. (As Layout.getOffsetForHorizontal does not return the
    // offset of the last character of the specified line)
    //
    // But this function will probably get called again immediately, must decrement the offset
    // by 1 to compensate for the change made below. (see previous HACK BLOCK)
    // ///////////////////HACK BLOCK///////////////////////////////////////////////////
    if (offset < textView.text.length - 1) {
        if (isEndOfLineOffset(layout, offset + 1)) {
            val left = layout.getPrimaryHorizontal(offset).toInt()
            val right = layout.getLineRight(line).toInt()
            val threshold = (right - left) / 2 // half the width of the last character
            if (x > right - threshold) {
                offset += 1
            }
        }
    }
    // ////////////////////////////////////////////////////////////////////////////////
    return offset
}
fun isEndOfLineOffset(layout: Layout, offset: Int): Boolean {
    return offset > 0 && layout.getLineForOffset(offset) === layout.getLineForOffset(offset - 1) + 1
}
fun getPreciseOffset(textView: TextView, x: Int, y: Int): Int {
    val layout = textView.layout
    return if (layout != null) {
        val topVisibleLine = layout.getLineForVertical(y)
        val offset = layout.getOffsetForHorizontal(topVisibleLine, x.toFloat())
        val offsetX = layout.getPrimaryHorizontal(offset).toInt()
        if (offsetX > x) {
            layout.getOffsetToLeftOf(offset)
        } else {
            offset
        }
    } else {
        -1
    }
}
fun getJsonContent(input: String): String {
    val startIndex = input.indexOf('{')
    val endIndex = input.lastIndexOf('}')
    if (startIndex >= 0 && endIndex > startIndex) {
        return input.substring(startIndex, endIndex + 1)
    }
    return ""
}
fun getMD5Code(info: String): String {
    try {
        val md5 = MessageDigest.getInstance("MD5")
        md5.update(info.toByteArray(charset("utf-8"))) // 设置编码格式
        val encryption = md5.digest()
        val stringBuffer = StringBuffer()
        for (i in encryption.indices) {
            if (Integer.toHexString(0xFF and encryption[i].toInt()).length == 1) {
                stringBuffer.append("0").append(Integer.toHexString(0xFF and encryption[i].toInt()))
            } else {
                stringBuffer.append(Integer.toHexString(0xFF and encryption[i].toInt()))
            }
        }
        return stringBuffer.toString()
    } catch (e: Exception) {
        return "MD5加密异常"
    }
}
fun generateAnalysis(key: String, type: String, more: String = "", startMore: String = ""): String {
    val template = "分析${key}的$startMore" +
        "以json的形式返回" +
        "class Node(): Serializable {" +
        "    var text: String" +
        "    var childNode: MutableList<Node>? = null" +
        "}" +
        "根节点text为“$type”$more"
    return template
}
fun getAuxiliaryWordsForNodes(subject: String, mainBody: String, nodeName: String = "", more: String = ""): String {
    val template = "分析${subject}的$mainBody" +
        "以json的形式返回" +
        "class Node(): Serializable {" +
        "    var text: String" +
        "    var childNode: MutableList<Node>? = null" +
        "}" +
        "根节点text为“$nodeName”$more"
    return template
}
fun showViews(vararg views: View) {
    for (view in views) {
        view.show()
    }
}
fun goneViews(vararg views: View) {
    for (view in views) {
        view.gone()
    }
}

fun ImageView.loads(url: String?, isCircle: Boolean = false, context: Context?) {
    this.load(url, isCircle)
    context?.let { context ->
        this.singleClick {
            val list = ArrayList<LocalMedia>()
            list.add(LocalMedia.generateHttpAsLocalMedia(url))
            PictureSelector.create(context)
                .openPreview()
                .setImageEngine(GlideEngine.createGlideEngine())
                .setAttachViewLifecycle(iBridgeViewLifecycle)
                .startActivityPreview(0, true, list)
        }
    }
}

fun ImageView.loads(url: String?, radius: Int, context: Context?) {
    this.load(url, radius)
    context?.let { context ->
        this.singleClick {
            val list = ArrayList<LocalMedia>()
            list.add(LocalMedia.generateHttpAsLocalMedia(url))
            PictureSelector.create(context)
                .openPreview()
                .setImageEngine(GlideEngine.createGlideEngine())
                .setAttachViewLifecycle(iBridgeViewLifecycle)
                .startActivityPreview(0, true, list)
        }
    }
}
fun ImageView.loads(url: String?, width: Int) {
    Glide.with(context)
        .load(url)
        .into(
            object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    this@loads.setImageDrawable(resource)
                    val width = width
                    val iw = resource.intrinsicWidth
                    val ih = resource.intrinsicHeight
                    val ratio = iw.toFloat() / ih.toFloat()
                    this@loads.layoutParams.height = (width / ratio).toInt()
                    this@loads.layoutParams.width = width
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            }
        )
}
private val centerCrop: CenterCrop by lazy { CenterCrop() }
private val roundedCornersMap: MutableMap<Int, RequestOptions> = mutableMapOf()
private fun roundedCorner(radius: Int): RequestOptions {
    return roundedCornersMap.getOrPut(radius) {
        RequestOptions().transform(centerCrop, RoundedCorners(radius))
    }
}
fun ImageView.loads(url: String?, radius: Int, width: Int, context: Context?) {
    context?.let {
        Glide.with(it)
            .load(url)
            .apply(roundedCorner(radius))
            .into(
                object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        this@loads.setImageDrawable(resource)
                        val iw = resource.intrinsicWidth
                        val width = width
                        val ih = resource.intrinsicHeight
                        val ratio = iw.toFloat() / ih.toFloat()
                        this@loads.layoutParams.height = (width / ratio).toInt()
                        this@loads.layoutParams.width = width
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                }
            )
        this.singleClick {
            val list = ArrayList<LocalMedia>()
            list.add(LocalMedia.generateHttpAsLocalMedia(url))
            PictureSelector.create(context)
                .openPreview()
                .setImageEngine(GlideEngine.createGlideEngine())
                .setAttachViewLifecycle(iBridgeViewLifecycle)
                .startActivityPreview(0, true, list)
        }
    }
}
fun FloatingActionButton.load(url: String) {
    Glide.with(this)
        .load(url)
        .into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                this@load.setImageDrawable(resource)
            }
            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
}
val iBridgeViewLifecycle = object : IBridgeViewLifecycle {
    override fun onViewCreated(
        fragment: Fragment?,
        view: View?,
        savedInstanceState: Bundle?
    ) {
        GlobalScope.launch {
            delay(100)
            launch(Dispatchers.Main) {
                (fragment as PictureSelectorPreviewFragment)
                    .view?.findViewById<PreviewTitleBar>(com.luck.picture.lib.R.id.title_bar)
                    ?.run {
                        findViewById<ImageView>(com.luck.picture.lib.R.id.ps_iv_delete)?.gone()
                        findViewById<MarqueeTextView>(com.luck.picture.lib.R.id.ps_tv_title)?.gone()
                    }
            }
        }
    }
    override fun onDestroy(fragment: Fragment?) {
    }
}
fun TextView.addOne() {
    val text = this.text.toString()
    if (text.isNotBlank() && text.toIntOrNull() != null) {
        this.text = (text.toInt() + 1).toString()
    }
}
fun TextView.minusOne() {
    val text = this.text.toString()
    if (text.isNotBlank() && text.toIntOrNull() != null) {
        this.text = (text.toInt() - 1).toString()
    }
}
fun MediaPlayer.loadAndPrepareByUrl(url: String, context: Context): MediaPlayer = this.apply {
    setAudioAttributes(audioAttributes)
    setDataSource(context, Uri.parse(url))
    prepareAsync()
}
val audioAttributes by lazy {
    AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
}
inline fun isMyServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val services = manager.getRunningServices(Integer.MAX_VALUE)
    for (serviceInfo in services) {
        if (serviceClass.name == serviceInfo.service.className) {
            return true
        }
    }
    return false
}
fun MoreViewAlpha0to1(views: List<List<View>>, time: Long = 400) {
    GlobalScope.launch {
        views.forEach {
            launch(Dispatchers.Main) {
                it.forEach {
                    if (it.alpha != 1f) {
                        ObjectAnimator.ofFloat(
                            it,
                            "alpha",
                            0f,
                            1f
                        ).apply {
                            duration = time
                            start()
                        }
                    }
                }
            }
            delay(time)
        }
    }
}
fun showKeyboard(view: View?) {
    view?.let {
        val inputMethodManager = BaseApplication.context()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun hideKeyboard(view: View?) {
    view?.let {
        val inputMethodManager = BaseApplication.context()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
fun MoreViewAlpha1to0(views: List<List<View>>, time: Long = 100) {
    GlobalScope.launch {
        views.forEach {
            launch(Dispatchers.Main) {
                it.forEach {
                    ObjectAnimator.ofFloat(
                        it,
                        "alpha",
                        1f,
                        0f
                    ).apply {
                        duration = time
                        start()
                    }
                }
            }
            delay(time)
        }
    }
}

//刷新rv布局 激活点击区域
fun RecyclerView.ensureInteractiveAfterDataSet(){
    this.requestLayout()
    this.post {
        this.scrollBy(0,1)
        this.scrollBy(0,-1)
    }
}
