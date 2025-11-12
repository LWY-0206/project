package com.jxdx.resource.resource
import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.corekit.util.load
import com.youth.banner.loader.ImageLoader


class GlideImageLoader : ImageLoader() {
    override fun displayImage(context: Context, path: Any?, imageView: ImageView) {
        // path 可以是 URL 字符串、本地文件路径、Drawable 资源 ID 等，Glide 会自动适配
        imageView.load(path as? String, 8) // 8为圆角半径，可调整
    }
}