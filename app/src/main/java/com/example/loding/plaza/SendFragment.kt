package com.example.loding.plaza

import android.text.TextUtils
import android.widget.Toast
import com.example.corekit.common.BaseFragment
import com.example.loding.databinding.FragmentSendBinding
import com.example.loding.entity.Dynamic
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SendFragment : BaseFragment<FragmentSendBinding>() {
    // 发布动态回调接口
    interface OnPublishListener {
        fun onPublish(item: Dynamic)
    }

    private var publishListener: OnPublishListener? = null

    fun setOnPublishListener(listener: OnPublishListener) {
        this.publishListener = listener
    }

    companion object {
        fun newInstance(): SendFragment = SendFragment()
    }

    override fun bindLayout(): FragmentSendBinding = FragmentSendBinding.inflate(layoutInflater)

    override fun initView() {
        setListeners()
    }

    override fun subscribeUi() {
        // 可以在这里处理UI订阅逻辑
    }

    private fun setListeners() {
        // 取消按钮点击事件
        find.tvCancel.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        // 发布按钮点击事件
        find.tvPublish.setOnClickListener {
            publishDynamic()
        }

        // 添加图片按钮点击事件
        find.ivAddImage.setOnClickListener {
            Toast.makeText(context, "添加图片功能开发中...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun publishDynamic() {
        // 获取用户输入的内容
        val content =
            find.etContent.text
                .toString()
                .trim()

        // 检查内容是否为空
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(context, "请输入内容", Toast.LENGTH_SHORT).show()
            return
        }

        // 获取当前时间
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentTime = dateFormat.format(Date())

//        // 创建新的动态项（使用模拟数据）
//        val newItem = Dynamic(
//            avatar = R.drawable.ic_avatar, // 默认头像
//            username = "我", // 用户名
//            publishTime = currentTime, // 发布时间
//            content = content, // 内容
//            images = arrayListOf(), // 图片列表（暂时为空）
//            likeCount = 0, // 点赞数
//            rewardCount = 0, // 打赏数
//            commentCount = 0, // 评论数
//            firstComment = null, // 第一条评论
//            comments = arrayListOf() // 完整评论列表
//        )

        // 调用回调方法，通知有新动态发布
//        publishListener?.onPublish(newItem)

        // 返回上一页
        activity?.supportFragmentManager?.popBackStack()
    }
}
