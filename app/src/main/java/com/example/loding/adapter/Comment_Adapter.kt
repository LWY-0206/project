package com.example.loding.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.corekit.recyclerview.MultipleTypeAdapter
import com.example.corekit.recyclerview.ViewHolderTag
import com.example.corekit.util.load
import com.example.loding.R
import com.example.loding.entity.CommentItem

@Suppress("ktlint:standard:class-naming")
class Comment_Adapter : MultipleTypeAdapter() {
    // 定义类型标识（与CommentItem的viewType()返回值对应）
    companion object {
        const val TYPE_COMMENT = 3 // 评论类型（必须和CommentItem的viewType一致）
    }

    override fun createViewHolder(
        viewType: Int,
        inflater: LayoutInflater,
        parent: ViewGroup,
    ): RecyclerView.ViewHolder? {
        when (viewType) {
            TYPE_COMMENT -> {
                // 加载评论布局，创建对应的ViewHolder
                val view = inflater.inflate(R.layout.comment_item, parent, false)
                return CommentViewHolder(view)
            }
            else -> return null
        }
    }

    // 评论的ViewHolder
    class CommentViewHolder(
        private val view: View,
    ) : RecyclerView.ViewHolder(view),
        ViewHolderTag<CommentItem> {
        override fun setHolder(entity: CommentItem) {
            // 绑定CommentItem的数据到布局
            // 用户头像
            view.findViewById<View>(R.id.civ_comment_avatar)?.let {
                if (it is androidx.appcompat.widget.AppCompatImageView) {
                    it.load(entity.avatarUrl, 1)
                }
            }

            // 用户名
            view.findViewById<View>(R.id.tv_comment_username)?.let {
                if (it is androidx.appcompat.widget.AppCompatTextView) {
                    it.text = entity.commentUserName
                }
            }

            // 评论时间
            view.findViewById<View>(R.id.tv_comment_time)?.let {
                if (it is androidx.appcompat.widget.AppCompatTextView) {
                    it.text = entity.createTime
                }
            }

            // 评论内容
            view.findViewById<View>(R.id.tv_comment_content)?.let {
                if (it is androidx.appcompat.widget.AppCompatTextView) {
                    it.text = entity.content
                }
            }

            // 根据是否已点赞设置不同的文本颜色
            val tvLikeComment = view.findViewById<View>(R.id.tv_like_comment)
            if (tvLikeComment is androidx.appcompat.widget.AppCompatTextView) {
                // 根据是否已点赞设置不同的文本颜色
                if (entity.isLiked) {
                    tvLikeComment.setTextColor(view.context.resources.getColor(android.R.color.holo_red_light))
                    tvLikeComment.text = "已点赞"
                } else {
                    tvLikeComment.setTextColor(view.context.resources.getColor(android.R.color.darker_gray))
                    tvLikeComment.text = "点赞"
                }
            }

            // 设置回复按钮点击事件
            view.findViewById<View>(R.id.tv_reply)?.setOnClickListener {
                // 处理回复逻辑
                // 可以在这里弹出回复对话框或跳转到回复页面
            }

            // 设置点赞按钮点击事件
            view.findViewById<View>(R.id.tv_like_comment)?.setOnClickListener {
                // 切换点赞状态
                entity.isLiked = !entity.isLiked

                // 更新点赞数
                if (entity.isLiked) {
                    entity.likeCount += 1
                } else {
                    entity.likeCount = Math.max(0, entity.likeCount - 1)
                }

                // 立即更新UI，实现伪造的点赞效果
                if (tvLikeComment is androidx.appcompat.widget.AppCompatTextView) {
                    if (entity.isLiked) {
                        tvLikeComment.setTextColor(view.context.resources.getColor(android.R.color.holo_red_light))
                        tvLikeComment.text = "已点赞"
                    } else {
                        tvLikeComment.setTextColor(view.context.resources.getColor(android.R.color.darker_gray))
                        tvLikeComment.text = "点赞"
                    }
                }
            }
        }

        override fun setHolder(
            entity: CommentItem,
            payload: Any,
        ) {
            // 处理部分更新（可选实现）
            // 这里可以根据payload类型进行针对性的更新，例如只更新点赞状态
            setHolder(entity) // 简化处理，直接调用完整更新
        }
    }
}