package com.jxdx.square.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.example.corekit.recyclerview.SingleTypeAdapter
import com.example.corekit.recyclerview.SingleViewHolder
import com.jxdx.square.R
import com.jxdx.square.databinding.CommentMessageItemBinding

class CommentDynamicAdapter: SingleTypeAdapter<String>() {
    override fun createViewHolder(
        viewType: Int,
        inflater: LayoutInflater,
        parent: ViewGroup
    ): SingleViewHolder<ViewBinding, Any>? {
        return CommentDynamicViewHolder(
            CommentMessageItemBinding.inflate(inflater, parent, false)
        ) as? SingleViewHolder<ViewBinding, Any>
    }

    class CommentDynamicViewHolder(view: CommentMessageItemBinding): SingleViewHolder<CommentMessageItemBinding, Any>(view) {
        override fun setHolder(entity: Any) {
            // 解析评论数据，格式为：用户名评论：\n评论内容\n评论时间
            val commentInfo = entity.toString().split('\n')
            if (commentInfo.size >= 3) {
                // 合并第一行和第二行作为评论内容（包含用户名和评论内容）
                val commentContent = commentInfo[0] + '\n' + commentInfo[1]
                // 第三行作为评论时间
                val commentTime = commentInfo[2]
                view.tvCommentContent.text = commentContent
                view.tvCommentTime.text = commentTime
            } else {
                // 兼容旧格式
                view.tvCommentContent.text = entity.toString()
                view.tvCommentTime.text = ""
            }
            view.ivAvatar.setImageResource(R.drawable.ic_avatar)
        }
    }
}