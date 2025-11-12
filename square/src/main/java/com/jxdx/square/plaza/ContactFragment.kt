package com.jxdx.square.plaza

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseFragment
import com.example.corekit.recyclerview.CommonItemDecoration
import com.jxdx.square.R
import com.jxdx.square.databinding.FragmentContactBinding
import com.jxdx.square.entity.Friend
import com.jxdx.square.friend.AddFriendActivity
import com.jxdx.square.friend.FriendDetailsFragment
import com.jxdx.square.friend.FriendMessageActivity
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
        android.util.Log.d("ContactFragment", "initView被调用")
        
        // 初始化空适配器
        friendTypeAdapter = FriendTypeAdapter(emptyList())

        // 设置RecyclerView
        find.friendList.adapter = friendTypeAdapter
        find.friendList.layoutManager = LinearLayoutManager(requireContext())
        // 添加间距（可选，使用文档中的通用间距工具）
        find.friendList.addItemDecoration(CommonItemDecoration(10f))

        // 清除之前的点击事件监听器
        find.addFriendLayout.setOnClickListener(null)
        find.newFriendLayout.setOnClickListener(null)

        // 设置添加好友按钮点击事件
        find.addFriendLayout.setOnClickListener {
            android.util.Log.d("ContactFragment", "添加好友按钮被点击")
            val intent = Intent(requireContext(), AddFriendActivity::class.java)
            startActivity(intent)
        }

        // 设置新的朋友按钮点击事件
        find.newFriendLayout.setOnClickListener {
            android.util.Log.d("ContactFragment", "新的朋友按钮被点击")
            // 跳转到好友申请消息界面
            val intent = Intent(requireContext(), FriendMessageActivity::class.java)
            startActivityForResult(intent, 1001) // 使用startActivityForResult
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
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.hide(this) // 隐藏当前Fragment
        transaction.add(android.R.id.content, fragment, "FriendDetailsFragment")
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun subscribeUi() {
        // 尝试从ViewModel获取好友数据
        friendViewModel.friendLiveData.observe(this) {
            it.onSuccess { friendList ->
                android.util.Log.d("ContactFragment", "收到好友列表更新，好友数量: ${friendList?.size}")
                friendTypeAdapter.clear()
                friendTypeAdapter.add(friendList)
            }
        }

        // 初始化时获取好友数据
        android.util.Log.d("ContactFragment", "开始获取好友数据")
        friendViewModel.getFriends()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == android.app.Activity.RESULT_OK) {
            // 从好友申请页面返回，刷新好友列表
            android.util.Log.d("ContactFragment", "从好友申请页面返回，刷新好友列表")
            friendViewModel.getFriends()
        }
    }
}
