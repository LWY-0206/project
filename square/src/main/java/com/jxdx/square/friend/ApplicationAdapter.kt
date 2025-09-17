package com.jxdx.square.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.example.corekit.recyclerview.SingleTypeAdapter
import com.example.corekit.recyclerview.SingleViewHolder
import com.example.corekit.util.load
import com.jxdx.square.databinding.FriendRequestItemBinding
import com.jxdx.square.entity.ApplicationMessage

class ApplicationAdapter(
    val list: ArrayList<ApplicationMessage>,
) : SingleTypeAdapter<ApplicationMessage>() {
    init {
        add(list)
    }

    override fun createViewHolder(
        viewType: Int,
        inflater: LayoutInflater,
        parent: ViewGroup,
    ): SingleViewHolder<ViewBinding, Any>? {
        val binding = FriendRequestItemBinding.inflate(inflater, parent, false)
        return ApplicationViewHolder(binding) as SingleViewHolder<ViewBinding, Any>
    }

    class ApplicationViewHolder(
        // 泛型1：指定具体Binding类（非ViewBinding），避免强转
        private val binding: FriendRequestItemBinding,
    ) : SingleViewHolder<FriendRequestItemBinding, ApplicationMessage>(binding) {
        override fun setHolder(entity: ApplicationMessage) {
            binding.usernameText.text = entity.applicantName
            binding.messageText.text = entity.remark
            binding.timeText.text = entity.createTime
            binding.avatarView.load(entity.applicantAvatar)
        }
    }
}
