package com.jxdx.square.friend

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseActivity
import com.jxdx.square.databinding.ActivityFriendMessageBinding

class FriendMessageActivity : BaseActivity<ActivityFriendMessageBinding>() {
    
    private val viewModel: FriendMessageViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[FriendMessageViewModel::class.java]
    }
    
    private val friendViewModel: FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }

    override fun bindLayout(): ActivityFriendMessageBinding = 
        ActivityFriendMessageBinding.inflate(layoutInflater)

    override fun initView() {
        // 设置返回按钮点击事件
        view.backButton.setOnClickListener {
            finish()
        }

        // 初始化RecyclerView
        view.friendRequestRecyclerView.layoutManager = LinearLayoutManager(this)
        view.friendRequestRecyclerView.setHasFixedSize(true)
        val adapter = ApplicationAdapter(arrayListOf())
        
        // 设置接受按钮点击事件
        adapter.setOnAcceptClickListener { application ->
            viewModel.acceptFriend(application.id)
        }
        
        // 设置拒绝按钮点击事件
        adapter.setOnRejectClickListener { application ->
            viewModel.rejectFriend(application.id)
        }
        
        view.friendRequestRecyclerView.adapter = adapter
    }

    override fun subscribeUi() {
        // 监听好友申请列表
        viewModel.applicationLiveData.observe(this) { resource ->
            resource.onSuccess { applicationList ->
                val adapter = view.friendRequestRecyclerView.adapter as? ApplicationAdapter
                // 使用BaseTypeAdapter提供的clearAndAdd方法更新数据
                applicationList?.let {
                    adapter?.clearAndAdd(it)
                }
            }
        }
        
        // 监听接受好友申请的结果
        viewModel.acceptFriendLiveData.observe(this) { resource ->
            resource.onSuccess { message ->
                android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
                // 接受成功后刷新申请列表
                viewModel.getApplications()
                // 刷新好友列表
                android.util.Log.d("FriendMessageActivity", "同意好友申请成功，开始刷新好友列表")
                friendViewModel.getFriends()
                
                // 设置结果，通知调用方刷新好友列表
                setResult(android.app.Activity.RESULT_OK)
            }
            
            resource.onError { error, _ ->
                android.widget.Toast.makeText(this, "接受失败: ${error?.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        // 监听拒绝好友申请的结果
        viewModel.rejectFriendLiveData.observe(this) { resource ->
            resource.onSuccess { message ->
                android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
                // 拒绝成功后刷新申请列表
                viewModel.getApplications()
            }
            
            resource.onError { error, _ ->
                android.widget.Toast.makeText(this, "拒绝失败: ${error?.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.getApplications()
    }
}
