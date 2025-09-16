package com.example.loding.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.corekit.recyclerview.ItemSelectListener
import com.example.corekit.recyclerview.MultipleTypeAdapter
import com.example.corekit.recyclerview.ViewHolderTag
import com.example.loding.entity.MessageItem
import com.jxdx.square.databinding.ItemMessageFriendBinding

class MessageAdapter : MultipleTypeAdapter() {
    // 定义类型标识（与MessageItem的viewType()返回值对应）
    companion object {
        const val TYPE_MESSAGE = 3
    }

    // 点击事件监听器
    private var itemClickListener: ItemSelectListener? = null

    // 设置点击事件监听器
    fun setOnItemClickListener(listener: ItemSelectListener) {
        itemClickListener = listener
    }

    override fun createViewHolder(
        viewType: Int,
        inflater: LayoutInflater,
        parent: ViewGroup,
    ): RecyclerView.ViewHolder? =
        when (viewType) {
            TYPE_MESSAGE -> {
                // 使用ViewBinding加载布局，创建对应的ViewHolder
                val binding = ItemMessageFriendBinding.inflate(inflater, parent, false)
                val viewHolder = MessageViewHolder(binding)

                // 设置点击事件
                binding.root.setOnClickListener {
                    itemClickListener?.onItemClick(viewHolder.adapterPosition)
                }

                viewHolder
            }
            else -> null
        }

    class MessageViewHolder(
        private val binding: ItemMessageFriendBinding,
    ) : RecyclerView.ViewHolder(binding.root),
        ViewHolderTag<MessageItem> {
        override fun setHolder(entity: MessageItem) {
            // 绑定头像
            binding.ivAvatar.setImageResource(entity.avatarResId)

            // 绑定名称
            binding.tvName.text = entity.name

            // 绑定时间
            binding.tvTime.text = entity.time

            // 绑定最近一条消息
            binding.tvMessage.text = entity.message
        }

        override fun setHolder(
            entity: MessageItem,
            payload: Any,
        ) {
            // 处理部分更新的逻辑
            // 这里可以根据payload的内容决定更新哪些控件
            // 例如，如果只需要更新消息内容，可以只设置binding.tvMessage.text
            setHolder(entity)
        }
    }
}
