package com.example.loding.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.corekit.common.BaseFragment
import com.example.corekit.util.load
import com.example.loding.databinding.FragmentFriendDetailsBinding

class FriendDetailsFragment : BaseFragment<FragmentFriendDetailsBinding>() {
    // 定义参数键
    companion object {
        private const val ARG_FRIEND_NAME = "friend_name"
        private const val ARG_FRIEND_AVATAR = "friend_avatar"

        // 创建Fragment实例并传递参数
        fun newInstance(friendName: String, friendAvatar: String): FriendDetailsFragment {
            val fragment = FriendDetailsFragment()
            val args = Bundle()
            args.putString(ARG_FRIEND_NAME, friendName)
            args.putString(ARG_FRIEND_AVATAR, friendAvatar)
            fragment.arguments = args
            return fragment
        }
    }

    private var friendName: String? = null
    private var friendAvatar: String? = null

    override fun bindLayout(): FragmentFriendDetailsBinding = FragmentFriendDetailsBinding.inflate(layoutInflater)

    override fun initView() {
        // 获取参数
        arguments?.let {
            friendName = it.getString(ARG_FRIEND_NAME)
            friendAvatar = it.getString(ARG_FRIEND_AVATAR)
        }

        // 更新UI显示
        friendName?.let {
            find.usernameText.text = it
        }

        friendAvatar?.let {
            find.avatarImage.load(it, isCircle = true)
        }

        // 设置返回按钮点击事件
        find.backButton.setOnClickListener {
            // 返回上一个Fragment
            parentFragmentManager.popBackStack()
        }
    }

    override fun subscribeUi() {
    }
}
