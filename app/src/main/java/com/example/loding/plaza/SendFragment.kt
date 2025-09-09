package com.example.loding.plaza

import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseFragment
import com.example.loding.adapter.DynamicBody
import com.example.loding.adapter.PostDynamicViewModel
import com.example.loding.databinding.FragmentSendBinding

class SendFragment : BaseFragment<FragmentSendBinding>() {
    // 发布动态回调接口
    interface OnPublishListener {
        fun onPublishSuccess()
    }

    private var publishListener: OnPublishListener? = null

    fun setOnPublishListener(listener: OnPublishListener) {
        this.publishListener = listener
    }

    // 初始化PostDynamicViewModel
    private val viewModel: PostDynamicViewModel by lazy {
        ViewModelProvider(requireActivity())[PostDynamicViewModel::class.java]
    }

    companion object {
        fun newInstance(): SendFragment = SendFragment()
    }

    override fun bindLayout(): FragmentSendBinding = FragmentSendBinding.inflate(layoutInflater)

    override fun initView() {
        setListeners()
    }

    override fun subscribeUi() {
        // 观察postDynamicLiveData，处理发布动态的结果
        viewModel.postDynamicLiveData.observe(this) {
            it.onSuccess { data ->
                // 即使data为null，也能正常处理成功情况
                Toast.makeText(context, "发布成功", Toast.LENGTH_SHORT).show()
                // 回调发布成功事件
                publishListener?.onPublishSuccess()
                // 返回上一页
                activity?.supportFragmentManager?.popBackStack()
            }

            it.onError { error, _ ->
                Toast.makeText(context, "发布失败", Toast.LENGTH_SHORT).show()
            }
        }
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
        val dynamicBody =
            DynamicBody(
                title = "写死的标题",
                content =
                    find.etContent.text
                        .toString()
                        .trim(),
                contentImageUrls = null,
            )

        // 检查内容是否为空
        if (TextUtils.isEmpty(dynamicBody.content)) {
            Toast.makeText(context, "请输入内容", Toast.LENGTH_SHORT).show()
            return
        }

        // 使用ViewModel发送网络请求发布动态
        viewModel.postDynamic(dynamicBody)
    }
}
