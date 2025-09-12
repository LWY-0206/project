package com.example.loding.plaza

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseFragment
import com.example.corekit.recyclerview.CommonItemDecoration
import com.example.loding.databinding.FragmentContactBinding
import com.example.loding.friend.FriendDetailsFragment
import com.example.loding.friend.FriendTypeAdapter
import com.example.loding.friend.FriendViewModel
import com.example.loding.entity.Friend

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

        // 设置好友列表项点击事件监听器
        friendTypeAdapter.setOnItemClickListener(object : FriendTypeAdapter.OnItemClickListener {
            override fun onItemClick(friend: Friend) {
                // 跳转到好友详情页
                navigateToFriendDetails(friend)
            }
        })
    }

    // 跳转到好友详情页
    private fun navigateToFriendDetails(friend: Friend) {
        val fragment = FriendDetailsFragment.newInstance(friend.friendName, friend.friendAvatar)
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
