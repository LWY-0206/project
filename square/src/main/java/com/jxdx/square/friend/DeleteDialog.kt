package com.jxdx.square.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseDialog
import com.example.corekit.http.bean.Resource
import com.jxdx.square.databinding.FriendDialogBinding

class DeleteDialog : BaseDialog<FriendDialogBinding>() {
    private lateinit var deleteFriendViewModel: DeleteFriendViewModel
    private lateinit var friendViewModel: FriendViewModel
    private var friendId: Int = 0

    init {
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        alpha = 0F
        ifCancelOnTouch = true
        enableBack = true
    }

    override fun initView(view: FriendDialogBinding) {
        // 初始化ViewModel
        deleteFriendViewModel = ViewModelProvider(requireActivity())[DeleteFriendViewModel::class.java]
        friendViewModel = ViewModelProvider(requireActivity())[FriendViewModel::class.java]

        // 获取传递的friendId参数
        friendId = arguments?.getInt("friendId", 0) ?: 0

        view.cancelButton.setOnClickListener {
            dismiss()
        }
        
        view.deleteButton.setOnClickListener {
            if (friendId == 0) {
                Toast.makeText(context, "好友ID无效", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 执行删除操作
            deleteFriendViewModel.deleteFriend(friendId)
        }
    }

    override fun getView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
    ): FriendDialogBinding = FriendDialogBinding.inflate(layoutInflater, parent, false)

    override fun onStart() {
        super.onStart()
        
        // 观察删除结果
        deleteFriendViewModel.deleteFriendLiveData.observe(this) { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    Toast.makeText(context, "删除好友成功", Toast.LENGTH_SHORT).show()
                    dismiss()
                    // 刷新好友列表
                    friendViewModel.getFriends()
                    // 通知父Fragment并自动返回上一页
                    (parentFragment as? FriendDetailsFragment)?.onFriendDeleted()
                }
                Resource.Status.ERROR -> {
                    Toast.makeText(
                        context, 
                        "删除失败: ${resource.error?.message ?: "未知错误"}", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    // 其他状态，如加载中等
                }
            }
        }
    }


    companion object {
        fun newInstance(friendId: Int): DeleteDialog {
            val dialog = DeleteDialog()
            val bundle = Bundle()
            bundle.putInt("friendId", friendId)
            dialog.arguments = bundle
            return dialog
        }
    }
}
