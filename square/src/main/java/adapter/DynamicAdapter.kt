package com.example.loding.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.corekit.recyclerview.MultipleTypeAdapter
import com.example.corekit.recyclerview.ViewHolderTag
import com.example.corekit.util.load
import com.example.loding.activity.DynamicdetailsActivity
import com.example.loding.entity.Dynamic
import com.jxdx.square.R
import kotlin.math.min

class DynamicAdapter : MultipleTypeAdapter() {
    // 定义类型标识（与DongtaiItem的viewType()返回值对应）
    companion object {
        const val TYPE_DYNAMIC = 3 // 动态内容类型（必须和DongtaiItem的viewType一致）
    }

    // 根据viewType创建ViewHolder
    override fun createViewHolder(
        viewType: Int,
        inflater: LayoutInflater,
        parent: ViewGroup,
    ): RecyclerView.ViewHolder? =
        when (viewType) {
            TYPE_DYNAMIC -> {
                // 加载动态内容布局，创建对应的ViewHolder
                val view = inflater.inflate(R.layout.dynamic_item, parent, false)
                DynamicViewHolder(view)
            }
            else -> null
        }

    // 动态内容的ViewHolder
    class DynamicViewHolder(
        private val view: View,
    ) : RecyclerView.ViewHolder(view),
        ViewHolderTag<Dynamic> {
        // 存储当前绑定的数据
        private var currentDynamic: Dynamic? = null

        // 初始化时设置点击事件
        init {
            // 为整个动态项设置点击事件
            view.setOnClickListener {
                // 获取当前绑定的数据
                val dynamicData = currentDynamic
                if (dynamicData != null) {
                    // 添加日志，确认传递的isLike值
                    Log.d("DynamicAdapter", "传递的点赞状态: ${dynamicData.isLiked}")
                    Log.d("DynamicAdapter", "传递的动态ID: ${dynamicData.id}")
                    // 从上下文启动动态详情页Activity
                    val intent = android.content.Intent(view.context, DynamicdetailsActivity::class.java)
                    // 传递动态ID
                    intent.putExtra("Id", dynamicData.id)
                    // 传递评论数量
                    intent.putExtra("commentCount", dynamicData.commentCount)
                    intent.putExtra("isLike", dynamicData.isLiked)
//                    intent.putExtra("username", dynamicData.userName)
                    // 启动Activity
                    view.context.startActivity(intent)
                }
            }
        }

        override fun setHolder(entity: Dynamic) {
            // 更新当前绑定的数据
            this.currentDynamic = entity
            Log.d("DynamicAdapter", "绑定数据时的isLike: ${entity.isLiked}")
            // 绑定DongtaiItem的数据到布局
            // 用户头像
            view.findViewById<View>(R.id.civ_user_avatar)?.let {
                if (it is androidx.appcompat.widget.AppCompatImageView) {
//                view -> if (view is AppCompatImageView) {
//                    val avatarUrl = entity.avatarUrl
//                    Log.d("AvatarLoad", "头像URL: $avatarUrl")
//                    // 使用ImageUtil和setSource加载图片
//                    if (!avatarUrl.isNullOrBlank()) {
//                        // 直接使用 Glide 加载，无需工具类
//                        Glide
//                            .with(view.context)
//                            .load(avatarUrl)
//                            .placeholder(R.drawable.ic_default_avatar) // 加载中占位图
//                            .centerCrop()
//                            .into(view) // view 是 AppCompatImageView
//                        Log.d("user", "图片加载指令已发送")
//                    } else {
//                        // URL为空时显示默认头像
//                        view.setImageResource(R.drawable.ic_default_avatar)
//                        Log.d("user", "URL为空，显示默认头像")
//                    }
//                    Log.d("AvatarLoad", "头像URL: ${entity.avatarUrl}")
                    it.load(entity.avatarUrl, false)
//                    it.load("https://tongue-srt.oss-cn-hangzhou.aliyuncs.com/2025/09/05/a0bf2736-b843-4505-ad82-36c38a925f7c.jpg", false)
//                    https://tongue-srt.oss-cn-hangzhou.aliyuncs.com/2025/09/05/a0bf2736-b843-4505-ad82-36c38a925f7c.jpg
//                    Log.d("user", "图片加载")
                }
            }

            // 用户名
            view.findViewById<View>(R.id.tv_username)?.let {
                if (it is androidx.appcompat.widget.AppCompatTextView) {
                    it.text = entity.userName
                }
            }

            // 发布时间
            view.findViewById<View>(R.id.tv_publish_time)?.let {
                if (it is androidx.appcompat.widget.AppCompatTextView) {
                    it.text = entity.createTime
                }
            }

            // 动态内容
            view.findViewById<View>(R.id.tv_dynamic_content)?.let {
                if (it is androidx.appcompat.widget.AppCompatTextView) {
                    it.text = entity.content
                }
            }

            // 图片展示区域
            val imagesContainer = view.findViewById<View>(R.id.ll_images_container)
            if (imagesContainer is ViewGroup && !entity.contentImageUrls.isNullOrEmpty()) {
                imagesContainer.visibility = View.VISIBLE

                // 首先隐藏所有图片视图
                val imageViewIds = listOf(R.id.iv_image_1, R.id.iv_image_2, R.id.iv_image_3)
                for (id in imageViewIds) {
                    view.findViewById<View>(id)?.visibility = View.GONE
                }

                // 然后处理需要显示的图片列表（最多3张）
                for (i in 0 until min(entity.contentImageUrls.size, 3)) {
                    val imageViewId = imageViewIds[i]
                    view.findViewById<View>(imageViewId)?.let {
                        if (it is androidx.appcompat.widget.AppCompatImageView) {
                            it.visibility = View.VISIBLE
                            it.load(entity.contentImageUrls[i], false)
                        }
                    }
                }
            } else {
                imagesContainer?.visibility = View.GONE
            }

            // 互动数据
            // 点赞数
            view.findViewById<View>(R.id.tv_like_count)?.let {
                if (it is androidx.appcompat.widget.AppCompatTextView) {
                    it.text = "${entity.likeCount}"
                }
            }

            // 打赏数
            view.findViewById<View>(R.id.tv_reward_count)?.let {
                if (it is androidx.appcompat.widget.AppCompatTextView) {
                    it.text = "${entity.rewardCount}"
                }
            }

            // 评论数
            view.findViewById<View>(R.id.tv_comment_count)?.let {
                if (it is androidx.appcompat.widget.AppCompatTextView) {
                    it.text = "${entity.commentCount}"
                }
            }
            // 是否点赞
            // 单独更新点赞图标状态（与当前绑定的entity同步）
            view.findViewById<AppCompatImageView>(R.id.iv_like)?.let { ivLike ->
                if (entity.isLiked) {
                    ivLike.setColorFilter(ContextCompat.getColor(view.context, R.color.red))
                } else {
                    ivLike.clearColorFilter()
                    ivLike.setImageResource(R.drawable.ic_like)
                }
            }

//            // 评论预览
//            val commentPreview = view.findViewById<View>(R.id.ll_first_comment)
//            if (commentPreview != null && !entity.firstComment.isNullOrEmpty()) {
//                commentPreview.visibility = View.VISIBLE
//                commentPreview.findViewById<View>(R.id.tv_comment_preview)?.let {
//                    if (it is androidx.appcompat.widget.AppCompatTextView) {
//                        it.text = entity.firstComment
//                    }
//                }
//            } else {
//                commentPreview?.visibility = View.GONE
//            }
        }

        override fun setHolder(
            entity: Dynamic,
            payload: Any,
        ) {
            // 处理部分更新（可选实现）
            // 这里可以根据payload类型进行针对性的更新，例如只更新点赞数等
            setHolder(entity) // 简化处理，直接调用完整更新
        }
    }
}
