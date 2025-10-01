package com.jxdx.square.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import com.example.corekit.recyclerview.SingleTypeAdapter
import com.example.corekit.recyclerview.SingleViewHolder
import com.example.corekit.util.load
import com.jxdx.square.databinding.FriendRequestItemBinding
import com.jxdx.square.entity.ApplicationMessage

class ApplicationAdapter(
    val list: ArrayList<ApplicationMessage>,
) : SingleTypeAdapter<ApplicationMessage>() {
    
    private var onAcceptClickListener: ((ApplicationMessage) -> Unit)? = null
    private var onRejectClickListener: ((ApplicationMessage) -> Unit)? = null
    
    init {
        add(list)
    }
    
    fun setOnAcceptClickListener(listener: (ApplicationMessage) -> Unit) {
        onAcceptClickListener = listener
    }
    
    fun setOnRejectClickListener(listener: (ApplicationMessage) -> Unit) {
        onRejectClickListener = listener
    }

    override fun createViewHolder(
        viewType: Int,
        inflater: LayoutInflater,
        parent: ViewGroup,
    ): SingleViewHolder<ViewBinding, Any>? {
        val binding = FriendRequestItemBinding.inflate(inflater, parent, false)
        return ApplicationViewHolder(binding, this) as SingleViewHolder<ViewBinding, Any>
    }

    class ApplicationViewHolder(
        // 泛型1：指定具体Binding类（非ViewBinding），避免强转
        private val binding: FriendRequestItemBinding,
        private val adapter: ApplicationAdapter,
    ) : SingleViewHolder<FriendRequestItemBinding, ApplicationMessage>(binding) {
        override fun setHolder(entity: ApplicationMessage) {
            binding.usernameText.text = entity.applicantName
            binding.messageText.text = entity.remark
            binding.timeText.text = entity.createTime
            binding.avatarView.load(entity.applicantAvatar)
            
            // 根据状态控制UI显示
            when (entity.status) {
                0 -> {
                    // 待确认状态：显示接受和拒绝按钮
                    binding.buttonContainer.visibility = android.view.View.VISIBLE
                    binding.statusText.visibility = android.view.View.GONE
                    
                    // 设置接受按钮点击事件
                    binding.acceptButton.setOnClickListener {
                        adapter.onAcceptClickListener?.invoke(entity)
                    }
                    
                    // 设置拒绝按钮点击事件
                    binding.rejectButton.setOnClickListener {
                        adapter.onRejectClickListener?.invoke(entity)
                    }
                }
                1 -> {
                    // 已确认状态：显示"已接收"
                    binding.buttonContainer.visibility = android.view.View.GONE
                    binding.statusText.visibility = android.view.View.VISIBLE
                    binding.statusText.text = "已接收"
                    binding.statusText.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                }
                2 -> {
                    // 已拒绝状态：显示"已拒绝"
                    binding.buttonContainer.visibility = android.view.View.GONE
                    binding.statusText.visibility = android.view.View.VISIBLE
                    binding.statusText.text = "已拒绝"
                    binding.statusText.setTextColor(android.graphics.Color.parseColor("#FF5252"))
                }
            }
        }
    }
}
