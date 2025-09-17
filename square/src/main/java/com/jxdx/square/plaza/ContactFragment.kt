package com.jxdx.square.plaza

import android.content.Intent
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseFragment
import com.example.corekit.recyclerview.CommonItemDecoration
import com.jxdx.square.R
import com.jxdx.square.databinding.FragmentContactBinding
import com.jxdx.square.entity.Friend
import com.jxdx.square.friend.AddFriendActivity
import com.jxdx.square.friend.FriendDetailsFragment
import com.jxdx.square.friend.FriendMessageFragment
import com.jxdx.square.friend.FriendTypeAdapter
import com.jxdx.square.friend.FriendViewModel

class ContactFragment : BaseFragment<FragmentContactBinding>() {
    private lateinit var friendTypeAdapter: FriendTypeAdapter

    // 初始化PostDynamicViewModel
    private val friendViewModel: FriendViewModel by lazy {
        ViewModelProvider(requireActivity())[FriendViewModel::class.java]
    }

    override fun bindLayout(): FragmentContactBinding = FragmentContactBinding.inflate(layoutInflater)

    override fun initView() {
        // 初始化空适配器
        friendTypeAdapter = FriendTypeAdapter(emptyList())

        // 设置RecyclerView
        find.friendList.adapter = friendTypeAdapter
        find.friendList.layoutManager = LinearLayoutManager(requireContext())
        // 添加间距（可选，使用文档中的通用间距工具）
        find.friendList.addItemDecoration(CommonItemDecoration(10f))

        // 设置添加好友按钮点击事件
        find.root.findViewById<LinearLayout>(R.id.add_friend_layout)?.setOnClickListener {
            val intent = Intent(requireContext(), AddFriendActivity::class.java)
            startActivity(intent)
        }

        // 设置新的朋友按钮点击事件
        find.root.findViewById<LinearLayout>(R.id.new_friend_layout)?.setOnClickListener {
            // 跳转到好友申请消息界面
            val fragment = FriendMessageFragment()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(android.R.id.content, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // 设置好友列表项点击事件监听器
        friendTypeAdapter.setOnItemClickListener(
            object : FriendTypeAdapter.OnItemClickListener {
                override fun onItemClick(friend: Friend) {
                    // 跳转到好友详情页
                    navigateToFriendDetails(friend)
                }
            },
        )
    }

    private fun navigateToFriendDetails(friend: Friend) {
        // 安全处理friendAvatar参数，即使为null也能正常运行
        val fragment = FriendDetailsFragment.newInstance(
            friend.friendName,
            friend.friendAvatar, // 已经修改为可空类型
            friend.friendId
        )
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(android.R.id.content, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun subscribeUi() {
        // 尝试从ViewModel获取好友数据
        friendViewModel.friendLiveData.observe(this) {
            it.onSuccess {
                friendTypeAdapter.clear()
                friendTypeAdapter.add(it)
            }
        }

        // 调用获取好友数据的方法
        friendViewModel.getFriends()
    }
}
