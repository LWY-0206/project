package com.jxdx.square.friend

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseFragment
import com.example.corekit.util.load
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
            friendAvatar: String?,
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
            // 获取好友ID并创建删除对话框
            val friendIdInt = friendId?.toIntOrNull() ?: 0
            if (friendIdInt == 0) {
                android.widget.Toast.makeText(requireContext(), "好友ID无效", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val deleteDialog = DeleteDialog.newInstance(friendIdInt)
            deleteDialog.show(parentFragmentManager, "DeleteFriendDialog")
        }

        // 设置前往聊天按钮点击事件
        find.goChatButton.setOnClickListener {
            // 检查好友信息是否有效
            if (friendName.isNullOrEmpty() || friendId.isNullOrEmpty()) {
                android.widget.Toast.makeText(requireContext(), "好友信息无效", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 使用SharedPreferences保存联系人信息
            val sharedPref = requireContext().getSharedPreferences("friend_chat", android.content.Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("friend_name", friendName)
            editor.putString("friend_id", friendId)
            editor.apply()
            
            // 模拟页面向左滑动，切换到消息页面
            // 通过回调函数通知父Fragment切换到消息页面
            switchToMessagePage()
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

    /**
     * 删除好友成功后的回调方法
     */
    fun onFriendDeleted() {
        // 显示删除成功提示
        android.widget.Toast.makeText(requireContext(), "好友已删除", android.widget.Toast.LENGTH_SHORT).show()
        
        // 返回上一页 - 使用Activity的FragmentManager确保正确返回
        requireActivity().supportFragmentManager.popBackStack()
    }
    
    /**
     * 切换到消息页面
     */
    private fun switchToMessagePage() {
        // 先保存Activity引用，避免Fragment销毁后无法访问
        val activity = activity
        if (activity == null) {
            android.util.Log.e("FriendDetailsFragment", "Activity为null，无法切换页面")
            return
        }
        
        // 返回到联系人页面
        parentFragmentManager.popBackStack()
        
        // 延迟执行，确保Fragment已经返回
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            // 检查Activity是否还存在
            if (activity.isFinishing || activity.isDestroyed) {
                android.util.Log.e("FriendDetailsFragment", "Activity已销毁，无法切换页面")
                return@postDelayed
            }
            
            // 通过多种方式查找TopFragment并切换到消息页面
            try {
                val fragmentManager = activity.supportFragmentManager
                
                // 方法1：遍历所有Fragment
                val fragments = fragmentManager.fragments
                android.util.Log.d("FriendDetailsFragment", "开始查找TopFragment，当前Fragment数量: ${fragments.size}")
                
                for (fragment in fragments) {
                    android.util.Log.d("FriendDetailsFragment", "检查Fragment: ${fragment.javaClass.simpleName}")
                    if (fragment.javaClass.simpleName == "TopFragment") {
                        android.util.Log.d("FriendDetailsFragment", "找到TopFragment，准备切换到消息页面")
                        switchViewPagerToMessage(fragment)
                        return@postDelayed
                    }
                }
                
                // 方法2：通过FragmentManager查找
                val topFragment = fragmentManager.findFragmentByTag("TopFragment")
                if (topFragment != null) {
                    android.util.Log.d("FriendDetailsFragment", "通过Tag找到TopFragment")
                    switchViewPagerToMessage(topFragment)
                    return@postDelayed
                }
                
                // 方法3：通过容器ID查找
                val containerFragment = fragmentManager.findFragmentById(android.R.id.content)
                if (containerFragment != null) {
                    android.util.Log.d("FriendDetailsFragment", "通过容器ID找到Fragment: ${containerFragment.javaClass.simpleName}")
                    if (containerFragment.javaClass.simpleName == "TopFragment") {
                        switchViewPagerToMessage(containerFragment)
                        return@postDelayed
                    }
                }
                
                android.util.Log.e("FriendDetailsFragment", "未找到TopFragment")
                
            } catch (e: Exception) {
                android.util.Log.e("FriendDetailsFragment", "切换到消息页面失败", e)
            }
        }, 500) // 延迟500ms执行
    }
    
    /**
     * 切换ViewPager到消息页面
     */
    private fun switchViewPagerToMessage(fragment: androidx.fragment.app.Fragment) {
        try {
            val viewPagerField = fragment.javaClass.getDeclaredField("mViewPager")
            viewPagerField.isAccessible = true
            val viewPager = viewPagerField.get(fragment) as? androidx.viewpager2.widget.ViewPager2
            if (viewPager != null) {
                viewPager.currentItem = 2 // 切换到消息页面（索引2）
                android.util.Log.d("FriendDetailsFragment", "成功切换到消息页面")
            } else {
                android.util.Log.e("FriendDetailsFragment", "ViewPager2为null")
            }
        } catch (e: Exception) {
            android.util.Log.e("FriendDetailsFragment", "切换ViewPager失败", e)
        }
    }
}
