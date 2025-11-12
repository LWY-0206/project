package com.jxdx.square.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.example.corekit.recyclerview.SingleTypeAdapter
import com.example.corekit.recyclerview.SingleViewHolder
import com.example.corekit.util.load
import com.jxdx.square.databinding.ItemFriendBinding
import com.jxdx.square.entity.Friend

class FriendTypeAdapter(
    private val userList: List<Friend>,
) : SingleTypeAdapter<Friend>() {
    init {
        add(userList)
    }

    // 定义点击事件监听器接口
    interface OnItemClickListener {
        fun onItemClick(friend: Friend)
    }

    // 保存点击事件监听器
    private var onItemClickListener: OnItemClickListener? = null

    // 设置点击事件监听器的方法
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    override fun createViewHolder(
        viewType: Int,
        inflater: LayoutInflater,
        parent: ViewGroup,
    ): SingleViewHolder<ViewBinding, Any>? {
        val binding = ItemFriendBinding.inflate(inflater, parent, false)
        return FriendViewHolder(binding) as SingleViewHolder<ViewBinding, Any>
    }

    inner class FriendViewHolder(
        private val binding: ItemFriendBinding,
    ) : SingleViewHolder<ViewBinding, Friend>(binding) {
        override fun setHolder(entity: Friend) {
            binding.tvName.text = entity.friendName
            binding.ivAvatar.load(entity.friendAvatar, isCircle = true)

            // 设置点击事件
            binding.root.setOnClickListener {
                onItemClickListener?.onItemClick(entity)
            }
        }
    }
}
