package com.jxdx.square.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.corekit.recyclerview.MultipleTypeAdapter
import com.example.corekit.recyclerview.MultipleViewHolder
import com.example.corekit.recyclerview.ViewHolderTag
import com.example.corekit.util.load
import com.jxdx.square.databinding.ItemMessageReceiveBinding
import com.jxdx.square.databinding.ItemMessageSendBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter : MultipleTypeAdapter() {
    companion object {
        const val TYPE_SEND = 1
        const val TYPE_RECEIVE = 2
    }

    override fun createViewHolder(
        viewType: Int,
        inflater: LayoutInflater,
        parent: ViewGroup,
    ): RecyclerView.ViewHolder? =
        when (viewType) {
            TYPE_SEND -> SendMessageViewHolder(ItemMessageSendBinding.inflate(inflater, parent, false))
            TYPE_RECEIVE -> ReceiveMessageViewHolder(ItemMessageReceiveBinding.inflate(inflater, parent, false))
            else -> null
        }

    class SendMessageViewHolder(
        private val binding: ItemMessageSendBinding,
    ) : RecyclerView.ViewHolder(binding.root),
        ViewHolderTag<Message> {
        override fun setHolder(
            entity: Message,
            payload: Any,
        ) {
            // 简化版本，不处理部分更新
            setHolder(entity)
        }

        override fun setHolder(entity: Message) {
            // 绑定发送者头像
            binding.ivSenderAvatar.load(entity.avatarUrl, 1)

            // 绑定发送消息内容，去除可能存在的ID前缀
            binding.tvSendContent.text = removeIdPrefix(entity.content)

            // 绑定发送时间
            binding.tvSendTime.text = formatTime(entity.time)
        }
        
        // 去除消息内容中可能存在的ID前缀
        private fun removeIdPrefix(content: String): String {
            val parts = content.split(" ", limit = 2)
            if (parts.size >= 2 && parts[0].toIntOrNull() != null) {
                // 如果第一个部分是数字，返回第二部分作为实际内容
                return parts[1]
            }
            // 否则返回原始内容
            return content
        }

        // 时间格式化工具方法
        fun formatTime(time: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(time))
        }
    }

    class ReceiveMessageViewHolder(
        private val binding: ItemMessageReceiveBinding,
    ) : MultipleViewHolder<Message>(binding.root) {
        override fun setHolder(entity: Message) {
            // 绑定接收者头像
            binding.ivReceiverAvatar.load(entity.avatarUrl, 1)

            // 绑定接收消息内容，去除可能存在的ID前缀
            binding.tvReceiveContent.text = removeIdPrefix(entity.content)

            // 绑定接收时间
            binding.tvReceiveTime.text = formatTime(entity.time)
        }
        
        // 去除消息内容中可能存在的ID前缀
        private fun removeIdPrefix(content: String): String {
            val parts = content.split(" ", limit = 2)
            if (parts.size >= 2 && parts[0].toIntOrNull() != null) {
                // 如果第一个部分是数字，返回第二部分作为实际内容
                return parts[1]
            }
            // 否则返回原始内容
            return content
        }

        // 时间格式化工具方法
        fun formatTime(time: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(time))
        }
    }
}
