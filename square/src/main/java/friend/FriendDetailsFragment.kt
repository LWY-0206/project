package com.example.loding.friend

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseFragment
import com.example.corekit.util.load
import com.example.loding.friend.DeleteDialog
import com.example.loding.friend.SearchFriendViewModel
import com.jxdx.square.databinding.FragmentFriendDetailsBinding

class
FriendDetailsFragment : BaseFragment<FragmentFriendDetailsBinding>() {
    private val viewModel: SearchFriendViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application),
        )[SearchFriendViewModel::class.java]
    }

    // 定义参数键
    companion object {
        private const val ARG_FRIEND_NAME = "friend_name"
        private const val ARG_FRIEND_AVATAR = "friend_avatar"
        private const val ARG_FRIEND_ID = "friend_id"

        // 创建Fragment实例并传递参数
        fun newInstance(
            friendName: String,
            friendAvatar: String,
            friendId: String = "",
        ): FriendDetailsFragment {
            val fragment = FriendDetailsFragment()
            val args = Bundle()
            args.putString(ARG_FRIEND_NAME, friendName)
            args.putString(ARG_FRIEND_AVATAR, friendAvatar)
            args.putString(ARG_FRIEND_ID, friendId)
            fragment.arguments = args
            return fragment
        }
    }

    private var friendName: String? = null
    private var friendAvatar: String? = null
    private var friendId: String? = null

    override fun bindLayout(): FragmentFriendDetailsBinding = FragmentFriendDetailsBinding.inflate(layoutInflater)

    override fun initView() {
        // 获取参数
        arguments?.let {
            friendName = it.getString(ARG_FRIEND_NAME)
            friendAvatar = it.getString(ARG_FRIEND_AVATAR)
            friendId = it.getString(ARG_FRIEND_ID)
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

        // 设置删除好友按钮点击事件
        find.deleteFriendButton.setOnClickListener {
            // 创建并显示删除好友对话框
            val deleteDialog = DeleteDialog()
            deleteDialog.show(parentFragmentManager, "DeleteFriendDialog")
        }
    }

    override fun subscribeUi() {
        // 调用getUserInfo方法加载用户详情
        friendId?.let {
            viewModel.getUserInfo(it)
        }

        viewModel.friendDetailsLiveData.observe(this) {
            it.onSuccess { userInfo ->
                userInfo?.let {
                    find.bioText.text = it.profile
                    find.avatarImage.load(it.avatarUrl)
                    find.userIdText.text = it.id.toString()
                    find.friendPhone.text = it.phone
                    if (it.status == 0) {
                        find.friendStatus.text = "学生"
                    } else if (it.status == 1) {
                        find.friendStatus.text = "老师"
                    } else {
                        find.friendStatus.text = "未认证"
                    }
                    find.classText.text = it.className
                }
            }
        }
    }
}
