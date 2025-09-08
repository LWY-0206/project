package com.example.loding.activity

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.corekit.common.BaseActivity
import com.example.loding.R
import com.example.loding.adapter.CommentViewModel
import com.example.loding.adapter.Comment_Adapter
import com.example.loding.adapter.PostDetailsViewModel
import com.example.loding.databinding.ActivityDynamicdetailsMainBinding
import com.example.loding.entity.CommentItem
import com.example.loding.entity.DynamicDetail

class DynamicdetailsActivity : BaseActivity<ActivityDynamicdetailsMainBinding>() {
    val viewModel1: PostDetailsViewModel by lazy {
        ViewModelProvider(this)[PostDetailsViewModel::class.java]
    }
    val viewModel2: CommentViewModel by lazy {
        ViewModelProvider(this)[CommentViewModel::class.java]
    }

    private lateinit var commentAdapter: Comment_Adapter

    override fun bindLayout(): ActivityDynamicdetailsMainBinding = ActivityDynamicdetailsMainBinding.inflate(layoutInflater)

    override fun initView() {
        // 初始化返回按钮点击事件
        view.ivBack.setOnClickListener { finish() }

        // 接收传递的参数
        val intent: Intent = intent
        val id = intent.getIntExtra("Id", -1)
        val commentCount = intent.getIntExtra("commentCount", -1)

        // 打印用户ID日志
        Log.d("DynamicDetails", "接收到的动态ID: $id")

        // 获取动态详情数据和评论
        if (id != -1) {
            viewModel1.getPostDetails(id)
            viewModel2.getComments(id, page = 1, size = commentCount)
        }

        // 初始化评论列表
        initCommentList()
    }

    private fun initCommentList() {
        view.rvComments.layoutManager = LinearLayoutManager(this)
        commentAdapter = Comment_Adapter()
        view.rvComments.adapter = commentAdapter
    }

    override fun subscribeUi() {
        // 观察动态详情数据变化
        viewModel1.dynamicDetailLiveData.observe(this) {
            it.onSuccess { dynamicDetails ->
                // 添加空值检查防止空指针异常
                if (dynamicDetails != null && dynamicDetails.isNotEmpty()) {
                    val dynamicDetail = dynamicDetails[0]
                    if (dynamicDetail != null) {
                        // 更新UI展示动态详情
                        updateDynamicDetail(dynamicDetail)
                    } else {
                        Log.e("DynamicDetails", "动态详情数据为空")
                    }
                } else {
                    Log.d("DynamicDetails", "获取到的动态详情列表为空")
                }
            }

            it.onError { error, _ ->
                Log.e("DynamicDetails", "获取动态详情失败:  ${error?.message ?: "未知错误"}")
            }
        }

        // 观察评论数据变化
        viewModel2.commentLiveData.observe(this) {
            it.onSuccess { commentList ->
                // 添加空值检查防止空指针异常
                if (commentList != null && commentList.isNotEmpty()) {
                    // 更新评论列表数据
                    commentAdapter.clearAndAdd(commentList)
                    Log.d("DynamicDetails", "成功加载评论数据，共 ${commentList.size} 条")
                } else {
                    Log.d("DynamicDetails", "获取到的评论列表为空")

                    val mockList = ArrayList<CommentItem>()
                    commentAdapter.clear()
                    commentAdapter.clearAndAdd(mockList)
                }
            }

            it.onError { error, _ ->
                Log.e("DynamicDetails", "获取评论失败: ${error?.message ?: "未知错误"}")
                val mockList = ArrayList<CommentItem>()
                commentAdapter.clear()
                commentAdapter.clearAndAdd(mockList)
            }
        }
    }

    /**
     * 更新动态详情UI
     */
    private fun updateDynamicDetail(dynamicDetail: DynamicDetail) {
        try {
            // 更新用户信息
            val avatarUrl = dynamicDetail.avatarUrl ?: ""
            Glide
                .with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_avatar)
                .into(view.civUserAvatar)
            view.tvUsername.text = dynamicDetail.userName ?: "未知用户"
            view.tvTimestamp.text = dynamicDetail.createTime ?: ""

            // 更新动态内容
            view.tvContent.text = dynamicDetail.content ?: ""

            // 更新互动数据
            view.tvLikeCount.text = dynamicDetail.likeCount.toString()
            view.tvRewardCount.text = dynamicDetail.rewardCount.toString()
            view.tvCommentTitleCount.text = "(${dynamicDetail.commentCount})"

            // 处理图片展示 - 确保contentImageUrls不为空
            val imageUrls = dynamicDetail.contentImageUrls ?: emptyList()
            handleImagesDisplay(imageUrls)
        } catch (e: Exception) {
            Log.e("DynamicDetails", "更新UI时发生异常: ${e.message}", e)
        }
    }

    /**
     * 处理图片展示逻辑
     */
    private fun handleImagesDisplay(imageUrls: List<String>) {
        try {
            if (imageUrls.isNullOrEmpty()) {
                // 没有图片时，隐藏图片区域
                view.llImagesContainer?.visibility = android.view.View.GONE
                return
            }

            // 确保图片容器不为空
            view.llImagesContainer?.visibility = android.view.View.VISIBLE

            if (imageUrls.size == 1) {
                // 单张图片展示
                view.ivSingleImage?.visibility = android.view.View.VISIBLE
                view.llImagesGrid?.visibility = android.view.View.GONE

                // 安全加载单张图片
                val firstImageUrl = imageUrls.getOrNull(0) ?: ""
                view.ivSingleImage?.let { imageView ->
                    Glide
                        .with(this)
                        .load(firstImageUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(imageView)
                }
            } else {
                // 多张图片网格展示（最多9张）
                view.ivSingleImage?.visibility = android.view.View.GONE
                view.llImagesGrid?.visibility = android.view.View.VISIBLE

                // 创建一个安全的图片视图列表，避免空指针
                val imageViews =
                    listOfNotNull(
                        view.ivImage1,
                        view.ivImage2,
                        view.ivImage3,
                        view.ivImage4,
                        view.ivImage5,
                        view.ivImage6,
                        view.ivImage7,
                        view.ivImage8,
                        view.ivImage9,
                    )

                for (i in imageViews.indices) {
                    val imageView = imageViews[i]
                    if (i < imageUrls.size) {
                        imageView.visibility = android.view.View.VISIBLE
                        // 安全获取图片URL
                        val imageUrl = imageUrls.getOrNull(i) ?: ""
                        Glide
                            .with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(imageView)
                    } else {
                        imageView.visibility = android.view.View.GONE
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DynamicDetails", "加载图片时发生异常: ${e.message}", e)
            // 发生异常时隐藏图片区域
            view.llImagesContainer?.visibility = android.view.View.GONE
        }
    }
}
